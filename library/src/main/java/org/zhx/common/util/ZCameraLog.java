package org.zhx.common.util;


import android.util.Log;

public class ZCameraLog {
    private boolean isDebug = true;
    private static String TAG = ZCameraLog.class.getSimpleName();

    public static void i(String tag, String msg) {
        Log.i(TAG, tag + "__" + msg);
    }

    public static void v(String tag, String msg) {
        Log.v(TAG, tag + "__" + msg);
    }

    public static void d(String tag, String msg) {
        Log.d(TAG, tag + "__" + msg);
    }

    public static void i(String msg) {
        i("", msg);
    }

    public static void v(String msg) {
        v("", msg);
    }

    public static void d(String msg) {
        d("", msg);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + "__" + msg);
    }

    public static void e(String msg) {
        e("", msg);
    }

}
