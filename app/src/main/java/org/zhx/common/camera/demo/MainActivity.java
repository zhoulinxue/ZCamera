package org.zhx.common.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import org.zhx.common.camera.widget.FocusRectView;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ImageUtil;
import org.zhx.common.util.PermissionsUtil;

import java.io.IOException;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener, CameraModel.view<Camera>, View.OnClickListener, SurfaceHolder.Callback {
    private ImageView mShowImage, mShutterImg, mFlashImg, mThumImag;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private CameraPresenter mPresenter;
    private int[] modelResId = {org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_auto_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_on_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_off_normal, org.zhx.common.camera.R.drawable.ic_camera_top_bar_flash_torch_normal};
    private RelativeLayout.LayoutParams showLp;
    private RelativeLayout mRootView;
    Point screenP, mPreviewPoint;
    FocusRectView mFocusView;
    private Bitmap mThumilBitmap;
    private Uri mUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new CameraPresenter(this);
        screenP = CameraUtil.getScreenMetrics(this);
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
        mPresenter.showImages();
    }

    private void initHolder() {
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.setOnTouchListener(this);
    }

    @Override
    public AppCompatActivity getContext() {
        return this;
    }

    @Override
    public void onError(int msg) {
        mShutterImg.setEnabled(true);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraCreate(CameraProxy<Camera> proxy) throws IOException {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            proxy.getCamera().setDisplayOrientation(90);
        } else {
            proxy.getCamera().setDisplayOrientation(0);
        }
        RelativeLayout.LayoutParams preViewLp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
        mPreviewPoint = new Point(proxy.getWidth(), proxy.getHeight());
        preViewLp.width = mPreviewPoint.x;
        preViewLp.height = mPreviewPoint.y;
        mSurfaceView.setLayoutParams(preViewLp);
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
            case R.id.z_thumil_img:
                Intent i = new Intent(this, ShowImageActivity.class);
                i.putExtra("bitmap", mUri.toString());
                ActivityOptionsCompat optionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(this, mThumImag, "image");
                startActivity(i, optionsCompat.toBundle());
                break;
        }
    }

    @Override
    public void onPictrueCallback(byte[] data) {
        mShutterImg.setEnabled(true);
        final Bitmap bitmap = ImageUtil.getBitmap(this, data, false);
        mShowImage.setImageBitmap(bitmap);
        mThumilBitmap = ImageUtil.getThumilImage(this, data);
        mShowImage.animate()
                .translationX(-(screenP.x / 2 - 2 * mThumImag.getX()))
                .translationY(screenP.y / 2 - 2 * mThumImag.getY())
                .scaleX(0.01f)
                .scaleY(0.01f)
                .setDuration(90)
                .withLayer()
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mRootView.removeView(mShowImage);
                        mShowImage.setImageBitmap(null);
                        ImageUtil.recycleBitmap(bitmap);
                        mThumImag.setImageBitmap(mThumilBitmap);
                        mShowImage = new ImageView(MainActivity.this);
                        mShowImage.setId(R.id.z_base_camera_showImg);
                        addView(mRootView.getChildCount(), mShowImage, showLp);
                    }
                })
                .setInterpolator(new AccelerateInterpolator()).start();
    }

    @Override
    public void onSaveResult(Uri uri) {
        if (uri != null)
            mUri = uri;
    }

    @Override
    public void showLastImag(ImageData imageData) {
        try {
            mUri = imageData.getContentUri();
            mThumImag.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), mUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
