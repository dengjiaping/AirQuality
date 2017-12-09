package com.honeywell.iaq.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeywell.iaq.base.IAQOtherTitleBarActivity;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.base.IAQType;
import com.honeywell.iaq.bean.WeatherInfo;
import com.honeywell.iaq.bean.WeatherItem;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQWeatherEvent;
import com.honeywell.iaq.fragment.OutdoorFragment;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import cz.msebera.android.httpclient.Header;
import de.greenrobot.event.EventBus;

/**
 * Created by H187512 on 9/24/2016.
 */
public class OutdoorActivity extends IAQOtherTitleBarActivity {

    private ArrayList<com.honeywell.iaq.bean.WeatherInfo> weatherInfos;
    public HashMap<String, String> WEATHER_MAP;

    private RelativeLayout mRelativeLayout;
    private TextView mDate, mPM25, mHumidity, mTemp, mWeather, mNotification;

    private TextView mWeekly1, mTemp1, mWeekly2, mTemp2, mWeekly3, mTemp3, mWeekly4, mTemp4, mWeekly5, mTemp5;

    private ImageView mNextPage, mWeatherIcon, mWeather1, mWeather2, mWeather3, mWeather4, mWeather5;

    private ProgressDialog mDialog;

    private String location, deviceId, suggestion;
    private TextView mLocation;
    private static final String TAG = "Outdoor";
    private Context mContext = OutdoorActivity.this;
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    @Override
    protected int getContent() {
        return R.layout.activity_outdoor;
    }

