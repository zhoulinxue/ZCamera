package org.zhx.common.camera.tasks;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;

import org.zhx.common.camera.CameraModel;
import org.zhx.common.camera.ImageData;
import org.zhx.common.camera.PictrueModel;
import org.zhx.common.util.ZCameraLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ImageSearchProcessor {
    private Context mContext;
    private PictrueModel.view mView;
    private String TAG = ImageSearchProcessor.class.getSimpleName();

    public ImageSearchProcessor(Context context, PictrueModel.view mView) {
        this.mContext = context;
        this.mView = mView;
    }

    public void showImags(String path) {
        new SearchImageTask(path).execute(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class SearchImageTask extends AsyncTask<Object, Object, List<ImageData>> {
        private String path;

        public SearchImageTask(String path) {
            this.path = path;
        }

        @Override
        protected List<ImageData> doInBackground(Object... objects) {
            ZCameraLog.e(TAG, "....Camera...search_start...............");
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
                if (!TextUtils.isEmpty(displayName)
                        && !TextUtils.isEmpty(path)
                        && displayName.contains(path)) {
                    dataList.add(data);
                }
            }

            if (null != dataList && dataList.size() != 0) {
                mView.onSearchResult(dataList);
            }

            return dataList;
        }

        @Override
        protected void onPostExecute(List<ImageData> datas) {
            if (datas == null || datas.size() == 0)
                mView.onEmptyFile();
        }
    }


}
