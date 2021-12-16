package org.zhx.common.camera;

import android.graphics.Bitmap;
import android.net.Uri;

import org.zhx.common.mvp.BaseView;

import java.io.IOException;
import java.util.List;

public interface ImageLoaderModel {
    public interface view {
        void onBitmapLoadSuc(Bitmap bitmap, int position);
    }
}
