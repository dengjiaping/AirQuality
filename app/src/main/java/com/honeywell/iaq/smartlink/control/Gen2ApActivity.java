package com.honeywell.iaq.smartlink.control;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.NetworkSetup5Activity;
import com.honeywell.iaq.base.IAQTitleBarActivity;


/**
 * Created by Jin on 04/09/2017.
 */

public class Gen2ApActivity extends IAQTitleBarActivity {

    private static final String TAG = "Gen2ApActivity";

    private Button mNextButton;

    @Override
    protected int getContent() {
        return R.layout.activity_gen2_ap;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.config_iaq);
    }

    @Override
    protected void initView() {
        super.initView();

        mNextButton = (Button) findViewById(R.id.btn_connect);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Gen2ApActivity.this, NetworkSetup5Activity.class);
                startActivity(intent);
            }
        });
    }

}
