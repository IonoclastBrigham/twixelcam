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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.ByteArrayOutputStream;


public class CameraTwixelator implements SurfaceHolder.Callback, Camera.PreviewCallback {
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
//        Log.d(TAG, "surfaceDestroyed()");
    }

    Bitmap decodeBmp(byte[] pYuvData) {
        Camera.Size tSize = mCamera.getParameters().getPreviewSize();
        ByteArrayOutputStream tOutStream = new ByteArrayOutputStream();
        YuvImage tYuv = new YuvImage(pYuvData, ImageFormat.NV21, tSize.width, tSize.height, null);
        tYuv.compressToJpeg(new Rect(0, 0, tSize.width, tSize.height), 100, tOutStream);
        byte[] tJpgBytes = tOutStream.toByteArray();
        return BitmapFactory.decodeByteArray(tJpgBytes, 0, tJpgBytes.length);
    }

    Bitmap twixelateBmp(Bitmap pBitmap) {
        int tWidth = pBitmap.getWidth(), tHeight = pBitmap.getHeight();
        Matrix tXform = new Matrix();
        tXform.preRotate(90);
        tXform.postScale(10.0f / tHeight, 14.0f / tWidth);
        Bitmap tTwixelated = Bitmap.createBitmap(pBitmap, 0, 0,
                tWidth, tHeight,
                tXform, false);
        pBitmap.recycle();
        return tTwixelated;
    }

    @Override
    public void onPreviewFrame(byte[] pYuvData, Camera pCamera) {
        Canvas tCanvas = mHolder.lockCanvas();
        if (tCanvas != null) {
            Bitmap tPreviewBmp = decodeBmp(pYuvData);
            if (tPreviewBmp == null) {
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
}