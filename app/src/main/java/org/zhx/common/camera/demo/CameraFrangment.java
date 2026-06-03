package org.zhx.common.camera.demo;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.zhx.common.camera.BaseFragment;
import org.zhx.common.camera.CameraModel;
import org.zhx.common.camera.CameraRatio;
import org.zhx.common.camera.CameraXPresenter;
import org.zhx.common.camera.Constants;
import org.zhx.common.camera.tasks.ImageSearchProcessor;
import org.zhx.common.camera.widget.FocusRectView;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.PermissionsUtil;
import org.zhx.common.util.ZCameraLog;

import java.util.ArrayList;

public class CameraFrangment extends BaseFragment implements CameraModel.view, View.OnClickListener {
    private ImageView mShutterImg, mFlashImg;
    private ThumbImageView mThumImag, mShowImage;
    private View animateHolder;
    private PreviewView mPreviewView;
    private CameraXPresenter mPresenter;
    private int[] modelResId = {
            R.drawable.ic_camera_top_bar_flash_auto_normal,
            R.drawable.ic_camera_top_bar_flash_on_normal,
            R.drawable.ic_camera_top_bar_flash_off_normal,
            R.drawable.ic_camera_top_bar_flash_torch_normal};
    private RelativeLayout.LayoutParams showLp;
    private RelativeLayout mRootView;
    Point screenP;
    FocusRectView mFocusView;
    private ImageSearchProcessor mImageSearchProcessor;
    protected CameraRatio mRatio = CameraRatio.SCANLE_16_9;
    private long DURATION = 90;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        screenP = CameraUtil.getScreenMetrics(getActivity());
        mImageSearchProcessor = new ImageSearchProcessor(getActivity(), this);
        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPreviewView = view.findViewById(R.id.preview_view);
        mShowImage = view.findViewById(R.id.z_base_camera_showImg);
        mShowImage.setDisableCircularTransformation(true);
        animateHolder = view.findViewById(R.id.animate_place_holder);
        mShutterImg = view.findViewById(R.id.z_take_pictrue_img);
        mRootView = view.findViewById(R.id.camera_root_layout);

        mPresenter = new CameraXPresenter(this);
        mPresenter.setPreviewView(mPreviewView);
        mPresenter.setLifecycleOwner(this);

        view.findViewById(R.id.btn_switch_camera).setOnClickListener(this);
        mFlashImg = view.findViewById(R.id.btn_flash_mode);
        mThumImag = view.findViewById(R.id.z_thumil_img);
        mThumImag.setOnClickListener(this);
        mShutterImg.setOnClickListener(this);
        mFlashImg.setOnClickListener(this);

        showLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        showLp.addRule(RelativeLayout.CENTER_IN_PARENT);

        // Edge-to-edge: pad bottom controls above the navigation bar
        View bottomLayout = view.findViewById(R.id.bottom_layout);
        ViewCompat.setOnApplyWindowInsetsListener(bottomLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        mImageSearchProcessor.showImags(Constants.FILE_DIR);
    }

    @Override
    public void onResume() {
        super.onResume();
        ZCameraLog.e("CameraFrangment onResume " + System.currentTimeMillis());
        if (PermissionsUtil.hasPermission(getActivity(), Manifest.permission.CAMERA)) {
            mPresenter.startCamera();
        } else {
            PermissionsUtil.requestPermission(this, Manifest.permission.CAMERA, Constants.CAMERA);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ZCameraLog.e("CameraFrangment onPause " + System.currentTimeMillis());
        mPresenter.release();
    }

    @Override
    public void onError(int msg) {
        super.onError(msg);
        mShutterImg.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.z_take_pictrue_img) {
            mShutterImg.setEnabled(false);
            mPresenter.takePictrue();
        } else if (id == R.id.btn_switch_camera) {
            mPresenter.switchCamera();
        } else if (id == R.id.btn_flash_mode) {
            int position = mPresenter.chanageFlashMode();
            mFlashImg.setImageResource(modelResId[position]);
        } else if (id == R.id.z_thumil_img) {
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
        ZCameraLog.e("....onEmptyFile..............." + System.currentTimeMillis());
        showImageData(null);
    }

    public void showImageData(Uri uri) {
        Glide.with(getActivity()).asBitmap().error(new ColorDrawable(Color.BLACK)).override(mThumImag.getHeight()).load(uri).into(mThumImag);
    }

    @Override
    public void showThumImage(final Uri uri) {
        ZCameraLog.e("....showThumImage...............uri: " + uri + System.currentTimeMillis() + ",  " + mThumImag.hashCode());
        Glide.with(getActivity()).asBitmap().override(mThumImag.getHeight()).load(uri).addListener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                ZCameraLog.e("....showThumImage...............onLoadFailed " + System.currentTimeMillis());
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                ZCameraLog.e("....showThumImage...............onResourceReady " + System.currentTimeMillis());
                mShowImage.animate()
                        .translationX(CameraUtil.dip2px(getActivity(), 47) - (screenP.x + mThumImag.getWidth() / 2) / 2)
                        .translationY(screenP.y / 2 - CameraUtil.dip2px(getActivity(), 55))
                        .scaleX(0.01f)
                        .scaleY(0.01f).setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                long value = animation.getCurrentPlayTime();
                                float process = value * 1f / DURATION;
                                float ratio = 1 - mThumImag.getWidth() * 2f / screenP.x;

                                if (process > ratio) {
                                    mThumImag.setVisibility(View.VISIBLE);
                                }

                                ZCameraLog.e("....Camera...showing update...............process: " + process + ", ratio: " + ratio);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPresenter.startCamera();
            }
        }
    }

    GestureDetector mDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            ZCameraLog.e("onSingleTapUp, event");
            if (isPreviewViewArea(event)) {
                ZCameraLog.e("onSingleTapUp, isPreviewViewArea == true");
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

    private boolean isPreviewViewArea(MotionEvent event) {
        return (mPreviewView != null)
                ? (event.getY() > mPreviewView.getTop() && event.getY() < mPreviewView.getBottom())
                : false;
    }

    private void addView(int childCount, View view, RelativeLayout.LayoutParams layoutParams) {
        mRootView.addView(view, childCount, layoutParams);
    }
}
