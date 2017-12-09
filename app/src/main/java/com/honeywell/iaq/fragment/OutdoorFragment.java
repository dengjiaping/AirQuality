package com.honeywell.iaq.fragment;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.HomeActivity;
import com.honeywell.iaq.base.IAQBaseFragment;
import com.honeywell.iaq.bean.WeatherInfo;
import com.honeywell.iaq.bean.WeatherItem;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.events.IAQEnvironmentDetailEvent;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQWeatherEvent;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
 * Created by zhujunyu on 2017/2/20.
 */

public class OutdoorFragment extends IAQBaseFragment {

    private ArrayList<WeatherInfo> weatherInfos;
    public HashMap<String, String> WEATHER_MAP;
    private TextView mDate, mPM25, mHumidity, mTemp, mWeather, mNotification;

    private TextView mWeekly1, mTemp1, mWeekly2, mTemp2, mWeekly3, mTemp3, mWeekly4, mTemp4, mWeekly5, mTemp5;

    private ImageView mNextPage, mWeatherIcon, mWeather1, mWeather2, mWeather3, mWeather4, mWeather5;

    private ProgressDialog mDialog;

    private String location, deviceId, suggestion;
    private TextView mLocation;
    private static final String TAG = "Outdoor";


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_outdoor;
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        weatherInfos = new ArrayList<>();
        initializeUI(view);
    }

    @Override
    public void onResume() {
        super.onResume();
//        getDeviceIdLocation();
//        if (Utils.isNetworkAvailable(getActivity())) {
////            mDialog.show();
////            mDialog.setMessage(getString(R.string.iaq_cloud_data));
//
//            refresh();
//        } else {
//            Utils.showToast(getActivity(), getString(R.string.no_network));
//        }
    }


    private void initializeUI(View view) {
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

//        mDialog = new ProgressDialog(getActivity());
//        mDialog.setTitle(getString(R.string.app_name));
//        mDialog.setCanceledOnTouchOutside(false);

//        deviceId = getActivity().getIntent().getStringExtra(Constants.KEY_DEVICE_ID);

        mDate = (TextView) view.findViewById(R.id.date);
        mDate.setText(getDate(getActivity()));
        mLocation = (TextView) view.findViewById(R.id.location);

        mPM25 = (TextView) view.findViewById(R.id.device_pm);
        mHumidity = (TextView) view.findViewById(R.id.device_humidity);
        mTemp = (TextView) view.findViewById(R.id.temp);
        mWeather = (TextView) view.findViewById(R.id.weather);
        mNotification = (TextView) view.findViewById(R.id.notification);

        mWeatherIcon = (ImageView) view.findViewById(R.id.weather_icon);

        mWeekly1 = (TextView) view.findViewById(R.id.weekly1);
        mWeekly2 = (TextView) view.findViewById(R.id.weekly2);
        mWeekly3 = (TextView) view.findViewById(R.id.weekly3);
        mWeekly4 = (TextView) view.findViewById(R.id.weekly4);
        mWeekly5 = (TextView) view.findViewById(R.id.weekly5);

        mWeather1 = (ImageView) view.findViewById(R.id.weather1);
        mWeather2 = (ImageView) view.findViewById(R.id.weather2);
        mWeather3 = (ImageView) view.findViewById(R.id.weather3);
        mWeather4 = (ImageView) view.findViewById(R.id.weather4);
        mWeather5 = (ImageView) view.findViewById(R.id.weather5);

        mTemp1 = (TextView) view.findViewById(R.id.temp1);
        mTemp2 = (TextView) view.findViewById(R.id.temp2);
        mTemp3 = (TextView) view.findViewById(R.id.temp3);
        mTemp4 = (TextView) view.findViewById(R.id.temp4);
        mTemp5 = (TextView) view.findViewById(R.id.temp5);

        mNextPage = (ImageView) view.findViewById(R.id.next_page);
        mNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                ((HomeActivity) getActivity()).changePage(1);
            }
        });
    }

    private void getDeviceIdLocation() {
        String[] strings = PreferenceUtil.getDeviceIdLoaction(getContext());
        location = strings[1];
        deviceId = strings[0];
        Log.d(TAG, "deviceId=" + deviceId + "location=" + location);
    }

    private void refresh() {
        if (deviceId != null && deviceId.length() > 0) {
            mLocation.setText(getLocation(deviceId));
            getWeather(deviceId);
            getForecastData(deviceId);
            getLifeSuggestion(deviceId);
        }
    }

    private String getLocation(String deviceId) {
        String location = "";
        String account = Utils.getSharedPreferencesValue(getActivity(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};
        Cursor cur = getActivity().getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
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

    private void getWeather(final String deviceId) {

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_WEATHER);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(getContext(), Constants.DEVICE_WEATHER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getContext(), Constants.GetDataFlag.HON_IAQ_GET_WEATHER, new IResponse() {
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

                String account = Utils.getSharedPreferencesValue(getContext(), Constants.KEY_ACCOUNT, "");
                String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
                String[] selectionArgs = new String[]{account, deviceId};
                getContext().getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

                if (deviceId != null && deviceId.length() > 0) {
                    getWeatherData(deviceId);
                }

            }
        }));

    }

    private void getForecastData(final String deviceId) {
        Log.d(TAG, "getForecastData: DeviceId=" + deviceId);
        weatherInfos.clear();
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_FORECAST);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(getContext(), Constants.DEVICE_WEATHER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getContext(), Constants.GetDataFlag.HON_IAQ_GET_FORECAST, new IResponse() {
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
        HttpUtils.getString(getContext(), Constants.DEVICE_LIFE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getContext(), Constants.GetDataFlag.HON_IAQ_LIFE_SUGGEST, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
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
                                ds.setColor(OutdoorFragment.this.getResources().getColor(R.color.opacity_white));       //设置文件颜色
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

        String account = Utils.getSharedPreferencesValue(getActivity(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};
        Cursor cur = getActivity().getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
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
                if (Utils.getPMLevel(getActivity(), value) == Constants.PM_LEVEL_1) {
                    mPM25.setTextColor(Color.parseColor("#1792E5"));
                } else if (Utils.getPMLevel(getActivity(), value) == Constants.PM_LEVEL_2) {
                    mPM25.setTextColor(Color.parseColor("#7EB338"));
                } else if (Utils.getPMLevel(getActivity(), value) == Constants.PM_LEVEL_3) {
                    mPM25.setTextColor(Color.parseColor("#FFC627"));
                } else if (Utils.getPMLevel(getActivity(), value) == Constants.PM_LEVEL_4) {
                    mPM25.setTextColor(Color.parseColor("#F37022"));
                } else if (Utils.getPMLevel(getActivity(), value) == Constants.PM_LEVEL_5) {
                    mPM25.setTextColor(Color.parseColor("#EE3124"));
                } else if (Utils.getPMLevel(getActivity(), value) == Constants.PM_LEVEL_6) {
                    mPM25.setTextColor(Color.parseColor("#9B59B6"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!TextUtils.isEmpty(temp)) {
            mTemp.setText(temp + getString(R.string.temperature_unit));
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

        mTemp1.setText(weatherInfos.get(1).getTemperatureLow() + "/" + weatherInfos.get(1).getTemperatureHigh() + getString(R.string.temperature_unit));
        mTemp2.setText(weatherInfos.get(2).getTemperatureLow() + "/" + weatherInfos.get(2).getTemperatureHigh() + getString(R.string.temperature_unit));
        mTemp3.setText(weatherInfos.get(3).getTemperatureLow() + "/" + weatherInfos.get(3).getTemperatureHigh() + getString(R.string.temperature_unit));
        mTemp4.setText(weatherInfos.get(4).getTemperatureLow() + "/" + weatherInfos.get(4).getTemperatureHigh() + getString(R.string.temperature_unit));
        mTemp5.setText(weatherInfos.get(5).getTemperatureLow() + "/" + weatherInfos.get(5).getTemperatureHigh() + getString(R.string.temperature_unit));

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

        mWeatherIcon.setImageResource(getWeatherIconDrawable(weatherInfos.get(0).getWeatherDay()));
    }


    public String getWeek(String strDate) {
        String week = null;
        try {
            Locale locale;
            if (Utils.isZh(this.getContext())) {
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

    public static int getWeatherIconDrawable(String weather) {
        Log.d(TAG, "getWeatherIconDrawable: Weather=" + weather);
        if (Constants.WEATHER_TYPE_BLUSTERY.equals(weather)) {
            return R.mipmap.blustery;
        } else if (Constants.WEATHER_TYPE_CLEAR.equals(weather)) {
            return R.mipmap.clear;
        } else if (Constants.WEATHER_TYPE_CLOUD.equals(weather)) {
            return R.mipmap.cloudy;
        } else if (Constants.WEATHER_TYPE_COLD.equals(weather)) {
            return R.mipmap.cold;
        } else if (Constants.WEATHER_TYPE_DUST.equals(weather)) {
            return R.mipmap.dust;
        } else if (Constants.WEATHER_TYPE_DUST_STORM.equals(weather)) {
            return R.mipmap.duststorm;
        } else if (Constants.WEATHER_TYPE_FAIR_DAY.equals(weather)) {
            return R.mipmap.fairday;
        } else if (Constants.WEATHER_TYPE_FAIR_NIGHT.equals(weather)) {
            return R.mipmap.fairnight;
        } else if (Constants.WEATHER_TYPE_FOGGY.equals(weather)) {
            return R.mipmap.foggy;
        } else if (Constants.WEATHER_TYPE_HAZE.equals(weather)) {
            return R.mipmap.haze;
        } else if (Constants.WEATHER_TYPE_HEAVY_RAIN.equals(weather)) {
            return R.mipmap.heavyrain;
        } else if (Constants.WEATHER_TYPE_HEAVY_SNOW.equals(weather)) {
            return R.mipmap.heavysnow;
        } else if (Constants.WEATHER_TYPE_HEAVY_STORM.equals(weather)) {
            return R.mipmap.heavystorm;
        } else if (Constants.WEATHER_TYPE_HOT.equals(weather)) {
            return R.mipmap.hot;
        } else if (Constants.WEATHER_TYPE_HURRICANE.equals(weather)) {
            return R.mipmap.hurricane;
        } else if (Constants.WEATHER_TYPE_ICE_RAIN.equals(weather)) {
            return R.mipmap.icerain;
        } else if (Constants.WEATHER_TYPE_LIGHT_RAIN.equals(weather)) {
            return R.mipmap.lightrain;
        } else if (Constants.WEATHER_TYPE_LIGHT_SNOW.equals(weather)) {
            return R.mipmap.lightsnow;
        } else if (Constants.WEATHER_TYPE_MODERATE_RAIN.equals(weather)) {
            return R.mipmap.moderaterain;
        } else if (Constants.WEATHER_TYPE_MODERATE_SNOW.equals(weather)) {
            return R.mipmap.moderatesnow;
        } else if (Constants.WEATHER_TYPE_MOSTLY_CLOUDY_DAY.equals(weather)) {
            return R.mipmap.mostlycloudyday;
        } else if (Constants.WEATHER_TYPE_MOSTLY_CLOUDY_NIGHT.equals(weather)) {
            return R.mipmap.mostlycloudynight;
        } else if (Constants.WEATHER_TYPE_OVERCAST.equals(weather)) {
            return R.mipmap.overcast;
        } else if (Constants.WEATHER_TYPE_PARTLY_CLOUDY_DAY.equals(weather)) {
            return R.mipmap.partlycloudyday;
        } else if (Constants.WEATHER_TYPE_PARTLY_CLOUDY_NIGHT.equals(weather)) {
            return R.mipmap.partlycloudynight;
        } else if (Constants.WEATHER_TYPE_SAND.equals(weather)) {
            return R.mipmap.sand;
        } else if (Constants.WEATHER_TYPE_SAND_STORM.equals(weather)) {
            return R.mipmap.sandstorm;
        } else if (Constants.WEATHER_TYPE_SEVERE_STORM.equals(weather)) {
            return R.mipmap.severestorm;
        } else if (Constants.WEATHER_TYPE_SHOWER.equals(weather)) {
            return R.mipmap.shower;
        } else if (Constants.WEATHER_TYPE_SLEET.equals(weather)) {
            return R.mipmap.sleet;
        } else if (Constants.WEATHER_TYPE_SNOW_FLURRY.equals(weather)) {
            return R.mipmap.snowflurry;
        } else if (Constants.WEATHER_TYPE_SNOW_STORM.equals(weather)) {
            return R.mipmap.snowstorm;
        } else if (Constants.WEATHER_TYPE_STORM.equals(weather)) {
            return R.mipmap.storm;
        } else if (Constants.WEATHER_TYPE_SUNNY.equals(weather)) {
            return R.mipmap.sunny;
        } else if (Constants.WEATHER_TYPE_THUNDERSHOWER.equals(weather)) {
            return R.mipmap.thundershower;
        } else if (Constants.WEATHER_TYPE_THUNDERSHOWER_WITH_HAIL.equals(weather)) {
            return R.mipmap.thundershowerwithhail;
        } else if (Constants.WEATHER_TYPE_TORNADO.equals(weather)) {
            return R.mipmap.tornado;
        } else if (Constants.WEATHER_TYPE_TROPICAL_STORM.equals(weather)) {
            return R.mipmap.tropicalstorm;
        } else if (Constants.WEATHER_TYPE_WINDY.equals(weather)) {
            return R.mipmap.windy;
        } else {
            return R.mipmap.unknown;
        }
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


    @Override
    public void onEventMainThread(IAQEvents event) {
        super.onEventMainThread(event);
        if (event instanceof IAQWeatherEvent) {
            IAQWeatherEvent iaqWeatherEvent = (IAQWeatherEvent) event;
            switch (iaqWeatherEvent.type) {
                case IAQWeatherEvent.GET_DATA_SUCCESS:
                    setData();
                    break;
                case IAQWeatherEvent.GET_DATA_FAIL:

                    break;
                case IAQWeatherEvent.GET_SUGGESTION_SUCCESS:

                    break;

                case IAQWeatherEvent.GET_LOCATION_SUCCESS:
                    getDeviceIdLocation();
                    mLocation.setText(getLocation(this.deviceId));
                    if (Utils.isNetworkAvailable(getActivity())) {
                        refresh();
                    } else {
                        Utils.showToast(getActivity(), getString(R.string.no_network));
                    }
                    break;
            }

        }
    }

}
