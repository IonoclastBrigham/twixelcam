// TCActivity.java
// Twixel camera activity
// TwixelCam Copyright © 2014-2016 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.ionoclast.camera.CameraController;
import com.ionoclast.twixelcam.CameraTwixelator;
import com.ionoclast.twixelcam.R;


public class CameraActivity extends Activity
{
	private static final String TAG = CameraActivity.class.getSimpleName();

	SurfaceView mViewViewfinder;
	CameraController mController;
	SurfaceView mViewTwixelated;

	Button mBtnClick;
	Button mBtnGallery;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);

		mViewViewfinder = (SurfaceView)findViewById(R.id.viewViewfinder);
		mViewViewfinder.setZOrderMediaOverlay(true);
		mViewTwixelated = (SurfaceView)findViewById(R.id.viewTwixelated);
 
		mBtnClick = (Button)findViewById(R.id.btnClick);
		mBtnGallery = (Button)findViewById(R.id.btnGallery);
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
		mBtnGallery.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View v)
			{
				startActivity(new Intent(CameraActivity.this, GalleryActivity.class));
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
