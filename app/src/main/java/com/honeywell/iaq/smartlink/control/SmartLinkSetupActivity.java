package com.honeywell.iaq.smartlink.control;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.APLinkActivity;
import com.honeywell.iaq.activity.MyIaqActivity2;
import com.honeywell.iaq.activity.NameIAQActivity;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.smartlink.manager.TIEnrollManager;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.widget.MessageBox;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jin on 04/09/2017.
 */

public class SmartLinkSetupActivity extends IAQTitleBarActivity {

    private static final String TAG = "SmartLinkSetupActivity";

    private EditText mSsid, mPwd;
    private Button mConnect;
    private CheckBox mCheckBox;
    private static String mToken = "";

    private APLinkHandler mHandler;
    private static final int BIND_SUCCESS = 1;
    private static final int BIND_FAIL = 2;
    private static final int BIND_IAQ = 4;
    private static final int MAX_BIND_COUNT = 10;
    private static final int CHECK_BIND_INTERVAL = 6 * 1000;
    private int bindCount = 0;
    private int bindRequestTime = 0;


    @Override
    protected int getContent() {
        return R.layout.activity_smart_link_setup;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.configure_iaq_wifi);
    }

    @Override
    protected void initView() {
        super.initView();
        mCheckBox = (CheckBox) findViewById(R.id.ch_wifi_pwd);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    mPwd.setSelection(mPwd.getText().length());
                } else {
                    mPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    mPwd.setSelection(mPwd.getText().length());
                }
            }
        });


        mPwd = (EditText) findViewById(R.id.ap_link_pwd);
        mPwd.setTypeface(Typeface.DEFAULT);
        mPwd.setTransformationMethod(new PasswordTransformationMethod());

        mConnect = (Button) findViewById(R.id.btn_connect);
        mConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mSsid.getText() != null && mSsid.getText().toString().length() > 0) {
//                    if (mPwd.getText() == null || mPwd.getText().toString().length() == 0) {
//                        Utils.showToast(SmartLinkSetupActivity.this, getString(R.string.input_password));
//                    } else {
                        mConnect.setEnabled(false);
                        mConnect.setText(getString(R.string.connecting));

                        String ssid = mSsid.getText().toString();
                        String password = mPwd.getText().toString();

                        TIEnrollManager.getInstance(SmartLinkSetupActivity.this).startSmartLink(SmartLinkSetupActivity.this, ssid, password);
                        TIEnrollManager.getInstance(SmartLinkSetupActivity.this).restartUdp();
                        TIEnrollManager.getInstance(SmartLinkSetupActivity.this).setSuccessCallback(new TIEnrollManager.SuccessCallback() {
                            @Override
                            public void onSuccess(final String serial, final String token) {
                                mToken = token;
                                Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, serial);

                                bindDevice(token);
                            }
                        });
                        TIEnrollManager.getInstance(SmartLinkSetupActivity.this).setErrolCallback(new TIEnrollManager.ErrolCallback() {
                            @Override
                            public void onError(String msg) {
//                                Utils.showToast(SmartLinkSetupActivity.this, msg);
                                showMessageBox();
                            }
                        });
