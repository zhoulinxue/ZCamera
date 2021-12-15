package org.zhx.common.camera;

import android.net.Uri;

import org.zhx.common.mvp.BaseView;

import java.io.IOException;
import java.util.List;

public interface PictrueModel {
    public interface view extends BaseView {
        void onSearchResult(List<ImageData> imageDatas);

        void onSaveResult(Uri uri);

        Uri saveDatas(int orientation, byte[] datas) throws IOException;

    }
}
