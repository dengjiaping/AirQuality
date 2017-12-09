package com.honeywell.iaq.permission;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.honeywell.net.utils.Logger;


/**
 * Created by zhujunyu on 2017/3/14.
 */

public class IAQPermission implements Permission {

    private PermissionListener mPermissionListener;

    public IAQPermission(PermissionListener permissionListener) {
        this.mPermissionListener = permissionListener;
    }


    private int checkPermission(Activity thisActivity, String permission) {
        int permissionCheck = ContextCompat.checkSelfPermission(thisActivity, permission);
        Logger.e("________", "" + permissionCheck + permission);
        return permissionCheck;
    }

    @Override
    public void requestCameraPermission(Activity permissionActivity) {

        int permission = checkPermission(permissionActivity, CAMERA);

        boolean isPermissionDialogAlreadyShown = ActivityCompat.shouldShowRequestPermissionRationale(permissionActivity, CAMERA);

        if (PackageManager.PERMISSION_GRANTED == permission) {
            mPermissionListener.onPermissionGranted(PermissionCodes.CAMERA_REQUEST_CODE);
        } else {
            mPermissionListener.onPermissionNotGranted(new String[]{CAMERA}, PermissionCodes.CAMERA_REQUEST_CODE);
        }
        if (permission == PackageManager.PERMISSION_DENIED && !isPermissionDialogAlreadyShown) {
            mPermissionListener.onPermissionDenied(PermissionCodes.CAMERA_REQUEST_CODE);
        }
    }


    public void checkAndRequestPermission(int permissionCodes, Activity permissionActivity) {
        switch (permissionCodes) {
            case Permission.PermissionCodes.CAMERA_REQUEST_CODE:
                requestCameraPermission(permissionActivity);
                break;
            case PermissionCodes.CAMERA_AND_STORAGE_REQUEST_CODE:
                requestStorageAndCameraPermission(permissionActivity);
            case PermissionCodes.STORAGE_REQUEST_CODE:
                requestStoragePermission(permissionActivity);
                break;
        }
    }


    public void requestStorageAndCameraPermission(Activity permissionActivity) {
        boolean storagePermission = isHasPermissionGranted(permissionActivity, new String[]{WRITE_STORAGE, READ_STORAGE});
        boolean cameraPermission = isHasPermissionGranted(permissionActivity, new String[]{CAMERA});

        if (!storagePermission && !cameraPermission) {
            mPermissionListener.onPermissionNotGranted(new String[]{WRITE_STORAGE, READ_STORAGE, CAMERA}, PermissionCodes.CAMERA_AND_STORAGE_REQUEST_CODE);
        } else if (storagePermission && !cameraPermission) {
            mPermissionListener.onPermissionNotGranted(new String[]{CAMERA}, PermissionCodes.CAMERA_REQUEST_CODE);
            mPermissionListener.onPermissionGranted(PermissionCodes.STORAGE_REQUEST_CODE);
        } else if (!storagePermission && cameraPermission) {
            mPermissionListener.onPermissionNotGranted(new String[]{WRITE_STORAGE, READ_STORAGE}, PermissionCodes.STORAGE_REQUEST_CODE);
            mPermissionListener.onPermissionGranted(PermissionCodes.CAMERA_REQUEST_CODE);
        } else {
            mPermissionListener.onPermissionGranted(PermissionCodes.CAMERA_AND_STORAGE_REQUEST_CODE);
        }

    }

    public void requestStoragePermission(Activity permissionActivity) {
        boolean storagePermission = isHasPermissionGranted(permissionActivity, new String[]{WRITE_STORAGE, READ_STORAGE});

        if (!storagePermission) {
            mPermissionListener.onPermissionNotGranted(new String[]{WRITE_STORAGE, READ_STORAGE}, PermissionCodes.STORAGE_REQUEST_CODE);
        } else {
            mPermissionListener.onPermissionGranted(PermissionCodes.STORAGE_REQUEST_CODE);
        }

    }


    public boolean isHasPermissionGranted(Activity permissionActivity, String[] permissionList) {

        boolean result = true;
        if (permissionList == null || permissionList.length == 0) {
            return result;
        }

        for (String permission : permissionList) {
            result = result && (checkPermission(permissionActivity, permission) == PackageManager.PERMISSION_GRANTED);
        }
        return result;
    }

}
