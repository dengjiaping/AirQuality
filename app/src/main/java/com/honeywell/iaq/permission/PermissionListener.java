package com.honeywell.iaq.permission;


public interface PermissionListener {
    // permission disabled
    public void onPermissionGranted(int permissionCode);
    // permission disabled and not granted yet request system to grant permission.
    public void onPermissionNotGranted(String[] permission, int permissionCode);
    // user denied to grant permission earlier
    public void onPermissionDenied(int permissionCode);
    
}
