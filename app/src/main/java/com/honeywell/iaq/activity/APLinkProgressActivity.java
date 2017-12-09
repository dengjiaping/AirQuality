package com.honeywell.iaq.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.R;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.AES;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.utils.network.WifiNetworkUtils;
import com.honeywell.lib.widgets.CircleWaveView;
import com.honeywell.net.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by E570281 on 12/16/2016.
 */
public class APLinkProgressActivity extends IAQTitleBarActivity {

    private static final String TAG = "APLinkProgress";

    private TextView mCloud;
    private TextView mTextViewWifi;

    private android.support.v7.widget.Toolbar mToolbar;

    private APLinkHandler mHandler;

    private static final int CONNECT_WIFI_SUCCESS = 0;

    private static final int BIND_SUCCESS = 1;

    private static final int BIND_FAIL = 2;

    private static final int CONNECT_WIFI_TIME_OUT = 3;

    private static final int BIND_IAQ = 4;

    private static final int CHECK_CONNECTED_WIFI_SSID = 5;

    private static final int CONNECT_WIFI_FAI = 6;

    private static final int MAX_BIND_COUNT = 10;

    private static final int CHECK_BIND_INTERVAL = 6 * 1000;

    private int bindCount = 0;

    private int bindRequestTime = 0;
    private String last16Key;
    private boolean isNeedEncrypt =false;

