package org.zhx.common.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.DisplayMetrics;

/**
 * 相机工具类
 *
 * @author zhx
 * @version 1.0, 2015-11-15 下午5:23:57
 */
public class CameraUtil {
    /**
     * @param
     * @return
     * @throws Exception
     * @author zhx
     */
    public static Point getScreenMetrics(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        ZCameraLog.i("Screen", "---Width = " + w_screen + " Height = " + h_screen
                + " densityDpi = " + dm.densityDpi);
        return new Point(w_screen, h_screen);
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        ZCameraLog.i("statusBarHeight", height + " ");
        return height;
    }

    /**
     * @param
     * @return int
     * @throws Exception
     * @author zhx
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * @param
     * @return int
     * @throws Exception
     * @author zhx
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
