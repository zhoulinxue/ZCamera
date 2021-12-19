package org.zhx.common.camera.demo;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_layout);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        CameraFrangment frangment = new CameraFrangment();
        Bundle bundle = new Bundle();
        bundle.putInt(CameraFrangment.SURFACE_TYPE, CameraFrangment.GL_SURFACEVIEW);
        frangment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, frangment).commit();
    }
}
