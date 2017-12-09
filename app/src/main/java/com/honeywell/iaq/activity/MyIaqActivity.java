package com.honeywell.iaq.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.events.IAQEnvironmentDetailEvent;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.db.IAQ;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.greenrobot.event.EventBus;

public class MyIaqActivity extends IAQTitleBarActivity implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "MyIaq";

    private static MyIaqActivity sContext;


    private static String serialNum, home, room;

    private static String deviceId;

    @Override
    protected int getContent() {
        return R.layout.activity_my_iaq;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.iaq_settings);
    }

    @Override
    protected void initView() {
        super.initView();
        sContext = this;

        serialNum = getIntent().getStringExtra(Constants.KEY_DEVICE_SERIAL);
        home = getIntent().getStringExtra(Constants.KEY_HOME);
        room = getIntent().getStringExtra(Constants.KEY_ROOM);
        deviceId = getIntent().getStringExtra(Constants.KEY_DEVICE_ID);
        getDeviceInformation();
    }


    private void getDeviceInformation() {
        String account = Utils.getSharedPreferencesValue(getContext(), Constants.KEY_ACCOUNT, "");
        Log.d(TAG, "getDeviceInformation: serialNum=" + serialNum);
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, serialNum};
        Cursor cur = this.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            home = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HOME));
            room = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ROOM));
            Log.d(TAG, "Room=" + room + ", home=" + home);

            cur.moveToNext();
        }
        cur.close();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange: New value=" + newValue);
        if (preference.equals(IaqSettingFragment.sHome)) {
            home = String.valueOf(newValue);
            updateDeviceInformation();
        } else if (preference.equals(IaqSettingFragment.sRoom)) {
            room = String.valueOf(newValue);
            updateDeviceInformation();
        }
        return true;
    }

    public static MyIaqActivity getContext() {
        return sContext;
    }

    private void updateDeviceInformation() {


//        Map<String, String> params = new HashMap<>();
//        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_LOCATION);
//        params.put(Constants.KEY_DEVICE_ID, deviceId);
//
//
//        String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
//        Log.e(TAG, "" + cookie);
//        if (cookie.length() > 0) {
//
//        }


        try {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put(Constants.KEY_HOME, home);
            deviceInfo.put(Constants.KEY_ROOM, room);
            JSONObject updateDevice = new JSONObject();
            updateDevice.put(Constants.KEY_TYPE, Constants.TYPE_UPDATE_DEVICE);
            updateDevice.put(Constants.KEY_DEVICE_ID, deviceId);
            updateDevice.put(Constants.KEY_DEVICE_INFO, deviceInfo);

            HttpUtils.getString(this, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(updateDevice), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_UPDATE_DEVICE, new IResponse() {
                @Override
                public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                    if (resultCode == 0) {
                        IaqSettingFragment.sHome.setSummary(home);
                        IaqSettingFragment.sRoom.setSummary(room);
                        updateDeviceInformationToDb();
                    }
                }
            }));

        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        EventBus.getDefault().post(new IAQEnvironmentDetailEvent(IAQEnvironmentDetailEvent.MODIFY_HOME_ROME_NAME,true,null));
    }

    public static class IaqSettingFragment extends PreferenceFragment {

        private static final String KEY_HOME = "iaq_home";

        private static final String KEY_ROOM = "iaq_room";

        private static final String KEY_SERIAL_NUMBER = "serial_number";

//        private static final String KEY_NETWORK_PROBLEM = "network_problem";

        private static final String KEY_REMOVE = "remove";

        static EditTextPreference sHome;
        static EditTextPreference sRoom;

        Preference sSerialNumber, sNetworkProblems, sRemove;

        private ProgressDialog mDialog;

        private IaqSettingFragmentHandler mHandler;

        private static final int REMOVE_IAQ_SUCCESS = 0;

        private static final int REMOVE_IAQ_FAIL = 1;

        class IaqSettingFragmentHandler extends Handler {
            private WeakReference<IaqSettingFragment> mActivityContent;

            private IaqSettingFragment mActivity;

            public IaqSettingFragmentHandler(IaqSettingFragment activity) {
                mActivityContent = new WeakReference<>(activity);
                mActivity = mActivityContent.get();
            }

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REMOVE_IAQ_SUCCESS:
                        if (mActivity.mDialog != null) {
                            mActivity.mDialog.dismiss();
                        }
                        mActivity.removeDeviceFromDb();
                        Intent intent = new Intent(mActivity.getActivity(), DashboardActivity.class);
                        mActivity.getActivity().startActivity(intent);
                        mActivity.getActivity().finish();
                        break;
                    case REMOVE_IAQ_FAIL:
                        if (mActivity.mDialog != null) {
                            mActivity.mDialog.dismiss();
                        }
                        Utils.showToast(mActivity.getActivity(), mActivity.getString(R.string.remove_iaq_fail));
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.fragment_my_iaq);

            sHome = (EditTextPreference) findPreference(KEY_HOME);
            sHome.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return MyIaqActivity.getContext().onPreferenceChange(preference, newValue);
                }
            });

            sRoom = (EditTextPreference) findPreference(KEY_ROOM);
            sRoom.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return MyIaqActivity.getContext().onPreferenceChange(preference, newValue);
                }
            });


            sRemove = (Preference) findPreference(KEY_REMOVE);
            sRemove.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showConfirmRemoveDeviceDialog();
                    return false;
                }
            });

            sSerialNumber = (Preference) findPreference(KEY_SERIAL_NUMBER);

            mHandler = new IaqSettingFragmentHandler(this);
            mDialog = new ProgressDialog(getActivity());
            mDialog.setTitle(getString(R.string.iaq_settings));
            mDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        public void onResume() {
            Log.d("IaqSettingFragment", "onResume");
            sHome.setSummary(home);
            sRoom.setSummary(room);
            sSerialNumber.setSummary(serialNum);
            super.onResume();
        }

        private void removeDeviceFromDb() {
            String account = Utils.getSharedPreferencesValue(getActivity().getApplicationContext(), Constants.KEY_ACCOUNT, "");
            String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
            String[] selectionArgs = new String[]{account, serialNum};
            getActivity().getContentResolver().delete(IAQ.BindDevice.DICT_CONTENT_URI, selection, selectionArgs);
        }

        private void showConfirmRemoveDeviceDialog() {
            AlertDialog mAlertDialog = new AlertDialog.Builder(sContext).setTitle(getString(R.string.iaq_settings)).setMessage(getString(R.string.confirm_remove_iaq)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Utils.isNetworkAvailable(sContext)) {
                        unbindDevice();
                    } else {
                        Utils.showToast(sContext, getString(R.string.no_network));
                    }
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create();
            mAlertDialog.show();
        }

        private void unbindDevice() {
            mDialog.setMessage(getString(R.string.removing_iaq));
            mDialog.show();

            Map<String, String> params = new HashMap<>();
            params.put(Constants.KEY_TYPE, Constants.TYPE_UNBIND_DEVICE);
            params.put(Constants.KEY_DEVICE_ID, deviceId);
            HttpUtils.getString(MyIaqActivity.getContext(), Constants.BIND_DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(MyIaqActivity.getContext(), Constants.GetDataFlag.HON_IAQ_UNBIND_DEVICE, new IResponse() {
                @Override
                public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                    if (resultCode == 0) {
                        mHandler.sendEmptyMessageDelayed(REMOVE_IAQ_SUCCESS, 1000);
                    } else {
                        mHandler.sendEmptyMessageDelayed(REMOVE_IAQ_FAIL, 1000);
                    }
                }
            }));


//            IAQRequestUtils.unbindDevice(getActivity(), deviceId, new IAQRequestUtils.HttpCallback() {
//                @Override
//                public void success(String responseStr, boolean isLast) {
//                    mHandler.sendEmptyMessageDelayed(REMOVE_IAQ_SUCCESS, 1000);
//                }
//
//                @Override
//                public void failed(String responseStr) {
//                    mHandler.sendEmptyMessageDelayed(REMOVE_IAQ_FAIL, 1000);
//                }
//            });

        }
    }
}

