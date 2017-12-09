package com.honeywell.iaq.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.bean.City;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by E570281 on 9/20/2016.
 */
public class NameIAQActivity extends IAQTitleBarActivity {
    private static final String TAG = "NameIAQ";

    private android.support.v7.widget.Toolbar mToolbar;

    private Button mBtnComplete;
    public static final int REQUEST_CODE_PICK_CITY = 233;
    private EditText mHomeName, mRoom;
    private TextView mCity;

    private static final int GET_LOCATION_ID_SUCCESS = 0;

    private static final int GET_LOCATION_ID_FAIL = 1;

    private static final int SET_LOCATION_ID_SUCCESS = 2;

    private static final int SET_LOCATION_ID_FAIL = 3;

    private static final int UPDATE_INFORMATION_SUCCESS = 4;

    private static final int UPDATE_INFORMATION_FAIL = 5;

    private ProgressDialog mDialog;

    private NameIAQHandler mHandler;

    private String locationId = Constants.DEFAULT_LOCATION_ID;

    private String deviceId = Constants.DEFAULT_DEVICE_ID;

    static class NameIAQHandler extends Handler {
        private WeakReference<NameIAQActivity> mActivityContent;

        private NameIAQActivity mActivity;

        public NameIAQHandler(NameIAQActivity activity) {
            mActivityContent = new WeakReference<>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LOCATION_ID_SUCCESS:
                    mActivity.setDeviceLocation();
                    break;
                case GET_LOCATION_ID_FAIL:
                    if (mActivity.mDialog != null) {
                        mActivity.mDialog.dismiss();
                    }

                    Utils.showToast(mActivity, mActivity.getString(R.string.set_city_fail));
                    break;
                case SET_LOCATION_ID_SUCCESS:
                    mActivity.updateDeviceInformation();
                    break;
                case SET_LOCATION_ID_FAIL:
                    if (mActivity.mDialog != null) {
                        mActivity.mDialog.dismiss();
                    }

                    Utils.showToast(mActivity, mActivity.getString(R.string.set_city_fail));
                    break;
                case UPDATE_INFORMATION_SUCCESS:
                    if (mActivity.mDialog != null) {
                        mActivity.mDialog.dismiss();
                    }

                    Intent intent = new Intent(mActivity, DashboardActivity.class);
                    mActivity.startActivity(intent);

//                    if (Utils.getDeviceCount(mActivity) > 1) {
//                        Intent intent = new Intent(mActivity, DashboardActivity.class);
//                        mActivity.startActivity(intent);
//                    } else {
//                        Intent intent = new Intent(mActivity, HomeActivity.class);
//                        mActivity.startActivity(intent);
//                    }
                    break;
                case UPDATE_INFORMATION_FAIL:
                    if (mActivity.mDialog != null) {
                        mActivity.mDialog.dismiss();
                    }

                    Utils.showToast(mActivity, mActivity.getString(R.string.update_iaq_info_fail));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected int getContent() {
        return R.layout.activity_name_iaq;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.name_iaq);
    }

    @Override
    protected void initView() {
        super.initView();
        mHandler = new NameIAQHandler(this);

        TextView mSerialNumber = (TextView) findViewById(R.id.serialNumber);
        mSerialNumber.setText("" + Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER));
        deviceId = getDeviceId();

        mBtnComplete = (Button) findViewById(R.id.btn_complete);
        mBtnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    String homeName = mHomeName.getText().toString().trim();
                    if (homeName.length() == 0) {
                        Utils.showToast(getApplicationContext(), getString(R.string.input_home));
                        return;
                    }
                    String roomName = mRoom.getText().toString().trim();
                    if (roomName.length() == 0) {
                        Utils.showToast(getApplicationContext(), getString(R.string.input_room));
                        return;
                    }

