package com.heiko.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


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

        Bitmap bitmap = BitmapFactory.decodeFile(url);
        int degree = getExifOrientation(url);
        if (degree == 90 || degree == 180 || degree == 270) { //有些摄像头方向不同，需要判断图片角度，进行旋转
            // Roate preview icon according to exif orientation
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            bitmap = Bitmap.createBitmap(bitmap,
                    0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        }
        imgPreview.setImageBitmap(bitmap);
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

    public int getExifOrientation(String filepath) {
        int degree = 0;

        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            // MmsLog.e(ISMS_TAG, "getExifOrientation():",ex);
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                // We only recognize a subset of orientation tag values.
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                    default:
                        break;
                }
            }
        }
        return degree;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
