package com.honeywell.iaq.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ScaleXSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.AboutActivity;
import com.honeywell.iaq.activity.HomeActivity;
import com.honeywell.iaq.activity.OutdoorActivity;
import com.honeywell.iaq.activity.TestActivity;
import com.honeywell.iaq.adapter.DetailViewPagerAdapter;
import com.honeywell.iaq.adapter.DeviceListAdapter;
import com.honeywell.iaq.base.IAQBaseFragment;
import com.honeywell.iaq.base.IAQType;
import com.honeywell.iaq.bean.WeatherItem;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.events.IAQEnvironmentDetailEvent;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQTemperatureEvent;
import com.honeywell.iaq.events.IAQWeatherEvent;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.widget.EnvironmentView;
import com.honeywell.net.FastTube;
import com.honeywell.net.core.TubeOptions;
import com.honeywell.net.exception.TubeException;
import com.honeywell.net.listener.StringTubeListener;
import com.honeywell.net.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by zhujunyu on 17/1/24.
 */

public class EnvironmentDetialFragment3 extends IAQBaseFragment {

    private static final String TAG = EnvironmentDetialFragment3.class.getSimpleName();
    private String deviceTemp, humidity, location, aqi, weather_temp;
    private String homeName;
    private String roomName;

    private String temPm25Value;
    private String pm25Value;
    private String hchoValue = "--";
    private String co2Value = "--";
    private String tvocValue = "--";
    private String temperatureUnit;

    private String pmStatus = "";
    private String deviceId;
    private String utcTime;
    //设备序列号
    private String currentSerialNum;
    private SpannableString spannableString;
    private RelativeLayout mOutdoor;
    private TextView mTvKnowIaq;
    private ImageView mNextPage;
    private ImageView mPrevious;
    private RelativeLayout mPMCircle;
    private LinearLayout mContentView;

    private TextView mTvHomeName;
    private TextView mTvRoomName;
    private TextView mLocation;
    private TextView mWeather;
    private TextView mAQI;
    private TextView mPM;
    private TextView mTemp;
    private TextView mHumidity;
    private TextView mPMStatus;
    private ImageView mFaceImageView;
    private RelativeLayout mRelativeLayoutPm;
    private LinearLayout mLinearLayoutTemHum;
    private EnvironmentView environmentView;
    private LinearLayout mLinearLayoutContent;

    private View mHCHOView;
    private View mCO2View;
    private View mTVOCView;

    public static final int PM_TYPE = 101;
    public static final int HCHO_TYPE = 102;
    public static final int CO2_TYPE = 103;
    public static final int TVOC_TYPE = 104;


