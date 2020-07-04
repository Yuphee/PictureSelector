package com.heiko.camera;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraLogger;
import com.otaliastudios.cameraview.CameraOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Facing;
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
    private long mTakePictureTime;
    private long mFlashTime;

    public static final String TAG = "Picture-CameraActivity";
    private String outputPath;
    private boolean enablePreview;
    private boolean enableVoice;
    private boolean enableFacing;
    private Integer maskImgRes;
    private File mFile;
    private CameraStore cameraStore;
    private ImageView imgFlash;

    public static final String SP_FLASH_ENABLE = "SP_FLASH_ENABLE_V2";
    public static final int REQUEST_COMPLETE = 1653;
    private AppCompatButton imgCameraFacing;
    private boolean isFrontFacing = false; //是否是前置摄像头
    ObjectAnimator facingAnimator;

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
            enableFacing = extras.getBoolean("enable_facing");
            maskImgRes = extras.getInt("mask_img_res", -1);

            Log.i(TAG, "output:" + outputPath + " enablePreview:" + enablePreview);
        }
        mFile = new File(outputPath);
        Log.i(TAG, "mFile:" + mFile.getPath());

        cameraStore = new CameraStore(this);

        imgFlash = findViewById(R.id.img_flash);
        View layoutFlash = findViewById(R.id.layout_flash);
        layoutFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long dTime = System.currentTimeMillis() - mFlashTime;
                Log.i(TAG, "layoutFlash dTime:" + dTime);
                if (dTime <= 600) return;
                mFlashTime = System.currentTimeMillis();

                int flashState = Integer.valueOf(imgFlash.getTag().toString());
                flashState = (++flashState) % 4;
                setFlash(flashState);
            }
        });

        int flashState = cameraStore.getInt(SP_FLASH_ENABLE, FlashEnum.AUTO);
        setFlash(flashState);

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

        imgCameraFacing = findViewById(R.id.img_camera_facing);
        imgCameraFacing.setVisibility(enableFacing ? View.VISIBLE : View.GONE);
        if (enableFacing) {
            imgCameraFacing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchCameraFacing();
                }
            });
        }
    }

    //切换前/后摄像头
    private void switchCameraFacing() {
        if (facingAnimator != null && facingAnimator.isRunning()) {
            return;
        }
        facingAnimator = ObjectAnimator.ofFloat(imgCameraFacing, "rotation", 0, -180);
        facingAnimator.setDuration(300);
        facingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        facingAnimator.start();
        if (isFrontFacing) {
            camera.setFacing(Facing.BACK);
        } else {
            camera.setFacing(Facing.FRONT);
        }
        isFrontFacing = !isFrontFacing;
    }

    private void setFlash(@FlashEnum int flashEnum) {
        cameraStore.putInt(SP_FLASH_ENABLE, flashEnum);
        imgFlash.setTag(flashEnum);
        switch (flashEnum) {
            case FlashEnum.AUTO:
                camera.setFlash(Flash.AUTO);
                imgFlash.setBackgroundResource(R.drawable.ic_camera_flash_auto);
                break;
            case FlashEnum.ON:
                camera.setFlash(Flash.ON);
                imgFlash.setBackgroundResource(R.drawable.ic_camera_flash_open);
                break;
            case FlashEnum.OFF:
                camera.setFlash(Flash.OFF);
                imgFlash.setBackgroundResource(R.drawable.ic_camera_flash_close);
                break;
            case FlashEnum.TORCH:
                camera.setFlash(Flash.TORCH);
                imgFlash.setBackgroundResource(R.drawable.ic_camera_flash_torch);
                break;
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

                if (enablePreview) {
                    ImagePreviewActivity.start(CameraActivity.this, mFile.getPath(), REQUEST_COMPLETE);
                } else {
                    setResult(Activity.RESULT_OK);
                    finish();
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
        long dTime = System.currentTimeMillis() - mTakePictureTime;
        Log.i(TAG, "takePicture dTime:" + dTime);
        if (dTime <= 1200) return;
        mTakePictureTime = System.currentTimeMillis();

        if (mCapturingPicture) return;
        mCapturingPicture = true;
        mCaptureTime = System.currentTimeMillis();
        mCaptureNativeSize = camera.getPictureSize();
        //camera.capturePicture();
        camera.takePicture();
        Log.i(TAG, "takePicture>>>");

        Intent intent = new Intent("action.com.luck.pictureselector.takePicture");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

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
