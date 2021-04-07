package org.zhx.common.camera;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.List;

public interface CameraModel {

    public interface presenter {
        public void startCamera(CameraAction action);

        public void resumenCamera(CameraAction action);

        public void releaseCamera(CameraAction action);

        public void switchCamera();

        public void takePictrue();

        public int chanageFlashMode();

        public void focusArea(float x, float y,View focus);

        boolean isFocusing();

        void showImages();
    }

    public interface view<T> {

        AppCompatActivity getContext();

        public void onError(@StringRes int msg);

        public void onCameraCreate(CameraProxy<T> proxy) throws IOException;

        public void onPictrueCallback(byte[] data);

        void onSaveResult(Uri uri);

        void showLastImag(ImageData imageData);
    }
}
