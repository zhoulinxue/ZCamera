package org.zhx.common.camera.demo;

import android.net.Uri;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.zhx.common.camera.CameraModel;
import org.zhx.common.camera.ImageData;
import org.zhx.common.camera.PictrueModel;
import org.zhx.common.mvp.BaseView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements BaseView, PictrueModel.view {
    protected List<ImageData> mImageDatas;

    @Override
    public AppCompatActivity getContext() {
        return this;
    }

    @Override
    public void onError(final int msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSearchResult(List<ImageData> imageDatas) {
        mImageDatas = imageDatas;
        try {
            setImageData(imageDatas.get(0).getContentUri(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void setImageData(Uri contentUri, boolean b) throws IOException;

    @Override
    public void onSaveResult(Uri uri) {
        if (mImageDatas == null) {
            mImageDatas = new ArrayList<>();
        }
        mImageDatas.add(0, new ImageData(uri));
        try {
            setImageData(uri, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
