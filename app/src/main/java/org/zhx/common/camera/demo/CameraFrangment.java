package org.zhx.common.camera.demo;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
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
import org.zhx.common.camera.renders.RenderCallback;
import org.zhx.common.camera.tasks.ImageSearchProcessor;
import org.zhx.common.camera.tasks.SensorProcessor;
import org.zhx.common.camera.widget.CameraGLSurfaceView;
import org.zhx.common.camera.widget.CustomGLSurfaceView;
import org.zhx.common.camera.widget.FocusRectView;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.PermissionsUtil;
import org.zhx.common.util.ZCameraLog;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraFrangment extends BaseFragment implements CameraModel.view<Camera>, View.OnClickListener, RenderCallback, SurfaceHolder.Callback {
    private String TAG = CameraFrangment.class.getSimpleName();
    public static final int SURFACEVIEW = 1;
    public static final int GL_SURFACEVIEW = 2;
    public static final String SURFACE_TYPE = "preview_type";
    private static final long SWITCH_DELAY = 25;
    private ImageView mShutterImg, mFlashImg,mShowImage;
    private ThumbImageView mThumImag;
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
    protected CameraRatio mRatio = CameraRatio.SCANLE_4_3;
    private int type = SURFACEVIEW;
    private SurfaceHolder mHolder;
    private float renderStartY;
    private float renderBottom;
    private float renderRight;
    private CameraProxy proxy;
    private long DURATION = 100;
    float topmargin = 0;
    int[] thumbLocation = new int[2];
    Rect showRect = new Rect();

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
        topmargin = getResources().getDimension(R.dimen.topbar_height);
        mShutterImg = view.findViewById(R.id.z_take_pictrue_img);
        mRootView = view.findViewById(R.id.camera_root_layout);

        if (SURFACEVIEW == type) {
            mSurfaceView = new SurfaceView(getActivity());
        } else {
            mSurfaceView = new CustomGLSurfaceView(getActivity());
            ((CustomGLSurfaceView) mSurfaceView).setCanvasTopmargin(topmargin);
        }

        mSurfaceView.setId(R.id.z_camera_preview);
        showLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        showLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mRootView.addView(mSurfaceView, showLp);
        view.findViewById(R.id.btn_switch_camera).setOnClickListener(this);
        mFlashImg = view.findViewById(R.id.btn_flash_mode);
        mThumImag = view.findViewById(R.id.z_thumil_img);
        mThumImag.setOnClickListener(this);
        mShutterImg.setOnClickListener(this);
        mFlashImg.setOnClickListener(this);
        initHolder();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mImageSearchProcessor) {
            mImageSearchProcessor.showImags(Constants.FILE_DIR);
        }
    }

    private void initHolder() {
        if (mSurfaceView instanceof CustomGLSurfaceView) {
            ((CustomGLSurfaceView) mSurfaceView).setViewRender(this);
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
        this.proxy = proxy;
        int previewHeight = 0;
        int previewWidth = screenP.x;

        if (proxy.getWidth() < proxy.getHeight()) {
            previewHeight = screenP.x * proxy.getHeight() / proxy.getWidth();
        } else {
            previewHeight = screenP.x * proxy.getWidth() / proxy.getHeight();
        }

        ZCameraLog.e(TAG,"onCameraCreate, previewWidth:" + previewWidth +", previewHeight:" + previewHeight);

        mPreviewPoint = new Point(previewWidth, previewHeight);
        RelativeLayout.LayoutParams lp= new RelativeLayout.LayoutParams(previewWidth,previewHeight);
        lp.topMargin = (int) topmargin;
        runOnUiThread(() -> mShowImage.setLayoutParams(lp));

        try {
            if (mSurfaceView instanceof CustomGLSurfaceView) {
                ((CustomGLSurfaceView) mSurfaceView).setRotation(getRotation(getCameraOrientation(proxy.getCameraId() != 0)), proxy.getCameraId() != 0);
                proxy.getCamera().setPreviewTexture(((CustomGLSurfaceView) mSurfaceView).getSurface());
            } else {
                proxy.getCamera().setPreviewDisplay(mHolder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                            ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), mThumImag, "image");
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
        Glide.with(getActivity()).asBitmap().error(new ColorDrawable(Color.BLACK)).override(mThumImag.getHeight()).load(uri).into(mThumImag);
    }

    @Override
    public void showThumImage(final Uri uri) {
        ZCameraLog.e("....showThumImage...............uri: "+ uri + System.currentTimeMillis() +",  "+ mThumImag.hashCode());
        mThumImag.getLocationOnScreen(thumbLocation);
        Glide.with(getActivity()).asBitmap().override(mThumImag.getHeight()).load(uri).addListener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                ZCameraLog.e("....showThumImage...............onLoadFailed "+ System.currentTimeMillis());
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                ZCameraLog.e("....showThumImage...............onResourceReady "+ System.currentTimeMillis());
                mShowImage.getGlobalVisibleRect(showRect);
                mShowImage.animate()
                        .translationXBy((thumbLocation[0] + mThumImag.getWidth()/2)- showRect.centerX())
                        .translationYBy((thumbLocation[1] + mThumImag.getHeight()/2) - showRect.centerY())
                        .scaleX(mThumImag.getWidth() / (mPreviewPoint.x * 1f))
                        .scaleY(mThumImag.getHeight() / (mPreviewPoint.y * 1f)).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
                            mThumImag.setVisibility(View.VISIBLE);
                            mThumImag.setImageBitmap(resource);
                            mShowImage.setTranslationX(0f);
                            mShowImage.setTranslationY(0f);
                            mShowImage.setScaleX(1f);
                            mShowImage.setScaleY(1f);
                            mShowImage.setImageBitmap(null);

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
        // camera的宽度是大于高度的，这里要保证expectWidth > expectHeight
        int expectWidth = Math.max(screenP.x, screenP.y);
        int expectHeight = Math.min(screenP.x, screenP.y);

        Camera.Size result = sizes.get(0);

        boolean hasSuitableSize = false;
        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size previewSize = sizes.get(i);
            // 找到一个与设置的分辨率差值最小的相机支持的分辨率大小
            if (previewSize.width * mRatio.getWidthRatio() / mRatio.getHeightRatio() == previewSize.height) {
                hasSuitableSize = true;
                int delta = Math.abs(screenP.x - previewSize.height);
                if (minDelta >= delta) {
                    minDelta = delta;
                    result = previewSize;
                }
            } else if (!hasSuitableSize) {
                if (previewSize.width == expectWidth) {
                    if (Math.abs(result.height - expectHeight)
                            > Math.abs(previewSize.height - expectHeight)) {
                        result = previewSize;
                    }
                } else if (previewSize.height == expectHeight) {
                    // 高度相等，则计算宽度最接近的Size
                    if (Math.abs(result.width - expectWidth)
                            > Math.abs(previewSize.width - expectWidth)) {
                        result = previewSize;
                    }
                }
            }
        }

        return result; // 默认返回与设置的分辨率最接近的预览尺寸
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ZCameraLog.e(TAG,"onRequestPermissionsResult, requestCode: " + requestCode);

        switch (requestCode) {
            case Constants.CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera(CameraAction.PERMISSITON_GRANTED);
                }
                break;
            case Constants.STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean hasCameraPermission = PermissionsUtil.hasPermission(getActivity(), Manifest.permission.CAMERA);
                    ZCameraLog.e(TAG,"onRequestPermissionsResult, hasCameraPermission :" + hasCameraPermission);

                    if (PermissionsUtil.hasPermission(getActivity(), Manifest.permission.CAMERA)) {
                        startCamera(CameraAction.PERMISSITON_GRANTED);
                    } else {
                        PermissionsUtil.requestPermission(this, Manifest.permission.CAMERA, Constants.CAMERA);
                    }
                } else {
                    ZCameraLog.e(TAG,"onRequestPermissionsResult, grantResults.length :" + grantResults.length
                            +", grantResults: " + grantResults);
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
            if (isSurfaceView(event)) {
                ZCameraLog.e("onSingleTapUp, isSurfaceView == true");
                if (mFocusView == null) {
                    mFocusView = new FocusRectView(getActivity());
                    RelativeLayout.LayoutParams focusLp = new RelativeLayout.LayoutParams(screenP.x, screenP.y);
                    focusLp.addRule(RelativeLayout.CENTER_IN_PARENT);
                    addView(mRootView.getChildCount(), mFocusView, focusLp);
                }

                if (!mPresenter.isFocusing()) {
                    mFocusView.setVisibility(View.VISIBLE);
                    mFocusView.setTouchFoucusRect(event.getX(), event.getY(), renderRight, renderBottom, renderStartY);
                    mPresenter.focusArea(event.getX(), event.getY(), mFocusView, proxy.getWidth(), proxy.getHeight());
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
        return event.getY() > renderStartY && event.getY() < (renderBottom + renderStartY);
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
    public void onCanvasReuslt(float start, float bottom, float right) {
        ZCameraLog.e("start:" + start + ", bottom:" + bottom + ", right:" + right);
        renderStartY = start;
        renderBottom = bottom;
        renderRight = right;
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

    @Override
    public void onPreviewFrame(byte[] data, int width, int height, boolean isFirstFrame) {
        ((CustomGLSurfaceView) mSurfaceView).onPreviewFrame(data, width, height, isFirstFrame);
    }
}
