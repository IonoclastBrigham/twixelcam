// CameraController.java
// Camera preview and control code
// TwixelCam - Copyright © 2014-2016 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;


public class CameraController implements SurfaceHolder.Callback
{
	private static final String TAG = CameraController.class.getSimpleName();

	private SurfaceHolder mHolder;
	private Camera mCamera;

	private ICameraTwiddler mTwiddler;


	public CameraController(SurfaceView pViewViewfinder)
	{
		mHolder = pViewViewfinder.getHolder();
		mHolder.addCallback(this);

		mCamera = Camera.open();

		// set Camera parameters
		Camera.Parameters tParams = mCamera.getParameters();
		tParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		tParams.setPreviewFormat(ImageFormat.NV21);


		List<Camera.Size> tSupportedSizes = tParams.getSupportedPictureSizes();
		Camera.Size tSmallest = tSupportedSizes.get(tSupportedSizes.size() - 1);
		tParams.setPictureSize(tSmallest.width, tSmallest.height);

		tSupportedSizes = tParams.getSupportedPreviewSizes();
		tSmallest = tSupportedSizes.get(tSupportedSizes.size() - 1);
		tParams.setPreviewSize(tSmallest.width, tSmallest.height);

		try
		{
			// this can throw if the camera doesn't like our settings
			mCamera.setParameters(tParams);
		}
		catch(RuntimeException e)
		{
			Log.e(TAG, "Error setting camera params; attempting to proceed", e);
		}
		mCamera.setDisplayOrientation(90);
	}

	public void SetTwiddler(ICameraTwiddler pTwiddler)
	{
		if(mCamera != null)
		{
			mTwiddler = pTwiddler;
			if(pTwiddler != null)
			{
				pTwiddler.AttachCamera(mCamera);
			}
		}
	}

	public void TakePhoto()
	{
		mCamera.takePicture(null, null, mTwiddler);
	}

	public void CloseCamera()
	{
		try
		{
			if (mCamera != null)
			{
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.setPreviewDisplay(null);
				mCamera.release();
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "Camera.release() failed", e);
		}
		finally
		{
			mTwiddler.CleanUp();
			mTwiddler = null;
			mCamera = null;
			mHolder.removeCallback(this);
		}
	}

	public void OpenGallery()
	{
		mTwiddler.OpenGallery();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.d(TAG, "surfaceCreated()");
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.startPreview();
		}
		catch (IOException e)
		{
			Log.w(TAG, "Error starting preview", e);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		Log.d(TAG, String.format("surfaceChanged(%d x %d)", width, height));
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.d(TAG, "surfaceDestroyed()");
	}
}