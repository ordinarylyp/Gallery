package com.sunmi.commmonlib.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Description:
 *
 * @author linyuanpeng on 2019-09-23.
 */
public class PermissionUtils {

    public static final int REQ_PERMISSIONS_CAMERA_STORAGE = 0x101;

    public static boolean checkSDCardCameraPermission(Activity activity) {
        String[] PERMISSIONS_STORAGE = {Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission1 = ContextCompat.checkSelfPermission(activity, PERMISSIONS_STORAGE[0]);
            int permission2 = ContextCompat.checkSelfPermission(activity, PERMISSIONS_STORAGE[1]);
            if (permission1 != PackageManager.PERMISSION_GRANTED ||
                    permission2 != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{
                        PERMISSIONS_STORAGE[0], PERMISSIONS_STORAGE[1]}, REQ_PERMISSIONS_CAMERA_STORAGE);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }
}