    public WifiNetworkUtils mNetworkUtils;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mNetworkUtils = WifiNetworkUtils.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNetworkUtils.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mNetworkUtils.onPaused();
    }

    static class APLinkHandler extends Handler {
        private WeakReference<APLinkProgressActivity> mActivityContent;

        private APLinkProgressActivity mActivity;

        public APLinkHandler(APLinkProgressActivity activity) {
            mActivityContent = new WeakReference<>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BIND_SUCCESS:
                    mActivity.bindCount = 0;
                    Utils.showToast(mActivity, mActivity.getString(R.string.bind_iaq_success));

                    Intent intent = new Intent(mActivity, NameIAQActivity.class);
                    mActivity.startActivity(intent);
                    break;
                case BIND_FAIL:
                    mActivity.bindCount = 0;
                    Utils.showToast(mActivity, mActivity.getString(R.string.bind_device_fail));
                    Intent networkSetupFail = new Intent(mActivity, IAQNetworkSetupFailActivity.class);
                    mActivity.startActivity(networkSetupFail);
                    break;
                case BIND_IAQ:
                    Log.d(TAG, "BIND_IAQ: Bind count=" + mActivity.bindCount);
                    if (mActivity.bindCount < MAX_BIND_COUNT) {
                        mActivity.bindCount++;
                        mActivity.bindDevice();
                    } else {
                        mActivity.bindCount = 0;
                        sendEmptyMessage(BIND_FAIL);
                    }
                    break;
                case CHECK_CONNECTED_WIFI_SSID:
//                    mActivity.checkConnectedWifi();
                    break;
                case CONNECT_WIFI_FAI:
                    mActivity.bindCount = 0;
                    Utils.showToast(mActivity, mActivity.getString(R.string.iaq_connect_wifi_fail));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected int getContent() {
        return R.layout.activity_ap_link_progress;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.iaq_cloud_connect);
    }

    @Override
    protected void initView() {
        super.initView();
        CircleWaveView loading = (CircleWaveView) findViewById(R.id.loading);
        loading.setWaveColor(Color.parseColor("#2D9CE8"));
        loading.setWaveInterval(50);
        mCloud = (TextView) findViewById(R.id.iaq_cloud_connected);
        String text = getString(R.string.iaq_cloud_connected) + getString(R.string.iaq_cloud_connect_success);
        SpannableString ss = new SpannableString(text);
        ss.setSpan(new ForegroundColorSpan(Color.argb(255, 0, 191, 255)), 0, getString(R.string.iaq_cloud_connected).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mCloud.setText(ss);

        mTextViewWifi = (TextView) findViewById(R.id.iaq_wifi_connect);
        String wifiText = getString(R.string.iaq_wifi_connect_on) + getString(R.string.iaq_wifi_connect_success);
        SpannableString spannableString = new SpannableString(wifiText);
        spannableString.setSpan(new ForegroundColorSpan(Color.argb(255, 0, 191, 255)), 0, getString(R.string.iaq_wifi_connect_on).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTextViewWifi.setText(spannableString);

        mHandler = new APLinkHandler(this);

        if(isNeedEncrypt){
            sendKeyToDevice();
        }else {
            connectIAQ(isNeedEncrypt);
        }

    }

    private void sendKeyToDevice() {

        JSONObject jsonObject = new JSONObject();
        String serialNum = Utils.getSharedPreferencesValue(this, Constants.KEY_CURRENT_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
        String random = AES.generateKey();
        String seriesNo = Arrays.toString(Arrays.copyOfRange(AES.shaEncrypt1(random + serialNum), 0, 15));
        last16Key = Arrays.toString(Arrays.copyOfRange(AES.shaEncrypt1(random + serialNum), 16, 31));
        jsonObject.optString("KEY_RANDOM", random);
        jsonObject.optString("KEY_SERIES", seriesNo);

        UdpThread udpBroadCast = new UdpThread(jsonObject.toString());
        udpBroadCast.start();


    }

    private void connectIAQ(boolean isNeedEncrypt) {
        if(isNeedEncrypt){
            String ssid = getIntent().getStringExtra(Constants.KEY_WIFI_SSID);
            AES.setKey(last16Key);
            String EncodeSsid = AES.encrypt(ssid);
            String password = getIntent().getStringExtra(Constants.KEY_WIFI_PASSWORD);
            String EncodePassword = AES.encrypt(password);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.KEY_WIFI_SSID, EncodeSsid);
                jsonObject.put(Constants.KEY_WIFI_PASSWORD, EncodePassword);
                jsonObject.put(Constants.KEY_LENGTH, 12);
                jsonObject.put(Constants.KEY_LENGTH, jsonObject.toString().length());
                UdpBroadCast udpBroadCast = new UdpBroadCast(jsonObject.toString());
                udpBroadCast.start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            String ssid = getIntent().getStringExtra(Constants.KEY_WIFI_SSID);

            String password = getIntent().getStringExtra(Constants.KEY_WIFI_PASSWORD);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(Constants.KEY_WIFI_SSID, ssid);
                jsonObject.put(Constants.KEY_WIFI_PASSWORD, password);
                jsonObject.put(Constants.KEY_LENGTH, 12);
                jsonObject.put(Constants.KEY_LENGTH, jsonObject.toString().length());
                UdpBroadCast udpBroadCast = new UdpBroadCast(jsonObject.toString());
                udpBroadCast.start();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private class UdpThread extends Thread {
        MulticastSocket sender = null;
        DatagramPacket dj = null;
        InetAddress group = null;
        byte[] data = new byte[1024];

        public UdpThread(String dataString) {
            data = dataString.getBytes();
        }

        @Override
        public void run() {
            super.run();
            try {
                sender = new MulticastSocket();
                group = InetAddress.getByName(Constants.DEFAULT_AP_IP);
                dj = new DatagramPacket(data, data.length, group, Constants.DEFAULT_IAQ_PORT);
                sender.send(dj);
                sleep(1000);
                connectIAQ(true);
            } catch (IOException e) {

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (sender != null) {
                    sender.close();
                }
            }


        }
    }

    private class UdpBroadCast extends Thread {
        MulticastSocket sender = null;
        DatagramPacket dj = null;
        InetAddress group = null;
        byte[] data = new byte[1024];

        public UdpBroadCast(String dataString) {
            data = dataString.getBytes();
        }

        @Override
        public void run() {
            try {
                sender = new MulticastSocket();
                group = InetAddress.getByName(Constants.DEFAULT_AP_IP);
                dj = new DatagramPacket(data, data.length, group, Constants.DEFAULT_IAQ_PORT);
                sender.send(dj);

                mHandler.sendEmptyMessageDelayed(BIND_IAQ, 10 * 1000);


            } catch (IOException e) {
                e.printStackTrace();
                mHandler.sendEmptyMessage(CONNECT_WIFI_FAI);
            } finally {
                if (sender != null) {
                    sender.close();
                }
            }
        }
    }

    private void bindDevice() {
        if (Utils.isNetworkAvailable(getApplicationContext())) {

            if (bindRequestTime < 6) {
                doBind();
            } else {
                mHandler.sendEmptyMessage(BIND_FAIL);
            }

        } else {
            mHandler.sendEmptyMessageDelayed(BIND_IAQ, CHECK_BIND_INTERVAL);
        }
    }

    private void doBind() {

        final String serialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_CURRENT_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_BIND_DEVICE);
        params.put(Constants.KEY_DEVICE_SERIAL, serialNum);
        params.put(Constants.KEY_DEVICE_PASSWORD, "000");
        HttpUtils.getString(this, Constants.BIND_DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_BIND_DEVICE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                bindRequestTime++;
                if (resultCode != 0) {
                    if (objects == null) {
                        //绑定
                        mHandler.sendEmptyMessageDelayed(BIND_IAQ, 6 * 1000);
//                        mHandler.sendEmptyMessage(BIND_FAIL);
                        return;
                    }
                    String responseStr = (String) objects[0];
                    if (responseStr.contains(Constants.KEY_ERROR_TYPE)) {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        String errorType = jsonObject.getString(Constants.KEY_ERROR_TYPE);
                        if (errorType != null) {
                            if (errorType.equals(Constants.ERROR_TYPE_CAN_NOT_BIND)) {
//                                Utils.showToast(getApplicationContext(), getString(R.string.bind_error_device_bind));
                            } else if (errorType.equals(Constants.ERROR_TYPE_PASSWORD_ERROR)) {
//                                Utils.showToast(getApplicationContext(), getString(R.string.bind_error_password_error));
                            }
                            mHandler.sendEmptyMessageDelayed(BIND_IAQ, 6 * 1000);
//                            mHandler.sendEmptyMessage(BIND_FAIL);
                        } else {
                            mHandler.sendEmptyMessageDelayed(BIND_IAQ, 6 * 1000);
//                            mHandler.sendEmptyMessage(BIND_FAIL);
                        }
                    } else {
                        mHandler.sendEmptyMessageDelayed(BIND_IAQ, 6 * 1000);
//                        mHandler.sendEmptyMessage(BIND_FAIL);
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
}
