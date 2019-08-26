package com.heiko.camera;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;

import java.io.File;
import java.io.FileOutputStream;

public class CameraActivity2 extends Activity {

    private CameraKitView cameraView;
    private AppCompatButton photoButton;
    private String outputPath;
    public static final String SP_FLASH = "sp_flash";
    public static final String TAG = "CameraKitView";
    private CheckBox cbFlash;
    private CameraStore cameraStore;
    private long startTime;
    private static final int FLASH_WAIT_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

        Log.v(TAG, "onCreate");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            outputPath = extras.getString("output");
            Log.i(TAG, "output:" + outputPath);
        }
        cameraView = findViewById(R.id.camera);
        photoButton = findViewById(R.id.btn_photo);
        photoButton.setOnClickListener(photoOnClickListener);
        cbFlash = findViewById(R.id.cb_flash);

        cameraStore = new CameraStore(this);
        boolean isFlashOpen = cameraStore.getBoolean(SP_FLASH, false);
        cbFlash.setChecked(isFlashOpen);
        setFlash(isFlashOpen);

        cameraView.setCameraListener(new CameraKitView.CameraListener() {
            @Override
            public void onOpened() {
                Log.v(TAG, "CameraListener: onOpened()");
            }

            @Override
            public void onClosed() {
                Log.v(TAG, "CameraListener: onClosed()");
            }
        });

        cameraView.setPreviewListener(new CameraKitView.PreviewListener() {
            @Override
            public void onStart() {
                Log.v(TAG, "PreviewListener: onStart()");
                //updateInfoText();
                startTime = System.currentTimeMillis();
                photoButton.setEnabled(true);
            }

            @Override
            public void onStop() {
                Log.v(TAG, "PreviewListener: onStop()");
            }
        });

        cameraView.requestPermissions(this);
        /*cameraView.setPermissionsListener(new CameraKitView.PermissionsListener() {
            @Override
            public void onPermissionsSuccess() {

            }

            @Override
            public void onPermissionsFailure() {

            }
        });*/

        findViewById(R.id.layout_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraActivity2.this.finish();
            }
        });

        findViewById(R.id.layout_flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean operation = !cbFlash.isChecked();
                Log.i(TAG, "operation:" + operation);
                setFlash(operation);
                cbFlash.setChecked(operation);
                cameraStore.putBoolean(SP_FLASH, operation);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    public void onPause() {
        cameraView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private long lastTime = 0;

    private View.OnClickListener photoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (System.currentTimeMillis() - lastTime > FLASH_WAIT_TIME) {
                lastTime = System.currentTimeMillis();
                //防止多次点击

                boolean isFlashOpen = cbFlash.isChecked();

                Log.i(TAG, "isFlashOpen:" + isFlashOpen);
                if (isFlashOpen) {
                    long dTime = System.currentTimeMillis() - startTime;
                    Log.i(TAG, "dTime:" + dTime);
                    if (dTime > FLASH_WAIT_TIME) {
                        realTakePicture();
                    } else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                realTakePicture();
                            }
                        }, FLASH_WAIT_TIME - dTime);
                    }
                } else {
                    realTakePicture();
                }
            }
        }

        private void realTakePicture() {
            Log.v(TAG, "photo button click");
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    ShutterPlayer shutter = new ShutterPlayer(CameraActivity2.this);
                    shutter.play();
                }
            }.start();
            Log.i(TAG, "camera");
            cameraView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView view, final byte[] photo) {
                    Log.i(TAG, "onImage:" + photo.length);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "onImage save.");
                            File savedPhoto = new File(outputPath);
                            try {
                                FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                                outputStream.write(photo);
                                outputStream.close();
                            } catch (java.io.IOException e) {
                                e.printStackTrace();
                            }
                            setResult(Activity.RESULT_OK);
                            CameraActivity2.this.finish();
                        }
                    }).start();
                }
            });

            /*cameraView.setCameraListener(new CameraKitView.CameraListener() {
                @Override
                public void onOpened() {

                }

                @Override
                public void onClosed() {

                }
            });*/
        }
    };

    private void setFlash(boolean isOpen) {
        if (isOpen) {
            if (cameraView.getFlash() != CameraKit.FLASH_ON) {
                cameraView.setFlash(CameraKit.FLASH_ON);
                startTime = System.currentTimeMillis();
            }
        } else {
            if (cameraView.getFlash() != CameraKit.FLASH_OFF) {
                cameraView.setFlash(CameraKit.FLASH_OFF);
                startTime = System.currentTimeMillis();
            }
        }
        Log.i(TAG, "setFlash:" + isOpen);
    }
}
