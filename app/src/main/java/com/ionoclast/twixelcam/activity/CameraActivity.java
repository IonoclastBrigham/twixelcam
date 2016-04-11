// TCActivity.java
// Twixel camera activity
// TwixelCam Copyright © 2014-2016 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.ionoclast.camera.CameraController;
import com.ionoclast.twixelcam.AppSettingsHelper;
import com.ionoclast.twixelcam.R;
import com.ionoclast.twixelcam.camera.CameraTwixelator;


public class CameraActivity extends Activity
{
	private static final String TAG = CameraActivity.class.getSimpleName();

	private static final String FRAG_TAG_PREFS = "prefs_tag";

	SurfaceView mViewViewfinder;
	CameraController mController;
	SurfaceView mViewTwixelated;

	Button mBtnClick;
	Button mBtnGallery;

	View mPrefsOverlay;
	boolean mPrefsShowing = false;

	
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

		mPrefsOverlay = findViewById(R.id.prefs_container);
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

	@Override
	public boolean onCreateOptionsMenu(Menu pMenu)
	{
		super.onCreateOptionsMenu(pMenu);
		getMenuInflater().inflate(R.menu.menu_camera, pMenu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem pItem)
	{
		switch(pItem.getItemId())
		{
			case R.id.menu_settings:
				if(!mPrefsShowing)
				{
					mBtnClick.setEnabled(false);
					mBtnGallery.setEnabled(false);
					mPrefsOverlay.setVisibility(View.VISIBLE);
					getFragmentManager().beginTransaction()
							.add(R.id.camera_frame, AppSettingsHelper.CreateFragment(), FRAG_TAG_PREFS)
							.commit();
					mPrefsShowing = true;
				}
				else
				{
					hide_prefs();
				}
				return true;
		}
		return super.onOptionsItemSelected(pItem);
	}

	@Override
	public void onBackPressed()
	{
		if(mPrefsShowing)
		{
			hide_prefs();
		}
		else
		{
			super.onBackPressed();
		}
	}

	private void hide_prefs()
	{
		mBtnClick.setEnabled(true);
		mBtnGallery.setEnabled(true);
		mPrefsOverlay.setVisibility(View.GONE);
		getFragmentManager().beginTransaction()
				.remove(getFragmentManager().findFragmentByTag(FRAG_TAG_PREFS))
				.commit();
		mPrefsShowing = false;
	}
}
