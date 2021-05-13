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
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import org.zhx.common.camera.CameraAction;
import org.zhx.common.camera.CameraModel;
import org.zhx.common.camera.CameraPresenter;
import org.zhx.common.camera.CameraProxy;
import org.zhx.common.camera.Constants;
import org.zhx.common.camera.ImageData;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new CameraPresenter(this);
        screenP = CameraUtil.getScreenMetrics(this);
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
        mPresenter.showImages();
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
    public void onCameraCreate(CameraProxy<Camera> proxy) throws IOException {
        final RelativeLayout.LayoutParams preViewLp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        mPreviewPoint = new Point(proxy.getWidth(), proxy.getHeight());
        preViewLp.width = mPreviewPoint.x;
        preViewLp.height = mPreviewPoint.y;
        mSurfaceView.post(new Runnable() {
            @Override
            public void run() {
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
                mPresenter.switchCamera();
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

    public void setImageData(Uri contentUri, boolean isAnimate) throws IOException {
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
                RelativeLayout.LayoutParams focusLp = new RelativeLayout.LayoutParams(mPreviewPoint.x, mPreviewPoint.y);
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