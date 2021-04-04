package org.zhx.common.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import org.zhx.common.util.ImageUtil;

public class RotationProcessor extends AsyncTask<Object, Object, byte[]> {
    private Context mContext;
    private byte[] data;
    private boolean isFrontCamera;
    private DataCallback mCallback;

    public RotationProcessor(Context mContext, byte[] data, boolean isFrontCamera, DataCallback mCallback) {
        this.mContext = mContext;
        this.data = data;
        this.isFrontCamera = isFrontCamera;
        this.mCallback = mCallback;
    }

    public AsyncTask setData(byte[] data) {
        this.data = data.clone();
        return this;
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        super.onPostExecute(bytes);
        if (mCallback != null) {
            Log.e("CameraPresenter", "....Camera...rotation_process_callback.......................");
            mCallback.onData(bytes);
        }
    }

    @Override
    protected byte[] doInBackground(Object... objects) {
        Log.e("CameraPresenter", "....Camera...rotation_process_start.......................");
        Bitmap bitmap = ImageUtil.getBitmap(mContext, data, true);
        Bitmap bm = bitmap;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Matrix matrix = new Matrix();
            matrix.setRotate(90, 0.1f, 0.1f);
            bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);
            if (isFrontCamera) {
                //前置摄像头旋转图片270度。
                matrix.setRotate(270);
                bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
            }
            ImageUtil.recycleBitmap(bitmap);
        }
        return ImageUtil.bitmap2Bytes(bm);
    }

    public interface DataCallback {
        public void onData(byte[] bitmapData);
    }
}
