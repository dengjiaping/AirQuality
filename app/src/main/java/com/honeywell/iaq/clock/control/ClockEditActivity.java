package com.honeywell.iaq.clock.control;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.clock.model.ClockFormat;
import com.honeywell.iaq.clock.model.ClockJson;
import com.honeywell.iaq.clock.model.ClockModel;
import com.honeywell.iaq.clock.view.ClockEditListAdapter;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.widget.MessageBox;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Jin on 06/09/2017.
 */

public class ClockEditActivity extends IAQTitleBarActivity implements View.OnClickListener {

    private static final String TAG = "ClockEditActivity";

    public static final String INTENT_EDIT_CLOCK_LIST = "com.honeywell.iaq.clock.control.INTENT_EDIT_CLOCK_LIST";
    public static final String INTENT_ONE_CLOCK = "com.honeywell.iaq.clock.control.intent_one_clock";
    public static final String INTENT_CHANGE_CLOCK = "com.honeywell.iaq.clock.control.intent_change_clock";
    public static final String INTENT_EDIT_CLOCK_DEVICE_ID = "com.honeywell.iaq.activity.INTENT_EDIT_CLOCK_DEVICE_ID";

    public static final int CHANGE_CLOCK_MODEL = 1;

    private ImageView mAddImageView;
    private Button mSaveClockButton;
    private ListView mClockListView;
    private ClockEditListAdapter mClockAdapter;
    private ArrayList<ClockModel> mClocks = new ArrayList<>();
    private String mDeviceId;


    @Override
    protected int getContent() {
        return R.layout.activity_clock_edit;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(getString(R.string.edit_clock));
    }

    @Override
    protected void initView() {
        mDeviceId = getIntent().getStringExtra(ClockSettingActivity.INTENT_CLOCK_DEVICE_ID);

        mAddImageView = (ImageView) findViewById(R.id.iv_add);
        mAddImageView.setOnClickListener(this);
        mSaveClockButton = (Button) findViewById(R.id.btn_save_clock);
        mSaveClockButton.setOnClickListener(this);

        try {
            mClocks = (ArrayList<ClockModel>) getIntent().getSerializableExtra(ClockSettingActivity.INTENT_CLOCK_LIST);
            mClockAdapter = new ClockEditListAdapter(this, mClocks);
            mClockListView = (ListView) findViewById(R.id.clock_edit_list);
            mClockListView.setAdapter(mClockAdapter);
            mClockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ClockModel clock = mClockAdapter.getItem(position);
                    Intent intent = new Intent(ClockEditActivity.this, ClockHourActivity.class);
                    intent.putExtra(ClockHourActivity.IS_CREATE_CLOCK, false);
                    intent.putExtra(INTENT_ONE_CLOCK, clock);
                    startActivityForResult(intent, CHANGE_CLOCK_MODEL);
                }
            });

            mClockAdapter.setDeleteClockCallback(new ClockEditListAdapter.DeleteClockCallback() {
                @Override
                public void onDelete(final ClockModel clock) {

                    MessageBox.createTwoButtonDialog(ClockEditActivity.this, null, getString(R.string.delete_clock),
                            getString(R.string.cancel), null,
                            getString(R.string.ok), new MessageBox.MyOnClick() {
                                @Override
                                public void onClick(View v) {
                                    for (int i = 0; i < mClocks.size(); i++) {
                                        if (mClocks.get(i).getId() == clock.getId()) {
                                            mClocks.remove(i);
                                            mClockAdapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                }
                            }
                    );
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (resultCode) {
            case CHANGE_CLOCK_MODEL:
                try {
                    ClockModel changedClock = (ClockModel) intent.getSerializableExtra(INTENT_CHANGE_CLOCK);
                    for (int i = 0; i < mClocks.size(); i++) {
                        if (mClocks.get(i).getId() == changedClock.getId()) {
                            mClocks.set(i, changedClock);
                            mClockAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mAddImageView) {
            if (mClocks.size() < 5) {
                Intent intent = new Intent(ClockEditActivity.this, ClockHourActivity.class);
                intent.putExtra(ClockHourActivity.IS_CREATE_CLOCK, true);
                intent.putExtra(INTENT_EDIT_CLOCK_LIST, mClocks);
                intent.putExtra(INTENT_EDIT_CLOCK_DEVICE_ID, mDeviceId);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            } else {
                MessageBox.createSimpleDialog(ClockEditActivity.this, null, getString(R.string.max_clock), null, null);
            }

        } else if (v == mSaveClockButton) {
            updateClockApi();
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
                if (resultCode == 0) {
                    finish();
                } else {
                    MessageBox.createSimpleDialog(ClockEditActivity.this, null, getString(R.string.setting_fail), null, null);
                }
            }
        }));
    }


}
