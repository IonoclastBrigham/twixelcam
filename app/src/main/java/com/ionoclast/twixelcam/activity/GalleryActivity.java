// GalleryActivity.java
// twixelated image gallery
// TwixelCam - Copyright © 2016 Brigham Toskin


package com.ionoclast.twixelcam.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.ionoclast.twixelcam.CameraTwixelator;
import com.ionoclast.twixelcam.R;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author btoskin &lt;brigham@ionoclast.com&gt; Ionoclast Laboratories, LLC.
 */
public class GalleryActivity extends Activity
{
	private static final String TAG = GalleryActivity.class.getSimpleName();

	private static final String KEY_SELECTED_PAGE = "selected_page";

	private ViewPager mPagerTwixelated;
	private File[] mFiles;
	private Bitmap[] mImages;

	private ThreadPoolExecutor mExecutor;

	private boolean mStopping = false;
	private int mSelected = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mPagerTwixelated = (ViewPager) findViewById(R.id.pager_gallery);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);

		mSelected = savedInstanceState.getInt(KEY_SELECTED_PAGE, 0);
		mPagerTwixelated.setCurrentItem(mSelected);
	}

	@Override
	protected void onSaveInstanceState(Bundle pOutState)
	{
		super.onSaveInstanceState(pOutState);

		mSelected = mPagerTwixelated.getCurrentItem();
		pOutState.putInt(KEY_SELECTED_PAGE, mSelected);
	}

	@Override
	public void onStart()
	{
		super.onStart();

		mFiles = list_files();
		mImages = new Bitmap[mFiles.length];
		mExecutor = new ThreadPoolExecutor(1, 4, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(4));
		mExecutor.execute(new LoadBitmapsTask(0, 4));

		mPagerTwixelated.setAdapter(new TwixelatedPageAdapter());
		if(mSelected > -1)
		{
			mPagerTwixelated.setCurrentItem(mSelected);
		}
	}

	@Override
	protected void onStop()
	{
		mStopping = true;

		mExecutor.shutdownNow();
		mExecutor = null;

		mPagerTwixelated.setAdapter(null);

		mFiles = null;
		for(int i = 0; i < mImages.length; ++i)
		{
			if(mImages[i] != null)
			{
				mImages[i].recycle();
			}
		}
		mImages = null;

		mStopping = false;

		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu pMenu)
	{
		getMenuInflater().inflate(R.menu.menu_gallery, pMenu);
		return super.onCreateOptionsMenu(pMenu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem pItem)
	{
		switch(pItem.getItemId())
		{
			case android.R.id.home:
				finish();
				return true;
			case R.id.menu_share:
				launch_share();
				return true;
		}
		return super.onOptionsItemSelected(pItem);
	}

	private File[] list_files()
	{
		File[] tFiles = CameraTwixelator.TWIXEL_DIR.listFiles();
		Arrays.sort(tFiles, new Comparator<File>()
		{
			@Override
			public int compare(File pLhs, File pRhs)
			{
				// sort newest first
				return Long.valueOf(pRhs.lastModified()).compareTo(pLhs.lastModified());
			}
		});
		return tFiles;
	}

	private void launch_share()
	{
		int tIndex = mPagerTwixelated.getCurrentItem();
		Uri tContentUri = CameraTwixelator.GetContentUri(this, mFiles[tIndex]);

		Intent tShareIntent = new Intent(Intent.ACTION_SEND);
		tShareIntent.putExtra(Intent.EXTRA_STREAM, tContentUri);
		tShareIntent.setType(MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(mFiles[tIndex].toString())));
		startActivity(Intent.createChooser(tShareIntent, "Share"));
	}

	private class LoadBitmapsTask implements Runnable
	{
		private int mStartIndex, mCount;

		public LoadBitmapsTask(int pStart, int pCount)
		{
			mStartIndex = pStart;
			mCount = pCount;
		}

		@Override
		public void run()
		{
			for(int i = mStartIndex; i < mStartIndex + mCount; ++i)
			{
				if(Thread.interrupted())
				{
					return;
				}

				synchronized(mImages)
				{
					if(mImages[i] == null)
					{
						String tFile = mFiles[i].toString();
						mImages[i] = BitmapFactory.decodeFile(tFile);
					}
				}
			}
		}
	}

	private class TwixelatedPageAdapter extends PagerAdapter
	{
		private Queue<ImageView> mRecycledViews = new ArrayDeque<ImageView>(4);

		@Override
		public int getCount()
		{
			return mImages.length;
		}

		@Override
		public Object instantiateItem(ViewGroup pParent, int pPosition)
		{
			ImageView tImageView = get_view();
			tImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

			synchronized(mImages)
			{
				if(mImages[pPosition] == null)
				{
					// load this one on demand, and queue up the next few on a thread
					mImages[pPosition] = BitmapFactory.decodeFile(mFiles[pPosition].toString());
					int tNumToLoad = Math.min(mFiles.length - pPosition - 1, 4);
					if(tNumToLoad > 0)
					{
						mExecutor.execute(new LoadBitmapsTask(pPosition + 1, tNumToLoad));
					}
				}
			}
			tImageView.setImageBitmap(mImages[pPosition]);
			pParent.requestLayout();

			pParent.addView(tImageView);
			return tImageView;
		}

		@Override
		public boolean isViewFromObject(View pView, Object pObject)
		{
			return pView == pObject;
		}

		@Override
		public void destroyItem(ViewGroup pParent, final int pPosition, final Object pObject)
		{
			if(mStopping)
			{
				return;
			}

			// clean up bitmap
			synchronized(mImages)
			{
				final Bitmap tBmp = mImages[pPosition];
				if(tBmp != null)
				{
					mImages[pPosition] = null;
					mExecutor.execute(new Runnable()
					{
						@Override
						public void run()
						{
							tBmp.recycle();
						}
					});
				}
			}

			// clear the image view
			Log.d(TAG, "Recycling view, " + (mRecycledViews.size() + 1) + " in queue");
			ImageView tImg = (ImageView)pObject;
			tImg.setImageDrawable(null);
			pParent.removeView(tImg);
			mRecycledViews.add(tImg);
		}

		@MainThread
		private ImageView get_view()
		{
			if(mRecycledViews.size() > 0)
			{
				Log.d(TAG, "Getting recycled view, " + (mRecycledViews.size() - 1) + " left in queue");
				return mRecycledViews.remove();
			}
			Log.d(TAG, "Getting new view instance.");
			return new ImageView(GalleryActivity.this);
		}
	}
}