                    String city = mCity.getText().toString().trim();
                    if (city.length() > 0) {
                        setDeviceLocation();
//                        getLocationInfo(city);
                    } else {
                        Utils.showToast(getApplicationContext(), getString(R.string.input_city));
                        return;
                    }
                } else {
                    Utils.showToast(getApplicationContext(), getString(R.string.no_network));
                }
            }
        });

        mHomeName = (EditText) findViewById(R.id.home_name);
        mRoom = (EditText) findViewById(R.id.room);
        mCity = (TextView) findViewById(R.id.city);
        mCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(NameIAQActivity.this, CityPickerActivity.class), REQUEST_CODE_PICK_CITY);
            }
        });

        mDialog = new ProgressDialog(this);
        mDialog.setTitle(getString(R.string.name_iaq));
        mDialog.setMessage(getString(R.string.update_iaq_info));
        mDialog.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            Utils.showToast(getApplicationContext(), getString(R.string.no_network));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_CITY && resultCode == RESULT_OK) {
            if (data != null) {
                City city = (City) data.getSerializableExtra(CityPickerActivity.KEY_PICKED_CITY);
                mCity.setText(city.getName());
                locationId = city.getLocationId();
            }
        }
    }

    private void getLocationInfo(final String city) {
        mDialog.show();
//
//        final String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
//        if (cookie.length() > 0) {
//            final AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                    String responseStr = new String(responseBody, 0, responseBody.length);
//                    Log.d(TAG, "responseStr=" + responseStr);
//                    if (responseStr.contains(Constants.KEY_LOCATION_ID)) {
//                        try {
//                            JSONObject jsonObject = new JSONObject(responseStr);
//                            JSONArray jsonArray = jsonObject.getJSONArray(Constants.KEY_LIST);
//                            for (int i = 0; i < jsonArray.length(); i++) {
//                                JSONObject msgType = (JSONObject) jsonArray.get(i);
//                                locationId = msgType.getString(Constants.KEY_LOCATION_ID);
//                                Log.d(TAG, "getLocationInfo: locationID=" + locationId);
//                            }
//                            mHandler.sendEmptyMessage(GET_LOCATION_ID_SUCCESS);
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            mHandler.sendEmptyMessage(GET_LOCATION_ID_FAIL);
//                        }
//                    } else {
//                        mHandler.sendEmptyMessage(GET_LOCATION_ID_FAIL);
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                    mHandler.sendEmptyMessage(GET_LOCATION_ID_FAIL);
//                }
//            };
//
////            new Thread(new Runnable() {
////                @Override
////                public void run() {
//            Map<String, String> params = new HashMap<>();
//            params.put(Constants.KEY_TYPE, Constants.TYPE_GET_LOCATION_INFO);
//            params.put(Constants.KEY_NAME, city);
//
//            HttpClientHelper.newInstance().httpRequest(getApplicationContext(), Constants.LOCATION_LIST_URL, params, HttpClientHelper.COOKIE, callback, HttpClientHelper.POST, cookie);
////                }
////            }).start();
//
//
//        }

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_LOCATION_INFO);
        params.put(Constants.KEY_NAME, city);
        HttpUtils.getString(getApplicationContext(), Constants.LOCATION_LIST_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getApplicationContext(), Constants.GetDataFlag.HON_IAQ_GET_LOCATION_INFO, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode != 0) {
                    return;
                }
                String responseStr = (String) objects[0];
                Log.d(TAG, "responseStr=" + responseStr);
                if (responseStr.contains(Constants.KEY_LOCATION_ID)) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONArray jsonArray = jsonObject.getJSONArray(Constants.KEY_LIST);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject msgType = (JSONObject) jsonArray.get(i);
                            locationId = msgType.getString(Constants.KEY_LOCATION_ID);
                            Log.d(TAG, "getLocationInfo: locationID=" + locationId);
                        }
                        mHandler.sendEmptyMessage(GET_LOCATION_ID_SUCCESS);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mHandler.sendEmptyMessage(GET_LOCATION_ID_FAIL);
                    }
                } else {
                    mHandler.sendEmptyMessage(GET_LOCATION_ID_FAIL);
                }
            }
        }));


    }

    private void setDeviceLocation() {
//        final String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
//        if (cookie.length() > 0) {
//            final AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                    mHandler.sendEmptyMessage(SET_LOCATION_ID_SUCCESS);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                    mHandler.sendEmptyMessage(SET_LOCATION_ID_FAIL);
//                }
//            };
//
////            new Thread(new Runnable() {
////                @Override
////                public void run() {
//            Map<String, String> params = new HashMap<String, String>();
//            params.put(Constants.KEY_TYPE, Constants.TYPE_SET_LOCATION);
//            params.put(Constants.KEY_DEVICE_ID, deviceId);
//            params.put(Constants.KEY_LOCATION_ID, locationId);
//            HttpClientHelper.newInstance().httpRequest(getApplicationContext(), Constants.DEVICE_LOCATION_URL, params, HttpClientHelper.COOKIE, callback, HttpClientHelper.POST, cookie);
////                }
////            }).start();
//        }
//
        showLoadingDialog();
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_SET_LOCATION);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        params.put(Constants.KEY_LOCATION_ID, locationId);
        HttpUtils.getString(getApplicationContext(), Constants.DEVICE_LOCATION_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getApplicationContext(), Constants.GetDataFlag.HON_IAQ_SET_DEVICE_LOCATION, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode != 0) {
                    mHandler.sendEmptyMessage(SET_LOCATION_ID_FAIL);
                    return;
                }
                mHandler.sendEmptyMessage(SET_LOCATION_ID_SUCCESS);
            }
        }));


    }

    private String getDeviceId() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER)};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Log.d(TAG, "getDeviceId: Count=" + dbCount);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
            cur.moveToNext();
        }
        cur.close();
        Log.d(TAG, "getDeviceId: deviceId=" + deviceId);
        return deviceId;
    }

    private void updateDeviceInformation() {
//        String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
//        if (cookie.length() > 0) {
//            try {
//                final String home = mHomeName.getText().toString().trim();
//                final String room = mRoom.getText().toString().trim();
//                final String city = mCity.getText().toString().trim();
//                JSONObject deviceInfo = new JSONObject();
//                deviceInfo.put(Constants.KEY_HOME, home);
//                deviceInfo.put(Constants.KEY_ROOM, room);
//                JSONObject updateDevice = new JSONObject();
//                updateDevice.put(Constants.KEY_TYPE, Constants.TYPE_UPDATE_DEVICE);
//                updateDevice.put(Constants.KEY_DEVICE_ID, deviceId);
//                updateDevice.put(Constants.KEY_DEVICE_INFO, deviceInfo);
//                AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
//                    @Override
//                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                        String serialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
//                        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
//                        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
//                        String[] selectionArgs = new String[]{account, serialNum};
//                        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
//                        int dbCount = cur.getCount();
//                        Log.d(TAG, "updateDeviceInformation: DBCount=" + dbCount);
//                        ContentValues cv = new ContentValues();
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_HOME, home);
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_ROOM, room);
//                        cv.put(IAQ.BindDevice.COLUMN_LOCATION, city);
//                        getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);
//
//                        cur.close();
//
//                        mHandler.sendEmptyMessage(UPDATE_INFORMATION_SUCCESS);
//                    }
//
//                    @Override
//                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                        if (responseBody != null) {
//                            String responseStr = new String(responseBody, 0, responseBody.length);
//                            Log.d(TAG, "updateDeviceInformation: onFailure=" + responseStr);
//                            if (responseStr.contains("InvalidDeviceId") || responseStr.contains("DeviceNotBind")) {
//
//                            }
//                        }
//                        mHandler.sendEmptyMessage(UPDATE_INFORMATION_FAIL);
//                    }
//                };
//                HttpClientHelper.newInstance().httpPostRequest(getApplicationContext(), Constants.DEVICE_URL, updateDevice, HttpClientHelper.COOKIE, callback, cookie);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//

        final String home = mHomeName.getText().toString().trim();
        final String room = mRoom.getText().toString().trim();
        final String city = mCity.getText().toString().trim();


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
                    if (resultCode != 0) {
                        mHandler.sendEmptyMessage(UPDATE_INFORMATION_FAIL);
                        return;
                    }
                    String serialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
                    String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
                    String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
                    String[] selectionArgs = new String[]{account, serialNum};
                    Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
                    int dbCount = cur.getCount();
                    Log.d(TAG, "updateDeviceInformation: DBCount=" + dbCount);
                    ContentValues cv = new ContentValues();
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_HOME, home);
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_ROOM, room);
                    cv.put(IAQ.BindDevice.COLUMN_LOCATION, city);
                    getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

                    cur.close();

                    mHandler.sendEmptyMessage(UPDATE_INFORMATION_SUCCESS);
                }
            }));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

}
