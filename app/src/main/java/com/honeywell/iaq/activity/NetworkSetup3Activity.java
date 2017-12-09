package com.honeywell.iaq.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeywell.iaq.AndroidBug5497Workaround;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.Utils;

/**
 * Created by E570281 on 8/17/2016.
 */
public class NetworkSetup3Activity extends IAQTitleBarActivity {

    private static final String TAG = "NetworkSetup3";

    private android.support.v7.widget.Toolbar mToolbar;

    private Button mNext;

    private TextView mSerialNumber;

    @Override
    protected int getContent() {
        return R.layout.activity_network_setup3;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.confirm_iaq);
    }

    @Override
    protected void initView() {
        super.initView();
//        AndroidBug5497Workaround.assistActivity(this);

        mNext = (Button) findViewById(R.id.btn_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    Intent intent = new Intent();
                    intent.setClass(NetworkSetup3Activity.this, NetworkSetup4Activity.class);
                    startActivity(intent);
                } else {
                    Utils.showToast(getApplicationContext(), getString(R.string.no_network));
                }
            }
        });

        mSerialNumber = (TextView) findViewById(R.id.serialNumber);
        String serialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_CURRENT_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
        Log.d(TAG, "Serial Num=" + serialNum);
        if (serialNum.length() > 0) {
            mSerialNumber.setText(getString(R.string.serial_number) + ": " + serialNum);
        }
        Utils.setListenerToRootView(this, R.id.activity_network_setup3, mNext);
    }

}