    public static final int HCHO_ONLY = 110;
    public static final int CO2_ONLY = 111;
    public static final int TVOC_ONLY = 112;
    public static final int HCHO_CO2 = 113;
    public static final int HCHO_TVOC = 114;
    public static final int CO2_TVOC = 115;
    public static final int HCHO_CO2_TVOC = 116;
    public static final int HCHO_CO2_TVOC_NONE = 117;
    public static int sSupport_TYPE;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void initView(View view) {
        super.initView(view);
        mOutdoor = (RelativeLayout) view.findViewById(R.id.layout1);
        mOutdoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((HomeActivity) getActivity()).changePage(0);
//                startActivity(new Intent(getActivity(),OutdoorActivity.class));

            }
        });
        mLocation = (TextView) view.findViewById(R.id.location);
        mWeather = (TextView) view.findViewById(R.id.weather_temp);
        mAQI = (TextView) view.findViewById(R.id.aqi);
        mPM = (TextView) view.findViewById(R.id.parameter_value);
        mTemp = (TextView) view.findViewById(R.id.device_temp);
        mHumidity = (TextView) view.findViewById(R.id.humidity_value);
        mPMStatus = (TextView) view.findViewById(R.id.pm_status);
        mFaceImageView = (ImageView) view.findViewById(R.id.pm_face_iv);

        mRelativeLayoutPm = (RelativeLayout) view.findViewById(R.id.rl_pm);
        mLinearLayoutTemHum = (LinearLayout) view.findViewById(R.id.ll_tem_hum);
        mNextPage = (ImageView) view.findViewById(R.id.next_page);
        mNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                ((HomeActivity) getActivity()).changePage(2);
            }
        });
        mPrevious = (ImageView) view.findViewById(R.id.previous);
        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                ((HomeActivity) getActivity()).changePage(0);
            }
        });

        mTvKnowIaq = (TextView) view.findViewById(R.id.tv_know_iaq);
        mTvKnowIaq.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        if (Utils.isZh(getContext())) {
            mTvKnowIaq.setVisibility(View.VISIBLE);
        } else {
            mTvKnowIaq.setVisibility(View.GONE);
        }
        mTvKnowIaq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isNetworkAvailable(getContext())) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(Constants.AD_URL);
                    intent.setData(content_url);
                    startActivity(intent);
                } else {
                    Utils.showToast(getContext(), getString(R.string.no_network));
                }
            }
        });
        mPMCircle = (RelativeLayout) view.findViewById(R.id.bg_pm_circle);
        mContentView = (LinearLayout) view.findViewById(R.id.ll);
        environmentView = (EnvironmentView) view.findViewById(R.id.circle_bg);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        params.width = getScreenWidth();
        params.height = getScreenWidth();
        params.setMargins(0, EnvironmentView.DISTANCE_OUT_IN, 0, 0);
        mContentView.setLayoutParams(params);
        mLinearLayoutContent = (LinearLayout) view.findViewById(R.id.ll_content);
        mTvHomeName = (TextView) view.findViewById(R.id.tv_home_name);
        mTvHomeName.setVisibility(View.GONE);
        mTvRoomName = (TextView) view.findViewById(R.id.tv_room_name);
        mTvRoomName.setVisibility(View.GONE);
        reProcessLayout();
    }

    @Override
    public void getData() {
        super.getData();
        currentSerialNum = Utils.getSharedPreferencesValue(getActivity().getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
        Logger.e("____________", "序列号" + currentSerialNum);
        refreshData();
        getDeviceInformation();
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_environment_detail3;
    }

    public void reProcessLayout() {
        int radius = getScreenWidth() / 2;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPMStatus.getLayoutParams();
        params.setMargins(0, radius * 25 / 100, 0, 0);
        mPMStatus.setLayoutParams(params);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) mRelativeLayoutPm.getLayoutParams();
        params2.setMargins(0, 0, 0, 0);
        mRelativeLayoutPm.setLayoutParams(params2);
        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) mLinearLayoutContent.getLayoutParams();
        params3.setMargins(0, radius * 10 / 100, 0, 0);
        mLinearLayoutContent.setLayoutParams(params3);
        RelativeLayout.LayoutParams params4 = (RelativeLayout.LayoutParams) mLinearLayoutTemHum.getLayoutParams();
        params4.setMargins(0, radius * 20 / 100, 0, 0);
        mLinearLayoutTemHum.setLayoutParams(params4);

    }


    private void refreshData() {
        if (Utils.isNetworkAvailable(getActivity().getApplicationContext())) {
            Logger.d(TAG, "start service open wss");
            Utils.startServiceByAction(getActivity().getApplicationContext(), Constants.ACTION_OPEN_WSS);
            refresh();
        } else {
            initIAQData();
            Utils.showToast(getActivity().getApplicationContext(), getString(R.string.no_network));
            EventBus.getDefault().post(new IAQEnvironmentDetailEvent(IAQEnvironmentDetailEvent.CHECK_NETWORK, true, null));
        }
    }

    private void refresh() {
        //根据提供的账号和设备编号从数据库中查询绑定的设备
        String account = Utils.getSharedPreferencesValue(getActivity().getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Cursor cur = getActivity().getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Logger.d(TAG, "refresh: Count=" + dbCount);
        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
            //最后一台设备天气获取完毕，发送获取成功标识

            getWeather(deviceId, cur.isLast());
            cur.moveToNext();
        }

        cur.close();
    }

    private void initIAQData() {
        mFaceImageView.setVisibility(View.INVISIBLE);
        environmentView.setColor(Color.LTGRAY);
        mPMStatus.setTextColor(Color.LTGRAY);
        mPM.setText(getString(R.string.default_pm));
        mTemp.setText(getString(R.string.unknown));
        mHumidity.setText(getString(R.string.unknown));
        hchoValue = "--";
        co2Value = "--";
        tvocValue = "--";
        setContentItemView();
        if (deviceId != null && deviceId.length() > 0) {
            if (Utils.getOnlineStatusFromDB(getActivity().getApplicationContext(), deviceId) == Constants.DEVICE_ONLINE) {
                if (Utils.isNetworkAvailable(getActivity().getApplicationContext())) {
                    mPMStatus.setText(getString(R.string.getting_data));
                } else {
                    mPMStatus.setText(getString(R.string.get_data_fail));
                }
            } else {
                mPMStatus.setText(getString(R.string.iaq_disconnect));
            }
        } else {
            if (Utils.isNetworkAvailable(getActivity().getApplicationContext())) {
                mPMStatus.setText(getString(R.string.getting_data));
            } else {
                mPMStatus.setText(getString(R.string.get_data_fail));
            }
        }
    }


    private void getWeather(final String deviceId, final boolean isLast) {
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_WEATHER);
        params.put(Constants.KEY_DEVICE_ID, deviceId);

        HttpUtils.getString(getContext(), Constants.DEVICE_WEATHER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getContext(), Constants.GetDataFlag.HON_IAQ_GET_WEATHER, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultList.size() <= 0) {
                    return;
                }


                String account = Utils.getSharedPreferencesValue(getContext(), Constants.KEY_ACCOUNT, "");
                String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
                String[] selectionArgs = new String[]{account, currentSerialNum};

                WeatherItem weatherItem = (WeatherItem) resultList.get(0);
                ContentValues cv = new ContentValues();
                cv.put(IAQ.BindDevice.COLUMN_WEATHER, weatherItem.getWeather());
                cv.put(IAQ.BindDevice.COLUMN_TEMPERATURE, weatherItem.getTemperature());
                cv.put(IAQ.BindDevice.COLUMN_HUMIDITY, weatherItem.getHumidity());
                cv.put(IAQ.BindDevice.COLUMN_PM25, weatherItem.getPm25());
                cv.put(IAQ.BindDevice.COLUMN_PM10, weatherItem.getPm10());
                cv.put(IAQ.BindDevice.COLUMN_AQI, weatherItem.getAqi());
                cv.put(IAQ.BindDevice.COLUMN_TIME, weatherItem.getTime());

                getActivity().getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

                if (isLast) {
                    //最后一条数据执行完 设置
                    setData();
                }

            }
        }));
    }

    private void setData() {
        String account = Utils.getSharedPreferencesValue(getActivity().getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Cursor cur = getActivity().getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Logger.d(TAG, "setData: Count=" + dbCount);


        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            int onlineStatus = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
            Logger.d(TAG, "setData: onlineStatus=" + onlineStatus);
            if (onlineStatus == Constants.DEVICE_ONLINE) {
                pm25Value = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_PM25));
                Logger.d(TAG, "setData: PM25=" + pm25Value);
                pmStatus = getString(R.string.getting_data);
                tvocValue = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TVOC));
                co2Value = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_CO2));
                hchoValue = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HCHO));

                showOnlineView();

            } else {
                showOffLineView();
            }

            deviceTemp = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE));
            temperatureUnit = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT));
            humidity = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY));
            showDeviceTempHumi();

            location = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_LOCATION));
            Logger.i("---测试location--", "" + location);
            if (TextUtils.isEmpty(location)) {
                //获取地点为空，改为从网络接口获取
                getDeviceLocation(deviceId);
            }

            temPm25Value = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_PM25));
            String tempValue = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_TEMPERATURE));
            String T = "";
            if (Constants.GEN_1.equals(IAQType.getGeneration(getContext()))) {
                if (Utils.isCelsius(getContext())) {
                    T = tempValue + getString(R.string.temperature_unit);
                } else {
                    float temp = Float.parseFloat(tempValue);
                    T = String.valueOf(Utils.C2W(temp)) + getString(R.string.temperature_f_unit);
                }
            } else if (Constants.GEN_2.equals(IAQType.getGeneration(getContext()))) {
                if (Utils.isCelsius(getContext(), deviceId)) {
                    T = tempValue + getString(R.string.temperature_unit);
                } else {
                    float temp = Float.parseFloat(tempValue);
                    T = String.valueOf(Utils.C2W(temp)) + getString(R.string.temperature_f_unit);
                }
            }

            weather_temp = getString(Constants.WEATHER_MAP.get(cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_WEATHER)))) + " " + T;
            utcTime = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_TIME));
        }

        showRightTopWeather();

        mPM.setText(pm25Value);
        mTemp.setText(deviceTemp);
        mHumidity.setText(humidity);
        mPMStatus.setText(pmStatus);
        showPmStatus(pmStatus);
        cur.close();
        PreferenceUtil.saveDeviceIdLocation(getContext(), deviceId, location);
        //地点设置完成，通知outdoor
        EventBus.getDefault().post(new IAQWeatherEvent(IAQWeatherEvent.GET_LOCATION_SUCCESS));
        setContentItemView();
    }


    private void reflshCirleView() {
        String account = Utils.getSharedPreferencesValue(getActivity().getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Logger.d(TAG, "setData: currentSerialNum=" + currentSerialNum);
        Cursor cur = getActivity().getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Logger.d(TAG, "setData: Count=" + dbCount);

        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            int onlineStatus = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
            Logger.d(TAG, "setData: onlineStatus=" + onlineStatus);
            if (onlineStatus == Constants.DEVICE_ONLINE) {
                pm25Value = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_PM25));
                Logger.d(TAG, "setData: PM25=" + pm25Value);
                if (Utils.isNetworkAvailable(getContext())) {
                    pmStatus = getString(R.string.getting_data);
                } else {
                    pmStatus = getString(R.string.get_data_fail);
                }

                tvocValue = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TVOC));
                co2Value = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_CO2));
                hchoValue = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HCHO));

                showOnlineView();

            } else {
                showOffLineView();
            }

            deviceTemp = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE));
            temperatureUnit = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT));
            humidity = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY));
            showDeviceTempHumi();

            location = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_LOCATION));
            Logger.i(TAG, "" + location);

            temPm25Value = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_PM25));
            String weatherKey = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_WEATHER));
            String tempValue = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_TEMPERATURE));
            String T = "";

            if (Constants.GEN_1.equals(IAQType.getGeneration(getContext()))) {
                if (Utils.isCelsius(getContext())) {
                    T = tempValue + getString(R.string.temperature_unit);
                } else {
                    float temp = Float.parseFloat(tempValue);
                    T = String.valueOf((int) Utils.C2W(temp)) + getString(R.string.temperature_f_unit);
                }
            } else if (Constants.GEN_2.equals(IAQType.getGeneration(getContext()))) {
                if (Utils.isCelsius(getContext(), deviceId)) {
                    T = tempValue + getString(R.string.temperature_unit);
                } else {
                    float temp = Float.parseFloat(tempValue);
                    T = String.valueOf((int) Utils.C2W(temp)) + getString(R.string.temperature_f_unit);
                }
            }

            Logger.i("weatherKey", "" + weatherKey);
            Logger.i("weatherKey", "" + Constants.WEATHER_MAP.containsKey(weatherKey));
            if (Constants.WEATHER_MAP.containsKey(weatherKey)) {
                weather_temp = getString(Constants.WEATHER_MAP.get(weatherKey)) + " " + T;
            }
            utcTime = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_TIME));
        }

        showRightTopWeather();

        mPM.setText(pm25Value);
        mTemp.setText(deviceTemp);
        mHumidity.setText(humidity);
        mPMStatus.setText(pmStatus);
        showPmStatus(pmStatus);
        cur.close();
        setContentItemView();
    }

    private void showOffLineView() {
        mFaceImageView.setVisibility(View.INVISIBLE);
        environmentView.setColor(Color.LTGRAY);
        pm25Value = getString(R.string.default_pm);
        pmStatus = getString(R.string.iaq_disconnect);
        mPMStatus.setTextColor(Color.LTGRAY);
        mTvKnowIaq.setTextColor(Color.LTGRAY);
        hchoValue = "--";
        co2Value = "--";
        tvocValue = "--";
        setContentItemView();

    }

    private void showOnlineView() {
        if (TextUtils.isEmpty(pm25Value)) {
            pm25Value = getString(R.string.default_pm);
            mPMStatus.setTextColor(Color.LTGRAY);
            environmentView.setColor(Color.LTGRAY);
            mFaceImageView.setVisibility(View.INVISIBLE);
        } else {
            try {
                pmStatus = Utils.getPMStatus(getActivity().getApplicationContext(), Integer.parseInt(pm25Value));
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                changePmBgFromLevel();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

    }

    private void showDeviceTempHumi() {

        if (deviceTemp == null || deviceTemp.length() == 0) {
            deviceTemp = getString(R.string.unknown);
        } else {

            if (Constants.GEN_1.equals(IAQType.getGeneration(getContext()))) {
                if (Utils.isCelsius(getContext())) {
                    deviceTemp = deviceTemp + getString(R.string.temperature_unit);
                } else {
                    float temp = Float.parseFloat(deviceTemp);
                    deviceTemp = String.valueOf(Utils.C2W(temp)) + getString(R.string.temperature_f_unit);
                }
            } else if (Constants.GEN_2.equals(IAQType.getGeneration(getContext()))) {
                if (Constants.TEMPERATURE_UNIT_C.equals(temperatureUnit)) {
                    deviceTemp = deviceTemp + getString(R.string.temperature_unit);
                } else if (Constants.TEMPERATURE_UNIT_F.equals(temperatureUnit)) {
                    float temp = Float.parseFloat(deviceTemp);
                    deviceTemp = String.valueOf(Utils.C2W(temp)) + getString(R.string.temperature_f_unit);
                } else {
                    deviceTemp = deviceTemp + getString(R.string.temperature_unit);
                }
            }

        }

        if (humidity == null || humidity.length() == 0) {
            humidity = getString(R.string.unknown);
        } else {
            humidity = humidity + getString(R.string.percent);
        }
    }

    private void showRightTopWeather() {
        mLocation.setText(location);
        mWeather.setText(weather_temp);
        setPmColor();
    }

    private void getDeviceLocation(final String deviceId) {

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_LOCATION);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(getContext(), Constants.DEVICE_LOCATION_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getContext(), Constants.GetDataFlag.HON_IAQ_GET_DEVICE_LOCATION, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode != 0) {
                    return;
                }
                String responseStr = (String) objects[0];
                if (!responseStr.contains(Constants.KEY_NAME)) {
                    return;
                }

                try {
                    JSONObject jsonObject = new JSONObject(responseStr);
                    Logger.d(TAG, "location=" + jsonObject.optString(Constants.KEY_NAME));

                    String account = Utils.getSharedPreferencesValue(getActivity().getApplicationContext(), Constants.KEY_ACCOUNT, "");
                    String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
                    String[] selectionArgs = new String[]{account, currentSerialNum};

                    ContentValues cv = new ContentValues();
                    location = jsonObject.optString(Constants.KEY_NAME);
                    cv.put(IAQ.BindDevice.COLUMN_LOCATION, location);
                    getActivity().getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);
                    EventBus.getDefault().post(new IAQEnvironmentDetailEvent(IAQEnvironmentDetailEvent.REFRESH_LOCATION, true, null));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }));

    }


    private void getDeviceInformation() {
        String account = Utils.getSharedPreferencesValue(getContext(), Constants.KEY_ACCOUNT, "");
        Logger.d(TAG, "getDeviceInformation: serialNum=" + currentSerialNum);
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Cursor cur = getActivity().getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            roomName = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ROOM));
            homeName = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HOME));
            Logger.d(TAG, "Room=" + roomName + ", home=" + homeName);

            cur.moveToNext();
        }
        cur.close();
        environmentView.setHomeRoomName(homeName, roomName);
    }


    public void setPmColor() {

        aqi = "PM2.5 " + temPm25Value;


        if (TextUtils.isEmpty(temPm25Value)) {
            return;
        }
        ForegroundColorSpan span = new ForegroundColorSpan(Color.GRAY);
        //根据pm值 设置字体颜色
        int pmLevel = Utils.getPMLevel(getActivity().getApplicationContext(), (int) Float.parseFloat(temPm25Value));
        switch (pmLevel) {
            case Constants.PM_LEVEL_1:
                span = new ForegroundColorSpan(this.getContext().getResources().getColor(R.color.fresh));
                break;
            case Constants.PM_LEVEL_2:
                span = new ForegroundColorSpan(this.getContext().getResources().getColor(R.color.good));
                break;
            case Constants.PM_LEVEL_3:
                span = new ForegroundColorSpan(this.getContext().getResources().getColor(R.color.light_pollution));
                break;
            case Constants.PM_LEVEL_4:
                span = new ForegroundColorSpan(this.getContext().getResources().getColor(R.color.mid_pollution));
                break;
            case Constants.PM_LEVEL_5:
                span = new ForegroundColorSpan(this.getContext().getResources().getColor(R.color.high_pollution));
                break;
            case Constants.PM_LEVEL_6:
                span = new ForegroundColorSpan(this.getContext().getResources().getColor(R.color.serious_pollution));
                break;


        }
        spannableString = new SpannableString(aqi);
        spannableString.setSpan(span, 6, aqi.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 6, aqi.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mAQI.setText(spannableString);
    }

    private void setContentItemView() {

        //根据设备支持的类型，添加不同的view
        mLinearLayoutContent.removeAllViews();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        mHCHOView = inflater.inflate(R.layout.detail_parameter_item, null);
        mCO2View = inflater.inflate(R.layout.detail_parameter_item, null);
        mTVOCView = inflater.inflate(R.layout.detail_parameter_item, null);
        sSupport_TYPE = IAQType.getSupportNewParameter(getContext());
//        sSupport_TYPE = HCHO_CO2_TVOC;
        switch (sSupport_TYPE) {
            case HCHO_ONLY:
                initItemViewData(mHCHOView, HCHO_TYPE);
                break;
            case CO2_ONLY:
                initItemViewData(mCO2View, CO2_TYPE);
                break;
            case TVOC_ONLY:
                initItemViewData(mTVOCView, TVOC_TYPE);
                break;
            case HCHO_CO2:
                initItemViewData(mHCHOView, HCHO_TYPE);
                initItemViewData(mCO2View, CO2_TYPE);
                break;
            case HCHO_TVOC:
                initItemViewData(mHCHOView, HCHO_TYPE);
                initItemViewData(mTVOCView, TVOC_TYPE);
                break;
            case CO2_TVOC:
                initItemViewData(mCO2View, CO2_TYPE);
                initItemViewData(mTVOCView, TVOC_TYPE);
                break;
            case HCHO_CO2_TVOC:
                initItemViewData(mHCHOView, HCHO_TYPE);
                initItemViewData(mCO2View, CO2_TYPE);
                initItemViewData(mTVOCView, TVOC_TYPE);
                break;
            case HCHO_CO2_TVOC_NONE:
                break;
        }

    }

    private void initItemViewData(View view, int type) {
        ImageView imageView = (ImageView) view.findViewById(R.id.iv_expression);
        TextView tvValue = (TextView) view.findViewById(R.id.tv_value);
        TextView tvUnitType = (TextView) view.findViewById(R.id.tv_unit_type);
        TextView tvUnit = (TextView) view.findViewById(R.id.tv_unit);
        switch (type) {
            case HCHO_TYPE:
                if (isHCHOLevelNormal()) {
                    imageView.setImageResource(R.mipmap.smile);
                } else {
                    imageView.setImageResource(R.mipmap.angry);
                }
                //数值除以1000保留两位小数 然后四舍五入
                if ("--".equals(hchoValue)) {
                    tvValue.setText(hchoValue + "");
                    tvUnitType.setText("HCHO");
                    tvUnit.setText(getString(R.string.hcho_unit));
                } else {
                    double result = Float.parseFloat(hchoValue);
                    Logger.e(TAG, "hcho:" + result / 1000);
                    tvValue.setText(String.format("%.2f", result / 1000) + "");
                    tvUnitType.setText("HCHO");
                    tvUnit.setText(getString(R.string.hcho_unit));
                }

                break;
            case CO2_TYPE:
                if (isCO2LevelNormal()) {
                    imageView.setImageResource(R.mipmap.smile);
                } else {
                    imageView.setImageResource(R.mipmap.angry);
                }
                tvValue.setText(co2Value + "");
                tvUnitType.setText("CO2");
                tvUnit.setText("ppm");
                break;
            case TVOC_TYPE:
                if (isTVOCLevelnormal()) {
                    imageView.setImageResource(R.mipmap.smile);
                } else {
                    imageView.setImageResource(R.mipmap.angry);
                }
                //数值除以1000保留两位小数 然后四舍五入

                if ("--".equals(tvocValue)) {
                    tvValue.setText(tvocValue + "");
                    tvUnitType.setText("TVOC");
                    tvUnit.setText(R.string.tvoc_unit);
                } else {
                    float result1 = Float.parseFloat(tvocValue);
                    Logger.e(TAG, "tvoc:" + result1 / 1000);
                    tvValue.setText(String.format("%.2f", result1 / 1000) + "");
                    tvUnitType.setText("TVOC");
                    tvUnit.setText(R.string.tvoc_unit);
                }


                break;
        }
        view.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        mLinearLayoutContent.addView(view);
    }


    private int getScreenWidth() {
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        return screenWidth;
    }


    private void changePmBgFromLevel() {

        if (getString(R.string.default_pm).equals(pm25Value)) {
            return;
        }
        int pmLevel = Utils.getPMLevel(getActivity().getApplicationContext(), Integer.parseInt(pm25Value));

        switch (pmLevel) {
            case Constants.PM_LEVEL_1:
                //空气清新
                environmentView.setColor(this.getContext().getResources().getColor(R.color.fresh));
                mPMStatus.setTextColor(this.getContext().getResources().getColor(R.color.fresh));
                mPM.setTextColor(this.getContext().getResources().getColor(R.color.pm_text_fresh));
                mTvKnowIaq.setTextColor(this.getContext().getResources().getColor(R.color.fresh));
                break;
            case Constants.PM_LEVEL_2:
                //良好
                mFaceImageView.setVisibility(View.VISIBLE);
                mFaceImageView.setImageResource(R.mipmap.smile);
                environmentView.setColor(this.getContext().getResources().getColor(R.color.good));
                mPMStatus.setTextColor(this.getContext().getResources().getColor(R.color.good));
                mPM.setTextColor(this.getContext().getResources().getColor(R.color.pm_text_good));
                mTvKnowIaq.setTextColor(this.getContext().getResources().getColor(R.color.good));
                break;
            case Constants.PM_LEVEL_3:
                //轻度污染
                environmentView.setColor(this.getContext().getResources().getColor(R.color.light_pollution));
                mPMStatus.setTextColor(this.getContext().getResources().getColor(R.color.light_pollution));
                mPM.setTextColor(this.getContext().getResources().getColor(R.color.pm_text_light_pollution));
                mTvKnowIaq.setTextColor(this.getContext().getResources().getColor(R.color.light_pollution));
                break;
            case Constants.PM_LEVEL_4:
                //中度污染
                mFaceImageView.setVisibility(View.VISIBLE);
                mFaceImageView.setImageResource(R.mipmap.orange_face);
                environmentView.setColor(this.getContext().getResources().getColor(R.color.mid_pollution));
                mPMStatus.setTextColor(this.getContext().getResources().getColor(R.color.mid_pollution));
                mPM.setTextColor(this.getContext().getResources().getColor(R.color.pm_text_mid_pollution));
                mTvKnowIaq.setTextColor(this.getContext().getResources().getColor(R.color.mid_pollution));
                break;
            case Constants.PM_LEVEL_5:
                //重度污染
                mFaceImageView.setVisibility(View.VISIBLE);
                mFaceImageView.setImageResource(R.mipmap.angry);
                environmentView.setColor(this.getContext().getResources().getColor(R.color.high_pollution));
                mPMStatus.setTextColor(this.getContext().getResources().getColor(R.color.high_pollution));
                mPM.setTextColor(this.getContext().getResources().getColor(R.color.pm_text_high_pollution));
                mTvKnowIaq.setTextColor(this.getContext().getResources().getColor(R.color.high_pollution));
                break;
            case Constants.PM_LEVEL_6:
                //严重污染
                environmentView.setColor(this.getContext().getResources().getColor(R.color.serious_pollution));
                mPMStatus.setTextColor(this.getContext().getResources().getColor(R.color.serious_pollution));
                mPM.setTextColor(this.getContext().getResources().getColor(R.color.pm_text_serious_pollution));
                mTvKnowIaq.setTextColor(this.getContext().getResources().getColor(R.color.serious_pollution));
                break;
        }

        if (!isHCHOLevelNormal() || !isCO2LevelNormal() || !isTVOCLevelnormal()) {
            environmentView.setColor(this.getContext().getResources().getColor(R.color.high_pollution));
        }

    }

    private boolean isHCHOLevelNormal() {

        if (TextUtils.isEmpty(hchoValue)) {
            hchoValue = "0";
        }

        try {
            int hchoLevel = Utils.getHCHOLevel(Float.parseFloat(hchoValue));
            if (hchoLevel == Constants.HCHO_NORMAL) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return true;
        }

    }

    private boolean isCO2LevelNormal() {
        if (TextUtils.isEmpty(co2Value)) {
            co2Value = "0";
        }
        try {
            int co2Level = Utils.getCO2Level(Integer.parseInt(co2Value));
            if (co2Level == Constants.CO2_NORMAL) {
                return true;
            } else {
                return false;

            }
        } catch (NumberFormatException e) {
            return true;
        }

    }

    private boolean isTVOCLevelnormal() {
        if (TextUtils.isEmpty(tvocValue)) {
            tvocValue = "0";
        }
        try {
            int tvocLevel = Utils.getTVOCLevel(Float.parseFloat(tvocValue));
            if (tvocLevel == Constants.TVOC_NORMAL) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return true;
        }

    }


    @Override
    public void onEventMainThread(IAQEvents event) {
        super.onEventMainThread(event);
        if (event instanceof IAQEnvironmentDetailEvent) {
            IAQEnvironmentDetailEvent ev = (IAQEnvironmentDetailEvent) event;
            switch (ev.type) {
                case IAQEnvironmentDetailEvent.GET_DATA_SUCCESS:
                    Logger.v("get data", " get data sucess");
                    setData();
                    break;
                case IAQEnvironmentDetailEvent.GET_DATA_FAIL:
                    Logger.v("get data", " get data failed");
                    initIAQData();
                    break;
                case IAQEnvironmentDetailEvent.CHECK_NETWORK:
                    Logger.v("check network", "check net work");
                    initIAQData();
                    break;
                case IAQEnvironmentDetailEvent.REFRESH_LOCATION:
                    Logger.v("Refresh", " location");
                    mLocation.setText(location);
                    break;
                case IAQEnvironmentDetailEvent.ACTION_GET_IAQ_DATA_SUCCESS:
                    Logger.v("get IAQ data", " sucess");
                    Logger.e("=======================", "=======================");
                    reflshCirleView();
                    break;
                case IAQEnvironmentDetailEvent.ACTION_WSS_CONNECTED:
                    Logger.v("wss connect", " wss connected");
//                    startServiceByAction(getContext(), Constants.ACTION_GET_IAQ_DATA, deviceId);
                    reflshCirleView();
                case IAQEnvironmentDetailEvent.MODIFY_HOME_ROME_NAME:
                    getDeviceInformation();
                    ((HomeActivity) getActivity()).refreshTitle();
                    break;
                default:
                    break;
            }
        } else if (event instanceof IAQTemperatureEvent) {
            reflshCirleView();
        }
    }


    private void startServiceByAction(Context context, String action, String deviceId) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(context.getPackageName());
        intent.putExtra(Constants.KEY_DEVICE_ID, deviceId);
        context.startService(intent);

    }

    /**
     * 如果正在获取数据或者设备离线则显示状态，否则隐藏状态
     */
    private void showPmStatus(String status) {
        if (status.equals(getString(R.string.getting_data)) || status.equals(getString(R.string.get_data_fail))
                || status.equals(getString(R.string.iaq_disconnect)))
            mPMStatus.setVisibility(View.VISIBLE);
        else
            mPMStatus.setVisibility(View.INVISIBLE);
    }

}
