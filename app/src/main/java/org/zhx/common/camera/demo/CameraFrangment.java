package org.zhx.common.camera.demo;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.zhx.common.camera.BaseFragment;
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
import org.zhx.common.util.PermissionsUtil;
import org.zhx.common.util.ZCameraLog;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraFrangment extends BaseFragment implements CameraModel.view<Camera>, View.OnClickListener, GLSurfaceView.Renderer, SurfaceHolder.Callback {
    public static final int SURFACEVIEW = 1;
    public static final int GL_SURFACEVIEW = 2;
    public static final String SURFACE_TYPE = "preview_type";
    private static final long SWITCH_DELAY = 25;
    private ImageView mShutterImg, mFlashImg;
    private ThumbImageView mThumImag,mShowImage;
    private View animateHolder;
    private SurfaceView mSurfaceView;
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
    protected CameraRatio mRatio = CameraRatio.SCANLE_16_9;
    private int type = SURFACEVIEW;
    private SurfaceHolder mHolder;
    private long DURATION = 90;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        screenP = CameraUtil.getScreenMetrics(getActivity());
        mPresenter = new CameraPresenter(this);
        mImageSearchProcessor = new ImageSearchProcessor(getActivity(), this);
        mSensorProcessor = new SensorProcessor(getActivity(), this);
        getLifecycle().addObserver(mPresenter);
        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();

        if (null != bundle && bundle.containsKey(SURFACE_TYPE)) {
            type = bundle.getInt(SURFACE_TYPE);
        }

        mShowImage = view.findViewById(R.id.z_base_camera_showImg);
        mShowImage.setDisableCircularTransformation(true);
        animateHolder = view.findViewById(R.id.animate_place_holder);
        mShutterImg = view.findViewById(R.id.z_take_pictrue_img);
        mRootView = view.findViewById(R.id.camera_root_layout);

        if (SURFACEVIEW == type) {
            mSurfaceView = new SurfaceView(getActivity());
        } else {
            mSurfaceView = new CameraGLSurfaceView(getActivity());
        }

        mSurfaceView.setId(R.id.z_camera_preview);
        showLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        showLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRootView.addView(mSurfaceView, showLp);
        view.findViewById(R.id.btn_switch_camera).setOnClickListener(this);
        mFlashImg = view.findViewById(R.id.btn_flash_mode);
        mThumImag = view.findViewById(R.id.z_thumil_img);
        mThumImag.setOnClickListener(this);
        mShutterImg.setOnClickListener(this);
        mFlashImg.setOnClickListener(this);
        initHolder();
        mImageSearchProcessor.showImags(Constants.FILE_DIR);
    }

    private void initHolder() {
        if (mSurfaceView instanceof CameraGLSurfaceView) {
            ((CameraGLSurfaceView) mSurfaceView).setViewRender(this);
        } else {
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
//            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    public void onError(int msg) {
        super.onError(msg);
        mShutterImg.setEnabled(true);
    }

    @Override
    public void onCameraCreate(final CameraProxy<Camera> proxy) {
        runOnUiThread(() -> {
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
            try {
                if (mSurfaceView instanceof CameraGLSurfaceView) {
                    ((CameraGLSurfaceView) mSurfaceView).setCameraId(proxy.getCameraId());
                    proxy.getCamera().setPreviewTexture(((CameraGLSurfaceView) mSurfaceView).getSurface());
                } else {
                    mSurfaceView.setLayoutParams(preViewLp);
                    proxy.getCamera().setPreviewDisplay(mHolder);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

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
                mHandler.postDelayed(() -> mPresenter.startCamera(CameraAction.SWITCH_CAMERA), SWITCH_DELAY);
                break;
            case R.id.btn_flash_mode:
                int position = mPresenter.chanageFlashMode();
                mFlashImg.setImageResource(modelResId[position]);
                break;
            case R.id.z_thumil_img:
                if (mImageDatas != null && mImageDatas.size() != 0) {
                    Intent i = new Intent(getActivity(), ShowImageActivity.class);
                    i.putParcelableArrayListExtra(Constants.HISTORE_PICTRUE, (ArrayList<? extends Parcelable>) mImageDatas);
                    ActivityOptionsCompat optionsCompat =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), animateHolder, "image");
                    startActivity(i, optionsCompat.toBundle());
                }
        }
    }

    @Override
    public void onEmptyFile() {
        ZCameraLog.e("....onEmptyFile..............."+ System.currentTimeMillis());
        showImageData(null);
    }

    public void showImageData(Uri uri) {
        Glide.with(getActivity()).asBitmap().error(new ColorDrawable(0)).override(mThumImag.getHeight()).load(uri).into(mThumImag);
    }

    @Override
    public void showThumImage(final Uri uri) {
        ZCameraLog.e("....showThumImage...............uri: "+ uri + System.currentTimeMillis() +",  "+ mThumImag.hashCode());
        Glide.with(getActivity()).asBitmap().override(mThumImag.getHeight()).load(uri).addListener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                ZCameraLog.e("....showThumImage...............onLoadFailed "+ System.currentTimeMillis());
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                ZCameraLog.e("....showThumImage...............onResourceReady "+ System.currentTimeMillis());
                mShowImage.animate()
                        .translationX(CameraUtil.dip2px(getActivity(), 47) -(screenP.x + mThumImag.getWidth()/2) / 2 )
                        .translationY(screenP.y / 2 - CameraUtil.dip2px(getActivity(), 55))
                        .scaleX(0.01f)
                        .scaleY(0.01f).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                long value = animation.getCurrentPlayTime();
                                float process = value*1f / DURATION;
                                float ratio = 1 - mThumImag.getWidth()*2f / screenP.x;

                                if (process > ratio) {
                                    mThumImag.setVisibility(View.VISIBLE);
                                }

                                ZCameraLog.e("....Camera...showing update...............process: " + process +", ratio: "+ ratio);
                            }
                        })
                        .setDuration(DURATION)
                        .withLayer()
                        .withStartAction(() -> mThumImag.setVisibility(View.GONE))
                        .withEndAction(() -> {
                            mRootView.removeView(mShowImage);
                            mThumImag.setImageBitmap(resource);
                            mShowImage.setImageBitmap(null);
                            mShowImage = new ThumbImageView(getActivity());
                            mShowImage.setDisableCircularTransformation(true);
                            mShowImage.setId(R.id.z_base_camera_showImg);
                            addView(mRootView.getChildCount(), mShowImage, showLp);
                            ZCameraLog.e("....Camera...show end...............resource: " + resource.getHeight() + System.currentTimeMillis());
                        }).setInterpolator(new AccelerateInterpolator()).start();
                return false;
            }
        }).into(mShowImage);
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
//            ZCameraLog.e("getSuitableSize, width:" + previewSize.width + ", height:" + previewSize.height);
            if (previewSize.width * mRatio.getWidthRatio() / mRatio.getHeightRatio() == previewSize.height) {
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
                    if (PermissionsUtil.hasPermission(getActivity(), Manifest.permission.CAMERA)) {
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

    GestureDetector mDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            ZCameraLog.e("onSingleTapUp, event");
             if(isSurfaceView(event)) {
                ZCameraLog.e("onSingleTapUp, isSurfaceView == true" );
                if (mFocusView == null) {
                    mFocusView = new FocusRectView(getActivity());
                    RelativeLayout.LayoutParams focusLp = new RelativeLayout.LayoutParams(screenP.x, screenP.y);
                    focusLp.addRule(RelativeLayout.CENTER_IN_PARENT);
                    addView(mRootView.getChildCount(), mFocusView, focusLp);
                }

                if (!mPresenter.isFocusing()) {
                    mFocusView.setVisibility(View.VISIBLE);
                    mFocusView.setTouchFoucusRect(event.getX(), event.getY());
                    mPresenter.focusArea(event.getX(), event.getY(), mFocusView);
                }
                return true;
            }

            return false;
        }
    });


    public boolean onTouch(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private boolean isSurfaceView(MotionEvent event) {
        return (mSurfaceView != null) ? (event.getY() > mSurfaceView.getTop() && event.getY() < mSurfaceView.getBottom()) : false;
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPresenter.startCamera(CameraAction.SURFACE_CREATE);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
