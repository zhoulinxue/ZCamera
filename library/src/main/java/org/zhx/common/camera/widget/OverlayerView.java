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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * 
* 希望有一天可以开源出来  org.zhx
* 
* @version 
*       1.0, 2015-11-15 下午7:11:49 
* @author  zhx
 */
public class OverlayerView extends AppCompatImageView implements LifecycleObserver {
	private static final String TAG = OverlayerView.class.getSimpleName();
	private Paint mLinePaint;
	private Paint mAreaPaint;
	private Rect mCenterRect = null;
	private Context mContext;
	private Paint mPaint;
	private boolean isShowScan;

	public OverlayerView(Context context) {
		super(context);
		init(context);
	}

	public OverlayerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	private void init(Context context) {
		initPaint();
		mContext = context;
		Point p = DisplayUtil.getScreenMetrics(mContext);
		width = p.x;
		height = p.y;
	}
	@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
	public void loop() {
		postDelayed(lineRunable,20);
	}
	Runnable lineRunable=new Runnable() {
		@Override
		public void run() {
			Log.e(TAG,"loop");
			if(isShowScan()) {
				invalidate();
				loop();
			}
		}
	};

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
		mPaint = new Paint();
		mPaint.setColor(Color.GREEN);
		mPaint.setAlpha(150);

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
	int startY;

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDraw...");
		if (mCenterRect == null)
			return;

		if(startY==0||startY>=(height+mCenterRect.height())/2 + 2){
			startY=(height-mCenterRect.height())/2-2;
		}else {
			startY+=2;
		}

		// 绘制四周阴影区域
		canvas.drawRect(0, 0, width, (height-mCenterRect.height())/2 - 2, mAreaPaint);
		canvas.drawRect(0, (height+mCenterRect.height())/2 + 2, width, height,
				mAreaPaint);
		canvas.drawRect(0, (height-mCenterRect.height())/2 - 2, mCenterRect.left - 2,
				(height+mCenterRect.height())/2 + 2, mAreaPaint);
		canvas.drawRect(mCenterRect.right + 2, (height-mCenterRect.height())/2 - 2,
				width, (height+mCenterRect.height())/2 + 2, mAreaPaint);


		
		canvas.drawRect(mCenterRect.left - 2, (height+mCenterRect.height())/2,
				mCenterRect.left + 50, (height+mCenterRect.height())/2 + 2, mPaint);// 左下 底部

		canvas.drawRect(mCenterRect.left - 2, (height+mCenterRect.height())/2-50,
				mCenterRect.left, (height+mCenterRect.height())/2 , mPaint);// 左下 左侧

		canvas.drawRect(mCenterRect.right - 50, (height+mCenterRect.height())/2,
				mCenterRect.right + 2, (height+mCenterRect.height())/2 + 2, mPaint);// 右下 右侧
		canvas.drawRect(mCenterRect.right, (height+mCenterRect.height())/2-50,
				mCenterRect.right + 2, (height+mCenterRect.height())/2, mPaint);// 右下 底部

		canvas.drawRect(mCenterRect.left - 2, (height-mCenterRect.height())/2 - 2,
				mCenterRect.left + 50, (height-mCenterRect.height())/2, mPaint);// 左上 顶部
		canvas.drawRect(mCenterRect.left - 2, (height-mCenterRect.height())/2,
				mCenterRect.left, (height-mCenterRect.height())/2 + 50, mPaint);// 左上 侧边
		canvas.drawRect(mCenterRect.right - 50, (height-mCenterRect.height())/2 - 2,
				mCenterRect.right + 2, (height-mCenterRect.height())/2, mPaint);// 右上 顶部
		canvas.drawRect(mCenterRect.right, (height-mCenterRect.height())/2,
				mCenterRect.right + 2, (height-mCenterRect.height())/2 + 50, mPaint);// 右上 右侧
		if(isShowScan())
		canvas.drawLine(mCenterRect.left - 2, startY, mCenterRect.right, startY, mPaint);
		super.onDraw(canvas);
	}

	public Rect getCenterRect() {
		return mCenterRect;
	}

	@OnLifecycleEvent(Lifecycle.Event.ON_STOP)
	public void onStop(){
		Log.e(TAG,"onStop");
		removeCallbacks(lineRunable);
	}

	public boolean isShowScan() {
		return isShowScan;
	}

	public void showScan(boolean showScan) {
		isShowScan = showScan;
	}
}

