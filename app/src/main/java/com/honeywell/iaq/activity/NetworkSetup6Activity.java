package com.honeywell.iaq.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
public class NetworkSetup6Activity extends IAQTitleBarActivity {

    private static final String TAG = "NetworkSetup6";

    private android.support.v7.widget.Toolbar mToolbar;

    private Button mNext;

    private String connectedWifiSSID;

    @Override
    protected int getContent() {
        return R.layout.activity_network_setup6;
    }


    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.connect_phone_iaq);
    }

    @Override
    protected void initView() {
        super.initView();
//        AndroidBug5497Workaround.assistActivity(this);

        connectedWifiSSID = getIntent().getStringExtra(Constants.KEY_WIFI_SSID);
        mNext = (Button) findViewById(R.id.btn_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(NetworkSetup6Activity.this, APLinkActivity.class);
                intent.putExtra(Constants.KEY_WIFI_SSID, connectedWifiSSID);
                startActivity(intent);
            }
        });

        Utils.setListenerToRootView(this, R.id.activity_network_setup6, mNext);
    }
}
