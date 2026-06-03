/*
 * Copyright (c) 2015-2020 Founder Ltd. All Rights Reserved.
 *
 *zhx for  org
 *
 *
 */

package org.zhx.common.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author zhx
 * @version 1.0, 2015-11-15 下午7:21:09
 */

public class ImageUtil {
    /**
     * @param
     * @return
     * @throws Exception
     * @author zhx
     */
    public static void recycleBitmap(Bitmap bitmap) {
        // Bitmap.recycle() is deprecated since API 34 (Android 14).
        // Let the garbage collector handle bitmap memory reclamation.
    }

    public static Bitmap getBitmap(Context context, byte[] data, boolean isScal) {
        //只请求图片宽高，不解析图片像素(请求图片属性但不申请内存，解析bitmap对象，该对象不占内存)
        Point displayPx = CameraUtil.getScreenMetrics(context);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        if (isScal) {
            opt.inJustDecodeBounds = true;
            //String path = Environment.getExternalStorageDirectory() + "/dog.jpg";
            BitmapFactory.decodeByteArray(data, 0, data.length, opt);
            int imageWidth = opt.outWidth;
            int imageHeight = opt.outHeight;
            Log.e("CameraPresenter", "bitmap...." + imageWidth + " xxx  " + imageHeight);
            int scale = 1;
            int scaleX = imageWidth / displayPx.x;
            int scaleY = imageHeight / displayPx.y;
            if (scaleX >= scaleY && scaleX > 1) {
                scale = scaleX;
            } else if (scaleX < scaleY && scaleY > 1) {
                scale = scaleY;
            }
            //设置缩放比例
            opt.inSampleSize = scale;
            opt.inJustDecodeBounds = false;
        }
        return BitmapFactory.decodeByteArray(data, 0, data.length, opt);
    }

    /**
     * 从uri  获取缩略图
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFormUri(Context context, Uri uri) {
        try {
            InputStream input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither = true;//optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();
            int originalWidth = onlyBoundsOptions.outWidth;
            int originalHeight = onlyBoundsOptions.outHeight;

            ZCameraLog.e("getBitmapFormUri, originalWidth:" + originalWidth + ", originalHeight:" + originalHeight);
            if ((originalWidth == -1) || (originalHeight == -1))
                return null;
            //图片分辨率以480x800为标准
            float hh = 800f;//这里设置高度为800f
            float ww = 480f;//这里设置宽度为480f
            //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
            int be = 1;//be=1表示不缩放
            if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
                be = (int) (originalWidth / ww);
            } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
                be = (int) (originalHeight / hh);
            }
            if (be <= 0)
                be = 1;
            //比例压缩
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = be;//设置缩放比例
            bitmapOptions.inDither = true;//optional
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            bitmap = adjustPhotoRotation(bitmap, getDegreeFromOrientation(context, uri));
            input.close();
            return bitmap;//再进行质量压缩
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
        return null;
    }
    /**
     * 获取图片旋转角度
     *
     * @param context
     * @param uri
     * @return
     * @throws Exception
     */

    private static int getDegreeFromOrientation(Context context, Uri uri) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(context.getContentResolver().openFileDescriptor(uri, "rw", null).getFileDescriptor());
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (Integer.valueOf(orientation)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }

        } catch (Exception e) {

        }
        return degree;
    }

    /**
     * 绘制Rect 倒角
     *
     * @param canvas
     * @param rect
     * @param paint
     */
    public static void drawRectCorner(Canvas canvas, Rect rect, Paint paint, int stroke) {
        //左下角
        canvas.drawRect(rect.left - stroke, rect.bottom, rect.left + 20, rect.bottom + stroke, paint);
        canvas.drawRect(rect.left - stroke, rect.bottom - 20, rect.left, rect.bottom, paint);
        //左上角
        canvas.drawRect(rect.left - stroke, rect.top - stroke, rect.left + 20, rect.top, paint);
        canvas.drawRect(rect.left - stroke, rect.top, rect.left, rect.top + 20, paint);
        //右上角
        canvas.drawRect(rect.right - 20, rect.top - stroke, rect.right + stroke, rect.top, paint);
        canvas.drawRect(rect.right, rect.top, rect.right + stroke, rect.top + 20, paint);
        //右下角
        canvas.drawRect(rect.right - 20, rect.bottom, rect.right + stroke, rect.bottom + stroke, paint);
        canvas.drawRect(rect.right, rect.bottom - 20, rect.right + stroke, rect.bottom, paint);
    }

    /**
     * 绘制Rect 以外的区域
     *
     * @param width
     * @param height
     * @param canvas
     * @param mCenterRect
     * @param mAreaPaint
     * @param stroke
     */
    public static void drawRectOutter(int width, int height, Canvas canvas, Rect mCenterRect, Paint mAreaPaint, int stroke) {
        canvas.drawRect(0, 0, width, (height - mCenterRect.height()) / 2 - stroke, mAreaPaint);
        canvas.drawRect(0, (height + mCenterRect.height()) / 2 + stroke, width, height,
                mAreaPaint);
        canvas.drawRect(0, (height - mCenterRect.height()) / 2 - stroke, mCenterRect.left - stroke,
                (height + mCenterRect.height()) / 2 + stroke, mAreaPaint);
        canvas.drawRect(mCenterRect.right + stroke, (height - mCenterRect.height()) / 2 - stroke,
                width, (height + mCenterRect.height()) / 2 + stroke, mAreaPaint);
    }

}
