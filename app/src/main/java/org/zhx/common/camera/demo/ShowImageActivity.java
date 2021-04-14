package org.zhx.common.camera.demo;

import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ImageUtil;
import org.zhx.common.util.ZCameraLog;

import java.io.InputStream;

import static androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90;

public class ShowImageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        SubsamplingScaleImageView showImage = findViewById(R.id.z_base_camera_showImg_big);
        String path = getIntent().getStringExtra("bitmap");
        Uri uri = Uri.parse(path);
        showImage.setImage(ImageSource.bitmap(ImageUtil.getBitmapFromUri(this, uri)));
    }
}
