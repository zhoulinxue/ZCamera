package org.zhx.common.camera.demo;

import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CameraActivity extends AppCompatActivity {
    private CameraFrangment frangment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Android 15 (API 35) enforces edge-to-edge display.
        // Ensure content draws behind system bars with FULLSCREEN.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            getWindow().setDecorFitsSystemWindows(false);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_layout);

        frangment = new CameraFrangment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, frangment).commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean alreadyClick = super.dispatchTouchEvent(ev);
        if (frangment != null && !alreadyClick) {
            frangment.onTouch(ev);
        }
        return alreadyClick;
    }
}
