package com.honeywell.iaq.permission;

import android.Manifest;
import android.app.Activity;


public interface Permission {

    public static final String CAMERA = Manifest.permission.CAMERA;
    public static final String WRITE_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    interface PermissionCodes {

        public static final int CAMERA_REQUEST_CODE = 1;
        public static final int STORAGE_REQUEST_CODE = 2;
        public static final int CAMERA_AND_STORAGE_REQUEST_CODE = 3;

    }

    public void requestCameraPermission(Activity permissionActivity);
    public void requestStorageAndCameraPermission(Activity permissionActivity);
}
