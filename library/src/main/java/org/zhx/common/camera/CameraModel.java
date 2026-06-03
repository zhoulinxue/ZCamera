package org.zhx.common.camera;

import android.net.Uri;
import android.view.View;

import java.util.List;

public interface CameraModel {

    public interface presenter {
        void startCamera();

        void takePictrue();

        int chanageFlashMode();

        void focusArea(float x, float y, View focus);

        boolean isFocusing();

        void switchCamera();

        void release();
    }

    public interface view extends PictrueModel.view {
        void onTakeComplete();

        void showThumImage(Uri uri);
    }
}
