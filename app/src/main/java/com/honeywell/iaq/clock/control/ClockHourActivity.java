package com.honeywell.iaq.clock.control;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.clock.model.ClockFormat;
import com.honeywell.iaq.clock.model.ClockFrequency;
import com.honeywell.iaq.clock.model.ClockJson;
import com.honeywell.iaq.clock.model.ClockModel;
import com.honeywell.iaq.clock.view.wheel.ArrayWheelAdapter;
import com.honeywell.iaq.clock.view.wheel.NumericWheelAdapter;
import com.honeywell.iaq.clock.view.wheel.WheelView;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.widget.MessageBox;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Jin on 06/09/2017.
 */

public class ClockHourActivity extends IAQTitleBarActivity implements View.OnClickListener {

    private static final String TAG = "ClockHourActivity";

    public static final int CHANGE_CLOCK_FREQ = 2;
    public static final String IS_CREATE_CLOCK = "com.honeywell.iaq.clock.control.IS_CREATE_CLOCK";
    public static final String INTENT_CLOCK_FREQ = "com.honeywell.iaq.clock.control.intent_clock_freq";
    public static final String INTENT_CLOCK_FREQ_BACK = "com.honeywell.iaq.clock.control.intent_clock_freq_back";

    private WheelView mHourWheel;
    private WheelView mMinuteWheel;
    private WheelView mNoonWheel;

    private Button mSaveClockButton;
    private TextView mRepeatTextView;
    private TextView mFreqTextView;
    private ImageView mForwardImageView;
    private RelativeLayout mFreqLayout;

    private ClockModel mClock;
    private String mDeviceId;


    @Override
    protected int getContent() {
        return R.layout.activity_clock_hour;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);

