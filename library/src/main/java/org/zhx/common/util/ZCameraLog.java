package org.zhx.common.util;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

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
        Log.d(TAG, tag + "__" + msg + " tempTime: " + getTime());
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
        Log.e(TAG, tag + "__" + msg + " tempTime: "+ getTime());
    }

    public static void e(String msg) {
        e("", msg);
    }


    private static String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS");
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    public static void e(String msg, Throwable throwable) {
        Log.e("",msg,throwable);
    }

    public static void e(String msg, Throwable throwable) {
        Log.e("",msg,throwable);
    }

}
