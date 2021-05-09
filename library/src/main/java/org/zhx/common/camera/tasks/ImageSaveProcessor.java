package org.zhx.common.camera.tasks;

import android.net.Uri;
import android.os.AsyncTask;

import androidx.exifinterface.media.ExifInterface;

import org.zhx.common.camera.CameraModel;
import org.zhx.common.camera.Constants;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ZCameraLog;


public class ImageSaveProcessor {
    private String TAG = ImageSaveProcessor.class.getSimpleName();
    private CameraModel.view mView;

    public ImageSaveProcessor(CameraModel.view view) {
        this.mView = view;
    }

    private class SaveImageTask extends AsyncTask<Object, Object, Uri> {
        private byte[] datas;
        private int orientation;
        private boolean isFrontCamera;

        public SaveImageTask(byte[] bitmap, int orientation, boolean isFrontCamera) {
            this.datas = bitmap;
            this.orientation = orientation;
            this.isFrontCamera = isFrontCamera;
        }

        @Override
        protected Uri doInBackground(Object... objects) {
            ZCameraLog.e(TAG, "....Camera...save_process_start..............." + System.currentTimeMillis());
            Uri uri = null;
            try {
                if (isFrontCamera) {
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:

                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            orientation = ExifInterface.ORIENTATION_FLIP_HORIZONTAL;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:

                            break;
                    }
                }
                uri = CameraUtil.saveImageData(mView.getContext(), datas, Constants.FILE_DIR);
                ExifInterface exifInterface = new ExifInterface(mView.getContext().getContentResolver().openFileDescriptor(uri, "rw", null).getFileDescriptor());
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation + "");
                exifInterface.saveAttributes();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            mView.onSaveResult(uri);
            ZCameraLog.e(TAG, "....Camera....ImageSaveProcessor....result..." + System.currentTimeMillis());

        }
    }

    public void excute(byte[] data, int degree, boolean isFrontCamera) {
        new SaveImageTask(data, degree, isFrontCamera).execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
