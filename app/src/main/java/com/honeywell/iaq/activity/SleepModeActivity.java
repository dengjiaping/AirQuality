package com.honeywell.iaq.activity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.clock.model.ClockFormat;
import com.honeywell.iaq.clock.model.ClockModel;
import com.honeywell.iaq.clock.view.wheel.ArrayWheelAdapter;
import com.honeywell.iaq.clock.view.wheel.NumericWheelAdapter;
import com.honeywell.iaq.clock.view.wheel.OnWheelChangedListener;
import com.honeywell.iaq.clock.view.wheel.WheelView;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.widget.MessageBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Jin on 06/09/2017.
 */

public class SleepModeActivity extends IAQTitleBarActivity implements View.OnClickListener {

    private static final String TAG = "SleepModeActivity";

    private String mDeviceId;

    private WheelView mStartHourWheel;
    private WheelView mStartMinuteWheel;
    private WheelView mStartNoonWheel;
    private WheelView mStopHourWheel;
    private WheelView mStopMinuteWheel;
    private WheelView mStopNoonWheel;

    private RelativeLayout mStartTimeLayout;
    private RelativeLayout mStopTimeLayout;
    private LinearLayout mStartClockLayout;
    private LinearLayout mStopClockLayout;
    private ImageView mStartArrowImageView;
    private ImageView mStopArrowImageView;
    private TextView mStartTimeTextView;
    private TextView mStopTimeTextView;
    private TextView mStartNoonTextView;
    private TextView mStopNoonTextView;
    private Button mSaveButton;

    private boolean mIsStartClockOpen = false;
    private boolean mIsStopClockOpen = false;
    private ClockModel mStartClockModel;
    private ClockModel mStopClockModel;

    @Override
    protected int getContent() {
        return R.layout.activity_sleep_mode;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(getString(R.string.select_time_period));
    }

    @Override
    protected void initView() {
        super.initView();

        mDeviceId = getIntent().getStringExtra(MyIaqActivity2.INTENT_DEVICE_ID);

        mStartHourWheel = (WheelView) findViewById(R.id.hour_wheel_start);
        mStartMinuteWheel = (WheelView) findViewById(R.id.minute_wheel_start);
        mStartNoonWheel = (WheelView) findViewById(R.id.noon_wheel_start);
        mStopHourWheel = (WheelView) findViewById(R.id.hour_wheel_stop);
        mStopMinuteWheel = (WheelView) findViewById(R.id.minute_wheel_stop);
        mStopNoonWheel = (WheelView) findViewById(R.id.noon_wheel_stop);

        mStartTimeLayout = (RelativeLayout) findViewById(R.id.start_time_layout);
        mStopTimeLayout = (RelativeLayout) findViewById(R.id.stop_time_layout);
        mStartArrowImageView = (ImageView) findViewById(R.id.start_arrow_iv);
        mStopArrowImageView = (ImageView) findViewById(R.id.stop_arrow_iv);
        mStartArrowImageView.setBackgroundResource(R.mipmap.drag_down);
        mStopArrowImageView.setBackgroundResource(R.mipmap.drag_down);
        mStartTimeTextView = (TextView) findViewById(R.id.start_time_tv);
        mStopTimeTextView = (TextView) findViewById(R.id.stop_time_tv);
        mStartNoonTextView = (TextView) findViewById(R.id.start_noon_tv);
        mStopNoonTextView = (TextView) findViewById(R.id.stop_noon_tv);
        mSaveButton = (Button) findViewById(R.id.btn_save_time);
        mSaveButton.setEnabled(true);

        mStartClockLayout = (LinearLayout) findViewById(R.id.clock_wheel_start);
        mStopClockLayout = (LinearLayout) findViewById(R.id.clock_wheel_stop);
        mStartTimeLayout.setOnClickListener(this);
        mStopTimeLayout.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);

