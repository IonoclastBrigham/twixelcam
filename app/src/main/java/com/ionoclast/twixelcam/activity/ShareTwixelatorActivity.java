// ShareTwixelator.java
// Shared image twixelator
// TwixelCam - Copyright © 2016 Brigham Toskin


package com.ionoclast.twixelcam.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.ionoclast.twixelcam.camera.CameraTwixelator;
import com.ionoclast.twixelcam.R;

import java.io.FileInputStream;
import java.io.IOException;


/**
 * @author btoskin &lt;brigham@ionoclast.com&gt; Ionoclast Laboratories, LLC.
 */
public class ShareTwixelatorActivity extends Activity
{
	private static final String TAG = ShareTwixelatorActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_twixelator);
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		new AsyncTask<Void, Void, Void>()
		{
			@Override
			protected Void doInBackground(Void... pParams)
			{
				Uri tImagePath = getIntent().getExtras().getParcelable(Intent.EXTRA_STREAM);
				try
				{
					// load original
					Bitmap tBmp = null;
					if(tImagePath.getScheme().equals("file"))
					{
						tBmp = BitmapFactory.decodeStream(new FileInputStream(tImagePath.getPath()));
					}
					else if(tImagePath.getScheme().equals("content"))
					{
						tBmp = MediaStore.Images.Media.getBitmap(getContentResolver(), tImagePath);
					}
					else
					{
						Log.e(TAG, "Unknown URI scheme: " + tImagePath);
						return null;
					}

					// twixelate it
					CameraTwixelator tTwixelator = new CameraTwixelator(ShareTwixelatorActivity.this);
					Bitmap tTwixelated = tTwixelator.TwixelateBmp(tBmp);
					tBmp.recycle();
					Bitmap tUpScaled = Bitmap.createScaledBitmap(tTwixelated, 400, 560, false);
					tTwixelated.recycle();

					// save to twixel folder
					tTwixelator.SaveTwixelFile(tUpScaled);
				}
				catch(IOException e)
				{
					Log.e(TAG, "Error processing bitmap", e);
				}

				return null;
			}

			@Override
			protected void onPostExecute(Void pVoid)
			{
				finish();

				Intent tGallery = new Intent(ShareTwixelatorActivity.this, GalleryActivity.class);
				tGallery.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(tGallery);
			}
		}.execute();
	}
}
