package com.luck.pictureselector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.heiko.camera.CameraActivity;
import com.luck.picture.lib.dialog.PictureDialog;

public class SimpleActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn_activity, btn_fragment;
    private PictureDialog compressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other);
        btn_activity = (Button) findViewById(R.id.btn_activity);
        btn_fragment = (Button) findViewById(R.id.btn_fragment);
        findViewById(R.id.btn_camera).setOnClickListener(this);
        btn_activity.setOnClickListener(this);
        btn_fragment.setOnClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_activity:
                intent = new Intent(SimpleActivity.this, MainActivity.class);
                startActivity(intent);
                //showCompressDialog();
                break;
            case R.id.btn_fragment:
                intent = new Intent(SimpleActivity.this, PhotoFragmentActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_camera:
                intent = new Intent(SimpleActivity.this, CameraActivity.class);
                startActivity(intent);
                break;
        }
    }

    protected void showCompressDialog() {
        if (!isFinishing()) {
            dismissCompressDialog();
            compressDialog = new PictureDialog(this);
            compressDialog.show();
        }
    }

    /**
     * dismiss compress dialog
     */
    protected void dismissCompressDialog() {
        try {
            if (!isFinishing()
                    && compressDialog != null
                    && compressDialog.isShowing()) {
                compressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
