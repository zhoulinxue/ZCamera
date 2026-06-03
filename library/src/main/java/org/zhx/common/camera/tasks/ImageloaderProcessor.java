package org.zhx.common.camera.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import org.zhx.common.camera.ImageLoaderModel;
import org.zhx.common.util.ImageUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageloaderProcessor {
    private Context mContext;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(2);
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public ImageloaderProcessor(Context context) {
        this.mContext = context;
    }

    public void loadImags(Uri uri, int position, ImageLoaderModel.view view) {
        mExecutor.execute(() -> {
            Bitmap bitmap = ImageUtil.getBitmapFormUri(mContext, uri);
            mMainHandler.post(() -> {
                if (null != view) {
                    view.onBitmapLoadSuc(bitmap, position);
                }
            });
        });
    }

}
