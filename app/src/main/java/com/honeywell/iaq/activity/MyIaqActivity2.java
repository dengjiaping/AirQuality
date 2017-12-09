package com.honeywell.iaq.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.iaq.R;

import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.clock.control.ClockSettingActivity;
import com.honeywell.iaq.clock.model.ClockFormat;
import com.honeywell.iaq.clock.model.ClockModel;
import com.honeywell.iaq.base.IAQType;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.events.IAQEnvironmentDetailEvent;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQGetDataFromWsEvent;
import com.honeywell.iaq.events.IAQTemperatureEvent;
import com.honeywell.iaq.fragment.EnvironmentDetialFragment3;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.widget.MessageBox;
import com.honeywell.net.utils.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

public class MyIaqActivity2 extends IAQTitleBarActivity implements View.OnClickListener {

    private static final String TAG = "MyIaq";

    public static final String INTENT_CLOCK_START_TIME = "com.honeywell.iaq.activity.INTENT_CLOCK_START_TIME";
    public static final String INTENT_CLOCK_STOP_TIME = "com.honeywell.iaq.activity.INTENT_CLOCK_STOP_TIME";
    public static final String INTENT_DEVICE_ID = "com.honeywell.iaq.activity.INTENT_DEVICE_ID";

    private static String serialNum, home, room, location;
    private static String sleepStatus, sleepStart, sleepStop, savePowerStatus, standbyScreen, tempUnit;

    private static String deviceId;

    private TextView mTvSerialNum;
    private TextView mEditHomeTextView;
    private Button mBtnRemove;
    private RelativeLayout mEditHomeLayout;
    private RelativeLayout mClockLayout;
    private RelativeLayout mStandbyLayout;
    private RelativeLayout mSleepModeLayout;
    private RelativeLayout mSavePowerLayout;
    private CheckBox mCheckBoxSleepMode;
    private CheckBox mSavePowerModeCheckbox;
    private CheckBox mCheckBoxF;
    private CheckBox mCheckBoxC;
    private TextView mStartStopTime;
    private TextView mSleepModeIndicateTextView;
    private ImageView mSleepModeForwardImageView;
    public static int sSupport_TYPE;
    public static final int MIN_CLICK_DELAY_TIME = 5000;
    private long lastClickTime = 0;
    private Toast mToast;
    private int devieceStutus;
    private ClockModel mStartClock;
    private ClockModel mStopClock;

    private View mLineStandby, mLineClock, mLineSavepower;
    private IntentFilter filter;
    private DashboardReceiver mReceiver;


