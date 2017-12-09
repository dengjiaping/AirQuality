package com.honeywell.iaq.activity;

import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.widget.MessageBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Jin on 06/09/2017.
 */

public class StandbyActivity extends IAQTitleBarActivity implements View.OnClickListener {

    private static final String TAG = "StandbyActivity";

    private static final String MODE_1 = "1";
    private static final String MODE_2 = "2";

    private ImageView mParameterImageView;
    private ImageView mTimeImageView;
    private ImageView mChooseParameterImageView;
    private ImageView mChooseTimeImageView;
    private TextView mChooseParameterTextView;
    private TextView mChooseTimeTextView;
    private Button mSaveButton;

    private boolean mIsChooseTime = false;
    private String mDeviceId;
    private String mScreen;


    @Override
    protected int getContent() {
        return R.layout.activity_standby;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(getString(R.string.standby_display_content));
    }

    @Override
    protected void initView() {
        super.initView();

        mDeviceId = getIntent().getStringExtra(MyIaqActivity2.INTENT_DEVICE_ID);

        mParameterImageView = (ImageView) findViewById(R.id.parameter_iv);
        mTimeImageView = (ImageView) findViewById(R.id.time_iv);
        mChooseParameterImageView = (ImageView) findViewById(R.id.choose_parameter_iv);
        mChooseTimeImageView = (ImageView) findViewById(R.id.choose_time_iv);
        mChooseParameterTextView = (TextView) findViewById(R.id.choose_parameter_tv);
        mChooseTimeTextView = (TextView) findViewById(R.id.choose_time_tv);
        mSaveButton = (Button) findViewById(R.id.btn_save_choose);

        mChooseParameterImageView.setBackgroundResource(R.mipmap.ic_radio_selected);
        mChooseTimeImageView.setBackgroundResource(R.mipmap.ic_radio_nomal);


        mParameterImageView.setOnClickListener(this);
        mTimeImageView.setOnClickListener(this);
        mChooseParameterImageView.setOnClickListener(this);
        mChooseTimeImageView.setOnClickListener(this);
        mChooseParameterTextView.setOnClickListener(this);
        mChooseTimeTextView.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mChooseParameterImageView || v == mChooseParameterTextView || v == mParameterImageView) {
            if (mIsChooseTime) {
                setParameterView();
            }
        } else if (v == mChooseTimeImageView || v == mChooseTimeTextView || v == mTimeImageView) {
            if (!mIsChooseTime) {
                setTimeView();
            }
        } else if (v == mSaveButton) {
            setScreenMode(mIsChooseTime ? MODE_2 : MODE_1);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        getDeviceInformation();

        if ("1".equals(mScreen)) {
            setParameterView();
        } else if ("2".equals(mScreen)) {
            setTimeView();
        }
    }

    private void setScreenMode(String mode) {
        showLoadingDialog();

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_STANDBY_SCREEN);
        params.put(Constants.KEY_DEVICE_ID, mDeviceId);
        params.put(Constants.KEY_STANDBY_MODE, mode);
        HttpUtils.getString(this, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_SET_STANDBY_SCREEN, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode == 0) {
                    finish();
                } else {
                    MessageBox.createSimpleDialog(StandbyActivity.this, null, getString(R.string.setting_fail), null, null);
                }

            }
        }));
    }

    private void getDeviceInformation() {
        String account = Utils.getSharedPreferencesValue(this, Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, mDeviceId};
        Cursor cur = this.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            mScreen = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN));
            cur.moveToNext();
        }
        cur.close();
    }

    private void setParameterView() {
        mIsChooseTime = false;
        mChooseParameterImageView.setBackgroundResource(R.mipmap.ic_radio_selected);
        mChooseTimeImageView.setBackgroundResource(R.mipmap.ic_radio_nomal);
    }

    private void setTimeView() {
        mIsChooseTime = true;
        mChooseParameterImageView.setBackgroundResource(R.mipmap.ic_radio_nomal);
        mChooseTimeImageView.setBackgroundResource(R.mipmap.ic_radio_selected);
    }
}
