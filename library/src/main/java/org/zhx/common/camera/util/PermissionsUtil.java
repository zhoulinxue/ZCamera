package org.zhx.common.camera.util;

import android.app.Activity;
import android.content.pm.PackageManager;

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

    public static void checkPermission(Activity context,String permission,int requestCode){

        // Here, thisActivity is the current activity(Manifest.permission.READ_CONTACTS)
        if (ContextCompat.checkSelfPermission(context,permission)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(context,permission,requestCode);

        } else {
            // Permission has already been granted
            context.onRequestPermissionsResult(requestCode,new String[]{permission},new int[]{PackageManager.PERMISSION_GRANTED});
        }

    }

    public static void requestPermission(Activity context, String permission,int resultCode){
        // Permission is not granted
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(context,permission)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            ActivityCompat.requestPermissions(context,
                    new String[]{permission},resultCode);
        } else {
            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(context,
                    new String[]{permission},resultCode);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

}