    @Override
    protected void initView() {
        super.initView();
        weatherInfos = new ArrayList<>();
        initializeUI();
        findViewById(R.id.header).setVisibility(View.GONE);
        getDeviceIdLocation();
        mLocation.setText(getLocation(this.deviceId));

        if (Utils.isNetworkAvailable(this)) {
            //读取缓存数据
            getForecastCacheData();
//            getSuggestCacheData();
            getWeatherData(deviceId);

            refresh();
        } else {
            Utils.showToast(this, getString(R.string.no_network));
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initializeUI() {
        mRelativeLayout = (RelativeLayout) findViewById(R.id.outdoor_layout);
        //根据时间替换背景
        Date date =new Date();
        SimpleDateFormat sdf =new SimpleDateFormat("HH:mm:ss");//只有时分秒
        String time=sdf.format(date);

        Logger.e(TAG,""+time);

       boolean isDay = Utils.isInTime("6:00:00-18:00:00",time);

        if(isDay){
            mRelativeLayout.setBackground(this.getResources().getDrawable(R.mipmap.outdoor_background3));
        }else {
            mRelativeLayout.setBackground(this.getResources().getDrawable(R.mipmap.outdoor_background1));
        }


        WEATHER_MAP = new HashMap<String, String>() {
            {
                put(Constants.WEATHER_TYPE_SUNNY, getString(R.string.sunny));
                put(Constants.WEATHER_TYPE_CLEAR, getString(R.string.sunny));
                put(Constants.WEATHER_TYPE_FAIR_DAY, getString(R.string.sunny));
                put(Constants.WEATHER_TYPE_FAIR_NIGHT, getString(R.string.sunny));
                put(Constants.WEATHER_TYPE_CLOUD, getString(R.string.cloudy));
                put(Constants.WEATHER_TYPE_PARTLY_CLOUDY_DAY, getString(R.string.partly_cloudy));
                put(Constants.WEATHER_TYPE_PARTLY_CLOUDY_NIGHT, getString(R.string.partly_cloudy));
                put(Constants.WEATHER_TYPE_MOSTLY_CLOUDY_DAY, getString(R.string.mostly_cloudy));
                put(Constants.WEATHER_TYPE_MOSTLY_CLOUDY_NIGHT, getString(R.string.mostly_cloudy));
                put(Constants.WEATHER_TYPE_OVERCAST, getString(R.string.overcast));
                put(Constants.WEATHER_TYPE_SHOWER, getString(R.string.shower));
                put(Constants.WEATHER_TYPE_THUNDERSHOWER, getString(R.string.thundershower));
                put(Constants.WEATHER_TYPE_THUNDERSHOWER_WITH_HAIL, getString(R.string.thundershower_with_hail));
                put(Constants.WEATHER_TYPE_LIGHT_RAIN, getString(R.string.light_rain));
                put(Constants.WEATHER_TYPE_MODERATE_RAIN, getString(R.string.moderate_rain));
                put(Constants.WEATHER_TYPE_HEAVY_RAIN, getString(R.string.heavy_rain));
                put(Constants.WEATHER_TYPE_STORM, getString(R.string.storm));
                put(Constants.WEATHER_TYPE_HEAVY_STORM, getString(R.string.heavy_storm));
                put(Constants.WEATHER_TYPE_SEVERE_STORM, getString(R.string.severe_storm));
                put(Constants.WEATHER_TYPE_ICE_RAIN, getString(R.string.ice_rain));
                put(Constants.WEATHER_TYPE_SLEET, getString(R.string.sleet));
                put(Constants.WEATHER_TYPE_SNOW_FLURRY, getString(R.string.snow_flurry));
                put(Constants.WEATHER_TYPE_LIGHT_SNOW, getString(R.string.light_snow));
                put(Constants.WEATHER_TYPE_MODERATE_SNOW, getString(R.string.moderate_snow));
                put(Constants.WEATHER_TYPE_HEAVY_SNOW, getString(R.string.heavy_snow));
                put(Constants.WEATHER_TYPE_SNOW_STORM, getString(R.string.snowstorm));
                put(Constants.WEATHER_TYPE_DUST, getString(R.string.dust));
                put(Constants.WEATHER_TYPE_SAND, getString(R.string.sand));
                put(Constants.WEATHER_TYPE_DUST_STORM, getString(R.string.dust_storm));
                put(Constants.WEATHER_TYPE_SAND_STORM, getString(R.string.sandstorm));
                put(Constants.WEATHER_TYPE_FOGGY, getString(R.string.foggy));
                put(Constants.WEATHER_TYPE_HAZE, getString(R.string.haze));
                put(Constants.WEATHER_TYPE_WINDY, getString(R.string.windy));
                put(Constants.WEATHER_TYPE_BLUSTERY, getString(R.string.blustery));
                put(Constants.WEATHER_TYPE_HURRICANE, getString(R.string.hurricane));
                put(Constants.WEATHER_TYPE_TROPICAL_STORM, getString(R.string.tropical_storm));
                put(Constants.WEATHER_TYPE_TORNADO, getString(R.string.tornado));
                put(Constants.WEATHER_TYPE_COLD, getString(R.string.cold));
                put(Constants.WEATHER_TYPE_UNKNOWN, getString(R.string.unknown));
            }
        };
        mDate = (TextView) findViewById(R.id.date);
        mDate.setText(getDate(this));
        mLocation = (TextView) findViewById(R.id.location);

        mPM25 = (TextView) findViewById(R.id.device_pm);
        mHumidity = (TextView) findViewById(R.id.device_humidity);
        mTemp = (TextView) findViewById(R.id.temp);
        mWeather = (TextView) findViewById(R.id.weather);
        mNotification = (TextView) findViewById(R.id.notification);

        mWeatherIcon = (ImageView) findViewById(R.id.weather_icon);

        mWeekly1 = (TextView) findViewById(R.id.weekly1);
        mWeekly2 = (TextView) findViewById(R.id.weekly2);
        mWeekly3 = (TextView) findViewById(R.id.weekly3);
        mWeekly4 = (TextView) findViewById(R.id.weekly4);
        mWeekly5 = (TextView) findViewById(R.id.weekly5);

        mWeather1 = (ImageView) findViewById(R.id.weather1);
        mWeather2 = (ImageView) findViewById(R.id.weather2);
        mWeather3 = (ImageView) findViewById(R.id.weather3);
        mWeather4 = (ImageView) findViewById(R.id.weather4);
        mWeather5 = (ImageView) findViewById(R.id.weather5);

        mTemp1 = (TextView) findViewById(R.id.temp1);
        mTemp2 = (TextView) findViewById(R.id.temp2);
        mTemp3 = (TextView) findViewById(R.id.temp3);
        mTemp4 = (TextView) findViewById(R.id.temp4);
        mTemp5 = (TextView) findViewById(R.id.temp5);

        mNextPage = (ImageView) findViewById(R.id.next_page);
        mNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
//                ((HomeActivity) getActivity()).changePage(1);
            }
        });
    }

    public static String getDate(Context context) {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mYear = String.valueOf(c.get(Calendar.YEAR));
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = context.getString(R.string.seven);
        } else if ("2".equals(mWay)) {
            mWay = context.getString(R.string.one);
        } else if ("3".equals(mWay)) {
            mWay = context.getString(R.string.two);
        } else if ("4".equals(mWay)) {
            mWay = context.getString(R.string.three);
        } else if ("5".equals(mWay)) {
            mWay = context.getString(R.string.four);
        } else if ("6".equals(mWay)) {
            mWay = context.getString(R.string.five);
        } else if ("7".equals(mWay)) {
            mWay = context.getString(R.string.six);
        }
        return mWay + " " + mMonth + context.getString(R.string.month) + mDay + context.getString(R.string.day) + mYear + context.getString(R.string.year);
    }


    private void getDeviceIdLocation() {
        String[] strings = PreferenceUtil.getDeviceIdLoaction(this);
        location = strings[1];
        deviceId = strings[0];
        Log.d(TAG, "deviceId=" + deviceId + "location=" + location);
    }


    private String getLocation(String deviceId) {
        String location = "";
        String account = Utils.getSharedPreferencesValue(this, Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};
        Cursor cur = this.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        int count = cur.getCount();
        if (count > 0) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                location = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_LOCATION));
                Log.d(TAG, "getLocation: location=" + location);
                cur.moveToNext();
            }
        }
        cur.close();
        return location;
    }

    private void refresh() {
        if (deviceId != null && deviceId.length() > 0) {
            mLocation.setText(getLocation(deviceId));
            getWeather(deviceId);
            getForecastData(deviceId);
            getLifeSuggestion(deviceId);
        }
    }


    private void getWeather(final String deviceId) {

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_WEATHER);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(this, Constants.DEVICE_WEATHER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_GET_WEATHER, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultList.size() <= 0) {
                    return;
                }

                WeatherItem weatherItem = (WeatherItem) resultList.get(0);
                ContentValues cv = new ContentValues();
                cv.put(IAQ.BindDevice.COLUMN_WEATHER, weatherItem.getWeather());
                cv.put(IAQ.BindDevice.COLUMN_TEMPERATURE, weatherItem.getTemperature());
                cv.put(IAQ.BindDevice.COLUMN_HUMIDITY, weatherItem.getHumidity());
                cv.put(IAQ.BindDevice.COLUMN_PM25, weatherItem.getPm25());
                cv.put(IAQ.BindDevice.COLUMN_PM10, weatherItem.getPm10());
                cv.put(IAQ.BindDevice.COLUMN_AQI, weatherItem.getAqi());
                cv.put(IAQ.BindDevice.COLUMN_TIME, weatherItem.getTime());

                String account = Utils.getSharedPreferencesValue(mContext, Constants.KEY_ACCOUNT, "");
                String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
                String[] selectionArgs = new String[]{account, deviceId};
                mContext.getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

                if (deviceId != null && deviceId.length() > 0) {
                    getWeatherData(deviceId);
                }

            }
        }));

    }

    private void getForecastCacheData() {
        Log.d(TAG, "getForecastData: DeviceId=" + deviceId);
        String string = Utils.getSharedPreferencesValue(this, Constants.KEY_FORECAST_DATA, null);
        if (string == null) {
            return;
        }
        if (TextUtils.isEmpty(string)) {
            return;
        }

        weatherInfos.clear();
        try {

            JSONObject jsonObject = new JSONObject(string);
            JSONArray jsonArray = null;
            jsonArray = jsonObject.getJSONArray(Constants.KEY_LIST);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject msgType = (JSONObject) jsonArray.get(i);
                WeatherInfo weatherInfo = new WeatherInfo();
                weatherInfo.setWeatherDay(msgType.getString(Constants.KEY_WEATHER_DAY));
                weatherInfo.setWeatherNight(msgType.getString(Constants.KEY_WEATHER_NIGHT));
                weatherInfo.setTemperatureLow(msgType.getString(Constants.KEY_TEMPERATURE_LOW));
                weatherInfo.setTemperatureHigh(msgType.getString(Constants.KEY_TEMPERATURE_HIGH));
                weatherInfo.setDate(msgType.getString(Constants.KEY_DATE));
                weatherInfos.add(weatherInfo);
            }
            setData();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getWeatherCacheData() {
        String string = Utils.getSharedPreferencesValue(this, Constants.KEY_WEATHER_DATA, null);
        if (string == null) {
            return;
        }
        if (TextUtils.isEmpty(string)) {
            return;
        }

    }

    private void getSuggestCacheData() {
        String string = Utils.getSharedPreferencesValue(this, Constants.KEY_SUGGEST_DATA, null);
        if (string == null) {
            return;
        }
        if (TextUtils.isEmpty(string)) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(string);
            JSONObject airPollution = jsonObject.getJSONObject(Constants.KEY_AIR_POLLUTION);
            suggestion = airPollution.getString(Constants.KEY_DETAILS);

            SpannableString spStr = new SpannableString(suggestion);
            spStr.setSpan(new ClickableSpan() {
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(mContext.getResources().getColor(R.color.opacity_white));       //设置文件颜色
                    ds.setUnderlineText(true);      //设置下划线
                }

                @Override
                public void onClick(View widget) {
                }
            }, 0, suggestion.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mNotification.setText(spStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    private void getForecastData(final String deviceId) {
        weatherInfos.clear();
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_FORECAST);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(mContext, Constants.DEVICE_WEATHER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(mContext, Constants.GetDataFlag.HON_IAQ_GET_FORECAST, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode == 0) {
                    weatherInfos = resultList;

                    setData();
                } else {
                    EventBus.getDefault().post(new IAQWeatherEvent(IAQWeatherEvent.GET_DATA_FAIL));
                }
            }
        }));
    }


    private void getLifeSuggestion(final String deviceId) {


        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_LIFE_SUGGESTION);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(mContext, Constants.DEVICE_LIFE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(mContext, Constants.GetDataFlag.HON_IAQ_LIFE_SUGGEST, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if(null ==objects[0]){
                    return;
                }
                String responseStr = (String) objects[0];
                if (resultCode == 0) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONObject airPollution = jsonObject.getJSONObject(Constants.KEY_AIR_POLLUTION);
                        suggestion = airPollution.getString(Constants.KEY_DETAILS);

                        SpannableString spStr = new SpannableString(suggestion);
                        spStr.setSpan(new ClickableSpan() {
                            @Override
                            public void updateDrawState(TextPaint ds) {
                                super.updateDrawState(ds);
                                ds.setColor(mContext.getResources().getColor(R.color.opacity_white));       //设置文件颜色
                                ds.setUnderlineText(true);      //设置下划线
                            }

                            @Override
                            public void onClick(View widget) {
                            }
                        }, 0, suggestion.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        mNotification.setText(spStr);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    EventBus.getDefault().post(new IAQWeatherEvent(IAQWeatherEvent.GET_DATA_FAIL));
                }

            }
        }));
    }


    private void getWeatherData(String deviceId) {
        String outdoorPM25 = null;
        String outdoorHumidity = null;
        String weather = null;
        String temp = null;

        String account = Utils.getSharedPreferencesValue(mContext, Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};
        Cursor cur = mContext.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Log.d(TAG, "getWeatherData: DBCount=" + dbCount);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            outdoorPM25 = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_PM25));
            outdoorHumidity = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_HUMIDITY));
            weather = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_WEATHER));
            temp = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_TEMPERATURE));
            cur.moveToNext();
        }
        cur.close();

        Log.d(TAG, "PM25=" + outdoorPM25 + ", humidity=" + outdoorHumidity);
        if (outdoorPM25 == null || outdoorPM25.length() == 0) {
            mPM25.setText(getString(R.string.default_pm));
        } else {
            mPM25.setText(outdoorPM25);
            try {
                int value = (int) Float.parseFloat(outdoorPM25);
                if (Utils.getPMLevel(mContext, value) == Constants.PM_LEVEL_1) {
                    mPM25.setTextColor(Color.parseColor("#1792E5"));
                } else if (Utils.getPMLevel(mContext, value) == Constants.PM_LEVEL_2) {
                    mPM25.setTextColor(Color.parseColor("#7EB338"));
                } else if (Utils.getPMLevel(mContext, value) == Constants.PM_LEVEL_3) {
                    mPM25.setTextColor(Color.parseColor("#FFC627"));
                } else if (Utils.getPMLevel(mContext, value) == Constants.PM_LEVEL_4) {
                    mPM25.setTextColor(Color.parseColor("#F37022"));
                } else if (Utils.getPMLevel(mContext, value) == Constants.PM_LEVEL_5) {
                    mPM25.setTextColor(Color.parseColor("#EE3124"));
                } else if (Utils.getPMLevel(mContext, value) == Constants.PM_LEVEL_6) {
                    mPM25.setTextColor(Color.parseColor("#9B59B6"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Constants.GEN_1.equals(IAQType.getGeneration(this))) {
            if (!TextUtils.isEmpty(temp)) {
                if(Utils.isCelsius(getApplicationContext())){
                    mTemp.setText(temp + getString(R.string.temperature_unit));
                }else {
                    mTemp.setText(String.valueOf(Utils.C2W(Float.parseFloat(temp))) + getString(R.string.temperature_f_unit));
                }
            }
        } else if (Constants.GEN_2.equals(IAQType.getGeneration(this))) {
            if (!TextUtils.isEmpty(temp)) {
                if (Utils.isCelsius(getApplicationContext(), deviceId)) {
                    mTemp.setText(temp + getString(R.string.temperature_unit));
                } else {
                    mTemp.setText(String.valueOf(Utils.C2W(Float.parseFloat(temp))) + getString(R.string.temperature_f_unit));
                }
            }
        }


        if (outdoorHumidity == null || outdoorHumidity.length() == 0) {
            mHumidity.setText(getString(R.string.unknown));
        } else {
            outdoorHumidity = outdoorHumidity + getString(R.string.percent);
            mHumidity.setText(outdoorHumidity);
        }
        mWeather.setText(this.WEATHER_MAP.get(weather));
    }


    private void setData() {
//        if (mDialog != null) {
//            mDialog.dismiss();
//        }

//        mTemp.setText(weatherInfos.get(0).getTemperatureLow() + "/" + weatherInfos.get(0).getTemperatureHigh() + getString(R.string.temperature_unit));

        if (Constants.GEN_1.equals(IAQType.getGeneration(this))) {
            if (Utils.isCelsius(getApplicationContext())) {
                mTemp1.setText(weatherInfos.get(1).getTemperatureLow() + "/" + weatherInfos.get(1).getTemperatureHigh() + getString(R.string.temperature_unit));
                mTemp2.setText(weatherInfos.get(2).getTemperatureLow() + "/" + weatherInfos.get(2).getTemperatureHigh() + getString(R.string.temperature_unit));
                mTemp3.setText(weatherInfos.get(3).getTemperatureLow() + "/" + weatherInfos.get(3).getTemperatureHigh() + getString(R.string.temperature_unit));
                mTemp4.setText(weatherInfos.get(4).getTemperatureLow() + "/" + weatherInfos.get(4).getTemperatureHigh() + getString(R.string.temperature_unit));
                mTemp5.setText(weatherInfos.get(5).getTemperatureLow() + "/" + weatherInfos.get(5).getTemperatureHigh() + getString(R.string.temperature_unit));
            } else {
                mTemp1.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(1).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(1).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
                mTemp2.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(2).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(2).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
                mTemp3.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(3).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(3).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
                mTemp4.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(4).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(4).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
                mTemp5.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(5).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(5).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
            }
        } else if (Constants.GEN_2.equals(IAQType.getGeneration(this))) {
            if (Utils.isCelsius(getApplicationContext(), deviceId)) {
                mTemp1.setText(weatherInfos.get(1).getTemperatureLow() + "/" + weatherInfos.get(1).getTemperatureHigh() + getString(R.string.temperature_unit));
                mTemp2.setText(weatherInfos.get(2).getTemperatureLow() + "/" + weatherInfos.get(2).getTemperatureHigh() + getString(R.string.temperature_unit));
                mTemp3.setText(weatherInfos.get(3).getTemperatureLow() + "/" + weatherInfos.get(3).getTemperatureHigh() + getString(R.string.temperature_unit));
                mTemp4.setText(weatherInfos.get(4).getTemperatureLow() + "/" + weatherInfos.get(4).getTemperatureHigh() + getString(R.string.temperature_unit));
                mTemp5.setText(weatherInfos.get(5).getTemperatureLow() + "/" + weatherInfos.get(5).getTemperatureHigh() + getString(R.string.temperature_unit));
            } else {
                mTemp1.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(1).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(1).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
                mTemp2.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(2).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(2).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
                mTemp3.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(3).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(3).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
                mTemp4.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(4).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(4).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
                mTemp5.setText(String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(5).getTemperatureLow())))+ "/" + String.valueOf(Utils.C2W(Float.parseFloat(weatherInfos.get(5).getTemperatureHigh()))) + getString(R.string.temperature_f_unit));
            }
        }

        mWeekly1.setText(getWeek(weatherInfos.get(1).getDate()));
        mWeekly2.setText(getWeek(weatherInfos.get(2).getDate()));
        mWeekly3.setText(getWeek(weatherInfos.get(3).getDate()));
        mWeekly4.setText(getWeek(weatherInfos.get(4).getDate()));
        mWeekly5.setText(getWeek(weatherInfos.get(5).getDate()));

        mWeather1.setImageResource(getWeatherIconDrawable(weatherInfos.get(1).getWeatherDay()));
        mWeather2.setImageResource(getWeatherIconDrawable(weatherInfos.get(2).getWeatherDay()));
        mWeather3.setImageResource(getWeatherIconDrawable(weatherInfos.get(3).getWeatherDay()));
        mWeather4.setImageResource(getWeatherIconDrawable(weatherInfos.get(4).getWeatherDay()));
        mWeather5.setImageResource(getWeatherIconDrawable(weatherInfos.get(5).getWeatherDay()));


        mWeatherIcon.setImageResource(getWeatherIconDrawable(mWeather.getText().toString()));
    }

    public String getWeek(String strDate) {
        String week = null;
        try {
            Locale locale;
            if (Utils.isZh(mContext)) {
                locale = Locale.CHINESE;
            } else {
                locale = Locale.ENGLISH;
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", locale);
            Date date = format.parse(strDate);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE", locale);
            week = sdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
//        Log.d(TAG, "Date string=" + strDate + ", Week=" + week);
        return week;
    }

    public int getWeatherIconDrawable(String weather) {
        Log.d(TAG, "getWeatherIconDrawable: Weather=" + weather);
        if (Constants.WEATHER_TYPE_BLUSTERY.equals(weather) || getString(R.string.blustery).equals(weather)) {
            return R.mipmap.blustery;
        } else if (Constants.WEATHER_TYPE_CLEAR.equals(weather)) {
            return R.mipmap.clear;
        } else if (Constants.WEATHER_TYPE_CLOUD.equals(weather) || getString(R.string.cloudy).equals(weather)) {
            return R.mipmap.cloudy;
        } else if (Constants.WEATHER_TYPE_COLD.equals(weather) || getString(R.string.cold).equals(weather)) {
            return R.mipmap.cold;
        } else if (Constants.WEATHER_TYPE_DUST.equals(weather) || getString(R.string.dust).equals(weather)) {
            return R.mipmap.dust;
        } else if (Constants.WEATHER_TYPE_DUST_STORM.equals(weather) || getString(R.string.storm).equals(weather)) {
            return R.mipmap.duststorm;
        } else if (Constants.WEATHER_TYPE_FAIR_DAY.equals(weather)) {
            return R.mipmap.fairday;
        } else if (Constants.WEATHER_TYPE_FAIR_NIGHT.equals(weather)) {
            return R.mipmap.fairnight;
        } else if (Constants.WEATHER_TYPE_FOGGY.equals(weather) || getString(R.string.foggy).equals(weather)) {
            return R.mipmap.foggy;
        } else if (Constants.WEATHER_TYPE_HAZE.equals(weather) || getString(R.string.haze).equals(weather)) {
            return R.mipmap.haze;
        } else if (Constants.WEATHER_TYPE_HEAVY_RAIN.equals(weather) || getString(R.string.heavy_rain).equals(weather)) {
            return R.mipmap.heavyrain;
        } else if (Constants.WEATHER_TYPE_HEAVY_SNOW.equals(weather) || getString(R.string.heavy_snow).equals(weather)) {
            return R.mipmap.heavysnow;
        } else if (Constants.WEATHER_TYPE_HEAVY_STORM.equals(weather) || getString(R.string.heavy_storm).equals(weather)) {
            return R.mipmap.heavystorm;
        } else if (Constants.WEATHER_TYPE_HOT.equals(weather) || getString(R.string.hot).equals(weather)) {
            return R.mipmap.hot;
        } else if (Constants.WEATHER_TYPE_HURRICANE.equals(weather) || getString(R.string.hurricane).equals(weather)) {
            return R.mipmap.hurricane;
        } else if (Constants.WEATHER_TYPE_ICE_RAIN.equals(weather) || getString(R.string.ice_rain).equals(weather)) {
            return R.mipmap.icerain;
        } else if (Constants.WEATHER_TYPE_LIGHT_RAIN.equals(weather) || getString(R.string.light_rain).equals(weather)) {
            return R.mipmap.lightrain;
        } else if (Constants.WEATHER_TYPE_LIGHT_SNOW.equals(weather) || getString(R.string.light_snow).equals(weather)) {
            return R.mipmap.lightsnow;
        } else if (Constants.WEATHER_TYPE_MODERATE_RAIN.equals(weather) || getString(R.string.moderate_rain).equals(weather)) {
            return R.mipmap.moderaterain;
        } else if (Constants.WEATHER_TYPE_MODERATE_SNOW.equals(weather) || getString(R.string.moderate_snow).equals(weather)) {
            return R.mipmap.moderatesnow;
        } else if (Constants.WEATHER_TYPE_MOSTLY_CLOUDY_DAY.equals(weather) || getString(R.string.mostly_cloudy).equals(weather)) {
            return R.mipmap.mostlycloudyday;
        } else if (Constants.WEATHER_TYPE_MOSTLY_CLOUDY_NIGHT.equals(weather) || getString(R.string.mostly_cloudy).equals(weather)) {
            return R.mipmap.mostlycloudynight;
        } else if (Constants.WEATHER_TYPE_OVERCAST.equals(weather)  || getString(R.string.overcast).equals(weather)) {
            return R.mipmap.overcast;
        } else if (Constants.WEATHER_TYPE_PARTLY_CLOUDY_DAY.equals(weather) || getString(R.string.partly_cloudy).equals(weather)) {
            return R.mipmap.partlycloudyday;
        } else if (Constants.WEATHER_TYPE_PARTLY_CLOUDY_NIGHT.equals(weather) || getString(R.string.mostly_cloudy).equals(weather)) {
            return R.mipmap.partlycloudynight;
        } else if (Constants.WEATHER_TYPE_SAND.equals(weather) || getString(R.string.sand).equals(weather)) {
            return R.mipmap.sand;
        } else if (Constants.WEATHER_TYPE_SAND_STORM.equals(weather) || getString(R.string.sandstorm).equals(weather)) {
            return R.mipmap.sandstorm;
        } else if (Constants.WEATHER_TYPE_SEVERE_STORM.equals(weather) || getString(R.string.severe_storm).equals(weather)) {
            return R.mipmap.severestorm;
        } else if (Constants.WEATHER_TYPE_SHOWER.equals(weather) || getString(R.string.shower).equals(weather)) {
            return R.mipmap.shower;
        } else if (Constants.WEATHER_TYPE_SLEET.equals(weather) || getString(R.string.sleet).equals(weather)) {
            return R.mipmap.sleet;
        } else if (Constants.WEATHER_TYPE_SNOW_FLURRY.equals(weather) || getString(R.string.snow_flurry).equals(weather)) {
            return R.mipmap.snowflurry;
        } else if (Constants.WEATHER_TYPE_SNOW_STORM.equals(weather) || getString(R.string.snowstorm).equals(weather)) {
            return R.mipmap.snowstorm;
        } else if (Constants.WEATHER_TYPE_STORM.equals(weather) || getString(R.string.storm).equals(weather)) {
            return R.mipmap.storm;
        } else if (Constants.WEATHER_TYPE_SUNNY.equals(weather) || getString(R.string.sunny).equals(weather)) {
            return R.mipmap.sunny;
        } else if (Constants.WEATHER_TYPE_THUNDERSHOWER.equals(weather) || getString(R.string.thundershower).equals(weather)) {
            return R.mipmap.thundershower;
        } else if (Constants.WEATHER_TYPE_THUNDERSHOWER_WITH_HAIL.equals(weather) || getString(R.string.thundershower_with_hail).equals(weather)) {
            return R.mipmap.thundershowerwithhail;
        } else if (Constants.WEATHER_TYPE_TORNADO.equals(weather) || getString(R.string.tornado).equals(weather)) {
            return R.mipmap.tornado;
        } else if (Constants.WEATHER_TYPE_TROPICAL_STORM.equals(weather) || getString(R.string.tropical_storm).equals(weather)) {
            return R.mipmap.tropicalstorm;
        } else if (Constants.WEATHER_TYPE_WINDY.equals(weather) || getString(R.string.windy).equals(weather)) {
            return R.mipmap.windy;
        } else {
            return R.mipmap.unknown;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //继承了Activity的onTouchEvent方法，直接监听点击事件
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //当手指按下的时候
            x1 = event.getX();
            y1 = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //当手指离开的时候
            x2 = event.getX();
            y2 = event.getY();
            if (y1 - y2 > 50) {
                OutdoorActivity.this.finish();
//                Toast.makeText(OutdoorActivity.this, "向上滑", Toast.LENGTH_SHORT).show();
            } else if (y2 - y1 > 50) {
//                Toast.makeText(OutdoorActivity.this, "向下滑", Toast.LENGTH_SHORT).show();
            } else if (x1 - x2 > 50) {
//                Toast.makeText(OutdoorActivity.this, "向左滑", Toast.LENGTH_SHORT).show();
            } else if (x2 - x1 > 50) {
//                Toast.makeText(OutdoorActivity.this, "向右滑", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onEventMainThread(IAQEvents event) {
        super.onEventMainThread(event);
        if (event instanceof IAQWeatherEvent) {
            IAQWeatherEvent iaqWeatherEvent = (IAQWeatherEvent) event;
            switch (iaqWeatherEvent.type) {
                case IAQWeatherEvent.GET_DATA_SUCCESS:
//                    setData();
                    break;
                case IAQWeatherEvent.GET_DATA_FAIL:

                    break;
                case IAQWeatherEvent.GET_SUGGESTION_SUCCESS:

                    break;

                case IAQWeatherEvent.GET_LOCATION_SUCCESS:
//                    getDeviceIdLocation();
//                    mLocation.setText(getLocation(this.deviceId));

                    break;
            }

        }
    }


}
