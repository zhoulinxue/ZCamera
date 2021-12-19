package org.zhx.common.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import org.zhx.common.util.ImageUtil;

/**
 * 对焦 区域
 */

public class FocusRectView extends AppCompatImageView {
    private Rect touchFocusRect;//焦点附近设置矩形区域作为对焦区域
    private Paint touchFocusPaint;//新建画笔
    private float radius;
    private float x, y;
    private int maxWidth;

    public FocusRectView(Context context) {
        this(context, null, 0);
    }

    public FocusRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FocusRectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //画笔设置
        touchFocusPaint = new Paint();
        touchFocusPaint.setAntiAlias(false);
        touchFocusPaint.setColor(Color.GREEN);
        touchFocusPaint.setAntiAlias(true);
        touchFocusPaint.setStyle(Paint.Style.STROKE);
        touchFocusPaint.setStrokeWidth(5);
    }

    //对焦并绘制对焦矩形框
    public void setTouchFoucusRect(float x, float y) {
        this.x = x;
        this.y = y;
        maxWidth = 80;
        //以焦点为中心，宽度为200的矩形框
        touchFocusRect = new Rect((int) (x - maxWidth), (int) (y - maxWidth), (int) (x + maxWidth), (int) (y + maxWidth));
        maxWidth = (touchFocusRect.right - touchFocusRect.left);
        isScal = false;
        radius = maxWidth / 2;
        loop(0);
    }

    @Override
    protected void onDraw(Canvas canvas) { //在画布上绘图，postInvalidate()后自动调用
        drawTouchFocusRect(canvas);
        super.onDraw(canvas);
    }

    private void drawTouchFocusRect(Canvas canvas) {
        if (null != touchFocusRect) {
//            canvas.drawRect(touchFocusRect, touchFocusPaint);
//            if (!isRect) {
            canvas.drawCircle(x, y, radius, touchFocusPaint);
//            } else {
            ImageUtil.drawRectCorner(canvas, touchFocusRect, touchFocusPaint, 2);
//            }
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility == GONE) {
            removeCallbacks(cicleRunable);
            postInvalidate();
        }
        super.setVisibility(visibility);
    }

    private boolean isScal = false;
    Runnable cicleRunable = new Runnable() {
        @Override
        public void run() {
            int time = 2;
            float bounds = maxWidth / 2 * 1.2f;
            if (radius < maxWidth / 2 * 0.8f) {
                setVisibility(GONE);
            } else {
                if (radius < bounds && !isScal) {
                    radius += 10;
                } else if (radius > bounds) {
                    isScal = true;
                    radius -= 2;
                } else if (radius > maxWidth / 2 * 0.8f) {
                    time = 20;
                    radius -= 2;
                }
                touchFocusRect = new Rect((int) (x - radius), (int) (y - radius), (int) (x + radius), (int) (y + radius));
                invalidate();
                loop(time);
            }

        }
    };

    private void loop(int time) {
        postDelayed(cicleRunable, time);
    }

}

