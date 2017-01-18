package com.cameralib;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.IOException;

/**
 * Created by viktoria on 1/18/17.
 */

public class Camera1Fragment extends BasicCameraFragment implements Camera.AutoFocusCallback {
    private static final String TAG = "Camera1Fragment";
    private Camera camera;
    private int cameraId;
    boolean isTakingPicture = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    camera = Camera.open(i);
                    cameraId = i;
                }
            }
            if (camera == null) {
                camera = Camera.open();
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        setPreviewSize();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (!isTakingPicture && camera != null) {
            isTakingPicture = true;
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO) ||
                    parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_MACRO)) {
                camera.autoFocus(this);
            } else {
                takePicture();
            }
        }
    }

    public static BasicCameraFragment newInstance(String directoryPath) {
        BasicCameraFragment fragment = new Camera1Fragment();
        Bundle args = new Bundle();
        args.putString("directoryPath", directoryPath);
        fragment.setArguments(args);
        return fragment;
    }

    void setPreviewSize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        boolean widthIsMax = display.getWidth() > display.getHeight();
        Camera.Size size = camera.getParameters().getPreviewSize();
        RectF rectDisplay = new RectF();
        RectF rectPreview = new RectF();
        rectDisplay.set(0, 0, display.getWidth(), display.getHeight());
        if (widthIsMax) {
            rectPreview.set(0, 0, size.width, size.height);
        } else {
            rectPreview.set(0, 0, size.height, size.width);
        }

        Matrix matrix = new Matrix();

        matrix.setRectToRect(rectPreview, rectDisplay,
                Matrix.ScaleToFit.START);
        matrix.mapRect(rectPreview);
        mTextureView.setAspectRatio((int) rectPreview.width(), (int) rectPreview.height());

    }

    void setCameraDisplayOrientation(int cameraId) {
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result = 0;

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        // задняя камера
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            result = ((360 - degrees) + info.orientation);
        } else
            // передняя камера
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = ((360 - degrees) - info.orientation);
                result += 360;
            }
        result = result % 360;
        camera.setDisplayOrientation(result);
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            try {
                camera.setPreviewTexture(texture);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            camera.stopPreview();
            setCameraDisplayOrientation(cameraId);
            try {
                camera.setPreviewTexture(texture);
                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (success) {
            takePicture();
        }
    }

    private void takePicture() {
        camera.setPreviewCallback(null);
        showProgress();
        camera.takePicture(null, null, pictureCallback);
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                isTakingPicture = false;
                Log.d(TAG, mFile.toString());
                hideProgress();
                mBackgroundHandler.post(new ImageSaver(Camera1Fragment.this, data, mFile));
                camera.startPreview();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    };
}
