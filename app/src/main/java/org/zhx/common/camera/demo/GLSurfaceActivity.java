package org.zhx.common.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import org.zhx.common.camera.BaseActivity;
import org.zhx.common.camera.CameraAction;
import org.zhx.common.camera.CameraModel;
import org.zhx.common.camera.CameraPresenter;
import org.zhx.common.camera.CameraProxy;
import org.zhx.common.camera.CameraRatio;
import org.zhx.common.camera.Constants;
import org.zhx.common.camera.tasks.ImageSearchProcessor;
import org.zhx.common.camera.tasks.SensorProcessor;
import org.zhx.common.camera.widget.CameraGLSurfaceView;
import org.zhx.common.camera.widget.FocusRectView;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ImageUtil;
import org.zhx.common.util.PermissionsUtil;
import org.zhx.common.util.ZCameraLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceActivity extends BaseActivity implements View.OnTouchListener, CameraModel.view<Camera>, View.OnClickListener, GLSurfaceView.Renderer {
    private static final long SWITCH_DELAY = 25;
    private ImageView mShowImage, mShutterImg, mFlashImg, mThumImag;
    private View animateHolder;
    private CameraGLSurfaceView mSurfaceView;
    private CameraPresenter mPresenter;
    private int[] modelResId = {
            R.drawable.ic_camera_top_bar_flash_auto_normal,
            R.drawable.ic_camera_top_bar_flash_on_normal,
            R.drawable.ic_camera_top_bar_flash_off_normal,
            R.drawable.ic_camera_top_bar_flash_torch_normal};
    private RelativeLayout.LayoutParams showLp;
    private RelativeLayout mRootView;
    Point screenP, mPreviewPoint;
    FocusRectView mFocusView;
    private ImageSearchProcessor mImageSearchProcessor;
    private SensorProcessor mSensorProcessor;
    protected CameraRatio mRatio = CameraRatio.SCANLE_1_1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        screenP = CameraUtil.getScreenMetrics(this);
        mPresenter = new CameraPresenter(this);
        mImageSearchProcessor = new ImageSearchProcessor(this, this);
        mSensorProcessor = new SensorProcessor(this, this);
        getLifecycle().addObserver(mPresenter);
        setContentView(R.layout.activity_glsurface);
        getWindow().addFlags((WindowManager.LayoutParams.FLAG_FULLSCREEN));
        mShowImage = findViewById(R.id.z_base_camera_showImg);
        animateHolder = findViewById(R.id.animate_place_holder);
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
        mImageSearchProcessor.showImags(Constants.FILE_DIR);
    }

    private void initHolder() {
        mSurfaceView.setViewRender(this);
        mSurfaceView.setOnTouchListener(this);
    }

    @Override
    public void onError(int msg) {
        super.onError(msg);
        mShutterImg.setEnabled(true);
    }

    @Override
    public void onCameraCreate(final CameraProxy<Camera> proxy) throws IOException {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final RelativeLayout.LayoutParams preViewLp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
                int previewHeight = 0;
                int previewWidth = screenP.x;

                if (proxy.getWidth() < proxy.getHeight()) {
                    previewHeight = screenP.x * proxy.getHeight() / proxy.getWidth();
                } else {
                    previewHeight = screenP.x * proxy.getWidth() / proxy.getHeight();
                }

                mPreviewPoint = new Point(previewWidth, previewHeight);
                preViewLp.width = previewWidth;
                preViewLp.height = previewHeight;
                mSurfaceView.setLayoutParams(preViewLp);
            }
        });

        mSurfaceView.setCameraId(proxy.getCameraId());
        proxy.getCamera().setPreviewTexture(mSurfaceView.getSurface());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.z_take_pictrue_img:
                mShutterImg.setEnabled(false);
                mPresenter.takePictrue();
                break;
            case R.id.btn_switch_camera:
                mPresenter.releaseCamera(CameraAction.SWITCH_CAMERA);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPresenter.startCamera(CameraAction.SWITCH_CAMERA);
                    }
                }, SWITCH_DELAY);
                break;
            case R.id.btn_flash_mode:
                int position = mPresenter.chanageFlashMode();
                mFlashImg.setImageResource(modelResId[position]);
                break;
            case R.id.z_thumil_img:
                if (mImageDatas != null && mImageDatas.size() != 0) {
                    Intent i = new Intent(this, ShowImageActivity.class);
                    i.putParcelableArrayListExtra(Constants.HISTORE_PICTRUE, (ArrayList<? extends Parcelable>) mImageDatas);
                    ActivityOptionsCompat optionsCompat =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(this, animateHolder, "image");
                    startActivity(i, optionsCompat.toBundle());
                }
                break;
        }
    }

    public void showImageData(Uri contentUri, boolean isAnimate) throws IOException {
        final Bitmap bitmap = ImageUtil.getBitmapFormUri(this, contentUri);
        ZCameraLog.e("CameraPresenter", "....Camera...take complete..............." + System.currentTimeMillis());
        if (isAnimate) {
            mShowImage.setImageBitmap(bitmap);
            mShowImage.animate()
                    .translationX(-(screenP.x / 2 + mThumImag.getX()) + CameraUtil.dip2px(this, 45))
                    .translationY(screenP.y / 2 - mThumImag.getY() - CameraUtil.dip2px(this, 55))
                    .scaleX(0.01f)
                    .scaleY(0.01f)
                    .setDuration(90)
                    .withLayer()
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mRootView.removeView(mShowImage);
                            mShowImage.setImageBitmap(null);
                            mThumImag.setImageBitmap(bitmap);
                            mShowImage = new ImageView(GLSurfaceActivity.this);
                            mShowImage.setId(R.id.z_base_camera_showImg);
                            addView(mRootView.getChildCount(), mShowImage, showLp);
                            ZCameraLog.e("CameraPresenter", "....Camera...show end..............." + System.currentTimeMillis());
                        }
                    }).setInterpolator(new AccelerateInterpolator()).start();
        } else {
            mThumImag.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onTakeComplete() {
        mShutterImg.setEnabled(true);
    }

    @Override
    public int getDegree(boolean isFrontCamera) {
        return mSensorProcessor.getDegree(isFrontCamera);
    }

    @Override
    public Camera.Size getSuitableSize(List<Camera.Size> sizes) {
        int minDelta = Integer.MAX_VALUE; // 最小的差值，初始值应该设置大点保证之后的计算中会被重置
        int index = 0; // 最小的差值对应的索引坐标
        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size previewSize = sizes.get(i);
            // 找到一个与设置的分辨率差值最小的相机支持的分辨率大小
            ZCameraLog.e("getSuitableSize, width:" + previewSize.width + ", height:" + previewSize.height);
            if (previewSize.width * mRatio.getHeightRatio() / mRatio.getWidthRatio() == previewSize.height) {
                int delta = Math.abs(screenP.x - previewSize.height);
                if (minDelta >= delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return sizes.get(index); // 默认返回与设置的分辨率最接近的预览尺寸
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera(CameraAction.PERMISSITON_GRANTED);
                }
                break;
            case Constants.STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (PermissionsUtil.hasPermission(this, Manifest.permission.CAMERA)) {
                        startCamera(CameraAction.PERMISSITON_GRANTED);
                    } else {
                        PermissionsUtil.requestPermission(this, Manifest.permission.CAMERA, Constants.CAMERA);
                    }
                }
                break;


        }
    }

    private void startCamera(CameraAction action) {
        mPresenter.startCamera(action);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mSurfaceView) {
            if (mFocusView == null) {
                mFocusView = new FocusRectView(this);
                RelativeLayout.LayoutParams focusLp = new RelativeLayout.LayoutParams(screenP.x, screenP.y);
                focusLp.addRule(RelativeLayout.CENTER_IN_PARENT);
                addView(mRootView.getChildCount(), mFocusView, focusLp);
            }
            if (!mPresenter.isFocusing()) {
                mFocusView.setVisibility(View.VISIBLE);
                mFocusView.setTouchFoucusRect(event.getX(), event.getY());
                mPresenter.focusArea(event.getX(), event.getY(), mFocusView);
            }
        }
        return false;
    }

    private void addView(int childCount, View view, RelativeLayout.LayoutParams layoutParams) {
        mRootView.addView(view, childCount, layoutParams);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        startCamera(CameraAction.SURFACE_CREATE);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

}