package org.zhx.common.camera.demo;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import org.zhx.common.util.ImageUtil;

import java.io.IOException;

public class ShowImageActivity extends AppCompatActivity {
    private ProgressBar mBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        final SubsamplingScaleImageView showImage = findViewById(R.id.z_base_camera_showImg_big);
        mBar = findViewById(R.id.load_process);
        String path = getIntent().getStringExtra("bitmap");
        final Uri uri = Uri.parse(path);
        try {
            showImage.setImage(ImageSource.bitmap(ImageUtil.getBitmapFormUri(this, uri)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = ImageUtil.getBitmapFromUri(ShowImageActivity.this, uri);
                showImage.post(new Runnable() {
                    @Override
                    public void run() {
                        mBar.setVisibility(View.GONE);
                        showImage.setImage(ImageSource.bitmap(bitmap));
                    }
                });
            }
        }).start();
    }
}
