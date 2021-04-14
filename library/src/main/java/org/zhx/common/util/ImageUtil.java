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
import android.provider.MediaStore;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
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
    public static Bitmap getThumilImage(Context context, Uri uri) {
        Bitmap bitmap = null;
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        Point displayPx = CameraUtil.getScreenMetrics(context);
        InputStream stream = null;
        try {
            stream = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(stream, null, opt);
            int imageWidth = opt.outWidth;
            int imageHeight = opt.outHeight;
            int x = displayPx.x;
            int y = displayPx.y;
            int scale = 1;
            int scaleX = imageWidth / x;
            int scaleY = imageHeight / y;
            if (scaleX >= scaleY && scaleX > 1) {
                scale = scaleX;
            } else if (scaleX < scaleY && scaleY > 1) {
                scale = scaleY;
            }
            //设置缩放比例
            opt.inSampleSize = scale * 5;
            opt.inJustDecodeBounds = false;
            bitmap = adjustPhotoRotation(BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, opt), getDegreeFromOrientation(context, uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getThumilImage(Context context, byte[] data) {
        Point displayPx = CameraUtil.getScreenMetrics(context);
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        //String path = Environment.getExternalStorageDirectory() + "/dog.jpg";
        BitmapFactory.decodeByteArray(data, 0, data.length, opt);
        int imageWidth = opt.outWidth;
        int imageHeight = opt.outHeight;
        int x = displayPx.x;
        int y = displayPx.y;
        int scale = 1;
        int scaleX = imageWidth / x;
        int scaleY = imageHeight / y;
        if (scaleX >= scaleY && scaleX > 1) {
            scale = scaleX;
        } else if (scaleX < scaleY && scaleY > 1) {
            scale = scaleY;
        }
        //设置缩放比例
        opt.inSampleSize = scale * 4;
        opt.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data, 0, data.length, opt);
    }

    private static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }
        final float[] values = new float[9];
        m.getValues(values);
        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];
        m.postTranslate(targetX - x1, targetY - y1);
        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);
        recycleBitmap(bm);
        return bm1;
    }

    /**
     * 获取大图
     *
     * @param context
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        ZCameraLog.e("CameraPresenter", "....Camera...show_img.start.............." + System.currentTimeMillis());
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            int degree = getDegreeFromOrientation(context, uri);
            if (degree != 0)
                bitmap = adjustPhotoRotation(bitmap, degree);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ZCameraLog.e("CameraPresenter", "....Camera...show_img._suc.............." + System.currentTimeMillis());
        return bitmap;
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

    public static byte[] bitmap2Bytes(Bitmap bm, boolean isRecycl) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        if (isRecycl) {
            bm.recycle();
            bm = null;
        }
        return data;
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
