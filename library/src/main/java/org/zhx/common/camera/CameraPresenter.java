package org.zhx.common.camera;

import android.Manifest;
import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import org.zhx.common.util.DisplayUtil;
import org.zhx.common.util.PermissionsUtil;

import java.io.IOException;
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

    public CameraPresenter(CameraModel.view mView) {
        this.mView = mView;
        displayPx = DisplayUtil.getScreenMetrics(mView.getConText());
    }

    @Override
    public void startCamera(CameraAction action) {
        synchronized (mCameraLock) {
            if (CameraAction.SURFACE_CREATE == action) {
                isSurfaceDestory = false;
            }
            AppCompatActivity activity = (AppCompatActivity) mView.getConText();
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
        List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
        if (sizeList.size() > 0) {
            Camera.Size cameraSize = sizeList.get(0);
            for (Camera.Size size : sizeList) {
                if (size.width * size.height == displayPx.x * displayPx.y) {
                    cameraSize = size;
                    break;
                }
            }
            // 设置图片大小 为设备长宽
            parameters.setPictureSize(cameraSize.width, cameraSize.height);
        }

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
    public void takePictrue() {
        if (autoFocusManager != null) {
            autoFocusManager.start();
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.e(TAG, "....Camera...onAutoFocus......." + success);
        if (success) {
            if (previewSuc) {
                mCamera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        Log.e(TAG, "....Camera...takePicture.......................");
                        mView.onPictrueCallback(data, isFrontCamera);
                    }
                });
            } else {
                mView.onError(R.string.preview_error_string);
            }
        } else {
            mView.onError(R.string.focus_error);
        }
    }
}
