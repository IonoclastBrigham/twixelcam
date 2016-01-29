// CameraTwixelator.java
// Camera preview and capture twixelator
// TwixelCam - Copyright © 2014-2016 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


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
	Camera.Size mSize;


	public CameraTwixelator(SurfaceView pViewTwixelated)
	{
		mView = pViewTwixelated;
		mHolder = pViewTwixelated.getHolder();
		mHolder.addCallback(this);
	}

	public void AttachCamera(Camera pCamera)
	{
		mSize = pCamera.getParameters().getPreviewSize();
		pCamera.setPreviewCallback(this);
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		// nothing
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		mHaveSurface = true;
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mHaveSurface = false;
	}

	Bitmap decodeJpegBmp(byte[] pJpegData)
	{
		return BitmapFactory.decodeByteArray(pJpegData, 0, pJpegData.length);
	}

	Bitmap decodeYuvBmp(byte[] pYuvData)
	{
		ByteArrayOutputStream tOutStream = new ByteArrayOutputStream();
		YuvImage tYuv = new YuvImage(pYuvData, ImageFormat.NV21, mSize.width, mSize.height, null);
		tYuv.compressToJpeg(new Rect(0, 0, mSize.width, mSize.height), 100, tOutStream);
		byte[] tJpgBytes = tOutStream.toByteArray();
		return decodeJpegBmp(tJpgBytes);
	}

	Bitmap twixelateBmp(Bitmap pBitmap)
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
			Bitmap tPreviewBmp = decodeYuvBmp(pYuvData);
			if (tPreviewBmp == null)
			{
				// TODO
				mHolder.unlockCanvasAndPost(tCanvas);
				return;
			}
			Bitmap tTwixelated = twixelateBmp(tPreviewBmp);
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
		Bitmap tCapturedBmp = decodeJpegBmp(pJpegData);
		if (tCapturedBmp == null)
		{
			// TODO
			return;
		}
		Bitmap tTwixelated = twixelateBmp(tCapturedBmp);
		Bitmap tUpScaled = Bitmap.createScaledBitmap(tTwixelated, 400, 560, false);
		tTwixelated.recycle();

		// write file
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
			FileOutputStream tOut = new FileOutputStream(tFile);
			tUpScaled.compress(Bitmap.CompressFormat.PNG, 0, tOut);
			tOut.close();

			// force update media database
			MediaStore.Images.Media.insertImage(tCtxt.getContentResolver(), tFile.getPath(), tFile.getName(), "Twixelated!");
//			tCtxt.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//					Uri.parse("file://" + TWIXEL_DIR.toString())));

			Toast.makeText(tCtxt, "Saved File " + tFile.getPath(), Toast.LENGTH_LONG).show();
		}
		catch(Exception e)
		{
			Log.e(TAG, "Error", e);
		}
		finally
		{
			tUpScaled.recycle();
		}
	}
}
