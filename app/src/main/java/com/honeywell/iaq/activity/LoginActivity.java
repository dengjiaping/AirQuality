package com.honeywell.iaq.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.honeywell.iaq.AndroidBug5497Workaround;
import com.honeywell.iaq.adapter.CountryAdapter;
import com.honeywell.iaq.application.IAQApplication;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.utils.HttpRequestUtil;
import com.honeywell.iaq.R;
import com.honeywell.iaq.bean.Country;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.widget.MessageBox;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

public class LoginActivity extends IAQTitleBarActivity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    //    private EditText mPhoneNumber, mPwd;
    private EditText mPhoneNumber;
    private EditText mPwd;
    private Button mLogin;

    private TextView mRegister;
    private TextView mTvForgotPwd;

    private static final String PHONE_UUID = UUID.randomUUID().toString();

    private static final int LOGIN_SUCCESS = 0;

    private static final int LOGIN_FAIL = 1;

    private static final int HAD_BIND = 2;

    private static final int NO_BIND = 3;

    private static final int AUTO_LOGIN = 4;

    private static final int ONE_PHONE_BIND = 5;

    private Spinner mSpinner;

    private int mCountryIndex = Constants.COUNTRY_CHINA_INDEX;
    CountryAdapter mCountryAdapter;

    static class LoginHandler extends Handler {
        private WeakReference<LoginActivity> mActivityContent;

        private LoginActivity mActivity;

        public LoginHandler(LoginActivity activity) {
            mActivityContent = new WeakReference<>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_SUCCESS:
                    //  mActivity.isAccountBound();
                    //  登录成功 连接ws
                    Utils.startServiceByAction(mActivity, Constants.ACTION_OPEN_WSS);
                    mActivity.checkDeviceBound();
                    break;
                case LOGIN_FAIL:
                    mActivity.dismissLoadingDialog();
                    break;
                case HAD_BIND:
                    mActivity.dismissLoadingDialog();

                    Intent dashboardIntent = new Intent(mActivity, DashboardActivity.class);
                    mActivity.startActivity(dashboardIntent);
//                    mActivity.finish();
                    break;
                case ONE_PHONE_BIND:
                    mActivity.dismissLoadingDialog();

                    //只有一台设备，查询该设备信息是否完整
                    String currentSerialNum = Utils.getSharedPreferencesValue(mActivity, Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
                    if (mActivity.isDeviceInfoComplete(currentSerialNum)) {
                        Intent intent = new Intent(mActivity, DashboardActivity.class);
                        mActivity.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mActivity, NameIAQActivity.class);
                        mActivity.startActivity(intent);
                    }
                    break;
                case NO_BIND:
                    Intent intent = new Intent(mActivity, NetworkSetup1Activity.class);
                    mActivity.startActivity(intent);

                    mActivity.dismissLoadingDialog();
                    break;
                case AUTO_LOGIN:
                    String account = Utils.getSharedPreferencesValue(mActivity.getApplicationContext(), Constants.KEY_ACCOUNT, "");
                    String password = Utils.getSharedPreferencesValue(mActivity.getApplicationContext(), Constants.KEY_PASSWORD, "");
                    mActivity.doLogin(account, password);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private LoginHandler mHandler;

    @Override
    protected int getContent() {
        return R.layout.activity_login;
    }

    @Override
    protected void initLeftIcon(ImageView left) {
        left.setVisibility(View.GONE);
    }


    @Override
    protected void initTitle(TextView title) {
        title.setText(R.string.login);
    }

    protected void initView() {
//        AndroidBug5497Workaround.assistActivity(this);

        mCountryIndex = PreferenceUtil.getInt(getApplicationContext(), Constants.KEY_COUNTRY_INDEX, Constants.COUNTRY_CHINA_INDEX);
        mSpinner = (Spinner) findViewById(R.id.sp_country);

        mCountryAdapter = new CountryAdapter(this);
        mSpinner.setAdapter(mCountryAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCountryIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinner.setSelection(mCountryIndex);

        mHandler = new LoginHandler(this);

        mRegister = (TextView) findViewById(R.id.register);
        mRegister.setOnClickListener(this);
        mTvForgotPwd = (TextView) findViewById(R.id.forget_pwd);
        mTvForgotPwd.setOnClickListener(this);

        mPhoneNumber = (EditText) findViewById(R.id.phone_number);
        mPwd = (EditText) findViewById(R.id.password);
        mPwd.setTypeface(Typeface.DEFAULT);
        mPwd.setTransformationMethod(new PasswordTransformationMethod());
        mLogin = (Button) findViewById(R.id.btn_login);
        mLogin.setOnClickListener(this);
        mPhoneNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLogin.setEnabled((!TextUtils.isEmpty(s)) && (mPwd.getText().toString().length() >= 6));
            }
        });
        mPwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mLogin.setEnabled((!TextUtils.isEmpty(s)) && (!TextUtils.isEmpty(mPhoneNumber.getText().toString()))
                    && (mPwd.getText().toString().length() >= 6));
            }
        });
        Utils.setListenerToRootView(this, R.id.activity_login, mLogin);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (Utils.isNetworkAvailable(getApplicationContext())) {
            String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
            if (account.length() > 0) {
                showLoadingDialog();
                mHandler.sendEmptyMessageDelayed(AUTO_LOGIN, 2000);
            }
        } else {
            Utils.showToast(getApplicationContext(), getString(R.string.no_network));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.register:
//                Intent intent = new Intent(LoginActivity.this, SendValidationCodeActivity.class);
//                startActivity(intent);

                Intent intent = new Intent(LoginActivity.this, RegisterAndForgotPwdActivity.class);
                intent.putExtra(Constants.INTENT_TYPE, Constants.REGISTER_TYPE);
                startActivity(intent);
                break;
            case R.id.forget_pwd:
                Intent intent1 = new Intent(LoginActivity.this, RegisterAndForgotPwdActivity.class);
                intent1.putExtra(Constants.INTENT_TYPE, Constants.FORGOT_PASSWORD);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    private void login() {
        if (mPhoneNumber.getText() != null && mPhoneNumber.getText().toString().length() > 0) {
            if (mPwd.getText() == null || mPwd.getText().toString().length() == 0) {
                Utils.showToast(LoginActivity.this, getString(R.string.input_password));
            } else {
                final String account = mPhoneNumber.getText().toString();
                final String password = mPwd.getText().toString();


                doLogin(account, password);
            }
        } else {
            Utils.showToast(LoginActivity.this, getString(R.string.input_phone));
        }
    }

    private void doLogin(final String account, final String password) {

        if (Utils.isNetworkAvailable(getApplicationContext())) {
            mPhoneNumber.setText(account);
            mPwd.setText(password);

            final Country country = (Country) mCountryAdapter.getItem(mCountryIndex);
            showLoadingDialog();
            IAQRequestUtils.login2(this, account, password, country.getCode(), Utils.getLanguage(this),
                    new IAQRequestUtils.HttpCallback() {
                        @Override
                        public void success(String responseStr, boolean isLast) {
                            LoginActivity.this.dismissLoadingDialog();
                            PreferenceUtil.commitInt(getApplicationContext(), Constants.KEY_COUNTRY_INDEX, mCountryIndex);
                            try {
                                JSONObject jsonObject = new JSONObject(responseStr);
                                if (jsonObject.has(Constants.KEY_PHONE_ID)) {
                                    String phoneId = jsonObject.getString(Constants.KEY_PHONE_ID);

                                    Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, account);
//                                    Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_PASSWORD, password);
                                    Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_PHONE_ID, phoneId);
                                    mHandler.sendEmptyMessage(LOGIN_SUCCESS);
                                } else {
                                    String errorType = jsonObject.getString(Constants.KEY_ERROR_TYPE);
                                    if ("LoginLocked".equals(errorType)) {
                                        MessageBox.createSimpleDialog(LoginActivity.this, null, getString(R.string.phone_lock), null, null);
                                    } else if (jsonObject.has(Constants.KEY_ERROR_DETAIL)) {
                                        String string = jsonObject.optString(Constants.KEY_ERROR_DETAIL);
                                        if (string.contains(Constants.ERROR_DETAIL_LOGIN_UNREGISTER)) {
                                            Utils.showToast(getApplicationContext(), getString(R.string.not_register));
                                        } else if (string.contains(Constants.ERROR_DETAIL_LOGIN_WRONG_PASSWORD)) {
                                            Utils.showToast(getApplicationContext(), getString(R.string.wrong_password));
                                        } else {
                                            Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
                                        }
                                    } else {
                                        Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
                                    }
                                    mHandler.sendEmptyMessage(LOGIN_FAIL);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
                                mHandler.sendEmptyMessage(LOGIN_FAIL);
                            }

                        }

                        @Override
                        public void failed(String responseStr) {
                            LoginActivity.this.dismissLoadingDialog();
                            if (responseStr.length() > 0) {
                                try {
                                    JSONObject jsonObject = new JSONObject(responseStr);
                                    String errorDetail = jsonObject.getString(Constants.KEY_ERROR_DETAIL);
                                    Log.d(TAG, "Login error detail=" + errorDetail);
                                    if (errorDetail.contains(Constants.ERROR_DETAIL_LOGIN_UNREGISTER)) {
                                        Utils.showToast(getApplicationContext(), getString(R.string.not_register));
                                    } else if (errorDetail.contains(Constants.ERROR_DETAIL_LOGIN_WRONG_PASSWORD)) {
                                        Utils.showToast(getApplicationContext(), getString(R.string.wrong_password));
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
                                }
                                mHandler.sendEmptyMessage(LOGIN_FAIL);
                            } else {
                                Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
                                mHandler.sendEmptyMessage(LOGIN_FAIL);
                            }
                        }
                    });
        } else {
            Utils.showToast(getApplicationContext(), getString(R.string.no_network));
            mHandler.sendEmptyMessage(LOGIN_FAIL);
        }
    }

//    private String postLoginData(Context context, String urlStr, String jsonStr) {
//        byte[] data = jsonStr.getBytes();
//        OutputStream outputStream = null;
//        InputStream inputStream = null;
//        try {
//            URL url = new URL(urlStr);
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setConnectTimeout(3000);
//            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setUseCaches(false);
//            httpURLConnection.setRequestProperty("Content-Type", "application/json");
//            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
//
//            outputStream = httpURLConnection.getOutputStream();
//            outputStream.write(data);
//
//            int response = httpURLConnection.getResponseCode();
//            Log.d("postLoginData", "responseCode=" + response);
//            if (response == HttpURLConnection.HTTP_OK) {
//                String setCookie = httpURLConnection.getHeaderField(Constants.KEY_SET_COOKIE);
//                Log.d("postLoginData", "Get server Cookie: " + setCookie);
//                if (setCookie != null) {
//                    HttpRequestUtil.setCookie(context, setCookie);
//                    Utils.setSharedPreferencesValue(context, Constants.KEY_COOKIE, setCookie);
//                }
//
//                inputStream = httpURLConnection.getInputStream();
//                String responseStr = HttpRequestUtil.dealResponseResult(inputStream);
//                if (responseStr != null) {
//                    if (responseStr.contains(Constants.KEY_PHONE_ID)) {
//                        responseStr = Utils.replaceBlank(responseStr);
//                        int phoneIdIndex = responseStr.indexOf(Constants.KEY_PHONE_ID) + Constants.KEY_PHONE_ID.length();
//                        int start = responseStr.indexOf('\"', phoneIdIndex + 1) + 1;
//                        int end = responseStr.lastIndexOf('\"');
//                        String phoneId = responseStr.substring(start, end);
//                        Utils.setSharedPreferencesValue(context, Constants.KEY_PHONE_ID, phoneId);
//                        Log.d("postLoginData", "PhoneId=" + phoneId);
//                        return "Post success";
//                    } else {
//                        return responseStr;
//                    }
//                } else {
//                    return "Post Fail";
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return "Post Fail";
//    }
//
//    private void isAccountBound() {
//        Log.d(TAG, "isAccountBound");
//        final String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
//        Log.e("-----------", "+++++++++++++" + cookie);
//        if (cookie.length() > 0) {
//            final AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                    String responseStr = new String(responseBody, 0, responseBody.length);
//                    Log.d(TAG, "onSuccess: bind success" + responseStr);
//                    if (responseStr.contains(Constants.KEY_DEVICE_ID)) {
//                        parseBoundDevice(responseStr);
//                    } else {
//                        // In case all devices was removed in another phone
//                        removeAccountDataFromDB();
//                        mHandler.sendEmptyMessage(NO_BIND);
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                    if (responseBody != null) {
//                        String responseStr = new String(responseBody, 0, responseBody.length);
//                        Log.d(TAG, "onFailure: responseStr=" + responseStr);
//                    }
//                    Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
//                    mHandler.sendEmptyMessage(LOGIN_FAIL);
//                }
//            };
//
//            HttpClientHelper.newInstance().httpRequest(getApplicationContext(), Constants.BIND_DEVICE_URL, null, HttpClientHelper.COOKIE, callback, HttpClientHelper.GET, cookie);
//        } else {
//            Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
//            mHandler.sendEmptyMessage(LOGIN_FAIL);
//        }
//    }

    private boolean isDeviceInfoComplete(String currentSerialNum) {
        if (currentSerialNum == null) {
            return true;
        }
        String roomName = null;
        String homeName = null;
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        Log.d(TAG, "getDeviceInformation: serialNum=" + currentSerialNum);
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            roomName = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ROOM));
            homeName = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HOME));
            Log.d(TAG, "Room=" + roomName + ", home=" + homeName);

            cur.moveToNext();
        }
        cur.close();
        if (TextUtils.isEmpty(roomName) || TextUtils.isEmpty(homeName)) {
            return false;
        }
        return true;
    }


    private void checkDeviceBound() {
        HttpUtils.getPureString(this, Constants.BIND_DEVICE_URL, new TubeTask(LoginActivity.this, Constants.GetDataFlag.HON_IAQ_CHECK_DEVICE_BOUND, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode == 0) {
                    String responseStr = (String) objects[0];
                    Log.d(TAG, "onSuccess: bind success" + responseStr);
                    if (responseStr.contains(Constants.KEY_DEVICE_ID)) {
                        parseBoundDevice(responseStr);
                    } else {
                        // In case all devices was removed in another phone
                        removeAccountDataFromDB();
                        mHandler.sendEmptyMessage(NO_BIND);
                    }
                } else {
                    Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
                    mHandler.sendEmptyMessage(LOGIN_FAIL);
                }
            }
        }));
    }

