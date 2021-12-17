package org.zhx.common.util;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * Name: PermissionsUtil
 * Author: zhouxue
 * Email: 194093798@qq.com
 * Comment: //TODO
 * Date: 2020-01-30 00:11
 */
public class PermissionsUtil {

    public static void checkPermission(AppCompatActivity context, String permission, int requestCode) {
        // Here, thisActivity is the current activity
        if (!hasPermission(context, permission)) {
            requestPermission(context, permission, requestCode);
        } else {
            // Permission has already been granted
            context.onRequestPermissionsResult(requestCode, new String[]{permission}, new int[]{PackageManager.PERMISSION_GRANTED});
        }
    }

    public static boolean hasPermission(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(AppCompatActivity context, String permission, int resultCode) {
        requestPermissions(context,
                new String[]{permission}, resultCode);
    }

    public static void requestPermission(Fragment fragment, String permission, int resultCode) {
        fragment.requestPermissions(new String[]{permission}, resultCode);
    }

    public static void requestPermissions(AppCompatActivity context, String[] permissions, int resultCode) {
        ActivityCompat.requestPermissions(context, permissions, resultCode);
    }

}
