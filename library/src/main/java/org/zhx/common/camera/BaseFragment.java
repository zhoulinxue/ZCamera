package org.zhx.common.camera;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.zhx.common.mvp.BaseView;
import org.zhx.common.util.PermissionsUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseFragment extends Fragment implements BaseView, PictrueModel.view {
    protected List<ImageData> mImageDatas;
    protected Handler mHandler;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mHandler = new Handler();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onError(final int msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSearchResult(List<ImageData> imageDatas) {
        mImageDatas = imageDatas;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    showImageData(mImageDatas.get(0).getContentUri());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public abstract void showImageData(Uri uri) throws IOException;

    @Override
    public void onSaveResult(final ImageData data) {
        if (mImageDatas == null) {
            mImageDatas = new ArrayList<>();
        }
        mImageDatas.add(0, data);
    }

    @Override
    public boolean hasPermission(String permission) {
        return PermissionsUtil.hasPermission(getActivity(), permission);
    }

    @Override
    public void requestPermission(String permission, int requestCode) {
        PermissionsUtil.requestPermission(this, permission, requestCode);
    }

    protected void runOnUiThread(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }
}
