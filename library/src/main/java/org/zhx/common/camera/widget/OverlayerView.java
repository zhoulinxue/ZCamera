/* 
 * Copyright (c) 2015-2020 Founder Ltd. All Rights Reserved. 
 * 
 *zhx for  org
 * 
 * 
 */

package org.zhx.common.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import org.zhx.common.camera.util.DisplayUtil;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * 
* 希望有一天可以开源出来  org.zhx
* 
* @version 
*       1.0, 2015-11-15 下午7:11:49 
* @author  zhx
 */
public class OverlayerView extends AppCompatImageView {
	private static final String TAG = OverlayerView.class.getSimpleName();
	private Paint mLinePaint;
	private Paint mAreaPaint;
	private Rect mCenterRect = null;
	private Context mContext;
	private Paint paint;

	public OverlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initPaint();
		mContext = context;
		Point p = DisplayUtil.getScreenMetrics(mContext);
		width = p.x;
		height = p.y;
	}

	private void initPaint() {
		// 绘制中间透明区域矩形边界的Paint
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mLinePaint.setColor(Color.BLUE);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(5f);
		mLinePaint.setAlpha(0);

		// 绘制四周阴影区域
		mAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mAreaPaint.setColor(Color.GRAY);
		mAreaPaint.setStyle(Style.FILL);
		mAreaPaint.setAlpha(100);
		paint = new Paint();

	}


	public void setCenterRect(Rect r) {
		Log.i(TAG, "setCenterRect...");
		this.mCenterRect = r;
		postInvalidate();
	}

	public void clearCenterRect(Rect r) {
		this.mCenterRect = null;
	}

	int width, height;

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDraw...");
		if (mCenterRect == null)
			return;

		// 绘制四周阴影区域
		canvas.drawRect(0, 0, width, (height-mCenterRect.height())/2 - 2, mAreaPaint);
		canvas.drawRect(0, (height+mCenterRect.height())/2 + 2, width, height,
				mAreaPaint);
		canvas.drawRect(0, (height-mCenterRect.height())/2 - 2, mCenterRect.left - 2,
				(height+mCenterRect.height())/2 + 2, mAreaPaint);
		canvas.drawRect(mCenterRect.right + 2, (height-mCenterRect.height())/2 - 2,
				width, (height+mCenterRect.height())/2 + 2, mAreaPaint);

		paint.setColor(Color.WHITE);
		paint.setAlpha(150);
		
		canvas.drawRect(mCenterRect.left - 2, (height+mCenterRect.height())/2,
				mCenterRect.left + 50, (height+mCenterRect.height())/2 + 2, paint);// 左下 底部

		canvas.drawRect(mCenterRect.left - 2, (height+mCenterRect.height())/2-50,
				mCenterRect.left, (height+mCenterRect.height())/2 , paint);// 左下 左侧

		canvas.drawRect(mCenterRect.right - 50, (height+mCenterRect.height())/2,
				mCenterRect.right + 2, (height+mCenterRect.height())/2 + 2, paint);// 右下 右侧
		canvas.drawRect(mCenterRect.right, (height+mCenterRect.height())/2-50,
				mCenterRect.right + 2, (height+mCenterRect.height())/2, paint);// 右下 底部

		canvas.drawRect(mCenterRect.left - 2, (height-mCenterRect.height())/2 - 2,
				mCenterRect.left + 50, (height-mCenterRect.height())/2, paint);// 左上 顶部
		canvas.drawRect(mCenterRect.left - 2, (height-mCenterRect.height())/2,
				mCenterRect.left, (height-mCenterRect.height())/2 + 50, paint);// 左上 侧边
		canvas.drawRect(mCenterRect.right - 50, (height-mCenterRect.height())/2 - 2,
				mCenterRect.right + 2, (height-mCenterRect.height())/2, paint);// 右上 顶部
		canvas.drawRect(mCenterRect.right, (height-mCenterRect.height())/2,
				mCenterRect.right + 2, (height-mCenterRect.height())/2 + 50, paint);// 右上 右侧
		super.onDraw(canvas);
	}

	public Rect getCenterRect() {
		return mCenterRect;
	}


}

