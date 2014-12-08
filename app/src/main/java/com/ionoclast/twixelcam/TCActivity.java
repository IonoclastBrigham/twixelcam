// TCActivity.java
// Twixel main activity
// TwixelCam Copyright © 2014 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Button;


public class TCActivity extends Activity {
	private static final String TAG = "TCActivity";

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
        mViewViewfinder.setZOrderMediaOverlay(true);
        mViewTwixelated = (SurfaceView)findViewById(R.id.viewTwixelated);
 
//		mBtnClick = (Button)findViewById(R.id.btnClick);
//		mBtnClick.setOnClickListener(new OnClickListener() {
//            public void onClick(View v) {
//            }
//        });
	}

    @Override
    public void onResume() {
        super.onResume();

        Camera tCamera = Camera.open();
        mController = new CameraController(mViewViewfinder, tCamera);
        mTwixelator = new CameraTwixelator(mViewTwixelated, tCamera);
    }

    @Override
    public void onPause() {
        super.onPause();

        mController.CloseCamera();
    }
}
