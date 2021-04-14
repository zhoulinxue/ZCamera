package org.zhx.common.camera.tasks;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;

import org.zhx.common.util.ImageUtil;
import org.zhx.common.util.ZCameraLog;

public class RotationProcessor extends AsyncTask<Object, Object, Bitmap> {
    private Context mContext;
    private byte[] data;
    private boolean isFrontCamera;
    private DataCallback mCallback;
    private int degree;

    public RotationProcessor(Context mContext, int degree, byte[] data, boolean isFrontCamera, DataCallback mCallback) {
        this.mContext = mContext;
        this.data = data;
        this.isFrontCamera = isFrontCamera;
        this.mCallback = mCallback;
        this.degree = degree;
    }

    @Override
    protected void onPostExecute(Bitmap bytes) {
        super.onPostExecute(bytes);
        if (mCallback != null) {
            ZCameraLog.e("CameraPresenter", "....Camera...rotation_process_callback......................." + System.currentTimeMillis());
            mCallback.onData(bytes);
        }
    }

    @Override
    protected Bitmap doInBackground(Object... objects) {
        ZCameraLog.e("CameraPresenter", "....Camera...rotation_process_start......................." + System.currentTimeMillis());
        Bitmap bitmap = ImageUtil.getBitmap(mContext, data, false);
        Bitmap bm = bitmap;
        if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degree, 0.1f, 0.1f);
            bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);
        }
        return bm;
    }

    public interface DataCallback {
        public void onData(Bitmap bitmapData);
    }
}
