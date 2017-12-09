package com.honeywell.iaq.utils;

import android.content.Context;
import android.util.Log;

import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.DashboardActivity;
import com.honeywell.iaq.bean.WeatherItem;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.net.FastTube;
import com.honeywell.net.core.TubeOptions;
import com.honeywell.net.exception.TubeException;
import com.honeywell.net.listener.JSONTubeListener;
import com.honeywell.net.listener.StringTubeListener;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;


/**
 * Created by zhujunyu on 2017/2/24.
 */

public class IAQRequestUtils {
    private static final String PHONE_UUID = UUID.randomUUID().toString();
    private static final String TAG = IAQRequestUtils.class.getSimpleName();
    public static final int CONN_TIMEOUT = 8 * 1000;
    public static final int RECONN_TIMES = 2;

    /**
     * 登录
     *
     * @param context
     * @param account
     * @param password
     * @param countryCode
     * @param countryLanguage
     * @param httpCallback
     */
    public static void login(final Context context, String account, String password, String countryCode, String countryLanguage, final HttpCallback httpCallback) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_LOGIN_USER);
        params.put(Constants.KEY_PASSWORD, password);
        params.put(Constants.KEY_PHONE_NUMBER, countryCode + account);
//                    params.put(Const.KEY_PASSWORD, "hhjwq15151860095");
//                    params.put(Const.KEY_PHONE_NUMBER, "+8615151860095");
        params.put(Constants.KEY_PHONE_UUID, PHONE_UUID);
        params.put(Constants.KEY_PHONE_TYPE, Constants.PHONE_TYPE_ANDROID);
        params.put(Constants.KEY_LANGUAGE, countryLanguage);

        HttpClientHelper.newInstance().httpRequest(context, Constants.USER_URL, params, HttpClientHelper.NO_COOKIE, new MyHttpResponseHandler() {
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                String responseStr = new String(responseBody, 0, responseBody.length);
                for (int i = 0; i < headers.length; i++) {
                    if (headers[i].getName().equals(Constants.KEY_SET_COOKIE)) {
                        String value = headers[i].getValue();
                        Log.d(TAG, "Get server Cookie: " + value);
                        Utils.setSharedPreferencesValue(context, Constants.KEY_COOKIE, value);
                    }
                }
                httpCallback.success(responseStr, false);
            }

            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                if (responseBody != null && responseBody.length > 0) {
                    String responseStr = new String(responseBody, 0, responseBody.length);
                    httpCallback.failed(responseStr);
                } else {
                    Utils.showToast(context, context.getString(R.string.login_fail));
                }

            }
        }, HttpClientHelper.POST, null);
    }

    public static void login2(final Context context, String account, String password, String countryCode, String countryLanguage, final HttpCallback httpCallback) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_LOGIN_USER);
        params.put(Constants.KEY_PASSWORD, password);
        params.put(Constants.KEY_PHONE_NUMBER, countryCode + account);
