package org.zhx.common.camera.demo;

import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.zhx.common.camera.AutoFocusManager;
import org.zhx.common.util.DisplayUtil;
import org.zhx.common.camera.widget.OverlayerView;

public class Camera2Activity extends AppCompatActivity {

    public static BitmapFactory.Options opt;
    static {
        // 缩小原图片大小
        opt = new BitmapFactory.Options();
        opt.inSampleSize = 2;
    }

    private SurfaceView mPreView;
    private SurfaceHolder mHolder;

    private Camera mCamera;
    private boolean isPreview = false;
    private Point displayPx;
    private ImageView tpImg, showImg;
    private Button saveBtn;
    // 取景框
    private OverlayerView mLayer;
    private Rect rect;
    private boolean isTake = false;
    private String[] flashMedols={Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_TORCH};
    private int[]    modelResId={org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_auto_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_on_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_off_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_torch_normal};
    /**
     * 切换摄像头
     */
    private ImageView swImg;
    private ImageView flashModelImg;
    /**
     * 当前是否是前置摄像头
     */
    private boolean isFrontCamera = false;
    int modelIndex=0;
    private final int CAMERA =10;
    private AutoFocusManager autoFocusManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zcamera_base_layout);
        displayPx = DisplayUtil.getScreenMetrics(this);
        mPreView = findViewById(org.zhx.common.camera.R.id.z_base_camera_preview);
        tpImg = findViewById(org.zhx.common.camera.R.id.z_take_pictrue_img);
        saveBtn = findViewById(org.zhx.common.camera.R.id.z_base_camera_save);
        showImg = findViewById(org.zhx.common.camera.R.id.z_base_camera_showImg);
        mLayer = findViewById(org.zhx.common.camera.R.id.z_base_camera_over_img);
        swImg =  findViewById(org.zhx.common.camera.R.id.btn_switch_camera);
        flashModelImg= findViewById(org.zhx.common.camera.R.id.btn_flash_mode);


    }
}
