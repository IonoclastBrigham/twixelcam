// TCActivity.java
// Twixel main activity
// TwixelCam Copyright © 2014 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;


public class TCActivity extends Activity {
	private static final String TAG = "TCActivity";

    static TCActivity sOnly = null;

    SurfaceView mViewViewfinder;
    CameraController mController;
    SurfaceView mViewTwixelated;
    CameraTwixelator mTwixelator;

    Button mBtnClick;

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        sOnly = this;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        mViewViewfinder = (SurfaceView)findViewById(R.id.viewViewfinder);
        mViewViewfinder.setZOrderMediaOverlay(true);
        mViewTwixelated = (SurfaceView)findViewById(R.id.viewTwixelated);
 
		mBtnClick = (Button)findViewById(R.id.btnClick);
	}

    @Override
    public void onResume() {
        super.onResume();

        final Camera tCamera = Camera.open();
        mController = new CameraController(mViewViewfinder, tCamera);
        mTwixelator = new CameraTwixelator(mViewTwixelated, tCamera);
        mBtnClick.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tCamera.takePicture(null, null, mTwixelator);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        mController.CloseCamera();
    }
}
