package com.honeywell.iaq.task;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.honeywell.iaq.R;
import com.honeywell.iaq.bean.Result;
import com.honeywell.iaq.bean.WeatherInfo;
import com.honeywell.iaq.bean.WeatherItem;
import com.honeywell.iaq.interfaces.IData;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.exception.TubeException;
import com.honeywell.net.listener.StringTubeListener;
import com.honeywell.net.utils.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by zhujunyu on 2017/3/13.
 */

public class TubeTask implements StringTubeListener<Result> {

    private Context mContext;
    private IResponse mResponse;
    private int flag;

    public TubeTask(Context context, int flag, IResponse mResponse) {
        this.mContext = context;
        this.mResponse = mResponse;
        this.flag = flag;
    }

    @Override
    public Result doInBackground(String water) throws Exception {
//        Logger.d("----", "water" + water);

        Result result = new Result();
        //排除 验证验证码接口返回数据  修改设备名称接口
        if (!TextUtils.isEmpty(water) && flag != Constants.GetDataFlag.HON_IAQ_CHECK_VALIDATE_CODE) {
            if (water.contains(Constants.KEY_ERROR_TYPE)) {
                result.resultCode = Result.RESULT_ERROR;
                result.strArray = new String[1];
                result.strArray[0] = water;
                return result;
            }
        }

        switch (flag) {
            case Constants.GetDataFlag.HON_IAQ_REGISTER:
            case Constants.GetDataFlag.HON_IAQ_FORGOT_PWD:
            case Constants.GetDataFlag.HON_IAQ_GET_DEVICE_LOCATION:
            case Constants.GetDataFlag.HON_IAQ_CHECK_DEVICE_BOUND:

            case Constants.GetDataFlag.HON_IAQ_CHECK_VALIDATE_CODE:
            case Constants.GetDataFlag.HON_IAQ_GET_DAY_HISTORY:
            case Constants.GetDataFlag.HON_IAQ_GET_MONTH_HISTORY:
            case Constants.GetDataFlag.HON_IAQ_QUERY_ONLINE:
            case Constants.GetDataFlag.HON_IAQ_LOGOUT:
            case Constants.GetDataFlag.HON_IAQ_GET_LOCATION_INFO:
            case Constants.GetDataFlag.HON_IAQ_SET_DEVICE_LOCATION:
            case Constants.GetDataFlag.HON_IAQ_BIND_DEVICE:
            case Constants.GetDataFlag.HON_IAQ_SLEEP_MODE:
            case Constants.GetDataFlag.HON_IAQ_GET_CLOCK:
            case Constants.GetDataFlag.HON_IAQ_CHECK_REGISTER:

                result.resultCode = Result.RESULT_OK;
                result.strArray = new String[1];
                result.strArray[0] = water;
                break;
            case Constants.GetDataFlag.HON_IAQ_LIFE_SUGGEST:
                Utils.setSharedPreferencesValue(mContext,Constants.KEY_SUGGEST_DATA,water);
                break;
            case Constants.GetDataFlag.HON_IAQ_GET_VALIDATE_CODE:
            case Constants.GetDataFlag.HON_IAQ_UPDATE_DEVICE:
            case Constants.GetDataFlag.HON_IAQ_UNBIND_DEVICE:
            case Constants.GetDataFlag.HON_IAQ_SET_STANDBY_SCREEN:
            case Constants.GetDataFlag.HON_IAQ_SAVE_POWER_MODE:
            case Constants.GetDataFlag.HON_IAQ_SET_CLOCK:
            case Constants.GetDataFlag.HON_IAQ_SET_TEMPERATURE:
                result.resultCode = Result.RESULT_OK;
                break;
            case Constants.GetDataFlag.HON_IAQ_GET_WEATHER:
                Utils.setSharedPreferencesValue(mContext,Constants.KEY_WEATHER_DATA,water);
                JSONObject json = new JSONObject(water);
                WeatherItem weatherItem = new WeatherItem();
                weatherItem.setWeather(json.optString(Constants.KEY_WEATHER));
                weatherItem.setTemperature(json.optString(Constants.KEY_TEMPERATURE));
                weatherItem.setHumidity(json.optString(Constants.KEY_HUMIDITY));
                weatherItem.setPm25(json.optString(Constants.KEY_PM25));
                weatherItem.setPm10(json.optString(Constants.KEY_PM10));
                weatherItem.setAqi(json.optString(Constants.KEY_AQI));
                weatherItem.setTime(json.optString(Constants.KEY_TIME));
                result.resultList.add(weatherItem);
                result.resultCode = Result.RESULT_OK;
                break;
            case Constants.GetDataFlag.HON_IAQ_GET_FORECAST:
                Utils.setSharedPreferencesValue(mContext,Constants.KEY_FORECAST_DATA,water);
                JSONObject jsonObject = new JSONObject(water);
                JSONArray jsonArray = jsonObject.getJSONArray(Constants.KEY_LIST);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject msgType = (JSONObject) jsonArray.get(i);
                    WeatherInfo weatherInfo = new WeatherInfo();
                    weatherInfo.setWeatherDay(msgType.getString(Constants.KEY_WEATHER_DAY));
                    weatherInfo.setWeatherNight(msgType.getString(Constants.KEY_WEATHER_NIGHT));
                    weatherInfo.setTemperatureLow(msgType.getString(Constants.KEY_TEMPERATURE_LOW));
                    weatherInfo.setTemperatureHigh(msgType.getString(Constants.KEY_TEMPERATURE_HIGH));
                    weatherInfo.setDate(msgType.getString(Constants.KEY_DATE));
                    result.resultList.add(weatherInfo);
                }
                result.resultCode = Result.RESULT_OK;
                break;

            default:
                result.resultCode = Result.RESULT_OK;
                break;
        }

        return result;
    }

    @Override
    public void onSuccess(Result result) {
        if (result.resultCode == 500) {
            Utils.showToast(mContext, mContext.getResources().getString(R.string.no_network));
        }
        try {
            mResponse.response(result.resultList, result.resultCode, result.strArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFailed(TubeException e) {
        Log.d("TUBE TASK", "onFailed" + e.getLocalizedMessage());
        Result result = new Result();
        result.resultCode = Result.RESULT_EXCEPTION;
        this.onSuccess(result);
    }


}
