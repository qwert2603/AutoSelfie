package com.qwert2603.autoselfie.helpers;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.qwert2603.autoselfie.R;
import com.qwert2603.autoselfie.utils.LogUtils;

import java.util.List;

/**
 * Активити, для получения снимков.
 * Ибо нужен настоящий SurfaceView.
 * Эта активити прозрачная и только делает фото на фронталку.
 */
public class SurfaceViewActivity extends Activity {

    public static final String EXTRA_CALLBACK_ID = "com.qwert2603.autoselfie.EXTRA_CALLBACK_ID";

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar supportActionBar = getActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surf);

        int callbackId = getIntent().getIntExtra(EXTRA_CALLBACK_ID, 0);
        PhotoHelper.Callback callback = PhotoHelper.getCallback(callbackId);
        if (callback == null) {
            return;
        }

        try {
            final Camera camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setRotation(270);

            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            Camera.Size size = supportedPictureSizes.get(0);
            int leastArea = size.width * size.height;
            for (Camera.Size s : supportedPictureSizes) {
                int area = size.width * size.height;
                if (area < leastArea) {
                    size = s;
                    leastArea = area;
                }
            }
            parameters.setPreviewSize(size.width, size.height);
            parameters.setPictureSize(size.width, size.height);
            LogUtils.d("size == " + size.width + " * " + size.height);

            camera.setParameters(parameters);
            final SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        LogUtils.d("SurfaceViewActivity#surfaceCreated");
                        camera.setPreviewDisplay(surfaceHolder);
                        camera.startPreview();
                        camera.takePicture(null, null, (data, camera1) -> {
                            LogUtils.d("SurfaceViewActivity#takePicture & data.length == " + data.length);
                            callback.onSuccess(BitmapFactory.decodeByteArray(data, 0, data.length));
                            camera.stopPreview();
                            camera.release();
                            suicide();
                        });
                    } catch (Exception e) {
                        callback.onError(e);
                        camera.release();
                        suicide();
                    }

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                }
            });
        } catch (Exception e) {
            callback.onError(e);
            suicide();
        }
    }

    private void suicide() {
        //ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        //activityManager.killBackgroundProcesses(getPackageName());
        SurfaceViewActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        LogUtils.d("SurfaceViewActivity#onDestroy");
        super.onDestroy();
    }
}
