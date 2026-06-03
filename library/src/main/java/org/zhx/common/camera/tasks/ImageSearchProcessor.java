package org.zhx.common.camera.tasks;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;

import org.zhx.common.camera.ImageData;
import org.zhx.common.camera.PictrueModel;
import org.zhx.common.util.ZCameraLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageSearchProcessor {
    private Context mContext;
    private PictrueModel.view mView;
    private String TAG = ImageSearchProcessor.class.getSimpleName();
    private ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public ImageSearchProcessor(Context context, PictrueModel.view mView) {
        this.mContext = context;
        this.mView = mView;
    }

    public void showImags(String path) {
        mExecutor.execute(() -> {
            ZCameraLog.e(TAG, "....Camera...search_start...............");
            List<ImageData> dataList = new ArrayList<>();
            String[] projection = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED};
            Cursor cursor = null;
            try {
                cursor = mContext.getApplicationContext().getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC");
                if (cursor == null) {
                    mMainHandler.post(() -> mView.onEmptyFile());
                    return;
                }
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int dateModifiedColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
                int displayNameColumn =
                        cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn);
                    String displayName = cursor.getString(displayNameColumn);

                    Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                    );
                    ImageData data = new ImageData();
                    data.setContentUri(contentUri);
                    data.setDateAdded(cursor.getLong(dateModifiedColumn));
                    data.setDisplayName(displayName);
                    data.setId(id);
                    if (!TextUtils.isEmpty(displayName)
                            && !TextUtils.isEmpty(path)
                            && displayName.contains(path)) {
                        dataList.add(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            final List<ImageData> resultList = dataList;
            mMainHandler.post(() -> {
                if (!resultList.isEmpty()) {
                    mView.onSearchResult(resultList);
                } else {
                    ZCameraLog.e(TAG, "....Camera...search...no images found...");
                    mView.onEmptyFile();
                }
            });
        });
    }

}
