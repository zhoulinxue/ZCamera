package org.zhx.common.camera;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import org.zhx.common.mvp.BaseView;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ImageUtil;
import org.zhx.common.util.PermissionsUtil;
import org.zhx.common.util.ZCameraLog;

import java.io.IOException;
import java.io.InputStream;
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

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onSearchResult(List<ImageData> imageDatas) throws Exception {
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

    @RequiresApi(api = Build.VERSION_CODES.P)
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

    @Override
    public int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    @Override
    public int getRotation() {
        if (getActivity() == null) {
            return 0;
        }
        return getActivity().getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public Uri saveDatas(int orientation, byte[] datas, boolean isFrontCamera) throws IOException {
        Uri uri = CameraUtil.saveImageData(getActivity(), datas, Constants.FILE_DIR);
        ContentResolver provider = getActivity().getContentResolver();
        ParcelFileDescriptor descriptor = provider.openFileDescriptor(uri, "rw", null);
        ExifInterface exifInterface = new ExifInterface(descriptor.getFileDescriptor());
        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation + "");
        exifInterface.saveAttributes();
        ZCameraLog.e("saveDatas,uri:" + uri.toString() + "........." + System.currentTimeMillis());
        return uri;
    }

    @Override
    public byte[] flipDatas(byte[] datas) {
        return ImageUtil.flipFrontDatas(getActivity(), datas);
    }

    protected void runOnUiThread(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }
}
