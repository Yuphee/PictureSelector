package com.heiko.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


/**
 * 相机拍照后预览
 */
public class ImagePreviewActivity extends AppCompatActivity {

    private ImageView imgPreview;
    private ImageView imgComplete;
    private ImageView imgClose;

    public static final String KEY_URL = "key_url";
    private String url;

    public static void start(Activity activity, String url, int requestCode) {
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        intent.putExtra(KEY_URL, url);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_image_preview);

        url = getIntent().getExtras().getString(KEY_URL);

        imgPreview = findViewById(R.id.img_preview);
        imgComplete = findViewById(R.id.img_complete);
        imgClose = findViewById(R.id.img_close);

        Glide.with(this).load(url).into(imgPreview);
        imgComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