        try {
            mStartClockModel = (ClockModel) getIntent().getSerializableExtra(MyIaqActivity2.INTENT_CLOCK_START_TIME);
            mStopClockModel = (ClockModel) getIntent().getSerializableExtra(MyIaqActivity2.INTENT_CLOCK_STOP_TIME);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mStartTimeLayout) {
            mIsStartClockOpen = !mIsStartClockOpen;
            mStartClockLayout.setVisibility(mIsStartClockOpen? View.VISIBLE:View.GONE);
            mStartArrowImageView.setBackgroundResource(mIsStartClockOpen? R.mipmap.drag_up:R.mipmap.drag_down);
        } else if (v == mStopTimeLayout) {
            mIsStopClockOpen = !mIsStopClockOpen;
            mStopClockLayout.setVisibility(mIsStopClockOpen? View.VISIBLE:View.GONE);
            mStopArrowImageView.setBackgroundResource(mIsStopClockOpen? R.mipmap.drag_up:R.mipmap.drag_down);
        } else if (v == mSaveButton) {
            showLoadingDialog();

            Map<String, String> params = new HashMap<>();
            params.put(Constants.KEY_TYPE, Constants.TYPE_SET_SLEEPMODE);
            params.put(Constants.KEY_DEVICE_ID, mDeviceId);
            params.put(Constants.KEY_SLEEP_MODE, "on");
            params.put(Constants.KEY_SLEEP_START, mStartClockModel.get24Time());
            params.put(Constants.KEY_SLEEP_END, mStopClockModel.get24Time());
            HttpUtils.getString(this, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_SLEEP_MODE, new IResponse() {
                @Override
                public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                    dismissLoadingDialog();
                    if (resultCode == 0) {
                        finish();
                    } else {
                        MessageBox.createSimpleDialog(SleepModeActivity.this, null, getString(R.string.setting_fail), null, null);
                    }

                }
            }));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mStartClockModel == null || mStopClockModel == null)
            return;

        initClockWheelView();

        mStartClockModel.switch12or24Format(ClockFormat.is24HourFormat(this));
        mStopClockModel.switch12or24Format(ClockFormat.is24HourFormat(this));
        initClockWheelViewFromClockModel();

        if (mStartClockModel != null && mStopClockModel != null) {
            mStartNoonTextView.setText(mStartClockModel.getNoon());
            mStopNoonTextView.setText(mStopClockModel.getNoon());
            mStartTimeTextView.setText(mStartClockModel.getTime());
            mStopTimeTextView.setText(mStopClockModel.getTime());
        }

        mStartHourWheel.showItem();
        mStartMinuteWheel.showItem();
        mStartNoonWheel.showItem();
        mStopHourWheel.showItem();
        mStopMinuteWheel.showItem();
        mStopNoonWheel.showItem();
    }

    private void initClockWheelView() {
        if (ClockFormat.is24HourFormat(this)) {
            mStartHourWheel.setAdapter(new NumericWheelAdapter(0, 23, "%02d"));
            mStartNoonWheel.setVisibility(View.INVISIBLE);

            mStopHourWheel.setAdapter(new NumericWheelAdapter(0, 23, "%02d"));
            mStopNoonWheel.setVisibility(View.INVISIBLE);
        } else {
            mStartHourWheel.setAdapter(new NumericWheelAdapter(1, 12, "%02d"));
            mStartNoonWheel.setVisibility(View.VISIBLE);
            mStartNoonWheel.setAdapter(new ArrayWheelAdapter<>(
                    new String[]{getString(R.string.morning), getString(R.string.afternoon)}, 2));
            mStartNoonWheel.setCyclic(false);

            mStopHourWheel.setAdapter(new NumericWheelAdapter(1, 12, "%02d"));
            mStopNoonWheel.setVisibility(View.VISIBLE);
            mStopNoonWheel.setAdapter(new ArrayWheelAdapter<>(
                    new String[]{getString(R.string.morning), getString(R.string.afternoon)}, 2));
            mStopNoonWheel.setCyclic(false);
        }

        mStartHourWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartHourWheel.showItem();
            }
        });
        mStartHourWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mStartTimeTextView.setText(getStartTimeFromWheel());
                mStartClockModel.setTime(getStartTimeFromWheel());
            }
        });

        mStopHourWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopHourWheel.showItem();
            }
        });
        mStopHourWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mStopTimeTextView.setText(getStopTimeFromWheel());
                mStopClockModel.setTime(getStopTimeFromWheel());
            }
        });

        mStartMinuteWheel.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
        mStartMinuteWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartMinuteWheel.showItem();
            }
        });
        mStartMinuteWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mStartTimeTextView.setText(getStartTimeFromWheel());
                mStartClockModel.setTime(getStartTimeFromWheel());
            }
        });

        mStopMinuteWheel.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
        mStopMinuteWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopMinuteWheel.showItem();
            }
        });
        mStopMinuteWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mStopTimeTextView.setText(getStopTimeFromWheel());
                mStopClockModel.setTime(getStopTimeFromWheel());
            }
        });

        mStartNoonWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartNoonWheel.showItem();
            }
        });
        mStartNoonWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mStartNoonTextView.setText(getStartNoonFromWheel());
                mStartClockModel.setNoon(getStartNoonFromWheel());
            }
        });

        mStopNoonWheel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopNoonWheel.showItem();
            }
        });
        mStopNoonWheel.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                mStopNoonTextView.setText(getStopNoonFromWheel());
                mStopClockModel.setNoon(getStopNoonFromWheel());
            }
        });
    }

    private void initClockWheelViewFromClockModel() {
        if (mStartClockModel.getTime() != null) {
            try {
                String[] time = mStartClockModel.getTime().split(":");
                if (ClockFormat.is24HourFormat(this))
                    mStartHourWheel.setCurrentItem(Integer.parseInt(time[0]));
                else
                    mStartHourWheel.setCurrentItem(Integer.parseInt(time[0]) - 1);
                mStartMinuteWheel.setCurrentItem(Integer.parseInt(time[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            String noon = mStartClockModel.getNoon();
            if (noon.equals("")) {
                mStartNoonWheel.setCurrentItem(2);
            } else if (noon.equals(getString(R.string.morning))) {
                mStartNoonWheel.setCurrentItem(0);
            } else if (noon.equals(getString(R.string.afternoon))) {
                mStartNoonWheel.setCurrentItem(1);
            }
        }

        if (mStopClockModel.getTime() != null) {
            try {
                String[] time = mStopClockModel.getTime().split(":");
                if (ClockFormat.is24HourFormat(this))
                    mStopHourWheel.setCurrentItem(Integer.parseInt(time[0]));
                else
                    mStopHourWheel.setCurrentItem(Integer.parseInt(time[0]) - 1);
                mStopMinuteWheel.setCurrentItem(Integer.parseInt(time[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }

            String noon = mStopClockModel.getNoon();
            if (noon.equals("")) {
                mStopNoonWheel.setCurrentItem(2);
            } else if (noon.equals(getString(R.string.morning))) {
                mStopNoonWheel.setCurrentItem(0);
            } else if (noon.equals(getString(R.string.afternoon))) {
                mStopNoonWheel.setCurrentItem(1);
            }
        }
    }

    private String getStartTimeFromWheel() {
        String time;
        if (ClockFormat.is24HourFormat(SleepModeActivity.this))
            time = (mStartHourWheel.getCurrentItem()) + ":";
        else
            time = (mStartHourWheel.getCurrentItem() + 1) + ":";
        if (mStartMinuteWheel.getCurrentItem() < 10)
            time += ("0" + mStartMinuteWheel.getCurrentItem());
        else
            time += mStartMinuteWheel.getCurrentItem();
        return time;
    }

    private String getStopTimeFromWheel() {
        String time;
        if (ClockFormat.is24HourFormat(SleepModeActivity.this))
            time = (mStopHourWheel.getCurrentItem()) + ":";
        else
            time = (mStopHourWheel.getCurrentItem() + 1) + ":";
        if (mStopMinuteWheel.getCurrentItem() < 10)
            time += ("0" + mStopMinuteWheel.getCurrentItem());
        else
            time += mStopMinuteWheel.getCurrentItem();
        return time;
    }

    private String getStartNoonFromWheel() {
        String noon = "";
        if (mStartNoonWheel.getCurrentItem() == 0) {
            noon = getString(R.string.morning);
        } else if (mStartNoonWheel.getCurrentItem() == 1) {
            noon = getString(R.string.afternoon);
        }
        if (ClockFormat.is24HourFormat(SleepModeActivity.this))
            noon = "";
        return noon;
    }

    private String getStopNoonFromWheel() {
        String noon = "";
        if (mStopNoonWheel.getCurrentItem() == 0) {
            noon = getString(R.string.morning);
        } else if (mStopNoonWheel.getCurrentItem() == 1) {
            noon = getString(R.string.afternoon);
        }
        if (ClockFormat.is24HourFormat(SleepModeActivity.this))
            noon = "";
        return noon;
    }

}