//                    }
                } else {
                    Utils.showToast(SmartLinkSetupActivity.this, getString(R.string.input_ssid));
                }
            }
        });
        Utils.setListenerToRootView(this, R.id.activity_ap_link, mConnect);

        mHandler = new APLinkHandler(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        String ssid = "";
        if (Utils.isWifiConnected(this)) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            if (info != null && info.getSSID() != null) {
                ssid = Utils.removeDoubleQuotes(info.getSSID());
                Log.e(TAG, "Connected wifi SSID=" + ssid);
            } else {
                Utils.showToast(getApplicationContext(), getString(R.string.no_connect_wifi));
            }
        }
        mSsid = (EditText) findViewById(R.id.ap_link_ssid);
        if (ssid != null) {
            mSsid.setText(ssid);
            mSsid.setSelection(ssid.length());
        }

    }


    private void bindDevice(String token) {
        if (Utils.isNetworkAvailable(getApplicationContext())) {

            if (bindRequestTime < 6) {
                doBind(token);
            } else {
                mHandler.sendEmptyMessage(BIND_FAIL);
            }

        } else {
            mHandler.sendEmptyMessageDelayed(BIND_IAQ, CHECK_BIND_INTERVAL);
        }
    }

    private void doBind(String token) {

        final String serialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_BIND_DEVICE);
        params.put(Constants.KEY_DEVICE_SERIAL, serialNum);
        params.put(Constants.KEY_DEVICE_PASSWORD, token);
        HttpUtils.getString(this, Constants.BIND_DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_BIND_DEVICE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                bindRequestTime++;
                if (resultCode != 0) {
                    if (objects == null) {
                        //绑定
                        mHandler.sendEmptyMessageDelayed(BIND_IAQ, 6 * 1000);
                        return;
                    }
                    String responseStr = (String) objects[0];
                    if (responseStr.contains(Constants.KEY_ERROR_TYPE)) {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        String errorType = jsonObject.getString(Constants.KEY_ERROR_TYPE);
                        if (errorType != null) {
                            if (errorType.equals(Constants.ERROR_TYPE_CAN_NOT_BIND)) {
                                if ("already bound".equals(jsonObject.getString(Constants.KEY_ERROR_DETAIL)) && (bindRequestTime == 1)) {
                                    MessageBox.createSimpleDialog(SmartLinkSetupActivity.this, null, getString(R.string.device_register_already), null, new MessageBox.MyOnClick() {
                                        @Override
                                        public void onClick(View v) {
                                            finish();
                                        }
                                    });
                                }
                            } else if (errorType.equals(Constants.ERROR_TYPE_PASSWORD_ERROR)) {

                            }
                            mHandler.sendEmptyMessageDelayed(BIND_IAQ, 6 * 1000);
                        } else {
                            mHandler.sendEmptyMessageDelayed(BIND_IAQ, 6 * 1000);
                        }
                    } else {
                        mHandler.sendEmptyMessageDelayed(BIND_IAQ, 6 * 1000);
                    }
                    return;
                }
                String responseStr = (String) objects[0];
                if (responseStr.contains(Constants.KEY_DEVICE_ID)) {
                    Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, serialNum);
                    String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
                    String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
                    String[] selectionArgs = new String[]{account, serialNum};
                    Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
                    int count = cur.getCount();
                    Log.d(TAG, "doBind: Account DBCount=" + count);
                    ContentValues cv = new ContentValues();
                    cv.put(IAQ.BindDevice.COLUMN_ACCOUNT, account);
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER, serialNum);
                    // Device online when bind success
                    cv.put(IAQ.BindDevice.COLUMN_ONLINE_STATUS, Constants.DEVICE_ONLINE);
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_ID, jsonObject.optString(Constants.KEY_DEVICE_ID));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_PM25, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TVOC, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_CO2, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_HCHO, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT, "");

                    cv.put(IAQ.BindDevice.COLUMN_LOCATION, "");
                    cv.put(IAQ.BindDevice.COLUMN_WEATHER, "");
                    cv.put(IAQ.BindDevice.COLUMN_TEMPERATURE, "");
                    cv.put(IAQ.BindDevice.COLUMN_HUMIDITY, "");
                    cv.put(IAQ.BindDevice.COLUMN_PM25, "");
                    cv.put(IAQ.BindDevice.COLUMN_PM10, "");
                    cv.put(IAQ.BindDevice.COLUMN_AQI, "");
                    cv.put(IAQ.BindDevice.COLUMN_TIME, "");

                    if (count == 0) {
                        getContentResolver().insert(IAQ.BindDevice.DICT_CONTENT_URI, cv);
                    } else {
                        getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);
                    }

                    cur.close();
                    mHandler.sendEmptyMessage(BIND_SUCCESS);
                    return;
                }

                mHandler.sendEmptyMessage(BIND_FAIL);
            }
        }));

    }


    static class APLinkHandler extends Handler {
        private WeakReference<SmartLinkSetupActivity> mActivityContent;

        private SmartLinkSetupActivity mActivity;

        public APLinkHandler(SmartLinkSetupActivity activity) {
            mActivityContent = new WeakReference<>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BIND_SUCCESS:
                    mActivity.bindCount = 0;

                    Intent intent = new Intent(mActivity, NameIAQActivity.class);
                    mActivity.startActivity(intent);
                    mActivity.finish();
                    break;
                case BIND_FAIL:
                    mActivity.bindCount = 0;
                    mActivity.showMessageBox();
                    break;
                case BIND_IAQ:
                    Log.d(TAG, "BIND_IAQ: Bind count=" + mActivity.bindCount);
                    if (mActivity.bindCount < MAX_BIND_COUNT) {
                        mActivity.bindCount++;
                        mActivity.bindDevice(mToken);
                    } else {
                        mActivity.bindCount = 0;
                        sendEmptyMessage(BIND_FAIL);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void showMessageBox() {
        MessageBox.createTwoButtonDialog(SmartLinkSetupActivity.this, null, getString(R.string.connect_another_way),
                getString(R.string.cancel), new MessageBox.MyOnClick() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                },
                getString(R.string.ok), new MessageBox.MyOnClick() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SmartLinkSetupActivity.this, Gen2ApActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }


}
