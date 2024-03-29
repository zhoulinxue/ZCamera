package org.zhx.common.camera;

import android.graphics.ImageDecoder;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import org.zhx.common.mvp.BaseView;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ImageUtil;
import org.zhx.common.util.PermissionsUtil;
import org.zhx.common.util.ZCameraLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity implements BaseView, PictrueModel.view {
    protected List<ImageData> mImageDatas;

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
            showImageData(imageDatas.get(0).getContentUri(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void showImageData(Uri contentUri, boolean b) throws IOException;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onSaveResult(ImageData data) {
        if (mImageDatas == null) {
            mImageDatas = new ArrayList<>();
        }
        mImageDatas.add(0, data);
        try {
            showImageData(data.getContentUri(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return PermissionsUtil.hasPermission(this, permission);
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
        return getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    public Uri saveDatas(int orientation, byte[] datas, boolean isFrontCamera) throws IOException {
        byte[] finaldata = datas;

        if (isFrontCamera) {
            finaldata = ImageUtil.flipFrontDatas(this, datas);
        }

        Uri uri = CameraUtil.saveImageData(this, finaldata, Constants.FILE_DIR);
        ExifInterface exifInterface = new ExifInterface(getContentResolver().openFileDescriptor(uri, "rw", null).getFileDescriptor());
        exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation + "");
        exifInterface.saveAttributes();
        ZCameraLog.e("saveDatas,uri:" + uri.toString());
        return uri;
    }

}
