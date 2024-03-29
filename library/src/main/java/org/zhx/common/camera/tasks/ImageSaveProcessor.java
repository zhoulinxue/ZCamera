package org.zhx.common.camera.tasks;

import android.net.Uri;
import android.os.AsyncTask;

import androidx.exifinterface.media.ExifInterface;

import org.zhx.common.camera.CameraModel;
import org.zhx.common.camera.Constants;
import org.zhx.common.camera.ImageData;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ZCameraLog;


public class ImageSaveProcessor {
    private String TAG = ImageSaveProcessor.class.getSimpleName();
    private CameraModel.view mView;

    public ImageSaveProcessor(CameraModel.view view) {
        this.mView = view;
    }

    private class SaveImageTask extends AsyncTask<Object, Object, ImageData> {
        private byte[] datas;
        private int orientation;
        private boolean isFrontCamera;

        public SaveImageTask(byte[] datas, int orientation, boolean isFrontCamera) {
            this.datas = datas;
            this.orientation = orientation;
            this.isFrontCamera = isFrontCamera;
        }

        @Override
        protected ImageData doInBackground(Object... objects) {
            ZCameraLog.d(TAG, "....Camera...save_process_start...............");
            Uri uri = null;
            try {
                if (isFrontCamera) {
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            orientation = ExifInterface.ORIENTATION_FLIP_HORIZONTAL;
                            break;
                    }
                }
                uri = mView.saveDatas(orientation, datas, isFrontCamera);
            } catch (Exception e) {
                e.printStackTrace();
            }
            ImageData data = new ImageData(uri, datas);
            mView.onSaveResult(data);
            return data;
        }

        @Override
        protected void onPostExecute(ImageData data) {
            ZCameraLog.d(TAG, "....Camera....ImageSaveProcessor....result...");
            mView.showThumImage(data.getContentUri());
        }
    }

    public void excute(byte[] data, int degree, boolean isFrontCamera) {
        new SaveImageTask(data, degree, isFrontCamera).execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
