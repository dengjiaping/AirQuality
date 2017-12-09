package com.honeywell.iaq.clock.control;

import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.MyIaqActivity2;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.clock.model.ClockFormat;
import com.honeywell.iaq.clock.model.ClockJson;
import com.honeywell.iaq.clock.model.ClockModel;
import com.honeywell.iaq.clock.view.ClockListAdapter;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.widget.MessageBox;
import com.honeywell.net.exception.TubeException;
import com.honeywell.net.listener.JSONTubeListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jin on 06/09/2017.
 */

public class ClockSettingActivity extends IAQTitleBarActivity implements View.OnClickListener {

    private static final String TAG = "ClockSettingActivity";
    public static final String INTENT_CLOCK_LIST = "com.honeywell.iaq.clock.control.intent_clock_list";
    public static final String INTENT_CLOCK_DEVICE_ID = "com.honeywell.iaq.activity.INTENT_CLOCK_DEVICE_ID";

    private ImageView mAddImageView;
    private Button mEditClockButton;
    private ListView mClockListView;
    private ClockListAdapter mClockAdapter;

    private ArrayList<ClockModel> mClocks = new ArrayList<>();
    private String mDeviceId;

    @Override
    protected int getContent() {
        return R.layout.activity_clock_setting;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(getString(R.string.clock));
    }


    @Override
    protected void initView() {
        mDeviceId = getIntent().getStringExtra(MyIaqActivity2.INTENT_DEVICE_ID);

        mAddImageView = (ImageView) findViewById(R.id.iv_add);
        mAddImageView.setOnClickListener(this);
        mEditClockButton = (Button) findViewById(R.id.btn_save_clock);
        mEditClockButton.setOnClickListener(this);

        mClockAdapter = new ClockListAdapter(this, mClocks);
        mClockListView = (ListView) findViewById(R.id.clock_list);
        mClockListView.setAdapter(mClockAdapter);

        mClockAdapter.setToggleClockCallback(new ClockListAdapter.ToggleClockCallback() {
            @Override
            public void onSet(int clockId, boolean isOn) {
                for (int i = 0; i < mClocks.size(); i++) {
                    if (mClocks.get(i).getId() == clockId) {
                        mClocks.get(i).setActive(isOn);
                        break;
                    }
                }

                updateClockApi();
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == mAddImageView) {
            if (mClocks.size() < 5) {
                Intent intent = new Intent(ClockSettingActivity.this, ClockHourActivity.class);
                intent.putExtra(INTENT_CLOCK_LIST, mClocks);
                intent.putExtra(INTENT_CLOCK_DEVICE_ID, mDeviceId);
                intent.putExtra(ClockHourActivity.IS_CREATE_CLOCK, true);
                startActivity(intent);
            } else {
                MessageBox.createSimpleDialog(ClockSettingActivity.this, null, getString(R.string.max_clock), null, null);
            }

        } else if (v == mEditClockButton) {
            if (mClocks.size() > 0) {
                Intent intent = new Intent(ClockSettingActivity.this, ClockEditActivity.class);
                intent.putExtra(INTENT_CLOCK_LIST, mClocks);
                intent.putExtra(INTENT_CLOCK_DEVICE_ID, mDeviceId);
                startActivity(intent);
            } else {
                Intent intent = new Intent(ClockSettingActivity.this, ClockHourActivity.class);
                intent.putExtra(INTENT_CLOCK_LIST, mClocks);
                intent.putExtra(INTENT_CLOCK_DEVICE_ID, mDeviceId);
                intent.putExtra(ClockHourActivity.IS_CREATE_CLOCK, true);
                startActivity(intent);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // In case user change 12/24 format
        for (ClockModel clock : mClocks) {
            clock.switch12or24Format(ClockFormat.is24HourFormat(this));
        }
        mClockAdapter.notifyDataSetChanged();

        getClocks();

    }

    private void getClocks() {
        showLoadingDialog();

        mClocks.clear();
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_CLOCK);
        params.put(Constants.KEY_DEVICE_ID, mDeviceId);
        HttpUtils.getString(this, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_GET_CLOCK, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode != 0) {
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject((String) objects[0]);
                    JSONArray jsonArray = jsonObject.getJSONArray(Constants.KEY_CLOCKS);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jb = (JSONObject) jsonArray.get(i);
                        String activate = jb.getString("activate");
                        String time = jb.getString("time");
                        JSONArray daysArray = jb.getJSONArray("day");
                        ArrayList<Integer> days = new ArrayList<>();
                        for (int j = 0; j < daysArray.length(); j++) {
                            days.add(daysArray.getInt(j));
                        }

                        ClockModel clock = new ClockModel(i, time, days, "on".equals(activate),
                                ClockFormat.is24HourFormat(ClockSettingActivity.this));
                        mClocks.add(clock);
                    }
                    mClockAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (mClocks.size() > 0)
                        mEditClockButton.setText(getString(R.string.edit_clock));
                    else
                        mEditClockButton.setText(getString(R.string.create_clock));
                }

            }
        }));

    }

    private void updateClockApi() {
        showLoadingDialog();

        List<ClockJson> clockJsons = new ArrayList<>();
        for (ClockModel clockModel : mClocks) {
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

                if (resultCode != 0) {
                    MessageBox.createSimpleDialog(ClockSettingActivity.this, null, getString(R.string.setting_fail), null, null);
                }

            }
        }));
    }
}
