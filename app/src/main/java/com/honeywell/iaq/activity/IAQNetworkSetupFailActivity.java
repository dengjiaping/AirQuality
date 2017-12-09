package com.honeywell.iaq.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;

/**
 * Created by E570281 on 10/11/2016.
 */
public class IAQNetworkSetupFailActivity extends IAQTitleBarActivity {

    private android.support.v7.widget.Toolbar mToolbar;

    private Button mWifiIndicatorOff, mWifiIndicatorOn, mCloudIndicatorOn;

    @Override
    protected int getContent() {
        return R.layout.activity_iaq_setup_network_fail;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.iaq_network_fail);
    }

    @Override
    protected void initView() {
        super.initView();
        mWifiIndicatorOff = (Button) findViewById(R.id.wifi_indicator_off);
        mWifiIndicatorOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(IAQNetworkSetupFailActivity.this).inflate(R.layout.wifi_indicator_off, null);
                showErrorDialog(view);
            }
        });
        mWifiIndicatorOn = (Button) findViewById(R.id.wifi_indicator_on);
        mWifiIndicatorOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(IAQNetworkSetupFailActivity.this).inflate(R.layout.cloud_indicator_red, null);
                showErrorDialog(view);
            }
        });
        mCloudIndicatorOn = (Button) findViewById(R.id.cloud_indicator_on);
        mCloudIndicatorOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = LayoutInflater.from(IAQNetworkSetupFailActivity.this).inflate(R.layout.cloud_indicator_white, null);
                showErrorDialog(view);
            }
        });
    }

    private void showErrorDialog(View view) {
        AlertDialog mAlertDialog = new AlertDialog.Builder(IAQNetworkSetupFailActivity.this)
                .setTitle(getString(R.string.iaq_network_fail))
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(IAQNetworkSetupFailActivity.this, NetworkSetup1Activity.class);
                        startActivity(intent);
                        finish();
                    }
                }).create();
        mAlertDialog.show();
    }
}
