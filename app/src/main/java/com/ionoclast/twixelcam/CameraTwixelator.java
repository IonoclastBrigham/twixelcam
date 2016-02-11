// CameraTwixelator.java
// Camera preview and capture twixelator
// TwixelCam - Copyright © 2014-2016 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static android.graphics.Bitmap.Config;


public class CameraTwixelator implements SurfaceHolder.Callback, ICameraTwiddler
{
	private static final String TAG = CameraTwixelator.class.getSimpleName();

	private static final File TWIXEL_DIR;
	static
	{
		File tDcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		TWIXEL_DIR = new File(tDcim, "TwixelCam");
	}

	private View mView;
	private SurfaceHolder mHolder;
	private boolean mHaveSurface = false;

	Camera mCamera;
	Camera.Size mPreviewSize;
	int mPreviewFormat;


	public CameraTwixelator(SurfaceView pViewTwixelated)
	{
		mView = pViewTwixelated;
		mHolder = pViewTwixelated.getHolder();
		mHolder.addCallback(this);
	}

	public void AttachCamera(Camera pCamera)
	{
		mPreviewSize = pCamera.getParameters().getPreviewSize();
		mPreviewFormat = pCamera.getParameters().getPreviewFormat();
		pCamera.setPreviewCallback(this);
		mCamera = pCamera;
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
		BitmapFactory.Options tOptions = new BitmapFactory.Options();
		tOptions.inPreferredConfig = Config.RGB_565;
		return BitmapFactory.decodeByteArray(pBmpData, 0, length, tOptions);
	}

	private Bitmap decode_yuv_preview(byte[] pYuvData)
	{
		ByteArrayOutputStream tOutStream = new ByteArrayOutputStream(pYuvData.length);
		YuvImage tYuv = new YuvImage(pYuvData, mPreviewFormat, mPreviewSize.width, mPreviewSize.height, null);
		tYuv.compressToJpeg(new Rect(0, 0, mPreviewSize.width, mPreviewSize.height), 100, tOutStream);
		return decode_jpg_data(tOutStream.toByteArray(), tOutStream.size());
	}

	private Bitmap twixelate_bmp(Bitmap pBitmap)
	{
		int tWidth = pBitmap.getWidth(), tHeight = pBitmap.getHeight();
		Matrix tXform = new Matrix();
		tXform.preRotate(90);
		tXform.postScale(10.0f / tHeight, 14.0f / tWidth);
		Bitmap tTwixelated = Bitmap.createBitmap(pBitmap, 0, 0,
				tWidth, tHeight,
				tXform, true);
		pBitmap.recycle();
		return tTwixelated;
	}

	@Override
	public void onPreviewFrame(byte[] pYuvData, Camera pCamera)
	{
		if(!mHaveSurface)
		{
			return;
		}

		Canvas tCanvas = mHolder.lockCanvas();
		if (tCanvas != null)
		{
			Bitmap tPreviewBmp = decode_yuv_preview(pYuvData);
			if (tPreviewBmp == null)
			{
				// TODO
				mHolder.unlockCanvasAndPost(tCanvas);
				return;
			}
			Bitmap tTwixelated = twixelate_bmp(tPreviewBmp);
			Matrix tXform = new Matrix();
			tXform.setScale(mView.getWidth() / 10.0f, mView.getHeight() / 14.0f);
			tCanvas.drawBitmap(tTwixelated, tXform, null);
			tTwixelated.recycle();
			mHolder.unlockCanvasAndPost(tCanvas);
		}
	}

	@Override
	public void onPictureTaken(byte[] pJpegData, Camera pCamera)
	{
		// get twixelated bitmap
		Bitmap tCapturedBmp = decode_jpg_data(pJpegData, pJpegData.length);
		if (tCapturedBmp == null)
		{
			// TODO
			return;
		}
		Bitmap tTwixelated = twixelate_bmp(tCapturedBmp);
		Bitmap tUpScaled = Bitmap.createScaledBitmap(tTwixelated, 400, 560, false);
		tTwixelated.recycle();
		tCapturedBmp.recycle();

		// write file
		FileOutputStream tOut = null;
		try
		{
			Context tCtxt = mView.getContext();
			if(!TWIXEL_DIR.exists() && !TWIXEL_DIR.mkdirs())
			{
				Log.e(TAG, "Error: unable to create output dir");
				Toast.makeText(tCtxt, "Error Creating Output Dir", Toast.LENGTH_LONG).show();
				return;
			}

			File tFile = File.createTempFile("twixel", ".png", TWIXEL_DIR);
			tOut = new FileOutputStream(tFile);
			tUpScaled.compress(Bitmap.CompressFormat.PNG, 0, tOut);

			// force update media database
			tCtxt.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					Uri.parse("file://" + tFile.toString())));
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

			// kick start the camera back up
			// TODO: move to controller; onActivityResult()?
			mCamera.startPreview();
		}
	}
}
