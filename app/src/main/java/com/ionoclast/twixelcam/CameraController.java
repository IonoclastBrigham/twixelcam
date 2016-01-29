// CameraController.java
// Camera preview and control code
// TwixelCam - Copyright © 2014-2016 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.hardware.Camera;
import android.hardware.Camera.Size;
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
		mCamera = Camera.open();
		mHolder = pViewViewfinder.getHolder();
		mHolder.addCallback(this);
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
		try {
			if (mCamera != null)
			{
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
			}
		}
		catch (Exception e)
		{
			Log.e(TAG, "Camera.release() failed", e);
		}
		finally
		{
			mTwiddler = null;
			mCamera = null;
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		Log.d(TAG, String.format("surfaceChanged(%d x %d)", width, height));
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.d(TAG, "surfaceCreated()");
		synchronized (this)
		{
			// set Camera parameters
			Camera.Parameters params = mCamera.getParameters();
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
			List<Size> tSupportedSizes = params.getSupportedPictureSizes();
			params.setPictureSize(tSupportedSizes.get(tSupportedSizes.size() - 1).width,
					tSupportedSizes.get(tSupportedSizes.size() - 1).height);
			tSupportedSizes = params.getSupportedPreviewSizes();
			params.setPreviewSize(tSupportedSizes.get(tSupportedSizes.size() - 1).width,
					tSupportedSizes.get(tSupportedSizes.size() - 1).height);
			mCamera.setParameters(params);
			mCamera.setDisplayOrientation(90);

			try {
				mCamera.setPreviewDisplay(mHolder);
				mCamera.startPreview();
			}
			catch (IOException e)
			{
				Log.w(TAG, "Error setting preview display", e);
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.d(TAG, "surfaceDestroyed()");
	}
}