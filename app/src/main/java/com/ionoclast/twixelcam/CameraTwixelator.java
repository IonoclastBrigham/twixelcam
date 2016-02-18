// CameraTwixelator.java
// Camera preview and capture twixelator
// TwixelCam - Copyright © 2014-2016 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.ionoclast.camera.ICameraTwiddler;
import com.ionoclast.util.ByteArrayWrapperOutputStream;

import java.io.File;
import java.io.FileOutputStream;

import static android.graphics.Bitmap.Config;


public class CameraTwixelator implements SurfaceHolder.Callback, ICameraTwiddler
{
	private static final String TAG = CameraTwixelator.class.getSimpleName();

	public static final File TWIXEL_DIR;
	static
	{
		File tDcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		TWIXEL_DIR = new File(tDcim, "TwixelCam");
	}

	Context mCtxt;

	private View mView;
	private SurfaceHolder mHolder;
	private boolean mHaveSurface = false;

	private Camera.Size mPreviewDimens;
	private int mPreviewFormat;
	private Handler mRenderThread;
	private ByteArrayWrapperOutputStream mJpgBytesOutStream;
	private Rect mYuvArea;
	private BitmapFactory.Options mBmpOptions;
	private Matrix mTwixelateXform;


	public CameraTwixelator(SurfaceView pViewTwixelated)
	{
		this(pViewTwixelated.getContext());

		mView = pViewTwixelated;
		mHolder = pViewTwixelated.getHolder();
		mHolder.addCallback(this);

		HandlerThread tThread = new HandlerThread("twixelator");
		tThread.start();
		mRenderThread = new Handler(tThread.getLooper(),
									new TwixelatorPreviewCallback());
	}

	public CameraTwixelator(Context pCtxt)
	{
		mCtxt = pCtxt;
		mTwixelateXform = new Matrix();
	}

	@Override
	public void AttachCamera(Camera pCamera)
	{
		Camera.Parameters tParams = pCamera.getParameters();

		mPreviewDimens = tParams.getPreviewSize();
		mPreviewFormat = tParams.getPreviewFormat();
		init_jpg_buffer(tParams);

		mYuvArea = new Rect(0, 0, mPreviewDimens.width, mPreviewDimens.height);
		mBmpOptions = new BitmapFactory.Options();
		mBmpOptions.inPreferredConfig = Config.RGB_565;

		pCamera.setPreviewCallback(this);
	}

	private void init_jpg_buffer(Camera.Parameters pParams)
	{
		// as a hard upper limit, we need W * H * 16bits of working memory,
		// picking the larger of the preview or the picture capture size.
		Camera.Size tPicDimens = pParams.getPictureSize();
		int maxBufSize = Math.max(mPreviewDimens.width * mPreviewDimens.height,
								  tPicDimens.width * tPicDimens.height) * 2;

		mJpgBytesOutStream = new ByteArrayWrapperOutputStream(new byte[maxBufSize]);
	}

	@Override
	public void CleanUp()
	{
		mRenderThread.removeCallbacksAndMessages(null);
		mRenderThread.getLooper().getThread().interrupt();
		mRenderThread = null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		// nothing
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		mHaveSurface = true;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mHaveSurface = false;
	}

	private Bitmap decode_jpg_data(byte[] pBmpData, int length)
	{
		return BitmapFactory.decodeByteArray(pBmpData, 0, length, mBmpOptions);
	}

	private Bitmap decode_yuv_preview(byte[] pYuvData)
	{
		mJpgBytesOutStream.reset();
		YuvImage tYuv = new YuvImage(pYuvData, mPreviewFormat, mPreviewDimens.width, mPreviewDimens.height, null);
		tYuv.compressToJpeg(mYuvArea, 100, mJpgBytesOutStream);
		return decode_jpg_data(mJpgBytesOutStream.toByteArray(), mJpgBytesOutStream.size());
	}

	public Bitmap TwixelateBmp(Bitmap pBitmap)
	{
		int tWidth = pBitmap.getWidth(), tHeight = pBitmap.getHeight();
		mTwixelateXform.reset();
		mTwixelateXform.preRotate(90);
		mTwixelateXform.postScale(10.0f / tHeight, 14.0f / tWidth);
		return Bitmap.createBitmap(pBitmap, 0, 0, tWidth, tHeight, mTwixelateXform, true);
	}

