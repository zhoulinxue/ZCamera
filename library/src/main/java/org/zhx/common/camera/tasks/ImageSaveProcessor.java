package org.zhx.common.camera.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.exifinterface.media.ExifInterface;

import org.zhx.common.camera.Constants;
import org.zhx.common.util.CameraUtil;
import org.zhx.common.util.ImageUtil;
import org.zhx.common.util.ZCameraLog;

import java.io.IOException;

public class ImageSaveProcessor {
    private Context mContext;
    private UriResult mReuslt;

    public ImageSaveProcessor(Context mContext, UriResult mReuslt) {
        this.mContext = mContext;
        this.mReuslt = mReuslt;
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
            ZCameraLog.e("CameraPresenter", orientation + "....Camera...save_process_start..............." + System.currentTimeMillis());
            Uri uri = null;
            try {
                uri = CameraUtil.saveImageData(mContext, bitmap, Constants.FILE_DIR);
                ExifInterface exifInterface = new ExifInterface(mContext.getContentResolver().openFileDescriptor(uri, "rw", null).getFileDescriptor());
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, orientation + "");
                exifInterface.saveAttributes();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return uri;
        }

        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            if (mReuslt != null) {
                mReuslt.onResult(uri);
            }
        }
    }

    public void excute(byte[] data, int degree) {
        new SaveImageTask(data, degree).execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface UriResult {
        public void onResult(Uri uri);
    }
}