//                    params.put(Const.KEY_PASSWORD, "hhjwq15151860095");
//                    params.put(Const.KEY_PHONE_NUMBER, "+8615151860095");
        params.put(Constants.KEY_PHONE_UUID, PHONE_UUID);
        params.put(Constants.KEY_PHONE_TYPE, Constants.PHONE_TYPE_ANDROID);
        params.put(Constants.KEY_LANGUAGE, countryLanguage);

        JSONObject bodyAsJson = new JSONObject(params);
        String JsonStr = bodyAsJson.toString();

        HttpEntity entity1 = null;

        try {
            entity1 = new StringEntity(JsonStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map<String, String> map = new HashMap<>();
        String cookie = Utils.getSharedPreferencesValue(context, Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
        map.put("Cookie", cookie);
        HttpUtils.getJSON(context, map, Constants.USER_URL, entity1, new JSONTubeListener<JSONObject>() {

            @Override
            public JSONObject doInBackground(JSONObject water) throws Exception {
                return water;
            }

            @Override
            public void onSuccess(JSONObject water) {

                String cookie = water.optString(Constants.KEY_COOKIE);
                Utils.setSharedPreferencesValue(context, Constants.KEY_COOKIE, cookie);
                httpCallback.success(water.toString(), false);
            }

            @Override
            public void onFailed(TubeException e) {
                httpCallback.failed("");
            }
        });


    }

    /**
     * 更新设备信息
     *
     * @param context
     * @param jsonObject
     * @param httpCallback
     */
    public static void updateDeviceInformation(Context context, JSONObject jsonObject, final HttpCallback httpCallback) {

        String cookie = Utils.getSharedPreferencesValue(context, Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
        if (cookie.length() <= 0) {
            return;
        }


        HttpClientHelper.newInstance().httpPostRequest(context, Constants.DEVICE_URL, jsonObject, HttpClientHelper.COOKIE, new MyHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                super.onSuccess(statusCode, headers, responseBody);
                String responseStr = new String(responseBody, 0, responseBody.length);
                httpCallback.success(responseStr, false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                super.onFailure(statusCode, headers, responseBody, error);
                String responseStr = new String(responseBody, 0, responseBody.length);
                httpCallback.failed(responseStr);
            }
        }, cookie);
    }


    /**
     * 获取设备的位置
     *
     * @param deviceId
     */
    public static void getDeviceLocation(Context context, final String deviceId, final HttpCallback httpCallback, final boolean isLast) {


        final String cookie = Utils.getSharedPreferencesValue(context.getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
        if (cookie.length() <= 0) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_LOCATION);
        params.put(Constants.KEY_DEVICE_ID, deviceId);

        HttpUtils.getString(context, Constants.DEVICE_LOCATION_URL, getRequestEntity(params), new TubeTask(context, Constants.GetDataFlag.HON_IAQ_GET_DEVICE_LOCATION, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode == 0) {
                    String locationName = (String) objects[0];
                    httpCallback.success(locationName, false);
                } else {
                    httpCallback.success("", false);
                }

            }
        }));

//
//        HttpClientHelper.newInstance().httpRequest(context.getApplicationContext(), Constants.DEVICE_LOCATION_URL, params, HttpClientHelper.COOKIE, new MyHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                String responseStr = new String(responseBody, 0, responseBody.length);
//                httpCallback.success(responseStr, isLast);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                String responseStr = new String(responseBody, 0, responseBody.length);
//                httpCallback.failed(responseStr);
//            }
//        }, HttpClientHelper.POST, cookie);

    }

    /**
     * 设定报告频率
     *
     * @param context
     * @param deviceId
     * @param callback
     */
    public static void setDataReportFrequency(Context context, final String deviceId, AsyncHttpResponseHandler callback) {
        final String cookie = Utils.getSharedPreferencesValue(context.getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
        if (cookie.length() <= 0) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_SET_REPORT_FREQUENCY);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        params.put(Constants.KEY_FREQUENCY, String.valueOf(DashboardActivity.FREQUENCY_INTERVAL));
        HttpClientHelper.newInstance().httpRequest(context, Constants.DEVICE_URL, params, HttpClientHelper.COOKIE, callback, HttpClientHelper.POST, cookie);
    }

    /**
     * 获取验证码
     *
     * @param context
     * @param phoneNum
     * @param countryCode
     * @param countryLanguage
     * @param httpCallback
     */
    public static void getValidateCode(Context context, String phoneNum, String countryCode, String countryLanguage, final HttpCallback httpCallback) {
        if (!Utils.checkPhoneNumber(phoneNum,countryCode)) {
            Utils.showToast(context, context.getString(R.string.wrong_phone_number));
            return;
        }
        String phoneNumber = countryCode + phoneNum;
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_SEND_V_CODE);
        params.put(Constants.KEY_LANGUAGE, countryLanguage);
        params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);

        HttpUtils.getString(context, Constants.USER_URL,  getRequestEntity(params), new TubeTask(context, Constants.GetDataFlag.HON_IAQ_GET_VALIDATE_CODE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode == 0) {
                    httpCallback.success("", true);
                }
            }
        }));


