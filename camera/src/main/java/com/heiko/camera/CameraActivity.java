package com.heiko.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Flash;
import com.otaliastudios.cameraview.size.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraView camera;

    private boolean mCapturingPicture;

    // To show stuff in the callback
    private Size mCaptureNativeSize;
    private long mCaptureTime;

    public static final String TAG = "Picture-CameraActivity";
    private String outputPath;
    private boolean enablePreview;
    private boolean enableVoice;
    private Integer maskImgRes;
    private File mFile;
    private CameraStore cameraStore;
    private CheckBox cbFlash;

    public static final String SP_FLASH_ENABLE = "SP_FLASH_ENABLE";
    public static final int REQUEST_COMPLETE = 1653;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_camera);
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE);

        camera = findViewById(R.id.camera);
        camera.setLifecycleOwner(this);
        camera.addCameraListener(new CameraListener() {
            @Override
            public void onCameraOpened(CameraOptions options) {
                Log.i(TAG, "onCameraOpened 当相机打开");
            }

            @Override
            public void onPictureTaken(@NonNull PictureResult result) {
                super.onPictureTaken(result);
                Log.i(TAG, "onPictureTaken 拍照回调");
                onPicture(result.getData());
            }

            @Override
            public void onVideoTaken(@NonNull VideoResult result) {
                super.onVideoTaken(result);
                Log.i(TAG, "onVideoTaken ");
            }
        });

        findViewById(R.id.btn_photo).setOnClickListener(this);

        Log.v(TAG, "onCreate");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            outputPath = extras.getString("output");
            enablePreview = extras.getBoolean("enable_preview");
            enableVoice = extras.getBoolean("enable_voice");
            maskImgRes = extras.getInt("mask_img_res", -1);

            Log.i(TAG, "output:" + outputPath + " enablePreview:" + enablePreview);
        }
        mFile = new File(outputPath);
        Log.i(TAG, "mFile:" + mFile.getPath());

        cameraStore = new CameraStore(this);

        cbFlash = findViewById(R.id.cb_flash);
        View layoutFlash = findViewById(R.id.layout_flash);
        layoutFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFlash(!cbFlash.isChecked());
            }
        });

        setFlash(cameraStore.getBoolean(SP_FLASH_ENABLE, false));

        View layoutClose = findViewById(R.id.layout_close);
        layoutClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (maskImgRes > 0) {
            ImageView imgCameraMask = findViewById(R.id.img_camera_mask);
            imgCameraMask.setVisibility(View.VISIBLE);
            imgCameraMask.setImageDrawable(getResources().getDrawable(maskImgRes));
        }
    }

    private void setFlash(boolean enable) {
        cameraStore.putBoolean(SP_FLASH_ENABLE, enable);
        if (enable) {
            camera.setFlash(Flash.ON);
            cbFlash.setChecked(true);
        } else {
            camera.setFlash(Flash.OFF);
            cbFlash.setChecked(false);
        }
    }

    private void onPicture(final byte[] jpeg) {
        mCapturingPicture = false;
        long callbackTime = System.currentTimeMillis();

        // This can happen if picture was taken with a gesture.
        if (mCaptureTime == 0) mCaptureTime = callbackTime - 300;
        if (mCaptureNativeSize == null) mCaptureNativeSize = camera.getPictureSize();


        mCaptureTime = 0;
        mCaptureNativeSize = null;

        new Thread() {
            @Override
            public void run() {
                super.run();

                FileOutputStream output = null;
                try {
                    Log.i(TAG, "写入图片:" + mFile.getPath() + " 是否存在:" + mFile.exists() + " buffer:" + jpeg.length);
                    if (mFile.exists()) {
                        boolean result = mFile.delete();
                        Log.i(TAG, "写入图片result:" + result);
                    }
                    output = new FileOutputStream(mFile);
                    output.write(jpeg);
                    Log.i(TAG, "写入图片成功");
                    if (enablePreview) {
                        ImagePreviewActivity.start(CameraActivity.this, mFile.getPath(), REQUEST_COMPLETE);
                    } else {
                        setResult(Activity.RESULT_OK);
                        finish();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "写入图片失败:" + e.getMessage());
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
        }.start();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_photo) {
            capturePhoto();
        }
    }

    private void capturePhoto() {
        if (mCapturingPicture) return;
        mCapturingPicture = true;
        mCaptureTime = System.currentTimeMillis();
        mCaptureNativeSize = camera.getPictureSize();
        //camera.capturePicture();
        camera.takePicture();

        if (enableVoice) {
            new ShutterPlayer(CameraActivity.this).play();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean valid = true;
        for (int grantResult : grantResults) {
            valid = valid && grantResult == PackageManager.PERMISSION_GRANTED;
        }
        if (valid && !camera.isOpened()) {
            camera.open();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_COMPLETE && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}
