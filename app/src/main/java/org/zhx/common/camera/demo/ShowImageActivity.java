package org.zhx.common.camera.demo;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class ShowImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        ImageView showImage = findViewById(R.id.z_base_camera_showImg_big);
        String path = getIntent().getStringExtra("bitmap");
        try {
            showImage.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        showImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