        if (getIntent().getBooleanExtra(IS_CREATE_CLOCK, false))
            title.setText(getString(R.string.create_clock));
        else
            title.setText(getString(R.string.edit_clock));

    }

    @Override
    protected void initView() {
        super.initView();

        mRepeatTextView = (TextView) findViewById(R.id.repeat_tv);
        mFreqTextView = (TextView) findViewById(R.id.freq_tv);
        mFreqTextView.setOnClickListener(this);
        mForwardImageView = (ImageView) findViewById(R.id.clock_forward);
        mForwardImageView.setOnClickListener(this);
        mFreqLayout = (RelativeLayout) findViewById(R.id.freq_layout);
        mFreqLayout.setOnClickListener(this);
        mSaveClockButton = (Button) findViewById(R.id.btn_save_clock);
        mSaveClockButton.setOnClickListener(this);
        mHourWheel = (WheelView) findViewById(R.id.hour_wheel);
        mMinuteWheel = (WheelView) findViewById(R.id.minute_wheel);
        mNoonWheel = (WheelView) findViewById(R.id.noon_wheel);
    }

    @Override
    public void onClick(View v) {
        if (v == mSaveClockButton) {
            if (mNoonWheel.getCurrentItem() == 2) {
                if (!ClockFormat.is24HourFormat(ClockHourActivity.this)) {
                    AlertDialog alertDialog = new AlertDialog.Builder(ClockHourActivity.this)
                            .setMessage(getString(R.string.choose_noon))
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).create();
                    alertDialog.show();
                    return;
                }
            }

            String time = getTimeFromWheel();
            String noon = getNoonFromWheel();
            mClock.setTime(time);
            mClock.setNoon(noon);

            handleSaveClick();

        } else if (v == mForwardImageView || v == mFreqTextView || v == mFreqLayout) {
            Intent intent = new Intent(ClockHourActivity.this, ClockWeekActivity.class);
            intent.putIntegerArrayListExtra(INTENT_CLOCK_FREQ, mClock.getFreqList());
            startActivityForResult(intent, CHANGE_CLOCK_FREQ);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClock = (ClockModel) getIntent().getSerializableExtra(ClockEditActivity.INTENT_ONE_CLOCK);
    }

    @Override
    protected void onResume() {
        super.onResume();


        initClockWheelView();

        String time = getTimeFromWheel();
        String noon = getNoonFromWheel();

        if (mClock == null) {
            mClock = new ClockModel(time, noon, initFreqList(), true, ClockFormat.is24HourFormat(ClockHourActivity.this));
        } else {
            mClock.switch12or24Format(ClockFormat.is24HourFormat(this));
            initClockWheelViewFromClockModel();
        }

        mHourWheel.showItem();
        mMinuteWheel.showItem();
        mNoonWheel.showItem();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 保存clock对象，用于处理更改系统时间格式的情况
        String time = getTimeFromWheel();
        String noon = getNoonFromWheel();
        mClock.setTime(time);
        mClock.setNoon(noon);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (resultCode) {
            case CHANGE_CLOCK_FREQ:
                ArrayList<Integer> freqList = intent.getIntegerArrayListExtra(INTENT_CLOCK_FREQ_BACK);
                String frequency = ClockFrequency.changeFreqListToString(freqList);
                mFreqTextView.setText(frequency);
                mClock.setFreqList(freqList);
                mClock.setFrequency(frequency);
                break;
            default:
                break;
        }
    }

    private ArrayList<Integer> initFreqList() {
        ArrayList<Integer> result = new ArrayList<>();
        result.add(0);
        result.add(1);
        result.add(2);
        result.add(3);
        result.add(4);
        result.add(5);
        result.add(6);
        return result;
    }

    private void initClockWheelView() {
        if (ClockFormat.is24HourFormat(this)) {
            mHourWheel.setAdapter(new NumericWheelAdapter(0, 23, "%02d"));
            mNoonWheel.setVisibility(View.INVISIBLE);
        } else {
            mHourWheel.setAdapter(new NumericWheelAdapter(1, 12, "%02d"));
            mNoonWheel.setVisibility(View.VISIBLE);
            mNoonWheel.setAdapter(new ArrayWheelAdapter<>(
                    new String[]{getString(R.string.morning), getString(R.string.afternoon)}, 2));
            mNoonWheel.setCyclic(false);
        }
        mHourWheel.setCurrentItem(6);
        mHourWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHourWheel.showItem();
            }
        });
        mMinuteWheel.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
        mMinuteWheel.setCurrentItem(30);
        mMinuteWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMinuteWheel.showItem();
            }
        });
        mNoonWheel.setCurrentItem(0);
        mNoonWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoonWheel.showItem();
            }
        });
    }

    private void initClockWheelViewFromClockModel() {
        mFreqTextView.setText(mClock.getFrequency());

        if (mClock.isRepeat())
            mRepeatTextView.setText(getString(R.string.repeat));
        else
            mRepeatTextView.setText("");

        if (mClock.getTime() != null) {
            try {
                String[] time = mClock.getTime().split(":");
                if (ClockFormat.is24HourFormat(this))
                    mHourWheel.setCurrentItem(Integer.parseInt(time[0]));
                else
                    mHourWheel.setCurrentItem(Integer.parseInt(time[0]) - 1);
                mMinuteWheel.setCurrentItem(Integer.parseInt(time[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String noon = mClock.getNoon();
        if (noon.equals("")) {
            mNoonWheel.setCurrentItem(2);
        } else if (noon.equals(getString(R.string.morning))) {
            mNoonWheel.setCurrentItem(0);
        } else if (noon.equals(getString(R.string.afternoon))) {
            mNoonWheel.setCurrentItem(1);
        }
    }

    private String getTimeFromWheel() {
        String time;
        if (ClockFormat.is24HourFormat(ClockHourActivity.this))
            time = (mHourWheel.getCurrentItem()) + ":";
        else
            time = (mHourWheel.getCurrentItem() + 1) + ":";
        if (mMinuteWheel.getCurrentItem() < 10)
            time += ("0" + mMinuteWheel.getCurrentItem());
        else
            time += mMinuteWheel.getCurrentItem();
        return time;
    }

    private String getNoonFromWheel() {
        String noon = "";
        if (mNoonWheel.getCurrentItem() == 0) {
            noon = getString(R.string.morning);
        } else if (mNoonWheel.getCurrentItem() == 1) {
            noon = getString(R.string.afternoon);
        }
        if (ClockFormat.is24HourFormat(ClockHourActivity.this))
            noon = "";
        return noon;
    }

    private void handleSaveClick() {
        try {
            // 来自ClockEditActivity
            if (getIntent().getSerializableExtra(ClockSettingActivity.INTENT_CLOCK_LIST) == null) {
                mDeviceId = getIntent().getStringExtra(ClockEditActivity.INTENT_EDIT_CLOCK_DEVICE_ID);

                if (getIntent().getBooleanExtra(IS_CREATE_CLOCK, false)) {
                    ArrayList<ClockModel> clocks = (ArrayList<ClockModel>) getIntent().getSerializableExtra(ClockEditActivity.INTENT_EDIT_CLOCK_LIST);
                    updateClockApi(clocks);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(ClockEditActivity.INTENT_CHANGE_CLOCK, mClock);
                    setResult(ClockEditActivity.CHANGE_CLOCK_MODEL, intent);
                    finish();
                }

            } else {
                // 来自ClockSettingActivity
                mDeviceId = getIntent().getStringExtra(ClockSettingActivity.INTENT_CLOCK_DEVICE_ID);
                ArrayList<ClockModel> clocks = (ArrayList<ClockModel>) getIntent().getSerializableExtra(ClockSettingActivity.INTENT_CLOCK_LIST);
                updateClockApi(clocks);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateClockApi(ArrayList<ClockModel> clocks) {
        showLoadingDialog();
        clocks.add(mClock);

        List<ClockJson> clockJsons = new ArrayList<>();
        for (ClockModel clockModel : clocks) {
            ClockJson clockJson = new ClockJson();
            clockJson.setTime(clockModel.get24Time());
            clockJson.setActivate(clockModel.isActive() ? ClockJson.SWITCH_ON : ClockJson.SWITCH_OFF);
            clockJson.setDay(clockModel.getFreqList());
            clockJsons.add(clockJson);
        }

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_SET_CLOCK);
        params.put(Constants.KEY_DEVICE_ID, mDeviceId);
        params.put(Constants.KEY_CLOCKS, new Gson().toJson(clockJsons));
        HttpUtils.getString(this, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_SET_CLOCK, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode == 0) {
                    finish();
                } else {
                    MessageBox.createSimpleDialog(ClockHourActivity.this, null, getString(R.string.setting_fail), null, null);
                }
            }
        }));


    }

}
