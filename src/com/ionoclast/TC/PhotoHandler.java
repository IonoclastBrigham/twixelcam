

package com.ionoclast.TC;

import java.io.IOException;
import java.util.List;

import com.ionoclast.R;
import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class PhotoHandler extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mHolder;

	Camera mCamera;


	public PhotoHandler(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);

		mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.w(this.getClass().getName(), "On Draw Called");
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
	}

	public void surfaceCreated(SurfaceHolder holder) {
		synchronized (this) {
			//this.setWillNotDraw(false); // This allows us to make our own draw
			// calls to this canvas

			mCamera = Camera.open();
			// get Camera parameters
			Camera.Parameters params = mCamera.getParameters();
			// set the focus mode
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			// set Camera parameters
			params.setRotation(90);


			List<Size> tPS = params.getSupportedPictureSizes();
			params.setPictureSize(tPS.get(tPS.size() - 1).width, tPS.get(tPS.size() - 1).height);
			//params.setPictureSize(tPS.get(0).width, tPS.get(0).height);

			mCamera.setParameters(params);

			try {
				mCamera.setPreviewDisplay(this.getHolder());
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mCamera.startPreview();
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		synchronized (this) {
			try {
				if (mCamera != null) {
					mCamera.release();
				}
			}
			catch (Exception e) {
				Log.e("Camera", e.getMessage());
			}
		}
	}

}