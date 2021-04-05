package org.zhx.common.camera;

import android.Manifest;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import org.zhx.common.util.DisplayUtil;
import org.zhx.common.util.ImageUtil;
import org.zhx.common.util.PermissionsUtil;

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
    private Point displayPx;
    private AutoFocusManager autoFocusManager;
    private boolean isSurfaceDestory = false;
    private boolean isFocus = false;
    private RotationProcessor mRprocessor;
    private View mFocusView;

    public CameraPresenter(CameraModel.view mView) {
        this.mView = mView;
        displayPx = DisplayUtil.getScreenMetrics(mView.getContext());
    }

    @Override
    public void startCamera(CameraAction action) {
        synchronized (mCameraLock) {
            if (CameraAction.SURFACE_CREATE == action) {
                isSurfaceDestory = false;
            }
            AppCompatActivity activity = (AppCompatActivity) mView.getContext();
            if (PermissionsUtil.hasPermission(activity, Manifest.permission.CAMERA)) {
                Log.e(TAG, action + "....Camera....start.......................");
                if (openCamera()) {
                    boolean setCamera = setCamera();
                    if (setCamera) {
                        if (autoFocusManager == null) {
                            autoFocusManager = new AutoFocusManager(mCamera, this);
                        }
                        mCamera.startPreview();
                        Log.e(TAG, action + "....Camera....preview.......................");
                    }
                }
            } else {
                PermissionsUtil.requestPermission(activity, Manifest.permission.CAMERA, Constants.CAMERA);
            }
        }
    }


    private boolean openCamera() {
        if (!isFrontCamera) {
            mCamera = Camera.open();
        } else {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                {
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCamera = Camera.open(i);
                        isFrontCamera = true;
                    }
                }
            }
        }
        Log.e(TAG, "open....." + (mCamera != null));
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
            if (mView != null) {
                mProxy = new CameraProxy<>(mCamera);
                mView.onCameraCreate(mProxy);
            }
        } catch (IOException e) {
            e.printStackTrace();
            previewSuc = false;
            Log.e(TAG, "set.....eception");
            return false;
        }
        Log.e(TAG, "set.....suc");
        return true;
    }

    private void setParamiters() {
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
    }

    /**
     * 旋转相机和设置预览大小
     *
     * @param parameters
     */
    public void setCameraSize(Camera.Parameters parameters) {

        //摄像头画面显示在Surface上
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        int[] a = new int[sizes.size()];
        int[] b = new int[sizes.size()];
        for (int i = 0; i < sizes.size(); i++) {
            int supportH = sizes.get(i).height;
            int supportW = sizes.get(i).width;
            a[i] = Math.abs(supportW - displayPx.x);
            b[i] = Math.abs(supportH - displayPx.y);
            Log.d(TAG, "supportW:" + supportW + "supportH:" + supportH);
        }
        int minW = 0, minA = a[0];
        for (int i = 0; i < a.length; i++) {
            if (a[i] <= minA) {
                minW = i;
                minA = a[i];
            }
        }
        int minH = 0, minB = b[0];
        for (int i = 0; i < b.length; i++) {
            if (b[i] < minB) {
                minH = i;
                minB = b[i];
            }
        }
        Log.d(TAG, "result=" + sizes.get(minW).width + "x" + sizes.get(minH).height);
        parameters.setPreviewSize(sizes.get(minW).width, sizes.get(minH).height); // 设置预览图像大小
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
            Log.e(TAG, "....release....");
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
                Log.e(TAG, action + "....Camera...end.......................");
            }
        }
    }

    @Override
    public void switchCamera() {
        isFrontCamera = !isFrontCamera;
        releaseCamera(CameraAction.SWITCH_CAMERA);
        startCamera(CameraAction.SWITCH_CAMERA);
    }

    @Override
    public void takePictrue() {
        if (isFocus) {
            if (previewSuc) {
                if (mFocusView != null) {
                    mFocusView.setVisibility(View.GONE);
                }
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Log.e(TAG, "....Camera...takePicture.......................");
                        if (mRprocessor == null) {
                            mRprocessor = new RotationProcessor(mView.getContext(), data, isFrontCamera, new RotationProcessor.DataCallback() {
                                @Override
                                public void onData(byte[] bitmapData) {
                                    mRprocessor = null;
                                    mView.onPictrueCallback(bitmapData);
                                }
                            });
                            mRprocessor.execute(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                        mCamera.startPreview();
                    }
                });
            } else {
                mView.onError(R.string.preview_error_string);
            }
        } else {
            mView.onError(R.string.focus_error);
        }
    }

    @Override
    public int chanageFlashMode() {
        Camera.Parameters parameters = mCamera.getParameters();
        List<String> flashmodels = parameters.getSupportedFlashModes();
        if (flashmodels.contains(flashMedols[modelIndex])) {
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
        if (!previewSuc || mCamera == null || autoFocusManager == null) {
            return;
        }
        this.mFocusView = focusView;
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Area> areas = new ArrayList<Camera.Area>();
        List<Camera.Area> areasMetrix = new ArrayList<Camera.Area>();
        Rect focusRect = ImageUtil.calculateTapArea(mView.getContext(), x, y, 1.0f);
        Rect metrixRect = ImageUtil.calculateTapArea(mView.getContext(), x, y, 1.5f);
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
        Log.e(TAG, "....Camera...onAutoFocus......." + success);
        isFocus = success;
        if (mFocusView != null) {
            mFocusView.setVisibility(View.GONE);
        }
    }
}
