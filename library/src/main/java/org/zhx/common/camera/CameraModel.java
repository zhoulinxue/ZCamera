package org.zhx.common.camera;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import org.zhx.common.mvp.BaseView;

import java.io.IOException;

public interface CameraModel {

    public interface presenter {
        public void startCamera(CameraAction action);

        public void resumenCamera(CameraAction action);

        public void releaseCamera(CameraAction action);

        public void switchCamera();

        public void takePictrue();

        public int chanageFlashMode();

        public void focusArea(float x, float y, View focus);

        boolean isFocusing();

    }

    public interface view<T> extends PictrueModel.view {


        void onCameraCreate(CameraProxy<T> proxy) throws IOException;


        void onTakeComplete();

        int getDegree(boolean isFrontCamera);

    }
}
