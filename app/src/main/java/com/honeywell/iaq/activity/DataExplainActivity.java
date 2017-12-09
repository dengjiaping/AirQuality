package com.honeywell.iaq.activity;

import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.lib.utils.ApplicationUtil;

/**
 * Created by Jin on 12/26/2016.
 */
public class DataExplainActivity extends IAQTitleBarActivity {

    @Override
    protected int getContent() {
        return R.layout.activity_data_explain;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.data_explain);
    }

    @Override
    protected void initView() {
        super.initView();

    }

}
