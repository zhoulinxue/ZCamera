package org.zhx.common.camera.tasks;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import org.zhx.common.camera.ImageData;
import org.zhx.common.util.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageSearchProcessor {
    private Context mContext;
    private ImageDataCallback mImageDataCallback;

    public ImageSearchProcessor(Context mContext, ImageDataCallback imageDataCallback) {
        this.mContext = mContext;
        this.mImageDataCallback = imageDataCallback;
    }

    public void showImags() {
        new SearchImageTask().execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class SearchImageTask extends AsyncTask<Object, Object, List<ImageData>> {


        @Override
        protected List<ImageData> doInBackground(Object... objects) {
            Log.e("CameraPresenter", "....Camera...search_start..............." + System.currentTimeMillis());
            List<ImageData> dataList = new ArrayList<>();
            String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED};
            Cursor cursor = mContext.getApplicationContext().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int dateModifiedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
            int displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

            while (cursor.moveToNext()) {

                // Here we'll use the column indexs that we found above.
                long id = cursor.getLong(idColumn);
                Date dateModified = new Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateModifiedColumn)));
                String displayName = cursor.getString(displayNameColumn);

                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                );
                ImageData data = new ImageData();
                data.setContentUri(contentUri);
                data.setDateAdded(dateModified);
                data.setDisplayName(displayName);
                data.setId(id);
                dataList.add(data);
            }

            return dataList;
        }

        @Override
        protected void onPostExecute(List<ImageData> datas) {
            super.onPostExecute(datas);
            if (mImageDataCallback != null) {
                mImageDataCallback.onResult(datas);
            }
        }
    }

    public interface ImageDataCallback {
        void onResult(List<ImageData> images);
    }
}
