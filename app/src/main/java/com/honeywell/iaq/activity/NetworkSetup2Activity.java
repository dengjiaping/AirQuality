package com.honeywell.iaq.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeywell.iaq.AndroidBug5497Workaround;
import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.permission.IAQPermission;
import com.honeywell.iaq.permission.Permission;
import com.honeywell.iaq.permission.PermissionListener;
import com.honeywell.iaq.utils.Utils;

/**
 * Created by E570281 on 8/17/2016.
 */
public class NetworkSetup2Activity extends IAQTitleBarActivity implements PermissionListener {


    private Button mScan;
    private IAQPermission mIaqPermission;
    private boolean isCameraPermission = false;
    private boolean isStoragePermission = false;

    @Override
    protected int getContent() {
        return R.layout.activity_network_setup2;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.scan_barcode);
    }

    @Override
    protected void initView() {
        super.initView();
//        AndroidBug5497Workaround.assistActivity(this);
        mIaqPermission = new IAQPermission(this);
        mScan = (Button) findViewById(R.id.btn_scan);
        mScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
                if (isCameraPermission && isStoragePermission) {
                    Intent intent = new Intent();
                    intent.setClass(NetworkSetup2Activity.this, MipcaActivityCaptureActivity.class);
                    startActivity(intent);
                }

            }
        });

        Utils.setListenerToRootView(this, R.id.activity_network_setup2, mScan);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            Utils.showToast(getApplicationContext(), getString(R.string.no_network));
        }
    }

    @Override
    public void onPermissionGranted(int permissionCode) {
        switch (permissionCode) {
            case Permission.PermissionCodes.CAMERA_REQUEST_CODE:
                isCameraPermission = true;
                break;
            case Permission.PermissionCodes.STORAGE_REQUEST_CODE:
                isStoragePermission = true;
            case Permission.PermissionCodes.CAMERA_AND_STORAGE_REQUEST_CODE:
                isStoragePermission = true;
                isCameraPermission = true;
                break;
        }
    }

    @Override
    public void onPermissionNotGranted(String[] permission, int permissionCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(permission, permissionCode);
        }
    }

    @Override
    public void onPermissionDenied(int permissionCode) {
    }


    private void checkPermission() {

        mIaqPermission.checkAndRequestPermission(Permission.PermissionCodes.CAMERA_AND_STORAGE_REQUEST_CODE, this);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case IAQPermission.PermissionCodes.CAMERA_AND_STORAGE_REQUEST_CODE:

                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            if (permissions[i].equals(Permission.CAMERA)) {
                            } else {
                            }
                            isCameraPermission = false;
                            return;
                        }
                    }
                    isCameraPermission = true;
                    Intent intent = new Intent();
                    intent.setClass(NetworkSetup2Activity.this, MipcaActivityCaptureActivity.class);
                    startActivity(intent);
                } else {
                    isCameraPermission = false;
                }
                break;

        }
    }


}
