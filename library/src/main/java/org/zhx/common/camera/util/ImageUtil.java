/* 
 * Copyright (c) 2015-2020 Founder Ltd. All Rights Reserved. 
 * 
 *zhx for  org
 * 
 * 
 */

package org.zhx.common.camera.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DebugUtils;
import android.util.Log;

/**
 * 
 * 希望有一天可以开源出来 org.zhx
 * 
 * @version 1.0, 2015-11-15 下午7:21:09
 * @author zhx
 */

public class ImageUtil {

	private static final String TAG = ImageUtil.class.getSimpleName();

	/**
	 *
	 * @param
	 * @return
	 * @throws Exception
	 * @author zhx
	 */
	public static Bitmap getRectBmp(Bitmap bm, Point p,int width,int height) {
		// TODO Auto-generated method stub
		Bitmap bitmap = resizeImage(bm, p.x, p.y);
		Bitmap rectbitmap = Bitmap.createBitmap(bitmap, (p.x-width)/2, (p.y-height)/2, width,
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
	 *
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

	public static Bitmap cropBitmap(Context context,Bitmap bitmap, int width, int height) {//从中间截取一个正方形
		Log.e(TAG,bitmap.getWidth()+"!!!"+width);
		return Bitmap.createBitmap(bitmap, (bitmap.getWidth() - width) / 2,
				(bitmap.getHeight()- DisplayUtil.getStatusBarHeight(context) - height) / 2, width, height);
	}

}
