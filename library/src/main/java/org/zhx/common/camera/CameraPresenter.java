package org.zhx.common.camera;

import android.Manifest;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import org.zhx.common.camera.tasks.ImageSaveProcessor;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ZCameraLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraPresenter implements CameraModel.presenter, Camera.AutoFocusCallback, LifecycleObserver {
    private String TAG = CameraPresenter.class.getSimpleName();
    private Camera mCamera;
    private CameraModel.view mView;
    private CameraProxy<Camera> mProxy;
    private boolean isFrontCamera = false;
    private Object mCameraLock = new Object();
    private boolean previewSuc = false;
    private String[] flashMedols = {Camera.Parameters.FLASH_MODE_AUTO, Camera.Parameters.FLASH_MODE_ON, Camera.Parameters.FLASH_MODE_OFF, Camera.Parameters.FLASH_MODE_TORCH};
    private int modelIndex = 0;
    private AutoFocusManager autoFocusManager;
    private boolean isSurfaceDestory = false;
    private boolean isFocus = false;
    private View mFocusView;
    private int mPreviewWidth = 1920;
    private int mPreviewHeight = 1080;
    private float mPreviewScale = mPreviewHeight * 1f / mPreviewWidth;
    private ImageSaveProcessor mImageSaveProcessor;
    private boolean hasAutoFocus = true;
    private int mCameraId;


    public CameraPresenter(final CameraModel.view view) {
        this.mView = view;
        mImageSaveProcessor = new ImageSaveProcessor(view);
    }

    @Override
    public void startCamera(CameraAction action) {
        synchronized (mCameraLock) {
            if (CameraAction.SURFACE_CREATE == action) {
                isSurfaceDestory = false;
            }
            if (mView.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ZCameraLog.e(TAG, action + "....Camera....start.......................");
                if (openCamera()) {
                    boolean setCamera = setCamera();
                    if (setCamera) {
                        if (autoFocusManager == null) {
                            autoFocusManager = new AutoFocusManager(mCamera, this);
                        }
                        ZCameraLog.e(TAG, action + "....Camera....preview.......................");
                    }
                } else {
                    mView.onError(R.string.open_error);
                }
            } else {
                mView.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Constants.STORAGE);
            }
        }
    }


    private boolean openCamera() {
        try {

            if (!isFrontCamera) {
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

                mCamera = Camera.open();

            } else {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        mCamera = Camera.open(i);
                        isFrontCamera = true;
                    }
                }
            }
            ZCameraLog.e(TAG, "open....." + (mCamera != null));
        } catch (Exception e) {
            return false;
        }
        return mCamera != null;
    }

    private boolean setCamera() {
        try {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    previewSuc = true;
                }
            });
            setParamiters();

            setOrientation();

            List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
            hasAutoFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO);
        } catch (IOException e) {
            e.printStackTrace();
            previewSuc = false;
            ZCameraLog.e(TAG, "set.....eception");
            return false;
        }
        ZCameraLog.e(TAG, "set.....suc");
        return true;
    }

    private void setOrientation() {
        if (mView.getOrientation() != Configuration.ORIENTATION_LANDSCAPE) {
            mCamera.setDisplayOrientation(90);
        } else {
            mCamera.setDisplayOrientation(0);
        }
    }

    private void setParamiters() throws IOException {
        Camera.Parameters parameters = mCamera.getParameters();
        // 设置闪光灯为自动 前置摄像头时 不能设置
        if (!isFrontCamera) {
            parameters.setFlashMode(flashMedols[modelIndex]);
        }
        setCameraSize(parameters);
        // 设置图片格式
        parameters.setPictureFormat(ImageFormat.JPEG);
        // 设置JPG照片的质量
        parameters.set("jpeg-quality", 100);
        mCamera.startPreview();
    }

    /**
     * 旋转相机和设置预览大小
     *
     * @param parameters 相机 属性
     */
    public void setCameraSize(Camera.Parameters parameters) throws IOException {
        //摄像头画面显示在Surface上
        List<Camera.Size> vSizes = parameters.getSupportedPreviewSizes();

        List<Camera.Size> pSizes = parameters.getSupportedPictureSizes();
        Camera.Size previewSize = getSuitableSize(vSizes);
        Camera.Size pictureSize = getSuitableSize(pSizes);
        ZCameraLog.e(TAG, "SupportedPreviewSize, width: " + mPreviewWidth + ", height: " + mPreviewHeight);

        parameters.setPreviewSize(previewSize.width, previewSize.height); // 设置预览图像大小
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        mCamera.setParameters(parameters);
        if (mView != null) {
            mProxy = new CameraProxy<>(mCamera, mPreviewWidth, mPreviewHeight, mCameraId);
            mView.onCameraCreate(mProxy);
        }
    }

    private Camera.Size getSuitableSize(List<Camera.Size> sizes) {
        int minDelta = Integer.MAX_VALUE; // 最小的差值，初始值应该设置大点保证之后的计算中会被重置
        int index = 0; // 最小的差值对应的索引坐标
        for (int i = 0; i < sizes.size(); i++) {
            Camera.Size previewSize = sizes.get(i);
            // 找到一个与设置的分辨率差值最小的相机支持的分辨率大小
            if (previewSize.width * mPreviewScale == previewSize.height) {
                int delta = Math.abs(mPreviewWidth - previewSize.width);
                if (delta == 0) {
                    return previewSize;
                }
                if (minDelta > delta) {
                    minDelta = delta;
                    index = i;
                }
            }
        }
        return sizes.get(index); // 默认返回与设置的分辨率最接近的预览尺寸
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        resumenCamera(CameraAction.ON_RESUME);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        releaseCamera(CameraAction.ON_STOP);
    }

    @Override
    public void resumenCamera(CameraAction action) {
        if (!isSurfaceDestory) {
            startCamera(action);
        }
    }

    @Override
    public void releaseCamera(CameraAction action) {
        synchronized (mCameraLock) {
            ZCameraLog.e(TAG, "....release....");
            isSurfaceDestory = CameraAction.SURFACE_CREATE == action;
            if (previewSuc) {
                if (autoFocusManager != null) {
                    autoFocusManager.stop();
                    autoFocusManager = null;
                }
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.setPreviewCallback(null);
                    mCamera.release();
                    mCamera = null;
                }
                previewSuc = false;
                ZCameraLog.e(TAG, action + "....Camera...end.......................");
            }
        }
    }

    @Override
    public void switchCamera() {
        isFrontCamera = !isFrontCamera;
        releaseCamera(CameraAction.SWITCH_CAMERA);
        startCamera(CameraAction.SWITCH_CAMERA);
    }

    private void takeRequest() {
        final int degree = mView.getDegree(isFrontCamera);
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                ZCameraLog.e(TAG, "....Camera...takePicture......................." + System.currentTimeMillis());
                mView.onTakeComplete();
                mImageSaveProcessor.excute(data, degree, isFrontCamera);
                mCamera.startPreview();
            }
        });

    }


    private boolean canTake() {
        return previewSuc;
    }

    @Override
    public void takePictrue() {
        if (mFocusView != null) {
            mFocusView.setVisibility(View.GONE);
        }
        if (canTake()) {
            takeRequest();
        } else {
            mView.onError(R.string.preview_error_string);
        }
    }

    @Override
    public int chanageFlashMode() {
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashmodels = parameters.getSupportedFlashModes();
        if (!isFrontCamera && flashmodels.contains(flashMedols[modelIndex])) {
            modelIndex++;
            if (modelIndex >= flashMedols.length) {
                modelIndex = 0;
            }
            parameters.setFlashMode(flashMedols[modelIndex]);
            mCamera.setParameters(parameters);
        }
        return modelIndex;
    }

    @Override
    public void focusArea(float x, float y, View focusView) {
        if (!previewSuc || mCamera == null || autoFocusManager == null || !hasAutoFocus) {
            isFocus = true;
            ZCameraLog.e(TAG, "...hasAutoFocus." + hasAutoFocus);
            if (focusView != null) {
                focusView.setVisibility(View.GONE);
            }
            return;
        }
        this.mFocusView = focusView;
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Area> areas = new ArrayList<Camera.Area>();
        List<Camera.Area> areasMetrix = new ArrayList<Camera.Area>();
        Rect focusRect = CameraUtil.calculateTapArea(focusView.getContext(), x, y, 1.0f);
        Rect metrixRect = CameraUtil.calculateTapArea(focusView.getContext(), x, y, 1.5f);
        areas.add(new Camera.Area(focusRect, 1000));
        areasMetrix.add(new Camera.Area(metrixRect, 1000));
        parameters.setMeteringAreas(areasMetrix);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setFocusAreas(areas);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        mCamera.autoFocus(autoFocusManager);
    }

    @Override
    public boolean isFocusing() {
        return autoFocusManager != null && autoFocusManager.isFocusing();
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        ZCameraLog.e(TAG, "....Camera...onAutoFocus......." + success);
        isFocus = success;
        if (mFocusView != null) {
            mFocusView.setVisibility(View.GONE);
        }
    }


}
