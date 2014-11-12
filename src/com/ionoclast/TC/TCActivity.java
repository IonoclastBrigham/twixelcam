

package com.ionoclast.TC;

import java.io.FileNotFoundException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import com.ionoclast.R;

public class TCActivity extends Activity {
	private static final String TAG = "CameraDemo";
	Camera camera;
	PhotoHandler preview;
	Button buttonClick;

	boolean mPreview;
	
	View mImagePreview;
	
	class PhotoTimerTask extends TimerTask {
		@Override
		public void run() {
			mPreview = true;
			preview.mCamera.takePicture(null, null, mPicture);
		}
	}

	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		preview = new PhotoHandler(this);
		((FrameLayout) findViewById(R.id.preview)).addView(preview);
		findViewById(R.id.preview).setVisibility(View.INVISIBLE);
		
		mImagePreview = findViewById(R.id.tv);

		Timer tTimer = new Timer();
		PhotoTimerTask tTask = new PhotoTimerTask();
		tTimer.schedule(tTask, 1000, 1);
 
		buttonClick = (Button) findViewById(R.id.buttonClick);
		buttonClick.setOnClickListener( new OnClickListener() {
			public void onClick(View v) {
				preview.mCamera.takePicture(null, null, mPicture);
				
			}
		});
	}
	
	private PictureCallback mPicture = new PictureCallback() {

	    @Override
	    public void onPictureTaken(byte[] pData, Camera pCamera) {

	        try {
	            Bitmap tPict = BitmapFactory.decodeByteArray(pData, 0, pData.length);
	            
	            Bitmap tB1 = Bitmap.createScaledBitmap(tPict, (int)(tPict.getWidth() *.1), (int)(tPict.getHeight() * .1), false);
	            Bitmap tB2 = Bitmap.createScaledBitmap(tB1, 480, 640, false);
	            
	            if (!mPreview) {
		            FileOutputStream out = new FileOutputStream("/sdcard/mods.png");
		            tB2.compress(Bitmap.CompressFormat.PNG, 90, out);
		            out.close();
	            }
	            
			    
			    Drawable backgroundImage = new BitmapDrawable(tPict);
			    mImagePreview.setBackgroundDrawable(backgroundImage);
				preview.mCamera.startPreview();
	            
	        } catch (Exception e) {
	        }
	    }
	};

}
