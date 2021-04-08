package org.zhx.common.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

public class FileUtil {
    // 保存图片

    public static Uri saveImageData(Context context, byte[] data) throws IOException {
        Uri uri = null;
        String name = "zCamera_" + System.currentTimeMillis() + ".jpg";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            ContentResolver contentResolver = context.getContentResolver();
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try {
                OutputStream out = contentResolver.openOutputStream(uri);
                out.write(data);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File eFile = Environment.getExternalStorageDirectory();
            File mDirectory = new File(eFile.toString() + File.separator + "zCamera");
            if (!mDirectory.exists()) {
                mDirectory.mkdirs();
            }
            File imageFile = new File(mDirectory, name);
            FileOutputStream out;
            out = new FileOutputStream(imageFile);
            out.write(data);
            uri = toUri(context, imageFile.getAbsolutePath());
            updatePhotoAlbum(context, imageFile, data);//更新图库
            out.close();
        }
        return uri;
    }


    /**
     * 兼容android 10
     * 更新图库
     *
     * @param mContext
     * @param file
     */
    public static void updatePhotoAlbum(Context mContext, File file, byte[] data) {


        MediaScannerConnection.scanFile(mContext.getApplicationContext(), new String[]{file.getAbsolutePath()}, new String[]{"image/jpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {

            }
        });

    }

    public static Uri toUri(Context context, String filePath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Log.e("!!!!!!!!!", context.getApplicationInfo().packageName);
            return FileProvider.getUriForFile(context, context.getApplicationInfo().packageName + ".provider", new File(filePath));
        }
        return Uri.fromFile(new File(filePath));
    }

    public static String getMimeType(File file) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(file.getName());
        return type;
    }
}
