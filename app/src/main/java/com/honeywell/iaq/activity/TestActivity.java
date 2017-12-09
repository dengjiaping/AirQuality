package com.honeywell.iaq.activity;

import android.app.Activity;
import android.os.Bundle;

import com.honeywell.iaq.R;

/**
 * Created by zhujunyu on 2017/3/24.
 */

public class TestActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