    @Override
    protected int getContent() {
        return R.layout.activity_my_iaq_setting;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.iaq_settings);
    }

    @Override
    protected void initView() {
        super.initView();

        mReceiver = new DashboardReceiver();
        filter = new IntentFilter();
        filter.addAction(Constants.ACTION_WSS_CONNECTED);
        filter.addAction(Constants.ACTION_WSS_CONNECT_FAIL);
        filter.addAction(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
        filter.addAction(Constants.ACTION_LOGOUT_FAIL);
        filter.addAction(Constants.ACTION_INVALID_NETWORK);
        registerReceiver(mReceiver, filter);


        serialNum = getIntent().getStringExtra(Constants.KEY_DEVICE_SERIAL);
        home = getIntent().getStringExtra(Constants.KEY_HOME);
        room = getIntent().getStringExtra(Constants.KEY_ROOM);
        deviceId = getIntent().getStringExtra(Constants.KEY_DEVICE_ID);

        mStartStopTime = (TextView) findViewById(R.id.tv_start_stop_time);
        mSleepModeIndicateTextView = (TextView) findViewById(R.id.tv_shut_down_led);
        mSleepModeForwardImageView = (ImageView) findViewById(R.id.sleep_mode_forward);

        mLineStandby = findViewById(R.id.line_standby);
        mLineClock = findViewById(R.id.line_clock);
        mLineSavepower = findViewById(R.id.line_save_power_mode);
        mTvSerialNum = (TextView) findViewById(R.id.device_serialNum);
        mBtnRemove = (Button) findViewById(R.id.btn_remove_device);
        mBtnRemove.setOnClickListener(this);
        mEditHomeLayout = (RelativeLayout) findViewById(R.id.edit_home_layout);
        mEditHomeLayout.setOnClickListener(this);
        mClockLayout = (RelativeLayout) findViewById(R.id.clock_setting_layout);
        mClockLayout.setOnClickListener(this);
        mStandbyLayout = (RelativeLayout) findViewById(R.id.standby_layout);
        mStandbyLayout.setOnClickListener(this);
        mSavePowerLayout = (RelativeLayout) findViewById(R.id.rl_save_power_mode);
        mSleepModeLayout = (RelativeLayout) findViewById(R.id.rl_sleep_mode);
        mSleepModeLayout.setOnClickListener(this);
        mEditHomeTextView = (TextView) findViewById(R.id.edit_home_tv);
        mCheckBoxSleepMode = (CheckBox) findViewById(R.id.cb_sleep_mode);
        mCheckBoxSleepMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (!mCheckBoxSleepMode.isPressed())
                    return;

                long currentTime = Calendar.getInstance().getTimeInMillis();
                Log.e(TAG, "" + (currentTime - lastClickTime));
                //判断设备在线状态
                if (devieceStutus == Constants.DEVICE_OFFLINE) {
                    mCheckBoxSleepMode.setChecked(!isChecked);
                    showToast(MyIaqActivity2.this.getResources().getString(R.string.iaq_set_sleep_mode_failed));
                    return;
                }

                if (currentTime - lastClickTime < MIN_CLICK_DELAY_TIME) {
                    showToast(MyIaqActivity2.this.getResources().getString(R.string.click_too_frequently));
                    mCheckBoxSleepMode.setChecked(!isChecked);
                    return;
                }

                if (Constants.GEN_2.equals(IAQType.getGeneration(MyIaqActivity2.this))) {
                    if (isChecked) {
                        mStartStopTime.setTextColor(getResources().getColor(R.color.text_73));
                        mSleepModeLayout.setClickable(true);
                        mSleepModeForwardImageView.setVisibility(View.VISIBLE);

                    } else {
                        mStartStopTime.setTextColor(getResources().getColor(R.color.text_color_hint));
                        mSleepModeLayout.setClickable(false);
                        mSleepModeForwardImageView.setVisibility(View.INVISIBLE);
                    }
                }

                lastClickTime = currentTime;

                setSleepMode(isChecked);
            }
        });

        mSavePowerModeCheckbox = (CheckBox) findViewById(R.id.cb_save_power_mode);
        mSavePowerModeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!mSavePowerModeCheckbox.isPressed())
                    return;

                long currentTime = Calendar.getInstance().getTimeInMillis();
                Log.e(TAG, "" + (currentTime - lastClickTime));
                //判断设备在线状态
                if (devieceStutus == Constants.DEVICE_OFFLINE) {
                    mSavePowerModeCheckbox.setChecked(!isChecked);
                    showToast(MyIaqActivity2.this.getResources().getString(R.string.iaq_set_sleep_mode_failed));
                    return;
                }

                if (currentTime - lastClickTime < MIN_CLICK_DELAY_TIME) {
                    showToast(MyIaqActivity2.this.getResources().getString(R.string.click_too_frequently));
                    mSavePowerModeCheckbox.setChecked(!isChecked);
                    return;
                }

                lastClickTime = currentTime;

                setSavePowerMode(isChecked);
            }
        });

        getDeviceInformation();

        getSleepMode();
        mCheckBoxC = (CheckBox) findViewById(R.id.cb_celsius);
        mCheckBoxF = (CheckBox) findViewById(R.id.cb_fahrenheit);

        sSupport_TYPE = IAQType.getSupportNewParameter(MyIaqActivity2.this);

        if (Constants.GEN_1.equals(IAQType.getGeneration(this))) {
            mStandbyLayout.setVisibility(View.GONE);
            mSavePowerLayout.setVisibility(View.GONE);
            mClockLayout.setVisibility(View.GONE);
            mSleepModeIndicateTextView.setVisibility(View.GONE);
            mStartStopTime.setVisibility(View.GONE);
            mSleepModeForwardImageView.setVisibility(View.GONE);
            mLineClock.setVisibility(View.GONE);
            mLineSavepower.setVisibility(View.GONE);
            mLineStandby.setVisibility(View.GONE);
            mSleepModeLayout.setClickable(false);

            if (Utils.isCelsius(getApplicationContext())) {
                mCheckBoxC.setChecked(true);
                mCheckBoxF.setChecked(false);
            } else {
                mCheckBoxC.setChecked(false);
                mCheckBoxF.setChecked(true);
            }

            switch (sSupport_TYPE) {
                case EnvironmentDetialFragment3.HCHO_ONLY:
                    mSleepModeLayout.setVisibility(View.VISIBLE);
                    break;
                case EnvironmentDetialFragment3.CO2_ONLY:
                    mSleepModeLayout.setVisibility(View.VISIBLE);
                    break;
                case EnvironmentDetialFragment3.TVOC_ONLY:
                    mSleepModeLayout.setVisibility(View.VISIBLE);
                    break;
                case EnvironmentDetialFragment3.HCHO_CO2:
                    mSleepModeLayout.setVisibility(View.VISIBLE);
                    break;
                case EnvironmentDetialFragment3.HCHO_TVOC:
                    mSleepModeLayout.setVisibility(View.VISIBLE);
                    break;
                case EnvironmentDetialFragment3.CO2_TVOC:
                    mSleepModeLayout.setVisibility(View.VISIBLE);
                    break;
                case EnvironmentDetialFragment3.HCHO_CO2_TVOC:
                    mSleepModeLayout.setVisibility(View.VISIBLE);
                    break;
                case EnvironmentDetialFragment3.HCHO_CO2_TVOC_NONE:
                    mSleepModeLayout.setVisibility(View.GONE);
                    break;
            }
        } else if (Constants.GEN_2.equals(IAQType.getGeneration(this))) {
//            mLineClock.setVisibility(View.VISIBLE);
            mLineClock.setVisibility(View.GONE);
            mLineSavepower.setVisibility(View.VISIBLE);
            mLineStandby.setVisibility(View.VISIBLE);
            mStandbyLayout.setVisibility(View.VISIBLE);
            mSavePowerLayout.setVisibility(View.VISIBLE);
            mSleepModeLayout.setVisibility(View.VISIBLE);
//            mClockLayout.setVisibility(View.VISIBLE);
            mClockLayout.setVisibility(View.GONE);
            mSleepModeLayout.setClickable(true);
            mSleepModeIndicateTextView.setVisibility(View.VISIBLE);
            mStartStopTime.setVisibility(View.VISIBLE);
            mSleepModeForwardImageView.setVisibility(View.VISIBLE);

            if (Constants.TEMPERATURE_UNIT_C.equals(tempUnit)) {
                mCheckBoxC.setChecked(true);
                mCheckBoxF.setChecked(false);
            } else if (Constants.TEMPERATURE_UNIT_F.equals(tempUnit)) {
                mCheckBoxC.setChecked(false);
                mCheckBoxF.setChecked(true);
            }

        }

        mCheckBoxC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckBoxC.setChecked(true);
                mCheckBoxF.setChecked(false);

                if (Constants.GEN_1.equals(IAQType.getGeneration(MyIaqActivity2.this))) {
                    Utils.setSharedPreferencesValue(MyIaqActivity2.this, Constants.KEY_TYPE_TEMP, Constants.KEY_CELSIUS);
                    EventBus.getDefault().post(new IAQTemperatureEvent());
                } else if (Constants.GEN_2.equals(IAQType.getGeneration(MyIaqActivity2.this))) {
                    setTemperatureMode(true);
                    Utils.setSharedPreferencesValue(MyIaqActivity2.this, Constants.KEY_TYPE_TEMP, Constants.KEY_CELSIUS);
                    EventBus.getDefault().post(new IAQTemperatureEvent());
                }
            }
        });

        mCheckBoxF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckBoxF.setChecked(true);
                mCheckBoxC.setChecked(false);

                if (Constants.GEN_1.equals(IAQType.getGeneration(MyIaqActivity2.this))) {
                    Utils.setSharedPreferencesValue(MyIaqActivity2.this, Constants.KEY_TYPE_TEMP, Constants.KEY_FAHRENHEIT);
                    EventBus.getDefault().post(new IAQTemperatureEvent());
                } else if (Constants.GEN_2.equals(IAQType.getGeneration(MyIaqActivity2.this))) {
                    setTemperatureMode(false);
                    Utils.setSharedPreferencesValue(MyIaqActivity2.this, Constants.KEY_TYPE_TEMP, Constants.KEY_FAHRENHEIT);
                    EventBus.getDefault().post(new IAQTemperatureEvent());
                }
            }
        });

    }

    private void getDeviceInformation() {
        String account = Utils.getSharedPreferencesValue(this, Constants.KEY_ACCOUNT, "");
        Log.d(TAG, "getDeviceInformation: serialNum=" + serialNum);
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, serialNum};
        Cursor cur = this.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            home = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HOME));
            room = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ROOM));
            location = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_LOCATION));
            devieceStutus = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
            sleepStatus = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SLEEP));
            sleepStart = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START));
            sleepStop = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP));
            savePowerStatus = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER));
            standbyScreen = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN));
            tempUnit = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT));

            Log.d(TAG, "Room=" + room + ", home=" + home);
            Log.e(TAG, "tempUnit=" + tempUnit);

            cur.moveToNext();
        }
        cur.close();

        setViewData();
    }

    private void setViewData() {
        mTvSerialNum.setText(serialNum);

        mEditHomeTextView.setText(location + "  " + home + "  " + room);
        mCheckBoxSleepMode.setChecked("on".equals(sleepStatus));
        mSavePowerModeCheckbox.setChecked("on".equals(savePowerStatus));

        if (Constants.GEN_2.equals(IAQType.getGeneration(MyIaqActivity2.this))) {
            if ("on".equals(sleepStatus)) {
                mStartStopTime.setTextColor(getResources().getColor(R.color.text_73));
                mSleepModeLayout.setClickable(true);
                mSleepModeForwardImageView.setVisibility(View.VISIBLE);

            } else {
                mStartStopTime.setTextColor(getResources().getColor(R.color.text_color_hint));
                mSleepModeLayout.setClickable(false);
                mSleepModeForwardImageView.setVisibility(View.INVISIBLE);
            }
        }


        ArrayList<Integer> days = new ArrayList<>();
        days.add(0);
        mStartClock = new ClockModel(0, sleepStart, days, true, ClockFormat.is24HourFormat(this));
        mStopClock = new ClockModel(0, sleepStop, days, true, ClockFormat.is24HourFormat(this));

        // In case user change 12/24 format
        mStartClock.switch12or24Format(ClockFormat.is24HourFormat(this));
        mStopClock.switch12or24Format(ClockFormat.is24HourFormat(this));

        mStartStopTime.setText(mStartClock.getNoon() + mStartClock.getTime() + "-"
                + mStopClock.getNoon() + mStopClock.getTime());

    }

    private void updateDeviceInformationToDb() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};
        ContentValues cv = new ContentValues();
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_HOME, home);
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_ROOM, room);
        getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

        Intent intent = new Intent(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
        sendBroadcast(intent);
        EventBus.getDefault().post(new IAQEnvironmentDetailEvent(IAQEnvironmentDetailEvent.MODIFY_HOME_ROME_NAME, true, null));
    }

    private void removeDeviceFromDb() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, serialNum};
        getContentResolver().delete(IAQ.BindDevice.DICT_CONTENT_URI, selection, selectionArgs);
    }

    private void unbindDevice() {
        showLoadingDialog();

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_UNBIND_DEVICE);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(MyIaqActivity2.this, Constants.BIND_DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(MyIaqActivity2.this, Constants.GetDataFlag.HON_IAQ_UNBIND_DEVICE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            removeDeviceFromDb();
                            Intent intent = new Intent(MyIaqActivity2.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 1000);


                } else {
                    Utils.showToast(MyIaqActivity2.this, getString(R.string.remove_iaq_fail));
                }
            }
        }));
    }


    private void setSleepMode(boolean isSleepmode) {
        showLoadingDialog();

        String sleepMode = "";
        if (isSleepmode) {
            sleepMode = "on";
        } else {
            sleepMode = "off";
        }

        Map<String, String> params = new HashMap<>();

        if (IAQType.getGeneration(this).equals(Constants.GEN_1)) {
            params.put(Constants.KEY_TYPE, Constants.TYPE_SET_SLEEPMODE);
            params.put(Constants.KEY_DEVICE_ID, deviceId);
            params.put(Constants.KEY_SLEEP_MODE, sleepMode);
        } else if (IAQType.getGeneration(this).equals(Constants.GEN_2)) {
            params.put(Constants.KEY_TYPE, Constants.TYPE_SET_SLEEPMODE);
            params.put(Constants.KEY_DEVICE_ID, deviceId);
            params.put(Constants.KEY_SLEEP_MODE, sleepMode);
            params.put(Constants.KEY_SLEEP_START, mStartClock.get24Time());
            params.put(Constants.KEY_SLEEP_END, mStopClock.get24Time());
        }

        HttpUtils.getString(MyIaqActivity2.this, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(MyIaqActivity2.this, Constants.GetDataFlag.HON_IAQ_SLEEP_MODE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode != 0) {
                    MessageBox.createSimpleDialog(MyIaqActivity2.this, null, getString(R.string.setting_fail), null, null);
                }
            }
        }));
    }

    private void setSavePowerMode(boolean isSavePower) {
        showLoadingDialog();

        String mode = "";
        if (isSavePower) {
            mode = "on";
        } else {
            mode = "off";
        }

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_SET_SAVE_POWER);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        params.put(Constants.KEY_SAVE_POWER_MODE, mode);
        HttpUtils.getString(MyIaqActivity2.this, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(MyIaqActivity2.this, Constants.GetDataFlag.HON_IAQ_SAVE_POWER_MODE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();

                if (resultCode != 0) {
                    MessageBox.createSimpleDialog(MyIaqActivity2.this, null, getString(R.string.setting_fail), null, null);
                }
            }
        }));
    }


    public void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(MyIaqActivity2.this, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnRemove) {
            AlertDialog mAlertDialog = new AlertDialog.Builder(MyIaqActivity2.this).setTitle(getString(R.string.iaq_settings)).setMessage(getString(R.string.confirm_remove_iaq)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Utils.isNetworkAvailable(MyIaqActivity2.this)) {
                        unbindDevice();
                    } else {
                        Utils.showToast(MyIaqActivity2.this, getString(R.string.no_network));
                    }
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create();
            mAlertDialog.show();
        } else if (v == mClockLayout) {
            Intent intent = new Intent(MyIaqActivity2.this, ClockSettingActivity.class);
            intent.putExtra(INTENT_DEVICE_ID, deviceId);
            startActivity(intent);
        } else if (v == mStandbyLayout) {
            Intent intent = new Intent(MyIaqActivity2.this, StandbyActivity.class);
            intent.putExtra(INTENT_DEVICE_ID, deviceId);
            startActivity(intent);
        } else if (v == mSleepModeLayout) {
            Intent intent = new Intent(MyIaqActivity2.this, SleepModeActivity.class);
            intent.putExtra(INTENT_CLOCK_START_TIME, mStartClock);
            intent.putExtra(INTENT_CLOCK_STOP_TIME, mStopClock);
            intent.putExtra(INTENT_DEVICE_ID, deviceId);
            startActivity(intent);
        } else if (v == mEditHomeLayout) {
            Intent intent = new Intent(MyIaqActivity2.this, EditHomeActivity.class);
            intent.putExtra(Constants.KEY_HOME, home);
            intent.putExtra(Constants.KEY_ROOM, room);
            intent.putExtra(Constants.KEY_LOCATION, location);
            startActivity(intent);
        }
    }

    private void getSleepMode() {
        String currentSerialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);

        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Logger.d(TAG, "setData: currentSerialNum=" + currentSerialNum);
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Logger.d(TAG, "setData: Count=" + dbCount);

        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {

            int onlineStatus = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
            Logger.d(TAG, "setData: onlineStatus=" + onlineStatus);
            if (onlineStatus == Constants.DEVICE_ONLINE) {
                String sleepMode = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SLEEP));
                Log.e(TAG, "sleepStatus mode " + sleepMode);

                if ("Off".equals(sleepMode)) {
                    mCheckBoxSleepMode.setChecked(false);
                } else if ("On".equals(sleepMode)) {
                    mCheckBoxSleepMode.setChecked(true);
                }


            }
        }
    }

    @Override
    public void onEventMainThread(IAQEvents event) {
        super.onEventMainThread(event);
        if (event instanceof IAQGetDataFromWsEvent) {
//            getSleepMode();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    private void refresh() {
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "")};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Log.d(TAG, "refresh: Count=" + dbCount);
        if (dbCount > 0) {
//            mDialog.setMessage(getString(R.string.iaq_cloud_data));

            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
                String serialNum = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER));
                int status = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
                Logger.d(TAG, "refresh: serialNum=" + serialNum + "deviceId:" + deviceId + "status:" + status);
                if (Constants.DEVICE_ONLINE == status) {
                    startServiceByAction(getApplicationContext(), Constants.ACTION_GET_IAQ_DATA, deviceId);
                }
                cur.moveToNext();
            }
        }
        cur.close();
    }

    private void startServiceByAction(Context context, String action, String deviceId) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(context.getPackageName());
        intent.putExtra(Constants.KEY_DEVICE_ID, deviceId);
        context.startService(intent);
    }

    class DashboardReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: Action=" + action);
            if (Constants.ACTION_WSS_CONNECTED.equals(action)) {
                refresh();

            } else if (Constants.ACTION_WSS_CONNECT_FAIL.equals(action)) {
                Utils.showToast(getApplicationContext(), getString(R.string.connect_cloud_fail));
            } else if (Constants.ACTION_GET_IAQ_DATA_SUCCESS.equals(action)) {
                getDeviceInformation();
            } else if (Constants.ACTION_LOGOUT_FAIL.equals(action)) {
                Utils.showToast(getApplicationContext(), getString(R.string.logout_fail));
            } else if (Constants.ACTION_INVALID_NETWORK.equals(action)) {
                Utils.showToast(getApplicationContext(), getString(R.string.no_network));
            }
        }
    }

    private void setTemperatureMode(final boolean isC) {
        showLoadingDialog();

        String mode = "";
        if (isC) {
            mode = "1";
        } else {
            mode = "2";
        }

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_SET_TEMPERATURE);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        params.put("temperatureUnit", mode);
        HttpUtils.getString(MyIaqActivity2.this, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(MyIaqActivity2.this, Constants.GetDataFlag.HON_IAQ_SET_TEMPERATURE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();

                if (resultCode != 0) {
                    if (isC) {
                        mCheckBoxC.setChecked(false);
                        mCheckBoxF.setChecked(true);

                    } else {
                        mCheckBoxC.setChecked(true);
                        mCheckBoxF.setChecked(false);
                    }

                    MessageBox.createSimpleDialog(MyIaqActivity2.this, null, getString(R.string.setting_fail), null, null);
                }
            }
        }));
    }
}

