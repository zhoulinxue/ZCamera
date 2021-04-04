package org.zhx.common.camera.demo;

import android.animation.Animator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.zhx.common.camera.CameraAction;
import org.zhx.common.camera.CameraModel;
import org.zhx.common.camera.CameraPresenter;
import org.zhx.common.camera.CameraProxy;
import org.zhx.common.camera.Constants;
import org.zhx.common.camera.widget.FocusRectView;
import org.zhx.common.camera.widget.OverlayerView;
import org.zhx.common.util.DisplayUtil;
import org.zhx.common.util.ImageUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, CameraModel.view<Camera>, View.OnClickListener, SurfaceHolder.Callback {
    private ImageView mShowImage, mShutterImg, mFlashImg, mThumImag;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private CameraPresenter mPresenter;
    private int[] modelResId = {org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_auto_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_on_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_off_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_torch_normal};
    private RelativeLayout.LayoutParams showLp;
    private RelativeLayout mRootView;
    private Handler mHandler;
    Point screenP;
    FocusRectView mFocusView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new CameraPresenter(this);
        mHandler = new Handler();
        screenP = DisplayUtil.getScreenMetrics(this);
        setContentView(R.layout.activity_main);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        mShowImage = findViewById(R.id.z_base_camera_showImg);
        showLp = (RelativeLayout.LayoutParams) mShowImage.getLayoutParams();
        mShutterImg = findViewById(R.id.z_take_pictrue_img);
        mSurfaceView = findViewById(R.id.z_base_camera_preview);
        mRootView = findViewById(R.id.root_layout);
        findViewById(R.id.btn_switch_camera).setOnClickListener(this);
        mFlashImg = findViewById(R.id.btn_flash_mode);
        mThumImag = findViewById(R.id.z_thumil_img);
        mThumImag.setOnClickListener(this);
        mFlashImg.setOnClickListener(this);
        mShutterImg.setOnClickListener(this);
        initHolder();
    }

    private void initHolder() {
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.setOnTouchListener(this);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void onError(int msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraCreate(CameraProxy<Camera> proxy) throws IOException {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            proxy.getCamera().setDisplayOrientation(90);
        } else {
            proxy.getCamera().setDisplayOrientation(0);
        }
        proxy.getCamera().setPreviewDisplay(mHolder);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.z_take_pictrue_img:
                mShutterImg.setEnabled(false);
                mPresenter.takePictrue();
                break;
            case R.id.btn_switch_camera:
                mPresenter.switchCamera();
                break;
            case R.id.btn_flash_mode:
                int position = mPresenter.chanageFlashMode();
                mFlashImg.setImageResource(modelResId[position]);
                break;
        }
    }

    @Override
    public void onPictrueCallback(byte[] data) {
        mShutterImg.setEnabled(true);
        Bitmap bitmap = ImageUtil.getBitmap(this, data, false);
        mShowImage.setImageBitmap(bitmap);
        final Bitmap thumil = bitmap;
        mShowImage.animate()
                .translationX(-(screenP.x / 2 - 2 * mThumImag.getX()))
                .translationY(screenP.y / 2 - 2 * mThumImag.getY())
                .scaleX(0.01f)
                .scaleY(0.01f)
                .setDuration(150)
                .withLayer()
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mRootView.removeView(mShowImage);
                        mShowImage.setImageBitmap(null);
                        mThumImag.setImageBitmap(thumil);
                        mShowImage = new ImageView(MainActivity.this);
                        mShowImage.setId(R.id.z_base_camera_showImg);
                        addView(1, mShowImage, showLp);
                    }
                })
                .setInterpolator(new AccelerateInterpolator()).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera(CameraAction.PERMISSITON_GRANTED);
                }
                return;
            }

        }
    }

    private void startCamera(CameraAction action) {
        mPresenter.startCamera(action);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startCamera(CameraAction.SURFACE_CREATE);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mPresenter.releaseCamera(CameraAction.SURFACE_DESTORY);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mSurfaceView) {
            int x = (int) (event.getX() / v.getWidth() * 2000) - 1000; // 获取映射区域的X坐标
            int y = (int) (event.getY() / v.getWidth() * 2000) - 1000; // 获取映射区域的Y坐标
            Point point = new Point(x, y);
            if (mFocusView == null) {
                mFocusView = new FocusRectView(this);
                addView(mRootView.getChildCount(), mFocusView, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            }
            mFocusView.setVisibility(View.VISIBLE);
            mFocusView.setTouchFoucusRect(event.getX(), event.getY());
            mPresenter.focusArea(mFocusView, point);
        }
        return false;
    }

    private void addView(int childCount, View view, RelativeLayout.LayoutParams layoutParams) {
        mRootView.addView(view, childCount, layoutParams);
    }
}
