package org.zhx.common.camera;

import android.hardware.Camera;
import android.net.Uri;

import org.zhx.common.mvp.BaseView;

import java.io.IOException;
import java.util.List;

public interface PictrueModel {
    public interface view extends BaseView {
        void onSearchResult(List<ImageData> imageDatas);

        void onSaveResult(ImageData data) ;

        Uri saveDatas(int orientation, byte[] datas,boolean isFrontCamera) throws IOException;

        void onEmptyFile();

        byte[] flipDatas(byte[] datas);
    }
}
