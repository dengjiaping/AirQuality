package com.honeywell.iaq.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQBaseFragment;
import com.honeywell.iaq.chart.manager.ChartDataManager;
import com.honeywell.iaq.chart.model.HistoryDataModel;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQWeatherEvent;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.widget.CustomScrollView;
import com.honeywell.net.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Qian Jin on 2/16/2017.
 */
public class DayDataFragment2 extends IAQBaseFragment {

    private ArrayList<HistoryDataModel> mIaqHistoryDataList = new ArrayList<>();
    protected ChartDataManager mChartDataManager;
    private List<PointValue> mPointPM25Values = new ArrayList<PointValue>();
    private List<PointValue> mPointTemValues = new ArrayList<PointValue>();
    private List<PointValue> mPointHumiValues = new ArrayList<PointValue>();
    private List<PointValue> mPointHOHCValues = new ArrayList<PointValue>();
    private List<PointValue> mPointCO2Values = new ArrayList<PointValue>();
    private List<PointValue> mPointTVOCValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();
    private float mLastY;
    private View mWholeView;
    private LineChartView mPm25ChartView, mTemperatureChartView, mHumidityChartView;
    private LineChartView mExtraFirstChartView;
    private LineChartView mExtraSecondhartView;
    private LineChartView mExtraThirdChartView;
    private TextView mPM25XUnit, mTemperatureXUnit, mHumidityXUnit;
    private TextView mHCHOXUnit;
    private TextView mCO2XUnit;
    private TextView mTVOCXUnit;
    private String currentSerialNum;
    private String deviceId;

    private RelativeLayout mPmView;
    private RelativeLayout mTmView;
    private RelativeLayout mHumView;
    private RelativeLayout mFirstView;
    private RelativeLayout mSecondView;
    private RelativeLayout mThirdView;
    private static int contentViewHeight;
    private CustomScrollView mScrollView;

    public static DayDataFragment2 newInstance(int page, int viewHeight) {
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_PAGE, page);
        DayDataFragment2 fragment = new DayDataFragment2();
        fragment.setArguments(args);
        contentViewHeight = viewHeight;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChartDataManager = new ChartDataManager(getContext());


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public int getLayout() {
        return R.layout.fragment_chart_2;
    }

    @Override
    public void initView(View view) {
        mPmView = (RelativeLayout) view.findViewById(R.id.pmView);
        mTmView = (RelativeLayout) view.findViewById(R.id.temPeratureView);
        mHumView = (RelativeLayout) view.findViewById(R.id.humidityView);
        mFirstView = (RelativeLayout) view.findViewById(R.id.firstView);
        mSecondView = (RelativeLayout) view.findViewById(R.id.secondView);
        mThirdView = (RelativeLayout) view.findViewById(R.id.thirdView);
        initHeight();
        mPm25ChartView = (LineChartView) view.findViewById(R.id.line_chart_pm25);
        mTemperatureChartView = (LineChartView) view.findViewById(R.id.line_chart_temperature);
        mHumidityChartView = (LineChartView) view.findViewById(R.id.line_chart_humidity);

        mPM25XUnit = (TextView) view.findViewById(R.id.pm25_day_unit);
        mPM25XUnit.setText(getResources().getString(R.string.hour_unit));

        mTemperatureXUnit = (TextView) view.findViewById(R.id.temperature_day_unit);
        mTemperatureXUnit.setText(getResources().getString(R.string.hour_unit));

        mHumidityXUnit = (TextView) view.findViewById(R.id.humidity_day_unit);
        mHumidityXUnit.setText(getResources().getString(R.string.hour_unit));
        mScrollView = (CustomScrollView) view.findViewById(R.id.scrollView);
        /****************/
        mExtraFirstChartView = (LineChartView) view.findViewById(R.id.line_chart_extra_first);
        mExtraSecondhartView = (LineChartView) view.findViewById(R.id.line_chart_extra_second);
        mExtraThirdChartView = (LineChartView) view.findViewById(R.id.line_chart_extra_third);
        mExtraFirstChartView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    mLastY = event.getY();
                }
//                if (action == MotionEvent.ACTION_MOVE) {
//                    float nowY = event.getY();
//                    Logger.e("-------",""+Math.abs(nowY - mLastY));
//                    if (Math.abs(nowY - mLastY) > 50) {
//                        Logger.e("-------","外层滑动");
//                        mScrollView.requestDisallowInterceptTouchEvent(true);
//
//                    } else {
//                        Logger.e("-------","内层滑动");
//                        mScrollView.requestDisallowInterceptTouchEvent(false);
//                    }
//                }

