package com.cameralib;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;

/**
 * Created by viktoria on 1/18/17.
 */

public abstract class BasicCameraFragment extends Fragment implements View.OnClickListener {
    protected static final float ALPHA_PROGRESS_VIEW = 0.9f;
    protected static final float SCALE_ANIMATION_START_VALUE = 0;
    protected static final float SCALE_ANIMATION_END_VALUE = 1;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    Handler mBackgroundHandler;
    CameraResultListener cameraResultListener;
    String directoryPath;
    File mFile;
    ImageButton photoBtn;
    AutoFitTextureView mTextureView;
    View mProgressView;
    ImageView resultImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mProgressView = view.findViewById(R.id.progressView);
        photoBtn = (ImageButton) view.findViewById(R.id.photo);
        resultImageView = (ImageView) view.findViewById(R.id.resultImage);
        photoBtn.setOnClickListener(this);
        return view;
    }

    @Override
    public abstract void onClick(View v);

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        directoryPath = getArguments().getString("directoryPath");
        initFile();
    }
    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
    }
    @Override
    public void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    void initFile() {
        mFile = new File(directoryPath,
                System.currentTimeMillis() + ".jpg");
    }

    void showProgress() {
        mProgressView.animate().alpha(ALPHA_PROGRESS_VIEW).start();
        photoBtn.setEnabled(false);
        resultImageView.setScaleX(SCALE_ANIMATION_START_VALUE);
        resultImageView.setScaleY(SCALE_ANIMATION_START_VALUE);
    }

    void hideProgress() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    photoBtn.setEnabled(true);
                    mProgressView.animate().alpha(0).start();
                }
            });
        }
    }

    void deliverResult() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(getActivity()).load(mFile).asBitmap().centerCrop().into(new BitmapImageViewTarget(resultImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        resultImageView.setImageDrawable(circularBitmapDrawable);
                        resultImageView.animate().scaleX(SCALE_ANIMATION_END_VALUE).scaleY(SCALE_ANIMATION_END_VALUE).start();
                    }
                });
                if (cameraResultListener != null) {
                    cameraResultListener.onImageTaken(mFile.getAbsolutePath());
                }
                initFile();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setListener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setListener();
    }

    private void setListener() {
        if (getActivity() instanceof CameraResultListener) {
            cameraResultListener = (CameraResultListener) getActivity();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        cameraResultListener = null;
    }
    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBackgroundThread.quitSafely();
        } else{
            mBackgroundThread.quit();
        }
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected static class ImageSaver implements Runnable {
        private WeakReference<BasicCameraFragment> fragmentWeakReference;
        private byte[] bytes;
        private Image image;
        private final File file;

        public ImageSaver(BasicCameraFragment fragment, Image image, File file) {
            this(fragment, file);
            this.image = image;
        }

        public ImageSaver(BasicCameraFragment fragment, byte[] bytes, File file) {
            this(fragment, file);
            this.bytes = bytes;
        }

        private ImageSaver(BasicCameraFragment fragment, File file) {
            this.fragmentWeakReference = new WeakReference<BasicCameraFragment>(fragment);
            this.file = file;
        }

        @Override
        public void run() {
            if (image != null) {
                saveImage();
            } else {
                writeBytesToFile(bytes);
            }
            final BasicCameraFragment fragment = fragmentWeakReference.get();
            if (fragment != null && fragment.getActivity() != null) {
                fragment.deliverResult();
            }
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        private void saveImage() {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            writeBytesToFile(bytes);
            image.close();
        }

        private void writeBytesToFile(byte[] bytes) {
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }

}