//
//    public boolean hasBoundDevices(Context context, String urlStr) {
//        InputStream inputStream = null;
//        try {
//            URL url = new URL(urlStr);
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("GET");
//            httpURLConnection.setConnectTimeout(3000);
//            httpURLConnection.setDoInput(true);
//            String cookie = HttpRequestUtil.getCookie(context);
//            Log.d("getBoundDevices", "GetCookie: " + cookie);
//            if (cookie != null) {
//                httpURLConnection.setRequestProperty(Constants.KEY_COOKIE, cookie);
//            }
//
//            int response = httpURLConnection.getResponseCode();
//            Log.d("getBoundDevices", "responseCode=" + response);
//            if (response == HttpURLConnection.HTTP_OK) {
//                inputStream = httpURLConnection.getInputStream();
//                String responseStr = HttpRequestUtil.dealResponseResult(inputStream);
//                Log.d("getBoundDevices", "Response string=" + responseStr);
//                if (responseStr.contains(Constants.KEY_DEVICE_ID)) {
//                    parseBoundDevice(responseStr);
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }

    public void parseBoundDevice(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            JSONArray jsonArray = jsonObject.getJSONArray(Constants.KEY_DEVICES);
            Log.d(TAG, "parseBoundDevice: Devices=" + jsonArray.toString());
            ArrayList<String> deviceIdsOnCloud = new ArrayList<>();
            String serialNum = "";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject msgType = (JSONObject) jsonArray.get(i);
                String deviceId = msgType.getString(Constants.KEY_DEVICE_ID);
                Log.d(TAG, "parseBoundDevice: DeviceId=" + deviceId);
                if (deviceId.length() > 0) {
                    deviceIdsOnCloud.add(deviceId);
                }
                serialNum = msgType.getString(Constants.KEY_DEVICE_SERIAL);

                boolean onlineStatus = msgType.getBoolean(Constants.KEY_ONLINE_STATUS);
                Log.d(TAG, "parseBoundDevice: Online Status=" + onlineStatus);
                int status = Constants.DEVICE_OFFLINE;
                if (onlineStatus) {
                    status = Constants.DEVICE_ONLINE;
                }

                String home = "";
                String room = "";
                if (!msgType.toString().contains("null")) {
                    JSONObject deviceInfo = msgType.getJSONObject(Constants.KEY_DEVICE_INFO);
                    if (deviceInfo != null && deviceInfo.toString().contains(Constants.KEY_HOME)) {
                        home = deviceInfo.getString(Constants.KEY_HOME);
                    }
                    if (deviceInfo != null && deviceInfo.toString().contains(Constants.KEY_ROOM)) {
                        room = deviceInfo.getString(Constants.KEY_ROOM);
                    }
                }

                String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
                String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
                String[] selectionArgs = new String[]{account, serialNum};
                Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
                int count = cur.getCount();
                Log.d(TAG, "parseBoundDevice: Account count=" + count);

                ContentValues cv = new ContentValues();
                cv.put(IAQ.BindDevice.COLUMN_ACCOUNT, account);
                cv.put(IAQ.BindDevice.COLUMN_DEVICE_ID, deviceId);
                cv.put(IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER, serialNum);
                cv.put(IAQ.BindDevice.COLUMN_ONLINE_STATUS, status);
                if (home != null) {
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_HOME, home);
                }
                if (room != null) {
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_ROOM, room);
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
            }

            // Remove device data if the device was removed in another phone
            ArrayList<String> discardDeviceIds = new ArrayList<>();
            String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
            String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
            String[] selectionArgs = new String[]{account};
            Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
            int dbCount = cur.getCount();
            Log.d(TAG, "parseBoundDevice: DBCount=" + dbCount);
            if (dbCount > 0) {
                cur.moveToFirst();

                while (!cur.isAfterLast()) {
                    String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
                    if (isDiscardDeviceId(deviceIdsOnCloud, deviceId)) {
                        discardDeviceIds.add(deviceId);
                    }
                    cur.moveToNext();
                }
            }
            cur.close();

            for (String discardId : discardDeviceIds) {
                removeDeviceFromDB(account, discardId);
            }

            if (deviceIdsOnCloud.size() > 1) {
                mHandler.sendEmptyMessage(HAD_BIND);
            } else {
                Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, serialNum);
                mHandler.sendEmptyMessage(ONE_PHONE_BIND);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showToast(getApplicationContext(), getString(R.string.login_fail));
            mHandler.sendEmptyMessage(LOGIN_FAIL);
        }
    }

    private void removeAccountDataFromDB() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{account};
        getContentResolver().delete(IAQ.BindDevice.DICT_CONTENT_URI, selection, selectionArgs);
    }

    private void removeDeviceFromDB(String account, String deviceId) {
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};
        getContentResolver().delete(IAQ.BindDevice.DICT_CONTENT_URI, selection, selectionArgs);
    }

    private boolean isDiscardDeviceId(ArrayList<String> deviceIdsOnCloud, String id) {
        for (String deviceId : deviceIdsOnCloud) {
            if (deviceId.equals(id)) {
                return false;
            }
        }
        return true;
    }


}
