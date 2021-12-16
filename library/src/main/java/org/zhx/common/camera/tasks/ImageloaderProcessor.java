package org.zhx.common.camera.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import org.zhx.common.camera.ImageLoaderModel;
import org.zhx.common.util.ImageUtil;

import java.io.IOException;

public class ImageloaderProcessor {
    private Context mContext;

    public ImageloaderProcessor(Context context) {
        this.mContext = context;
    }

    public void loadImags(Uri uri, int position, ImageLoaderModel.view view) {
        new LoadImageImageTask(uri, position, view).execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class LoadImageImageTask extends AsyncTask<Object, Object, Bitmap> {
        private Uri uri;
        private int position;
        ImageLoaderModel.view mView;


        public LoadImageImageTask(Uri uri, int position, ImageLoaderModel.view view) {
            this.uri = uri;
            this.position = position;
            this.mView = view;
        }

        @Override
        protected Bitmap doInBackground(Object... objects) {
            try {
                return ImageUtil.getBitmapFormUri(mContext, uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (null != mView) {
                mView.onBitmapLoadSuc(bitmap, position);
            }
        }
    }


}
