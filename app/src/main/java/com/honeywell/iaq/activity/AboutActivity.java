package com.honeywell.iaq.activity;

import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.lib.utils.ApplicationUtil;

/**
 * Created by E570281 on 12/26/2016.
 */
public class AboutActivity extends IAQTitleBarActivity {

    @Override
    protected int getContent() {
        return R.layout.activity_about;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.about);
    }

    @Override
    protected void initView() {
        super.initView();
        TextView version = (TextView) findViewById(R.id.version);
        version.setText(ApplicationUtil.getCurrentVersionName(getApplicationContext()));
    }

}
