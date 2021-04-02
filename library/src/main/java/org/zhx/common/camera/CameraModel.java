package org.zhx.common.camera;

import android.content.Context;

import androidx.annotation.StringRes;

import java.io.IOException;

public interface CameraModel {

    public interface presenter {
        public void startCamera(CameraAction action);

        public void resumenCamera(CameraAction action);

        public void releaseCamera(CameraAction action);

        public void takePictrue();
    }

    public interface view<T> {

        Context getConText();

        public void onError(@StringRes int msg);

        public void onCameraCreate(CameraProxy<T> proxy) throws IOException;

        public void onPictrueCallback(byte[] data,boolean isFrontCamera);

    }
}