	@Override
	public void onPreviewFrame(final byte[] pYuvData, Camera pCamera)
	{
		// we have to decode on the UI thread, because the buffer may be reused
		Bitmap tPreviewBmp = decode_yuv_preview(pYuvData);

		Message tMessage = new Message();
		tMessage.what = 0;
		Bundle args = new Bundle();
		args.putParcelable(TwixelatorPreviewCallback.ARG_FULL_IMAGE, tPreviewBmp);
		tMessage.setData(args);
		mRenderThread.dispatchMessage(tMessage);
	}

	@Override
	public void onPictureTaken(final byte[] pJpegData, final Camera pCamera)
	{
		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void[] params)
			{
				// get twixelated bitmap
				Bitmap tCapturedBmp = decode_jpg_data(pJpegData, pJpegData.length);
				if(tCapturedBmp == null)
				{
					// TODO
					return null;
				}
				Bitmap tTwixelated = TwixelateBmp(tCapturedBmp);
				tCapturedBmp.recycle();
				Bitmap tUpScaled = Bitmap.createScaledBitmap(tTwixelated, 400, 560, false);
				tTwixelated.recycle();

				SaveTwixelFile(tUpScaled);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid)
			{
				// kick start the camera back up
				// TODO: move to controller; onActivityResult()?
				pCamera.startPreview();
			}
		}.execute();
	}

	public void SaveTwixelFile(Bitmap tUpScaled)
	{
		FileOutputStream tOut = null;
		try
		{
			if(!TWIXEL_DIR.exists() && !TWIXEL_DIR.mkdirs())
			{
				Log.e(TAG, "Error: unable to create output dir");
				Toast.makeText(mCtxt, "Error Creating Output Dir", Toast.LENGTH_LONG).show();
				return;
			}

			File tFile = File.createTempFile("twixel", ".png", TWIXEL_DIR);
			tOut = new FileOutputStream(tFile);
			tUpScaled.compress(Bitmap.CompressFormat.PNG, 0, tOut);

			// force update media database
			AddToContentProvider(mCtxt, tFile);

		}
		catch(Exception e)
		{
			Log.e(TAG, "Error", e);
		}
		finally
		{
			try
			{
				tOut.close();
			}
			catch(Exception e)
			{
				// ignore
			}
			tUpScaled.recycle();
		}
	}

	private class TwixelatorPreviewCallback implements Handler.Callback
	{
		public static final String ARG_FULL_IMAGE = "full_image";

		@Override
		public boolean handleMessage(Message pMsg)
		{
			Bitmap tPreviewBmp = pMsg.getData().getParcelable(ARG_FULL_IMAGE);
			if (tPreviewBmp == null)
			{
				// TODO
				return false;
			}

			final Bitmap tTwixelated =  TwixelateBmp(tPreviewBmp);
			tPreviewBmp.recycle();

			mView.post(new Runnable()
			{
				@Override
				public void run()
				{
					if(!mHaveSurface)
					{
						return;
					}

					Canvas tCanvas = mHolder.lockCanvas();
					if (tCanvas != null)
					{
						mTwixelateXform.reset();
						mTwixelateXform.setScale(mView.getWidth() / 10.0f, mView.getHeight() / 14.0f);
						tCanvas.drawBitmap(tTwixelated, mTwixelateXform, null);
						tTwixelated.recycle();
						mHolder.unlockCanvasAndPost(tCanvas);
					}
				}
			});

			return false;
		}
	}

	public static Uri AddToContentProvider(@NonNull Context pCtxt, @NonNull File pImage)
	{
		ContentValues tValues = new ContentValues(2);
		tValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
		tValues.put(MediaStore.Images.Media.DATA, pImage.getAbsolutePath());
		return pCtxt.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, tValues);
	}

	public static Uri GetContentUri(@NonNull Context pCtxt, @NonNull File pImage)
	{
		String tPath = pImage.getAbsolutePath();
		Cursor tCursor = pCtxt.getContentResolver().query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				new String[]{MediaStore.Images.Media._ID},
				MediaStore.Images.Media.DATA + "=? ",
				new String[]{tPath},
				null);

		if(tCursor != null && tCursor.moveToFirst())
		{
			int tId = tCursor.getInt(tCursor.getColumnIndex(MediaStore.MediaColumns._ID));
			tCursor.close();
			return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + tId);
		}
		else if(pImage.exists())
		{
			return AddToContentProvider(pCtxt, pImage);
		}

		return null;
	}
}