                return false;
            }
        });

        mHCHOXUnit = (TextView) view.findViewById(R.id.extra_first_day_unit);
        mHCHOXUnit.setText(getResources().getString(R.string.hour_unit));
        mCO2XUnit = (TextView) view.findViewById(R.id.extra_second_day_unit);
        mCO2XUnit.setText(getResources().getString(R.string.hour_unit));
        mTVOCXUnit = (TextView) view.findViewById(R.id.extra_third_day_unit);
        mTVOCXUnit.setText(getResources().getString(R.string.hour_unit));
//
    }

    private void initHeight() {
        Logger.e("____________", "contentViewHeight" + contentViewHeight);
        ViewGroup.LayoutParams layoutParams;
        layoutParams = mPmView.getLayoutParams();
        layoutParams.height = contentViewHeight / 3;
        mPmView.setLayoutParams(layoutParams);
        mTmView.setLayoutParams(layoutParams);
        mHumView.setLayoutParams(layoutParams);
        mFirstView.setLayoutParams(layoutParams);
        mSecondView.setLayoutParams(layoutParams);
        mThirdView.setLayoutParams(layoutParams);
    }


    private void initData() {
        int support_type = getSupportNewParameter();
        support_type = EnvironmentDetialFragment3.HCHO_CO2_TVOC_NONE;
        Logger.e("____________", "support_type" + support_type);
        List<LineChartView> chartViews = new ArrayList<>();
        chartViews.add(mPm25ChartView);
        chartViews.add(mTemperatureChartView);
        chartViews.add(mHumidityChartView);

        switch (support_type) {
            case EnvironmentDetialFragment3.HCHO_ONLY:
                chartViews.add(mExtraFirstChartView);
                mSecondView.setVisibility(View.GONE);
                mThirdView.setVisibility(View.GONE);
                break;
            case EnvironmentDetialFragment3.CO2_ONLY:
                chartViews.add(mExtraSecondhartView);
                mFirstView.setVisibility(View.GONE);
                mThirdView.setVisibility(View.GONE);
                break;
            case EnvironmentDetialFragment3.TVOC_ONLY:
                chartViews.add(mExtraThirdChartView);
                mFirstView.setVisibility(View.GONE);
                mSecondView.setVisibility(View.GONE);
                break;
            case EnvironmentDetialFragment3.HCHO_CO2:
                chartViews.add(mExtraFirstChartView);
                chartViews.add(mExtraSecondhartView);
                mThirdView.setVisibility(View.GONE);
                break;
            case EnvironmentDetialFragment3.HCHO_TVOC:
                chartViews.add(mExtraFirstChartView);
                chartViews.add(mExtraThirdChartView);
                mSecondView.setVisibility(View.GONE);
                break;
            case EnvironmentDetialFragment3.CO2_TVOC:
                chartViews.add(mExtraSecondhartView);
                chartViews.add(mExtraThirdChartView);
                mFirstView.setVisibility(View.GONE);

                break;
            case EnvironmentDetialFragment3.HCHO_CO2_TVOC:
                chartViews.add(mExtraFirstChartView);
                chartViews.add(mExtraSecondhartView);
                chartViews.add(mExtraThirdChartView);
                break;
            case EnvironmentDetialFragment3.HCHO_CO2_TVOC_NONE:
                mFirstView.setVisibility(View.GONE);
                mSecondView.setVisibility(View.GONE);
                mThirdView.setVisibility(View.GONE);
                break;
        }

        getDayDataAndRefreshChart();

    }


    private int getSupportNewParameter() {

        currentSerialNum = Utils.getSharedPreferencesValue(getActivity().getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
        Logger.e("____________", "序列号" + currentSerialNum);
        if (currentSerialNum.startsWith("001000021") || currentSerialNum.startsWith("001000022")) {

            return EnvironmentDetialFragment3.HCHO_CO2_TVOC_NONE;

        } else if (currentSerialNum.startsWith("001000023") || currentSerialNum.startsWith("001000024")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC;
        } else if (currentSerialNum.startsWith("001000025") || currentSerialNum.startsWith("001000026")) {
            return EnvironmentDetialFragment3.TVOC_ONLY;
        } else if (currentSerialNum.startsWith("001000027") || currentSerialNum.startsWith("001000028")) {
            return EnvironmentDetialFragment3.HCHO_ONLY;
        } else if (currentSerialNum.startsWith("001000029") || currentSerialNum.startsWith("00100002a")) {
            return EnvironmentDetialFragment3.CO2_ONLY;
        } else if (currentSerialNum.startsWith("001000030")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC;
        } else if (currentSerialNum.startsWith("001000031")) {
            return EnvironmentDetialFragment3.TVOC_ONLY;
        } else if (currentSerialNum.startsWith("001000032")) {
            return EnvironmentDetialFragment3.HCHO_ONLY;
        } else if (currentSerialNum.startsWith("001000033")) {
            return EnvironmentDetialFragment3.CO2_ONLY;
        } else if (currentSerialNum.startsWith("001000034")) {
            return EnvironmentDetialFragment3.HCHO_CO2_TVOC;
        }
        return EnvironmentDetialFragment3.HCHO_CO2_TVOC;

    }


    private void getDeviceIdLocation() {
        String[] strings = PreferenceUtil.getDeviceIdLoaction(getContext());
        deviceId = strings[0];
    }

    public void getDayDataAndRefreshChart() {
        mIaqHistoryDataList.clear();
        String utcTimeEnd = Utils.getUTCTime().toString();
        String utcTimeStart = Utils.getDate(30, Constants.UTC_TIME_FORMATTER);

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_IAQ_HISTORY);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        params.put(Constants.KEY_GRANULARITY, Constants.GRANULARITY_HOUR);
        params.put(Constants.GRANULARITY_START, utcTimeStart);
        params.put(Constants.GRANULARITY_END, utcTimeEnd);

        HttpUtils.getString(getContext(), Constants.DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getContext(), Constants.GetDataFlag.HON_IAQ_GET_DAY_HISTORY, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode == 0) {
                    String responseStr = (String) objects[0];
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONArray jsonArray = jsonObject.optJSONArray(Constants.KEY_DATA);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject msgType = (JSONObject) jsonArray.get(i);
                            HistoryDataModel historyData = new HistoryDataModel();
                            historyData.setPm25(msgType.getString(Constants.KEY_PM25));
                            historyData.setTemperature(msgType.getString(Constants.KEY_TEMPERATURE));
                            historyData.setHumidity(msgType.getString(Constants.KEY_HUMIDITY));
                            String localUTCTime = Utils.utc2Local(msgType.getString(Constants.KEY_TIMESTAMP),
                                    Constants.UTC_TIME_FORMATTER, Constants.UTC_TIME_FORMATTER);
                            historyData.setTimestamp(localUTCTime);
                            mIaqHistoryDataList.add(historyData);
                        }

                        addHourZeroPoint();
                        //挨个刷新6个view

                        getAxisXLables();//获取x轴的标注
                        getAxisPoints();//获取坐标点
                        initLineChart(Constants.CHART_TYPE_PM25);
                        initLineChart(Constants.CHART_TYPE_TEMPERATURE);
                        initLineChart(Constants.CHART_TYPE_HUMIDITY);
                        initLineChart(Constants.CHART_TYPE_HCHO);
                        initLineChart(Constants.CHART_TYPE_CO2);
                        initLineChart(Constants.CHART_TYPE_TVOC);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }
        }));

    }


    private void addHourZeroPoint() {
        List<String> hourArrayAir = getHourStringsAir();

        mIaqHistoryDataList = setHourEmotionData(hourArrayAir);

    }

    //初始化一个 emotion折线和柱状图数据数组
    private ArrayList<HistoryDataModel> setHourEmotionData(List<String> dateArray) {
        ArrayList<HistoryDataModel> results = new ArrayList<>();
        for (String date : dateArray) {
            HistoryDataModel emotionDataModel = new HistoryDataModel();
            emotionDataModel.setDate(date);
            emotionDataModel.setTimestamp(date);
            for (int i = 0; i < mIaqHistoryDataList.size(); i++) {
                if (date.equals(mIaqHistoryDataList.get(i).getTimestamp().substring(0, 14))) {
                    emotionDataModel.setPm25(mIaqHistoryDataList.get(i).getPm25());
                    emotionDataModel.setTemperature(mIaqHistoryDataList.get(i).getTemperature());
                    emotionDataModel.setHumidity(mIaqHistoryDataList.get(i).getHumidity());
                    emotionDataModel.setTimestamp(mIaqHistoryDataList.get(i).getTimestamp());
                }
            }
            results.add(emotionDataModel);

            if (results.size() >= 24)
                break;
        }

        return results;
    }


    private List<String> getHourStringsAir() {
        List<String> resultArray = new ArrayList<>();
        String fromDateString;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:");

        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.HOUR, -24);
        //起始点
        String fromDateStringTemple = format.format(fromCalendar.getTime());
        Log.e("当天数据起始点时间", "" + fromDateStringTemple);
        for (int i = 0; i < 24; i++) {
            fromCalendar.add(Calendar.HOUR, 1);
            fromDateStringTemple = format.format(fromCalendar.getTime());
            Log.e("当天数据起始点时间", "" + fromDateStringTemple);
            resultArray.add(fromDateStringTemple);
        }

        return resultArray;
    }

    private String trimHour(String timestamp) {
        String dateStr = timestamp.substring(timestamp.lastIndexOf(Constants.UTC_TIME_SYMBOL) + 1,
                timestamp.indexOf(":"));
        if (dateStr.startsWith(Constants.ZERO)) {
            dateStr = dateStr.substring(dateStr.indexOf(Constants.ZERO) + 1);
        }

        return dateStr;
    }

    /**
     * X 轴的显示
     */
    private void getAxisXLables() {
        ArrayList<String> allDateList = new ArrayList<>();

        for (int i = 0; i < mIaqHistoryDataList.size(); i++) {

            allDateList.add(trimHour(mIaqHistoryDataList.get(i).getTimestamp()));

        }
        for (int i = 0; i < allDateList.size(); i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(allDateList.get(i)));
        }


    }

    /**
     * 图表的每个点的显示
     */
    private void getAxisPoints() {
        for (int i = 0; i < mIaqHistoryDataList.size(); i++) {
            String yPm25 = mIaqHistoryDataList.get(i).getPm25();
            String yTemp = mIaqHistoryDataList.get(i).getTemperature();
            mPointPM25Values.add(new PointValue(i, Float.parseFloat(yPm25)));
            mPointTemValues.add(new PointValue(i, Float.parseFloat(yTemp)));
            mPointHumiValues.add(new PointValue(i, Float.parseFloat(yTemp)));
            mPointHOHCValues.add(new PointValue(i, Float.parseFloat(yTemp)));
            mPointCO2Values.add(new PointValue(i, Float.parseFloat(yTemp)));
            mPointTVOCValues.add(new PointValue(i, Float.parseFloat(yTemp)));
        }
    }


    private void initLineChart(int type) {
        LineChartData data = new LineChartData();
        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X轴下面坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.parseColor("#D6D6D9"));//灰色
        axisX.setTextSize(11);//设置字体大小
        axisX.setMaxLabelChars(7); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        axisX.setHasLines(false); //x 轴分割线
        data.setAxisXBottom(axisX); //x 轴在底部


        Axis axisY = new Axis();  //Y轴
        axisY.setName("");//y轴标注
        axisY.setTextSize(11);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
        //设置行为属性，支持缩放、滑动以及平移
        List<Line> lines = new ArrayList<Line>();
        Line line = null;
        switch (type) {
            case Constants.CHART_TYPE_PM25:
                line = new Line(mPointPM25Values).setColor(Color.parseColor("#FFCD41"));  //折线的颜色
                line.setCubic(true);//曲线是否平滑
                line.setFilled(true);//是否填充曲线的面积
                line.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示
                lines.add(line);
                data.setLines(lines);
                initChart(mPm25ChartView, data);
                break;
            case Constants.CHART_TYPE_TEMPERATURE:
                line = new Line(mPointTemValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色
                line.setCubic(true);//曲线是否平滑
                line.setFilled(true);//是否填充曲线的面积
                line.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示
                lines.add(line);
                data.setLines(lines);
                initChart(mTemperatureChartView, data);
                break;
            case Constants.CHART_TYPE_HUMIDITY:
                line = new Line(mPointHumiValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色
                line.setCubic(true);//曲线是否平滑
                line.setFilled(true);//是否填充曲线的面积
                line.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示
                lines.add(line);
                data.setLines(lines);
                initChart(mHumidityChartView, data);
                break;
            case Constants.CHART_TYPE_HCHO:
                line = new Line(mPointHOHCValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色
                line.setCubic(true);//曲线是否平滑
                line.setFilled(true);//是否填充曲线的面积
                line.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示
                lines.add(line);
                data.setLines(lines);
                initChart(mExtraFirstChartView, data);
                break;
            case Constants.CHART_TYPE_CO2:
                line = new Line(mPointCO2Values).setColor(Color.parseColor("#FFCD41"));  //折线的颜色

                line.setCubic(true);//曲线是否平滑
                line.setFilled(true);//是否填充曲线的面积
                line.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示
                lines.add(line);
                data.setLines(lines);
                initChart(mExtraSecondhartView, data);
                break;
            case Constants.CHART_TYPE_TVOC:
                line = new Line(mPointTVOCValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色

                line.setCubic(true);//曲线是否平滑
                line.setFilled(true);//是否填充曲线的面积
                line.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示
                lines.add(line);
                data.setLines(lines);
                initChart(mExtraThirdChartView, data);
                break;
        }
    }

    private void initChart(LineChartView lineChartView, LineChartData data) {
        lineChartView.setInteractive(true);
        lineChartView.setZoomEnabled(false);
        lineChartView.setLineChartData(data);
        lineChartView.setVisibility(View.VISIBLE);

        Viewport v = new Viewport(lineChartView.getMaximumViewport());
        v.left = 0;
        v.right = 7;
        lineChartView.setCurrentViewport(v);
    }


    @Override
    public void onEventMainThread(IAQEvents event) {

        super.onEventMainThread(event);
        if (event instanceof IAQWeatherEvent) {
            IAQWeatherEvent iaqWeatherEvent = (IAQWeatherEvent) event;
            switch (iaqWeatherEvent.type) {
                case IAQWeatherEvent.GET_LOCATION_SUCCESS:
                    getDeviceIdLocation();
                    mChartDataManager.setDeviceId(deviceId);
                    initData();
                    break;
            }

        }
    }


}
