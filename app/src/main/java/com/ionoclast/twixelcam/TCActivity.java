// TCActivity.java
// Twixel main activity
// TwixelCam Copyright © 2014-2016 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;


public class TCActivity extends Activity
{
	private static final String TAG = TCActivity.class.getSimpleName();

	SurfaceView mViewViewfinder;
	CameraController mController;
	SurfaceView mViewTwixelated;

	Button mBtnClick;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mViewViewfinder = (SurfaceView)findViewById(R.id.viewViewfinder);
		mViewViewfinder.setZOrderMediaOverlay(true);
		mViewTwixelated = (SurfaceView)findViewById(R.id.viewTwixelated);
 
		mBtnClick = (Button)findViewById(R.id.btnClick);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		CameraTwixelator tTwixelator = new CameraTwixelator(mViewTwixelated);
		mController = new CameraController(mViewViewfinder);
		mController.SetTwiddler(tTwixelator);

		mBtnClick.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				mController.TakePhoto();
			}
		});
	}

	@Override
	public void onPause()
	{
		super.onPause();
		mController.CloseCamera();
		mController = null;
		mBtnClick.setOnClickListener(null);
	}
}
