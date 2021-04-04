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

import org.zhx.common.util.DisplayUtil;
import org.zhx.common.util.ImageUtil;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * 希望有一天可以开源出来  org.zhx
 *
 * @author zhx
 * @version 1.0, 2015-11-15 下午7:11:49
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
        postDelayed(lineRunable, 20);
    }

    Runnable lineRunable = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "loop");
            if (isShowScan()) {
                if (startY == 0 || startY >= (height + mCenterRect.height()) / 2 + 2) {
                    startY = (height - mCenterRect.height()) / 2 - 2;
                } else {
                    startY += 2;
                }
                invalidate();
                loop();
            }
        }
    };

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            removeCallbacks(lineRunable);
        }
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
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setAlpha(150);

    }

    public void setCenterRect(int width, int height) {
        Log.i(TAG, "setCenterRect...");
        this.mCenterRect = new Rect((this.width - width) / 2, (this.height - height) / 2, (this.width + width) / 2, (this.height + height) / 2);
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
        // 绘制四周阴影区域
        ImageUtil.drawRectOutter(width, height, canvas, mCenterRect, mAreaPaint, 2);
        // 绘制 4个倒角
        ImageUtil.drawRectCorner(canvas, mCenterRect, mPaint, 2);
        if (isShowScan())
            canvas.drawLine(mCenterRect.left - 2, startY, mCenterRect.right, startY, mPaint);
        super.onDraw(canvas);
    }

    public Rect getCenterRect() {
        return mCenterRect;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        Log.e(TAG, "onStop");
        removeCallbacks(lineRunable);
    }

    public boolean isShowScan() {
        return isShowScan;
    }

    public void showScan(boolean showScan) {
        isShowScan = showScan;
    }
}

