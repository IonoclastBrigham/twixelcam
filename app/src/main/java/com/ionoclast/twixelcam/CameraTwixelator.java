// CameraTwixelator.java
// Camera preview and capture twixelator
// TwixelCam Copyright © 2014 Brigham Toskin, Christopher Tooley


package com.ionoclast.twixelcam;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;


public class CameraTwixelator implements SurfaceHolder.Callback, Camera.PreviewCallback, Camera.PictureCallback {
    private final String TAG = "CameraTwixelator";

    private View mView;
    private SurfaceHolder mHolder;
    Camera mCamera;


    public CameraTwixelator(SurfaceView pViewTwixelated, Camera pCamera) {
        mCamera = pCamera;
        mView = pViewTwixelated;
        mHolder = pViewTwixelated.getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Log.d(TAG, "surfaceChanged()");
    }

    public void surfaceCreated(SurfaceHolder holder) {
//        Log.d(TAG, "surfaceCreated()");
        mCamera.setPreviewCallback(this);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed()");
    }

    Bitmap decodeJpegBmp(byte[] pJpegData) {
        return BitmapFactory.decodeByteArray(pJpegData, 0, pJpegData.length);
    }

    Bitmap decodeYuvBmp(byte[] pYuvData) {
        Camera.Size tSize = mCamera.getParameters().getPreviewSize();
        ByteArrayOutputStream tOutStream = new ByteArrayOutputStream();
        YuvImage tYuv = new YuvImage(pYuvData, ImageFormat.NV21, tSize.width, tSize.height, null);
        tYuv.compressToJpeg(new Rect(0, 0, tSize.width, tSize.height), 100, tOutStream);
        byte[] tJpgBytes = tOutStream.toByteArray();
        return decodeJpegBmp(tJpgBytes);
    }

    Bitmap twixelateBmp(Bitmap pBitmap) {
        int tWidth = pBitmap.getWidth(), tHeight = pBitmap.getHeight();
        Matrix tXform = new Matrix();
        tXform.preRotate(90);
        tXform.postScale(10.0f / tHeight, 14.0f / tWidth);
        Bitmap tTwixelated = Bitmap.createBitmap(pBitmap, 0, 0,
                tWidth, tHeight,
                tXform, true);
        pBitmap.recycle();
        return tTwixelated;
    }

    @Override
    public void onPreviewFrame(byte[] pYuvData, Camera pCamera) {
        Canvas tCanvas = mHolder.lockCanvas();
        if (tCanvas != null) {
            Bitmap tPreviewBmp = decodeYuvBmp(pYuvData);
            if (tPreviewBmp == null) {
                // TODO
                mHolder.unlockCanvasAndPost(tCanvas);
                return;
            }
            Bitmap tTwixelated = twixelateBmp(tPreviewBmp);
            Matrix tXform = new Matrix();
            tXform.setScale(mView.getWidth() / 10.0f, mView.getHeight() / 14.0f);
            tCanvas.drawBitmap(tTwixelated, tXform, null);
            tTwixelated.recycle();
            mHolder.unlockCanvasAndPost(tCanvas);
        }
    }

    @Override
    public void onPictureTaken(byte[] pJpegData, Camera pCamera) {
        Bitmap tCapturedBmp = decodeJpegBmp(pJpegData);
        if (tCapturedBmp == null) {
            // TODO
            return;
        }
        Bitmap tTwixelated = twixelateBmp(tCapturedBmp);
        Bitmap tUpScaled = Bitmap.createScaledBitmap(tTwixelated, 400, 560, false);
        tTwixelated.recycle();
        try {
            File tDcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File tTwixelDir = new File(tDcim, "TwixelCam");
            if(!tTwixelDir.exists() && !tTwixelDir.mkdirs())
            {
                Log.e(TAG, "Error: unable to create output dir");
                Toast.makeText(TCActivity.sOnly, "Error Creating Output Dir", Toast.LENGTH_LONG).show();
                return;
            }
            File tFile = File.createTempFile("twixel", ".png", tTwixelDir);
            FileOutputStream tOut = new FileOutputStream(tFile);
            tUpScaled.compress(Bitmap.CompressFormat.PNG, 0, tOut);
            tOut.close();

            Toast.makeText(TCActivity.sOnly, "Saved File " + tFile.getPath(), Toast.LENGTH_LONG).show();
        } catch(Exception e) {
            Log.e(TAG, "Error", e);
        } finally {
            tUpScaled.recycle();
        }
    }
}
