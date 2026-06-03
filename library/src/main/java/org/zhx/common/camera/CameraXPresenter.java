package org.zhx.common.camera;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.zhx.common.util.ZCameraLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraXPresenter implements CameraModel.presenter {
    private static final String TAG = CameraXPresenter.class.getSimpleName();
    private static final int FLASH_AUTO = 0;
    private static final int FLASH_ON = 1;
    private static final int FLASH_OFF = 2;
    private static final int FLASH_TORCH = 3;

    private CameraModel.view mView;
    private PreviewView mPreviewView;
    private LifecycleOwner mLifecycleOwner;
    private ProcessCameraProvider mCameraProvider;
    private ImageCapture mImageCapture;
    private Preview mPreview;
    private CameraControl mCameraControl;
    private Camera mCamera;
    private ExecutorService mExecutor;
    private Handler mMainHandler;
    private Context mContext;

    private int mLensFacing = CameraSelector.LENS_FACING_BACK;
    private int mFlashModeIndex = 0;
    private boolean isFrontCamera = false;
    private boolean isCameraReady = false;
    private View mFocusView;

    public CameraXPresenter(CameraModel.view view) {
        this.mView = view;
        mExecutor = Executors.newSingleThreadExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void setPreviewView(PreviewView previewView) {
        this.mPreviewView = previewView;
        if (previewView != null) {
            this.mContext = previewView.getContext();
        }
    }

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.mLifecycleOwner = lifecycleOwner;
    }

    @Override
    public void startCamera() {
        if (mPreviewView == null || mLifecycleOwner == null) {
            ZCameraLog.e(TAG, "startCamera: PreviewView or LifecycleOwner not set");
            return;
        }

        if (mContext == null) {
            mContext = mPreviewView.getContext();
        }

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(mContext);
        future.addListener(() -> {
            try {
                mCameraProvider = future.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                mMainHandler.post(() -> mView.onError(R.string.open_error));
            }
        }, ContextCompat.getMainExecutor(mContext));
    }

    private void bindCameraUseCases() {
        if (mCameraProvider == null) return;

        mCameraProvider.unbindAll();

        // Target aspect ratio — prefer 16:9, fall back to 4:3
        int aspectRatio = getTargetAspectRatio();

        // Preview use case
        mPreview = new Preview.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build();
        mPreview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        // Image capture use case
        ImageCapture.Builder imageCaptureBuilder = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetAspectRatio(aspectRatio);
        mImageCapture = imageCaptureBuilder.build();

        // Camera selector
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(mLensFacing)
                .build();

        // Bind to lifecycle
        try {
            mCamera = mCameraProvider.bindToLifecycle(
                    mLifecycleOwner, cameraSelector, mPreview, mImageCapture);
            mCameraControl = mCamera.getCameraControl();
            isFrontCamera = (mLensFacing == CameraSelector.LENS_FACING_FRONT);
            isCameraReady = true;

            // Re-apply flash mode after camera rebind
            applyFlashMode();

            ZCameraLog.e(TAG, "Camera bound: lensFacing=" + mLensFacing);
        } catch (Exception e) {
            e.printStackTrace();
            mMainHandler.post(() -> mView.onError(R.string.open_error));
        }
    }

    private void applyFlashMode() {
        if (mImageCapture == null || mCameraControl == null) return;
        switch (mFlashModeIndex) {
            case FLASH_AUTO:
                mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_AUTO);
                break;
            case FLASH_ON:
                mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
                break;
            case FLASH_OFF:
                mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                break;
            case FLASH_TORCH:
                mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                boolean hasFlash = mCamera.getCameraInfo().hasFlashUnit();
                if (hasFlash) {
                    mCameraControl.enableTorch(true);
                }
                break;
        }
    }

    private int getTargetAspectRatio() {
        // Default to 16:9 for modern devices
        return androidx.camera.core.AspectRatio.RATIO_16_9;
    }

    @Override
    public void takePictrue() {
        if (!isCameraReady || mImageCapture == null) return;

        String fileName = Constants.FILE_DIR + System.currentTimeMillis() + ".jpg";
        File photoDir = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DCIM), Constants.FILE_DIR);
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }
        File photoFile = new File(photoDir, fileName);

        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        metadata.setReversedHorizontal(isFrontCamera);

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile)
                        .setMetadata(metadata)
                        .build();

        mImageCapture.takePicture(options, mExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults results) {
                        Uri savedUri = insertToMediaStore(photoFile);
                        ZCameraLog.e(TAG, "Photo saved: " + savedUri);
                        final Uri uri = savedUri;
                        mMainHandler.post(() -> {
                            ImageData imageData = new ImageData(uri);
                            mView.onSaveResult(imageData);
                            mView.onTakeComplete();
                            mView.showThumImage(uri);
                        });
                    }

                    @Override
                    public void onError(ImageCaptureException exception) {
                        ZCameraLog.e(TAG, "Photo capture failed: " + exception.getMessage());
                        mMainHandler.post(() -> mView.onError(R.string.preview_error_string));
                    }
                });
    }

    private Uri insertToMediaStore(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/" + Constants.FILE_DIR);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        }

        Uri uri = mContext.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            try (OutputStream out = mContext.getContentResolver().openOutputStream(uri);
                 FileInputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    mContext.getContentResolver().update(uri, values, null, null);
                }
                return uri;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Uri.fromFile(file);
    }

    @Override
    public int chanageFlashMode() {
        if (isFrontCamera) {
            ZCameraLog.e(TAG, "Cannot change flash mode on front camera");
            return mFlashModeIndex;
        }

        mFlashModeIndex = (mFlashModeIndex + 1) % 4;
        if (mImageCapture == null) return mFlashModeIndex;

        switch (mFlashModeIndex) {
            case FLASH_AUTO:
                mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_AUTO);
                if (mCameraControl != null) mCameraControl.enableTorch(false);
                ZCameraLog.e(TAG, "Flash mode: AUTO");
                break;
            case FLASH_ON:
                mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_ON);
                if (mCameraControl != null) mCameraControl.enableTorch(false);
                ZCameraLog.e(TAG, "Flash mode: ON");
                break;
            case FLASH_OFF:
                mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                if (mCameraControl != null) mCameraControl.enableTorch(false);
                ZCameraLog.e(TAG, "Flash mode: OFF");
                break;
            case FLASH_TORCH:
                mImageCapture.setFlashMode(ImageCapture.FLASH_MODE_OFF);
                if (mCameraControl != null) {
                    boolean hasFlash = mCamera.getCameraInfo().hasFlashUnit();
                    if (hasFlash) {
                        mCameraControl.enableTorch(true);
                        ZCameraLog.e(TAG, "Torch ON");
                    } else {
                        ZCameraLog.e(TAG, "No flash unit available");
                    }
                }
                break;
        }
        return mFlashModeIndex;
    }

    @Override
    public void focusArea(float x, float y, View focusView) {
        this.mFocusView = focusView;
        if (mPreviewView == null || mCameraControl == null) return;

        MeteringPointFactory factory = mPreviewView.getMeteringPointFactory();
        MeteringPoint point = factory.createPoint(x, y);
        FocusMeteringAction action = new FocusMeteringAction.Builder(
                point, FocusMeteringAction.FLAG_AF | FocusMeteringAction.FLAG_AE)
                .build();

        ListenableFuture<FocusMeteringResult> future =
                mCameraControl.startFocusAndMetering(action);
        future.addListener(() -> {
            if (mFocusView != null) {
                mMainHandler.post(() -> mFocusView.setVisibility(View.GONE));
            }
        }, ContextCompat.getMainExecutor(mContext));
    }

    @Override
    public boolean isFocusing() {
        // CameraX handles focus internally—return false, the UI handles the rest
        return false;
    }

    @Override
    public void switchCamera() {
        mLensFacing = (mLensFacing == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;
        isCameraReady = false;
        bindCameraUseCases();
    }

    @Override
    public void release() {
        isCameraReady = false;
        if (mCameraProvider != null) {
            mCameraProvider.unbindAll();
            mCameraProvider = null;
        }
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdown();
        }
        mCameraControl = null;
        mCamera = null;
        mImageCapture = null;
        mPreview = null;
    }
}
