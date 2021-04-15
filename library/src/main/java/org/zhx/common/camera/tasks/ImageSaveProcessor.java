package org.zhx.common.camera.tasks;

import android.content.Context;
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
        private byte[] bitmap;
        private int orientation;

        public SaveImageTask(byte[] bitmap, int orientation) {
            this.bitmap = bitmap;
            this.orientation = orientation;
        }

        @Override
        protected Uri doInBackground(Object... objects) {
            ZCameraLog.e(TAG, "....Camera...save_process_start..............." + System.currentTimeMillis());
            Uri uri = null;
            try {
                uri = CameraUtil.saveImageData(mView.getContext(), bitmap, Constants.FILE_DIR);
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

    public void excute(byte[] data, int degree) {
        new SaveImageTask(data, degree).execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
