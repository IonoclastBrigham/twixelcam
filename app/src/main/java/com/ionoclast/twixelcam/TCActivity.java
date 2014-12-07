// TCActivity.java
// Twixel main activity
// TwixelCam Copyright © 2014 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class TCActivity extends Activity {
	private static final String TAG = "TCActivity";

    Camera mCamera;
    SurfaceView mViewViewfinder;
    CameraController mController;
    SurfaceView mViewTwixelated;
    CameraTwixelator mTwixelator;

    Button mBtnClick;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        mViewViewfinder = (SurfaceView)findViewById(R.id.viewViewfinder);
        mViewTwixelated = (SurfaceView)findViewById(R.id.viewTwixelated);
 
		mBtnClick = (Button)findViewById(R.id.btnClick);
		mBtnClick.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
//                mViewTwixelated.mCamera.takePicture(null, null, mPicture);
            }
        });
	}

    @Override
    public void onResume() {
        super.onResume();

        mCamera = Camera.open();
        mController = new CameraController(mViewViewfinder, mCamera);
        mTwixelator = new CameraTwixelator(mViewTwixelated, mCamera);
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            if (mCamera != null) {
                mCamera.release();
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Camera.release() failed", e);
        } finally {
            mCamera = null;
        }
    }
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] pData, Camera pCamera) {

//	        try {
//	            Bitmap tPict = BitmapFactory.decodeByteArray(pData, 0, pData.length);
//
//	            Bitmap tB1 = Bitmap.createScaledBitmap(tPict, (int)(tPict.getWidth() *.1), (int)(tPict.getHeight() * .1), false);
//	            Bitmap tB2 = Bitmap.createScaledBitmap(tB1, 480, 640, false);
//
//	            if (!mShowPreview) {
//		            FileOutputStream out = new FileOutputStream("/sdcard/mods.png");
//		            tB2.compress(Bitmap.CompressFormat.PNG, 90, out);
//		            out.close();
//	            }
//
//
//			    Drawable backgroundImage = new BitmapDrawable(tPict);
//			    mImagePreview.setBackgroundDrawable(backgroundImage);
//				mViewTwixelated.mCamera.startPreview();
//
//	        } catch (Exception e) {
//	        }
	    }
	};

}
