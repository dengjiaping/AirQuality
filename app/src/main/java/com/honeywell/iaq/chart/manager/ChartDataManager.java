package com.honeywell.iaq.chart.manager;

import android.content.Context;
import android.util.Log;

import com.honeywell.iaq.base.IAQType;
import com.honeywell.iaq.chart.model.HistoryDataModel;
import com.honeywell.iaq.chart.view.LineChartView;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Qian Jin on 2/16/2017.
 */

public class ChartDataManager {

    private static final String TAG = "ChartDataManager";

    private Context mContext;
    private String mDeviceId;
    private final int defaultIndex = -9; //默认前30天数据
    private int mXOffsetTemp = -9;
    private final int constantIndex = 9;
    private final int EMPTY = 0;
    private ArrayList<HistoryDataModel> mIaqHistoryDataList = new ArrayList<>();


    public ChartDataManager(Context context) {
        mContext = context;
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public void getDayDataAndRefreshChart(final List<LineChartView> chartViews) {
        mIaqHistoryDataList.clear();
        String utcTimeEnd = Utils.getUTCTime().toString();
        String utcTimeStart = Utils.getHour(48, Constants.UTC_TIME_FORMATTER);
//        String utcTimeStart = Utils.getHour(24, Constants.UTC_TIME_FORMATTER);

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_IAQ_HISTORY);
        params.put(Constants.KEY_DEVICE_ID, mDeviceId);
        params.put(Constants.KEY_GRANULARITY, Constants.GRANULARITY_HOUR);
        params.put(Constants.GRANULARITY_START, utcTimeStart);
        params.put(Constants.GRANULARITY_END, utcTimeEnd);
//        Logger.e("_____start time______", "" + utcTimeStart);
//        Logger.e("_____end time______", "" + utcTimeEnd);
        HttpUtils.getString(mContext, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(mContext, Constants.GetDataFlag.HON_IAQ_GET_DAY_HISTORY, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode == 0) {
                    String responseStr = (String) objects[0];
//                        Log.e(TAG, "getIAQDataHistory: responseStr=" + responseStr);
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONArray jsonArray = jsonObject.optJSONArray(Constants.KEY_DATA);
                        if (jsonArray != null) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject msgType = (JSONObject) jsonArray.get(i);
                                HistoryDataModel historyData = new HistoryDataModel();
                                historyData.setPm25(msgType.getString(Constants.KEY_PM25));

                                if (Constants.GEN_1.equals(IAQType.getGeneration(mContext))) {
                                    if (Utils.isCelsius(mContext)) {
                                        historyData.setTemperature(msgType.getString(Constants.KEY_TEMPERATURE));
                                    } else {
                                        historyData.setTemperature(String.valueOf(Utils.C2W(Float.parseFloat(msgType.getString(Constants.KEY_TEMPERATURE)))));
                                    }
                                } else if (Constants.GEN_2.equals(IAQType.getGeneration(mContext))) {
                                    if (Utils.isCelsius(mContext, mDeviceId)) {
                                        historyData.setTemperature(msgType.getString(Constants.KEY_TEMPERATURE));
                                    } else {
                                        historyData.setTemperature(String.valueOf(Utils.C2W(Float.parseFloat(msgType.getString(Constants.KEY_TEMPERATURE)))));
                                    }
                                }

                                historyData.setHumidity(msgType.getString(Constants.KEY_HUMIDITY));
                                String localUTCTime = Utils.utc2Local(msgType.getString(Constants.KEY_TIMESTAMP),
                                        Constants.UTC_TIME_FORMATTER, Constants.UTC_TIME_FORMATTER);
                                historyData.setTimestamp(localUTCTime);
                                historyData.setHcho(msgType.optString(Constants.KEY_DEVICE_HCHO));
                                historyData.setTvoc(msgType.optString(Constants.KEY_DEVICE_TVOC));
                                historyData.setCo2(msgType.optString(Constants.KEY_DEVICE_CO2));
                                mIaqHistoryDataList.add(historyData);

                            }
                        }

                        addHourZeroPoint();

                        for (LineChartView chart : chartViews) {
                            refreshLineChartView(chart, EMPTY, true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
        }));

    }

    public void getMonthDataAndRefreshChart(final List<LineChartView> chartViews) {
        mIaqHistoryDataList.clear();
        String utcTimeEnd = Utils.getUTCTime().toString();
        String utcTimeStart = Utils.getDate(30, Constants.UTC_TIME_FORMATTER);

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_IAQ_HISTORY);
        params.put(Constants.KEY_DEVICE_ID, mDeviceId);
        params.put(Constants.KEY_GRANULARITY, Constants.GRANULARITY_DAY);
        params.put(Constants.GRANULARITY_START, utcTimeStart);
        params.put(Constants.GRANULARITY_END, utcTimeEnd);

        HttpUtils.getString(mContext, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(mContext, Constants.GetDataFlag.HON_IAQ_GET_MONTH_HISTORY, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode != 0) {
                    return;
                }

                String responseStr = (String) objects[0];
//                Log.e(TAG, "getIAQDataHistory: responseStr=" + responseStr);
                try {
                    JSONObject jsonObject = new JSONObject(responseStr);
                    JSONArray jsonArray = jsonObject.optJSONArray(Constants.KEY_DATA);
                    if (jsonArray != null) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject msgType = (JSONObject) jsonArray.get(i);
                            HistoryDataModel historyData = new HistoryDataModel();
                            historyData.setPm25(msgType.getString(Constants.KEY_PM25));

                            if (Constants.GEN_1.equals(IAQType.getGeneration(mContext))) {
                                if (Utils.isCelsius(mContext)) {
                                    historyData.setTemperature(msgType.getString(Constants.KEY_TEMPERATURE));
                                } else {
                                    historyData.setTemperature(String.valueOf(Utils.C2W(Float.parseFloat(msgType.getString(Constants.KEY_TEMPERATURE)))));
                                }
                            } else if (Constants.GEN_2.equals(IAQType.getGeneration(mContext))) {
                                if (Utils.isCelsius(mContext, mDeviceId)) {
                                    historyData.setTemperature(msgType.getString(Constants.KEY_TEMPERATURE));
                                } else {
                                    historyData.setTemperature(String.valueOf(Utils.C2W(Float.parseFloat(msgType.getString(Constants.KEY_TEMPERATURE)))));
                                }
                            }

                            historyData.setHumidity(msgType.getString(Constants.KEY_HUMIDITY));
                            String localUTCTime = Utils.utc2Local(msgType.getString(Constants.KEY_TIMESTAMP),
                                    Constants.UTC_TIME_FORMATTER, Constants.UTC_TIME_FORMATTER);
                            historyData.setTimestamp(localUTCTime);
                            historyData.setHcho(msgType.optString(Constants.KEY_DEVICE_HCHO));
                            historyData.setTvoc(msgType.optString(Constants.KEY_DEVICE_TVOC));
                            historyData.setCo2(msgType.optString(Constants.KEY_DEVICE_CO2));
                            mIaqHistoryDataList.add(historyData);
                        }
                    }


                    addDayZeroPoint();

                    for (LineChartView chart : chartViews) {
                        refreshLineChartView(chart, EMPTY, false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }));

    }

    public void refreshLineChartView(LineChartView chartView, int index, boolean isDayRequest) {
        List<HistoryDataModel> models = mIaqHistoryDataList;
        if (models == null || models.size() == EMPTY) {
            return;
        }
        setPm25YValues(chartView, models);
        setTemYValues(chartView, models);
        setHumYValues(chartView, models);
        setHCHOYValues(chartView, models);
        setCO2YValues(chartView, models);
        setTVOCYValues(chartView, models);
        List<Float> yValue = new ArrayList<>();
        ArrayList<String> allDateList = new ArrayList<>();

        mXOffsetTemp += index;
        if (mXOffsetTemp > defaultIndex) {
            mXOffsetTemp = defaultIndex;
        } else if (mXOffsetTemp < (-models.size())) {
            mXOffsetTemp = -models.size();
        }

        int indexFor = models.size() + mXOffsetTemp;
        for (int i = indexFor; i < indexFor + constantIndex; i++) {
            if (i < 0 || i >= models.size())
                break;

            if (isDayRequest) {
//                Logger.e("___________________", "" + trimHour(models.get(i).getTimestamp()));
                allDateList.add(trimHour(models.get(i).getTimestamp()));
            } else {
//                Logger.e("___________________", "" + trimDay(models.get(i).getTimestamp()));
                allDateList.add(trimDay(models.get(i).getTimestamp()));
            }


            switch (chartView.getChartType()) {
                case LineChartView.CHART_TYPE_PM25:
                    String pm25 = models.get(i).getPm25();
                    yValue.add(Float.valueOf(pm25));
                    chartView.setYMax(chartView.getPm25YValues()[2]); // update view
                    break;

                case LineChartView.CHART_TYPE_TEMPERATURE:
                    String temperature = models.get(i).getTemperature();
                    yValue.add(Float.valueOf(temperature));
                    chartView.setYMax(chartView.getTemYValues()[2]); // update view
                    break;

                case LineChartView.CHART_TYPE_HUMIDITY:
                    String humidity = models.get(i).getHumidity();
                    yValue.add(Float.valueOf(humidity));
                    chartView.setYMax(chartView.getHumYValues()[2]); // update view
                    break;

                case LineChartView.CHART_TYPE_HCHO:
                    String hcho = models.get(i).getHcho();
                    yValue.add(Float.valueOf(hcho));
                    chartView.setYMax(chartView.getHOHCYValues()[2]); // update view
                    break;
                case LineChartView.CHART_TYPE_CO2:
                    String co2 = models.get(i).getCo2();
                    yValue.add(Float.valueOf(co2));
                    chartView.setYMax(chartView.getCo2YValues()[2]); // update view
                    break;
                case LineChartView.CHART_TYPE_TVOC:
                    String tvoc = models.get(i).getTvoc();
                    yValue.add(Float.valueOf(tvoc));
                    chartView.setYMax(chartView.getTVOCYValues()[2]); // update view
                    break;
                default:
                    break;
            }
        }

        chartView.setYAxisValue(yValue);
        chartView.setXAxisValue(allDateList);
        chartView.setTotalXNum(allDateList.size());
        chartView.setXOffset(mXOffsetTemp + allDateList.size());
        chartView.invalidate();


    }

    private void setPm25YValues(LineChartView chartView, List<HistoryDataModel> models) {
        //取出PM2.5最大值
        List<Float> listString = new ArrayList<Float>();
        for (HistoryDataModel model : models) {
            listString.add(Float.parseFloat(model.getPm25()));
        }
        int pmMax = (int) Float.parseFloat(String.valueOf(Collections.max(listString)));
        int pmHalfMax = (int) pmMax / 2;
        int[] pmYvalues = new int[3];
        pmYvalues[0] = 0;
        pmYvalues[1] = pmHalfMax;
        pmYvalues[2] = pmMax;
        chartView.setPm25YValues(pmYvalues);
    }

    private void setTemYValues(LineChartView chartView, List<HistoryDataModel> models) {
        //取出PM2.5最大值
        List<Float> listString = new ArrayList<Float>();
        for (HistoryDataModel model : models) {
            listString.add(Float.parseFloat(model.getTemperature()));
        }
        int pmMax = (int) Float.parseFloat(String.valueOf(Collections.max(listString)));
        int pmHalfMax = (int) pmMax / 2;
        int[] pmYvalues = new int[3];
        pmYvalues[0] = 0;
        pmYvalues[1] = pmHalfMax;
        pmYvalues[2] = pmMax;
        chartView.setTemYValues(pmYvalues);
    }

    private void setHumYValues(LineChartView chartView, List<HistoryDataModel> models) {
        //取出PM2.5最大值
        List<Float> listString = new ArrayList<Float>();
        for (HistoryDataModel model : models) {
            listString.add(Float.parseFloat(model.getHumidity()));
        }
        int pmMax = (int) Float.parseFloat(String.valueOf(Collections.max(listString)));
        int pmHalfMax = (int) pmMax / 2;
        int[] pmYvalues = new int[3];
        pmYvalues[0] = 0;
        pmYvalues[1] = pmHalfMax;
        pmYvalues[2] = pmMax;
        chartView.setHumYValues(pmYvalues);
    }

    private void setHCHOYValues(LineChartView chartView, List<HistoryDataModel> models) {
        //取出PM2.5最大值
        List<Float> listString = new ArrayList<Float>();
        for (HistoryDataModel model : models) {
            listString.add(Float.parseFloat(model.getHcho()));
        }
        float pmMax = (float) Float.parseFloat(String.valueOf(Collections.max(listString)));
        float pmHalfMax = (float) pmMax / 2;
        float[] pmYvalues = new float[3];
        pmYvalues[0] = 0;
        pmYvalues[1] = Utils.getFloat(pmHalfMax);
        pmYvalues[2] = Utils.getFloat(pmMax);
        chartView.setHOHCYValues(pmYvalues);
    }

    private void setCO2YValues(LineChartView chartView, List<HistoryDataModel> models) {
        //取出PM2.5最大值
        List<Float> listString = new ArrayList<Float>();
        for (HistoryDataModel model : models) {
            listString.add(Float.parseFloat(model.getCo2()));
        }
        int pmMax = (int) Float.parseFloat(String.valueOf(Collections.max(listString)));
        int pmHalfMax = (int) pmMax / 2;
        int[] pmYvalues = new int[3];
        pmYvalues[0] = 0;
        pmYvalues[1] = pmHalfMax;
        pmYvalues[2] = pmMax;
        chartView.setCo2YValues(pmYvalues);
    }

    private void setTVOCYValues(LineChartView chartView, List<HistoryDataModel> models) {
        //取出PM2.5最大值
        List<Float> listString = new ArrayList<Float>();
        for (HistoryDataModel model : models) {
            listString.add(Float.parseFloat(model.getTvoc()));
        }
        float pmMax = (float) Float.parseFloat(String.valueOf(Collections.max(listString)));
        float pmHalfMax = (float) pmMax / 2;
        float[] pmYvalues = new float[3];
        pmYvalues[0] = 0;
        pmYvalues[1] = Utils.getFloat(pmHalfMax);
        pmYvalues[2] = Utils.getFloat(pmMax);
        chartView.setTVOCYValues(pmYvalues);
    }


    private String trimDay(String timestamp) {
        String dateStr = timestamp.substring(timestamp.lastIndexOf('-') + 1,
                timestamp.indexOf(Constants.UTC_TIME_SYMBOL));
        if (Integer.valueOf(dateStr) == 1) {
            dateStr = timestamp.substring(5, timestamp.indexOf(Constants.UTC_TIME_SYMBOL));
        }
        if (dateStr.startsWith(Constants.ZERO)) {
            dateStr = dateStr.substring(dateStr.indexOf(Constants.ZERO) + 1);
        }

        return dateStr;
    }

    private String trimHour(String timestamp) {
        String dateStr = timestamp.substring(timestamp.lastIndexOf(Constants.UTC_TIME_SYMBOL) + 1,
                timestamp.indexOf(":"));
        if (dateStr.startsWith(Constants.ZERO)) {
            dateStr = dateStr.substring(dateStr.indexOf(Constants.ZERO) + 1);
        }

        return dateStr;
    }

    private List<HistoryDataModel> reverseDataSequence(List<HistoryDataModel> data) {
        List<HistoryDataModel> models = new ArrayList<>();

        if (data == null || data.size() == 0)
            return null;

        for (int i = 0; i < data.size(); i++) {
            models.add(data.get(data.size() - 1 - i));
        }

        return models;

    }

    private void addHourZeroPoint() {
        //建立此时刻往前24小时的时间段
        List<String> hourArrayAir = getHourStringsAir();
        //匹配时间，没有值的置为零
        mIaqHistoryDataList = setHourEmotionData(hourArrayAir);
//        for (int i = 0; i < mIaqHistoryDataList.size(); i++) {
//            Logger.e("时间", "" + mIaqHistoryDataList.get(i).getTimestamp());
//            Logger.e("数值", "" + mIaqHistoryDataList.get(i).getPm25());
//        }
    }

    private void addDayZeroPoint() {
        List<String> dateArrayAir = getDateStringsAir();
        mIaqHistoryDataList = setDayEmotionData(dateArrayAir);
    }

    //检查服务器传来的时间值，拼成连续的时间数组,空气
    private List<String> getDateStringsAir() {

        List<String> resultArray = new ArrayList<>();
        String fromDateString;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:");

        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.DATE, -30);
        //起始点
        String fromDateStringTemple = format.format(fromCalendar.getTime());

        for (int i = 0; i < 30; i++) {
            fromCalendar.add(Calendar.DATE, 1);
            fromDateStringTemple = format.format(fromCalendar.getTime());
            fromDateString = fromDateStringTemple.substring(0, 11);
            resultArray.add(fromDateString);
        }

        return resultArray;
    }

    private List<String> getHourStringsAir() {

        List<String> resultArray = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:");

        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.HOUR, -24);
        //起始点
        String fromDateStringTemple = format.format(fromCalendar.getTime());
        for (int i = 0; i < 24; i++) {
            fromCalendar.add(Calendar.HOUR, 1);
            fromDateStringTemple = format.format(fromCalendar.getTime());
            resultArray.add(fromDateStringTemple);
        }

        return resultArray;
    }

    //接口返回的数据和时间段匹配，时间段无值的置为零
    private ArrayList<HistoryDataModel> setHourEmotionData(List<String> dateArray) {
        ArrayList<HistoryDataModel> results = new ArrayList<>();
        for (String date : dateArray) {
            HistoryDataModel emotionDataModel = new HistoryDataModel();
            emotionDataModel.setDate(date);
            emotionDataModel.setTimestamp(date);
            for (int i = 0; i < mIaqHistoryDataList.size(); i++) {
                String timeStamp = mIaqHistoryDataList.get(i).getTimestamp().substring(0, 14);

                if (date.equals(timeStamp)) {
                    emotionDataModel.setPm25(mIaqHistoryDataList.get(i).getPm25());
                    emotionDataModel.setTemperature(mIaqHistoryDataList.get(i).getTemperature());
                    emotionDataModel.setHumidity(mIaqHistoryDataList.get(i).getHumidity());
                    emotionDataModel.setTimestamp(mIaqHistoryDataList.get(i).getTimestamp());
                    emotionDataModel.setHcho(mIaqHistoryDataList.get(i).getHcho());
                    emotionDataModel.setCo2(mIaqHistoryDataList.get(i).getCo2());
                    emotionDataModel.setTvoc(mIaqHistoryDataList.get(i).getTvoc());
                }
            }
            results.add(emotionDataModel);

            if (results.size() >= 24)
                break;
        }

        return results;
    }

    private ArrayList<HistoryDataModel> setDayEmotionData(List<String> dateArray) {
        ArrayList<HistoryDataModel> results = new ArrayList<>();
        for (String date : dateArray) {
            HistoryDataModel emotionDataModel = new HistoryDataModel();
            emotionDataModel.setDate(date);
            emotionDataModel.setTimestamp(date);
            for (int i = 0; i < mIaqHistoryDataList.size(); i++) {
                if (date.equals(mIaqHistoryDataList.get(i).getTimestamp().substring(0, 11))) {
                    emotionDataModel.setPm25(mIaqHistoryDataList.get(i).getPm25());
                    emotionDataModel.setTemperature(mIaqHistoryDataList.get(i).getTemperature());
                    emotionDataModel.setHumidity(mIaqHistoryDataList.get(i).getHumidity());
                    emotionDataModel.setTimestamp(mIaqHistoryDataList.get(i).getTimestamp());
                    emotionDataModel.setHcho(mIaqHistoryDataList.get(i).getHcho());
                    emotionDataModel.setCo2(mIaqHistoryDataList.get(i).getCo2());
                    emotionDataModel.setTvoc(mIaqHistoryDataList.get(i).getTvoc());
                }
            }
            results.add(emotionDataModel);
        }
        return results;
    }


}

