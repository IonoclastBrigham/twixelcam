// CameraController.java
// Camera preview and control code
// TwixelCam Copyright © 2014 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;


public class CameraController implements SurfaceHolder.Callback {
    private final String TAG = "CameraController";

    private SurfaceView mView;
    private SurfaceHolder mHolder;
	private Camera mCamera;


	public CameraController(SurfaceView pViewViewfinder, Camera pCamera) {
        mCamera = pCamera;
        mView = pViewViewfinder;
        mHolder = pViewViewfinder.getHolder();
        mHolder.addCallback(this);
    }

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, String.format("surfaceChanged(%d x %d)", width, height));
//        mHolder.setFormat(PixelFormat.TRANSPARENT);
	}

	public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated()");
		synchronized (this) {
			// set Camera parameters
			Camera.Parameters params = mCamera.getParameters();
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			List<Size> tSupportedSizes = params.getSupportedPictureSizes();
//			params.setPictureSize(tSupportedSizes.get(tSupportedSizes.size() - 1).width,
//                    tSupportedSizes.get(tSupportedSizes.size() - 1).height);
            tSupportedSizes = params.getSupportedPreviewSizes();
            params.setPreviewSize(tSupportedSizes.get(tSupportedSizes.size() - 1).width,
                    tSupportedSizes.get(tSupportedSizes.size() - 1).height);
            params.setPreviewFormat(ImageFormat.NV21);
			mCamera.setParameters(params);
            mCamera.setDisplayOrientation(90);

            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
			}
			catch (IOException e) {
				Log.w(TAG, "Error setting preview display", e);
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed()");
	}
}