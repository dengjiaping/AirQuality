//package com.honeywell.iaq.chart.manager;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.honeywell.iaq.chart.model.HistoryDataModel;
//import com.honeywell.iaq.interfaces.IResponse;
//import com.honeywell.iaq.task.TubeTask;
//import com.honeywell.iaq.utils.Constants;
//import com.honeywell.iaq.utils.HttpUtils;
//import com.honeywell.iaq.utils.IAQRequestUtils;
//import com.honeywell.iaq.utils.Utils;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import lecho.lib.hellocharts.view.LineChartView;
//
///**
// * Created by Qian Jin on 2/16/2017.
// */
//
//public class ChartDataManager2 {
//
//    private static final String TAG = "ChartDataManager";
//
//    private Context mContext;
//    private String mDeviceId;
//    private final int hourDefault = -9; //默认前30天数据
//    private final int defaultIndex = -24; //默认前30天数据
//    private int mXOffsetTemp = -9;
//    private final int constantIndex = 9;
//    private final int EMPTY = 0;
//    private ArrayList<HistoryDataModel> mIaqHistoryDataList = new ArrayList<>();
//
//
//    public ChartDataManager2(Context context, String deviceId) {
//        mContext = context;
//        mDeviceId = deviceId;
//    }
//
//    public void getDayDataAndRefreshChart(final List<LineChartView> chartViews) {
//        mIaqHistoryDataList.clear();
//        String utcTimeEnd = Utils.getUTCTime().toString();
//        String utcTimeStart = Utils.getDate(30, Constants.UTC_TIME_FORMATTER);
//
//        Map<String, String> params = new HashMap<>();
//        params.put(Constants.KEY_TYPE, Constants.TYPE_IAQ_HISTORY);
//        params.put(Constants.KEY_DEVICE_ID, mDeviceId);
//        params.put(Constants.KEY_GRANULARITY, Constants.GRANULARITY_HOUR);
//        params.put(Constants.GRANULARITY_START, utcTimeStart);
//        params.put(Constants.GRANULARITY_END, utcTimeEnd);
//
//        HttpUtils.getString(mContext, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(mContext, Constants.GetDataFlag.HON_IAQ_GET_DAY_HISTORY, new IResponse() {
//            @Override
//            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
//                if (resultCode == 0) {
//                    String responseStr = (String) objects[0];
//                    try {
//                        JSONObject jsonObject = new JSONObject(responseStr);
//                        JSONArray jsonArray = jsonObject.optJSONArray(Constants.KEY_DATA);
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            JSONObject msgType = (JSONObject) jsonArray.get(i);
//                            HistoryDataModel historyData = new HistoryDataModel();
//                            historyData.setPm25(msgType.getString(Constants.KEY_PM25));
//                            historyData.setTemperature(msgType.getString(Constants.KEY_TEMPERATURE));
//                            historyData.setHumidity(msgType.getString(Constants.KEY_HUMIDITY));
//                            String localUTCTime = Utils.utc2Local(msgType.getString(Constants.KEY_TIMESTAMP),
//                                    Constants.UTC_TIME_FORMATTER, Constants.UTC_TIME_FORMATTER);
//                            historyData.setTimestamp(localUTCTime);
//                            mIaqHistoryDataList.add(historyData);
//                        }
//
//                        addHourZeroPoint();
//
//                        for (LineChartView chart : chartViews) {
//                            refreshLineChartView(chart, EMPTY, true);
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//
//                }
//            }
//        }));
//
//    }
//
//    public void getMonthDataAndRefreshChart(final List<LineChartView> chartViews) {
//        mIaqHistoryDataList.clear();
//        String utcTimeEnd = Utils.getUTCTime().toString();
//        String utcTimeStart = Utils.getDate(30, Constants.UTC_TIME_FORMATTER);
//
//        Map<String, String> params = new HashMap<>();
//        params.put(Constants.KEY_TYPE, Constants.TYPE_IAQ_HISTORY);
//        params.put(Constants.KEY_DEVICE_ID, mDeviceId);
//        params.put(Constants.KEY_GRANULARITY, Constants.GRANULARITY_DAY);
//        params.put(Constants.GRANULARITY_START, utcTimeStart);
//        params.put(Constants.GRANULARITY_END, utcTimeEnd);
//
//        HttpUtils.getString(mContext, Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(mContext, Constants.GetDataFlag.HON_IAQ_GET_MONTH_HISTORY, new IResponse() {
//            @Override
//            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
//                if (resultCode != 0) {
//                    return;
//                }
//
//                String responseStr = (String) objects[0];
////                Log.e(TAG, "getIAQDataHistory: responseStr=" + responseStr);
//                try {
//                    JSONObject jsonObject = new JSONObject(responseStr);
//                    JSONArray jsonArray = jsonObject.optJSONArray(Constants.KEY_DATA);
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject msgType = (JSONObject) jsonArray.get(i);
//                        HistoryDataModel historyData = new HistoryDataModel();
//                        historyData.setPm25(msgType.getString(Constants.KEY_PM25));
//                        historyData.setTemperature(msgType.getString(Constants.KEY_TEMPERATURE));
//                        historyData.setHumidity(msgType.getString(Constants.KEY_HUMIDITY));
//                        String localUTCTime = Utils.utc2Local(msgType.getString(Constants.KEY_TIMESTAMP),
//                                Constants.UTC_TIME_FORMATTER, Constants.UTC_TIME_FORMATTER);
//                        historyData.setTimestamp(localUTCTime);
//                        mIaqHistoryDataList.add(historyData);
//                    }
//
//                    addDayZeroPoint();
//
//                    for (LineChartView chart : chartViews) {
//                        refreshLineChartView(chart, EMPTY, false);
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }));
//    }
//
//
//
//    public void refreshLineChartView(LineChartView chartView, int index, boolean isDayRequest) {
//        List<HistoryDataModel> models = mIaqHistoryDataList;
//        if (models == null || models.size() == EMPTY) {
//            return;
//        }
//
//        List<Float> yValue = new ArrayList<>();
//        ArrayList<String> allDateList = new ArrayList<>();
//        mXOffsetTemp += index;
//        if (mXOffsetTemp > defaultIndex) {
//            mXOffsetTemp = defaultIndex;
//        } else if (mXOffsetTemp < (-models.size())) {
//            mXOffsetTemp = -models.size();
//        }
//
//        int indexFor = models.size() + mXOffsetTemp;
//        for (int i = indexFor; i < indexFor + constantIndex; i++) {
//            if (i < 0 || i >= models.size())
//                break;
//
//            if (isDayRequest)
//                allDateList.add(trimHour(models.get(i).getTimestamp()));
//            else
//                allDateList.add(trimDay(models.get(i).getTimestamp()));
//
//            switch (chartView.getChartType()) {
//                case LineChartView.CHART_TYPE_PM25:
//                    String pm25 = models.get(i).getPm25();
//                    yValue.add(Float.valueOf(pm25));
//                    chartView.setYMax(LineChartView.PM25_Y_VALUES[4]); // update view
//                    break;
//
//                case LineChartView.CHART_TYPE_TEMPERATURE:
//                    String temperature = models.get(i).getTemperature();
//                    yValue.add(Float.valueOf(temperature));
//                    chartView.setYMax(LineChartView.TEMPERATURE_Y_VALUES[4]); // update view
//                    break;
//
//                case LineChartView.CHART_TYPE_HUMIDITY:
//                    String humidity = models.get(i).getHumidity();
//                    yValue.add(Float.valueOf(humidity));
//                    chartView.setYMax(LineChartView.HUMIDITY_Y_VALUES[4]); // update view
//                    break;
//
//                default:
//                    break;
//            }
//        }
//
//        chartView.setYAxisValue(yValue);
//        chartView.setXAxisValue(allDateList);
//        chartView.setTotalXNum(allDateList.size());
//        chartView.setXOffset(mXOffsetTemp + allDateList.size());
//
//    }
//
//    private String trimDay(String timestamp) {
//        String dateStr = timestamp.substring(timestamp.lastIndexOf('-') + 1,
//                timestamp.indexOf(Constants.UTC_TIME_SYMBOL));
//        if (Integer.valueOf(dateStr) == 1) {
//            dateStr = timestamp.substring(5, timestamp.indexOf(Constants.UTC_TIME_SYMBOL));
//        }
//        if (dateStr.startsWith(Constants.ZERO)) {
//            dateStr = dateStr.substring(dateStr.indexOf(Constants.ZERO) + 1);
//        }
//
//        return dateStr;
//    }
//
//    private String trimHour(String timestamp) {
//        String dateStr = timestamp.substring(timestamp.lastIndexOf(Constants.UTC_TIME_SYMBOL) + 1,
//                timestamp.indexOf(":"));
//        if (dateStr.startsWith(Constants.ZERO)) {
//            dateStr = dateStr.substring(dateStr.indexOf(Constants.ZERO) + 1);
//        }
//
//        return dateStr;
//    }
//
//    private List<HistoryDataModel> reverseDataSequence(List<HistoryDataModel> data) {
//        List<HistoryDataModel> models = new ArrayList<>();
//
//        if (data == null || data.size() == 0)
//            return null;
//
//        for (int i = 0; i < data.size(); i++) {
//            models.add(data.get(data.size() - 1 - i));
//        }
//
//        return models;
//
//    }
//
//    private void addHourZeroPoint() {
//        List<String> hourArrayAir = getHourStringsAir();
//
//        mIaqHistoryDataList = setHourEmotionData(hourArrayAir);
//
//    }
//
//    private void addDayZeroPoint() {
//        List<String> dateArrayAir = getDateStringsAir();
//        mIaqHistoryDataList = setDayEmotionData(dateArrayAir);
//    }
//
//    //检查服务器传来的时间值，拼成连续的时间数组,空气
//    private List<String> getDateStringsAir() {
//        List<String> resultArray = new ArrayList<>();
//        String fromDateString;
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//
//
//        Calendar fromCalendar = Calendar.getInstance();
//        fromCalendar.add(Calendar.DATE, defaultIndex);
//        //起始点
//        String fromDateStringTemple = format.format(fromCalendar.getTime());
//        Date fromDateFromServe = null;
//        Calendar calFromServe = Calendar.getInstance();
//        try {
//            fromDateFromServe = format.parse(mIaqHistoryDataList.get(mIaqHistoryDataList.size() - 1)
//                    .getTimestamp().substring(0, 11));
//            calFromServe.setTime(fromDateFromServe);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//
//        if (fromDateFromServe == null || fromCalendar.before(calFromServe)) {
//            fromDateString = fromDateStringTemple + 'T';
//        } else {
//            fromDateString = mIaqHistoryDataList.get(mIaqHistoryDataList.size() - 1)
//                    .getTimestamp().substring(0, 11);
//        }
//
//        Calendar currentCalendar = Calendar.getInstance();
//        currentCalendar.add(Calendar.DATE, -1);
//        String toDateString = format.format(currentCalendar.getTime());
//        resultArray.add(fromDateString);
//
//        Date fromDate = new Date();
//        try {
//            fromDate = format.parse(fromDateString);
//        } catch (ParseException ex) {
//            ex.printStackTrace();
//        }
//        for (int i = 1; i < 1000; i++) {
//            currentCalendar.setTime(fromDate);
//            currentCalendar.add(Calendar.DATE, i);  //把日期往后增加一天.整数往后推,负数往前移动
//            Date date = currentCalendar.getTime();   //这个时间就是日期往后推一天的结果
//
//            String dateString = format.format(date);
//            resultArray.add(dateString + "T");
//
//            if (dateString.equals(toDateString)) {
//                break;
//            }
//        }
//        return resultArray;
//    }
//
//    private List<String> getHourStringsAir() {
//        List<String> resultArray = new ArrayList<>();
//        String fromDateString;
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:");
//
//        Calendar fromCalendar = Calendar.getInstance();
//        fromCalendar.add(Calendar.HOUR, -24);
//        //起始点
//        String fromDateStringTemple = format.format(fromCalendar.getTime());
//        Log.e("当天数据起始点时间", "" + fromDateStringTemple);
//        for (int i = 0; i < 24; i++) {
//            fromCalendar.add(Calendar.HOUR, 1);
//             fromDateStringTemple = format.format(fromCalendar.getTime());
//            Log.e("当天数据起始点时间", "" + fromDateStringTemple);
//            resultArray.add(fromDateStringTemple);
//        }
//
//        return resultArray;
//    }
//
//    //初始化一个 emotion折线和柱状图数据数组
//    private ArrayList<HistoryDataModel> setHourEmotionData(List<String> dateArray) {
//        ArrayList<HistoryDataModel> results = new ArrayList<>();
//        for (String date : dateArray) {
//            HistoryDataModel emotionDataModel = new HistoryDataModel();
//            emotionDataModel.setDate(date);
//            emotionDataModel.setTimestamp(date);
//            for (int i = 0; i < mIaqHistoryDataList.size(); i++) {
//                if (date.equals(mIaqHistoryDataList.get(i).getTimestamp().substring(0, 14))) {
//                    emotionDataModel.setPm25(mIaqHistoryDataList.get(i).getPm25());
//                    emotionDataModel.setTemperature(mIaqHistoryDataList.get(i).getTemperature());
//                    emotionDataModel.setHumidity(mIaqHistoryDataList.get(i).getHumidity());
//                    emotionDataModel.setTimestamp(mIaqHistoryDataList.get(i).getTimestamp());
//                }
//            }
//            results.add(emotionDataModel);
//
//            if (results.size() >= 24)
//                break;
//        }
//
//        return results;
//    }
//
//    private ArrayList<HistoryDataModel> setDayEmotionData(List<String> dateArray) {
//        ArrayList<HistoryDataModel> results = new ArrayList<>();
//        for (String date : dateArray) {
//            HistoryDataModel emotionDataModel = new HistoryDataModel();
//            emotionDataModel.setDate(date);
//            emotionDataModel.setTimestamp(date);
//            for (int i = 0; i < mIaqHistoryDataList.size(); i++) {
//                if (date.equals(mIaqHistoryDataList.get(i).getTimestamp().substring(0, 11))) {
//                    emotionDataModel.setPm25(mIaqHistoryDataList.get(i).getPm25());
//                    emotionDataModel.setTemperature(mIaqHistoryDataList.get(i).getTemperature());
//                    emotionDataModel.setHumidity(mIaqHistoryDataList.get(i).getHumidity());
//                    emotionDataModel.setTimestamp(mIaqHistoryDataList.get(i).getTimestamp());
//                }
//            }
//            results.add(emotionDataModel);
//        }
//        return results;
//    }
//
//
//}
//
