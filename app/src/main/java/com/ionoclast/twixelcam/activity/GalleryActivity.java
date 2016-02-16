// GalleryActivity.java
// twixelated image gallery
// TwixelCam - Copyright © 2016 Brigham Toskin


package com.ionoclast.twixelcam.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ionoclast.twixelcam.CameraTwixelator;
import com.ionoclast.twixelcam.R;

import java.io.File;
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
	ViewPager mGalTwixelated;
	File[] mFiles;
	Bitmap[] mImages;

	ThreadPoolExecutor mExecutor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);

		mGalTwixelated = (ViewPager)findViewById(R.id.lst_gallery);

		mFiles = listFiles();
		mImages = new Bitmap[mFiles.length];
		mExecutor = new ThreadPoolExecutor(1, 4, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(4));
		mExecutor.execute(new LoadBitmapsTask(0, 4));

		mGalTwixelated.setAdapter(new TwixelatedPageAdapter());
	}

	private File[] listFiles()
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
		private Queue<ImageView> mRecycledViews;

		@Override
		public int getCount()
		{
			return mImages.length;
		}

		@Override
		public Object instantiateItem(ViewGroup pParent, int pPosition)
		{
			ImageView tImageView = new ImageView(GalleryActivity.this);
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
			ImageView tImg = (ImageView)pObject;
			tImg.setImageDrawable(null);

		}
	}
}