//        HttpClientHelper.newInstance().httpRequest(context, Constants.USER_URL, params, HttpClientHelper.NO_COOKIE, new MyHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                String responseStr = new String(responseBody, 0, responseBody.length);
//                httpCallback.success(responseStr, false);
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                String responseStr = new String(responseBody, 0, responseBody.length);
//                httpCallback.failed(responseStr);
//            }
//        }, HttpClientHelper.POST, null);
    }

    /**
     * 获取设备天气
     *
     * @param context
     * @param deviceId
     * @param httpCallback
     */
    public static void getWeather(Context context, final String deviceId, final HttpCallback httpCallback, final boolean isLast) {

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_WEATHER);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        JSONObject bodyAsJson = new JSONObject(params);
        String JsonStr = bodyAsJson.toString();
        HttpEntity entity1 = null;

//

        final String cookie = Utils.getSharedPreferencesValue(context, Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
        if (cookie.length() <= 0) {
            return;
        }


        HttpClientHelper.newInstance().httpRequest(context, Constants.DEVICE_WEATHER_URL, params, HttpClientHelper.COOKIE, new MyHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String responseStr = new String(responseBody, 0, responseBody.length);
                httpCallback.success(responseStr, isLast);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if (responseBody == null) {
                    return;
                }
                String responseStr = new String(responseBody, 0, responseBody.length);
                httpCallback.failed(responseStr);
            }
        }, HttpClientHelper.POST, cookie);

    }


    public static void getWeather2(Context context, String deviceId) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_WEATHER);
        params.put(Constants.KEY_DEVICE_ID, deviceId);

        HttpUtils.getString(context,Constants.DEVICE_WEATHER_URL,getRequestEntity(params),new TubeTask(context, Constants.GetDataFlag.HON_IAQ_GET_WEATHER, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if(resultList.size()<=0){
                    return;
                }

                WeatherItem weatherItem = (WeatherItem) resultList.get(0);

            }
        }));

    }


    /**
     * 解绑设备
     *
     * @param context
     * @param deviceId
     * @param httpCallback
     */
    public static void unbindDevice(Context context, final String deviceId, final HttpCallback httpCallback) {
        final String cookie = Utils.getSharedPreferencesValue(context, Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
        if (cookie.length() <= 0) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_UNBIND_DEVICE);
        params.put(Constants.KEY_DEVICE_ID, deviceId);

        HttpClientHelper.newInstance().httpRequest(context, Constants.BIND_DEVICE_URL, params, HttpClientHelper.COOKIE, new MyHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                String responseStr = new String(responseBody, 0, responseBody.length);
                httpCallback.success(responseStr, false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String responseStr = new String(responseBody, 0, responseBody.length);
                httpCallback.failed(responseStr);
            }
        }, HttpClientHelper.POST, cookie);
    }


    public static abstract class MyHttpResponseHandler extends AsyncHttpResponseHandler {

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            String responseStr = new String(responseBody, 0, responseBody.length);
            Log.d(TAG, " onSuccess: responseStr=" + responseStr);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            String responseStr = new String(responseBody, 0, responseBody.length);
            Log.d(TAG, " onFailure: responseStr=" + responseStr);
        }
    }


    /**
     * 正常请求header
     *
     * @param context
     * @return
     */
    public static Map<String, String> getHeaderParamsMap(Context context) {
        final String cookie = Utils.getSharedPreferencesValue(context, Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
        Map<String, String> map = new HashMap<>();
        map.put(Constants.KEY_COOKIE, cookie);
        return map;
    }


    public static HttpEntity getRequestEntity(Map<String, String> params) {
        JSONObject bodyAsJson = new JSONObject(params);
        String JsonStr = bodyAsJson.toString();
        HttpEntity entity1 = null;
        try {
            entity1 = new StringEntity(JsonStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Logger.e("request params",""+JsonStr);
        return entity1;
    }

    public static HttpEntity getRequestEntity(JSONObject json) {
        String JsonStr = json.toString();
        HttpEntity entity1 = null;
        try {
            entity1 = new StringEntity(JsonStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Logger.e("request params",""+JsonStr);
        return entity1;
    }


    public interface HttpCallback {
        void success(String responseStr, boolean isLast);

        void failed(String responseStr);
    }

}
