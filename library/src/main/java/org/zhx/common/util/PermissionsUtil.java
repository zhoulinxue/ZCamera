package org.zhx.common.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Name: PermissionsUtil
 * Author: zhouxue
 * Email: 194093798@qq.com
 * Comment: //TODO
 * Date: 2020-01-30 00:11
 */
public class PermissionsUtil {

    public static void checkPermission(AppCompatActivity context, String permission, int requestCode) {

        // Here, thisActivity is the current activity(Manifest.permission.READ_CONTACTS)
        if (!hasPermission(context, permission)) {
            requestPermission(context, permission, requestCode);

        } else {
            // Permission has already been granted
            context.onRequestPermissionsResult(requestCode, new String[]{permission}, new int[]{PackageManager.PERMISSION_GRANTED});
        }
    }

    public static boolean hasPermission(AppCompatActivity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestPermission(AppCompatActivity context, String permission, int resultCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
            ActivityCompat.requestPermissions(context,
                    new String[]{permission}, resultCode);
        } else {
            ActivityCompat.requestPermissions(context,
                    new String[]{permission}, resultCode);
        }
    }

}
