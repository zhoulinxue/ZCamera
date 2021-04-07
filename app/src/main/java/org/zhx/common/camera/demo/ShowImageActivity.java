package org.zhx.common.camera.demo;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.IOException;

public class ShowImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        SubsamplingScaleImageView showImage = findViewById(R.id.z_base_camera_showImg_big);
        String path = getIntent().getStringExtra("bitmap");
        showImage.setImage(ImageSource.uri(Uri.parse(path)));
    }
}
