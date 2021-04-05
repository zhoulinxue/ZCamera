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
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * 希望有一天可以开源出来 org.zhx
 *
 * @author zhx
 * @version 1.0, 2015-11-15 下午7:21:09
 */

public class ImageUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();

    /**
     * @param
     * @return
     * @throws Exception
     * @author zhx
     */
    public static Bitmap getRectBmp(Bitmap bm, Point p, int width, int height) {
        // TODO Auto-generated method stub
        Bitmap bitmap = resizeImage(bm, p.x, p.y);
        Bitmap rectbitmap = Bitmap.createBitmap(bitmap, (p.x - width) / 2, (p.y - height) / 2, width,
                height);
        return rectbitmap;
    }

    //使用Bitmap加Matrix来缩放
    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // if you want to rotate the Bitmap
        // matrix.postRotate(45);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        BitmapOrg.recycle();
        return resizedBitmap;
    }


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

    public static Bitmap cropBitmap(Bitmap bitmap, int width, int height) {//从中间截取一个正方形
        Log.e(TAG, bitmap.getWidth() + "!!!" + width);
        return Bitmap.createBitmap(bitmap, (bitmap.getWidth() - width) / 2,
                (bitmap.getHeight() - height) / 2, width, height);
    }

    public static Bitmap getBitmap(Context context, byte[] data, boolean isScal) {
        //只请求图片宽高，不解析图片像素(请求图片属性但不申请内存，解析bitmap对象，该对象不占内存)
        Point displayPx = DisplayUtil.getScreenMetrics(context);
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

    public static Bitmap getThumilImage(Context context, byte[] data) {
        Point displayPx = DisplayUtil.getScreenMetrics(context);
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

    public static Rect calculateTapArea(Context context, float x, float y, float coefficient) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerY = 0;
        int centerX = 0;
        centerY = (int) (x / DisplayUtil.getScreenMetrics(context).x * 2000 - 1000);
        centerX = (int) (y / DisplayUtil.getScreenMetrics(context).y * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

}
