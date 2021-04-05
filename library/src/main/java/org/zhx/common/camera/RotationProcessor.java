package org.zhx.common.camera;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import org.zhx.common.util.ImageUtil;

public class RotationProcessor {
    private Context mContext;


    private DataCallback mCallback;

    public RotationProcessor(Context mContext, DataCallback mCallback) {
        this.mContext = mContext;
        this.mCallback = mCallback;
    }


    private class RotationTask extends AsyncTask<Object, Object, byte[]> {
        private byte[] data;
        private boolean isFrontCamera;

        public RotationTask(byte[] data, boolean isFrontCamera) {
            this.data = data;
            this.isFrontCamera = isFrontCamera;
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
            Bitmap bm = null;
            byte[] datas = null;
            try {
                Bitmap bitmap = ImageUtil.getBitmap(mContext, data, false);
                bm = bitmap;
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
                datas = ImageUtil.bitmap2Bytes(bm);
            } catch (Exception e) {
                Log.e("CameraPresenter", "....Camera...rotation_process_error...." + e.getMessage());
            } finally {
                ImageUtil.recycleBitmap(bm);
            }
            return datas;
        }
    }

    public void excute(byte[] data, boolean isFrontCamera) {
        new RotationTask(data, isFrontCamera).execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public interface DataCallback {
        public void onData(byte[] bitmapData);
    }
}
