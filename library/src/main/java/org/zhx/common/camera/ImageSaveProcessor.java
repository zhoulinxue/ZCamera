package org.zhx.common.camera;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.zhx.common.util.FileUtil;

import java.io.IOException;

public class ImageSaveProcessor {
    private Context mContext;
    private UriResult mReuslt;

    public ImageSaveProcessor(Context mContext, UriResult mReuslt) {
        this.mContext = mContext;
        this.mReuslt = mReuslt;
    }

    private class SaveImageTask extends AsyncTask<Object, Object, Uri> {
        private byte[] datas;

        public SaveImageTask(byte[] datas) {
            this.datas = datas;
        }

        @Override
        protected Uri doInBackground(Object... objects) {
            Uri uri = null;
            try {
                uri = FileUtil.saveImageData(mContext, datas);
            } catch (IOException e) {
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

    public void excute(byte[] data) {
        new SaveImageTask(data.clone()).execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface UriResult {
        public void onResult(Uri uri);
    }
}
