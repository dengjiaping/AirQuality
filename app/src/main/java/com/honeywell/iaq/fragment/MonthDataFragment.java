package com.honeywell.iaq.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQType;
import com.honeywell.iaq.chart.manager.ChartDataManager;
import com.honeywell.iaq.chart.manager.ChartDataManager3;
import com.honeywell.iaq.chart.view.LineChartView;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQTemperatureEvent;
import com.honeywell.iaq.events.IAQWeatherEvent;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Qian Jin on 2/16/2017.
 */
public class MonthDataFragment extends Fragment {

    protected ChartDataManager mChartDataManager;

    private View mWholeView;
    private LineChartView mPm25ChartView, mTemperatureChartView, mHumidityChartView;
    private LineChartView mExtraFirstChartView;
    private LineChartView mExtraSecondhartView;
    private LineChartView mExtraThirdChartView;
    private TextView mPM25XUnit, mTemperatureXUnit, mHumidityXUnit;
    private TextView mHCHOXUnit;
    private TextView mCO2XUnit;
    private TextView mTVOCXUnit;
    private TextView mTemperatureYUnit;
    private String deviceId;

    private RelativeLayout mPmView;
    private RelativeLayout mTmView;
    private RelativeLayout mHumView;
    private RelativeLayout mFirstView;
    private RelativeLayout mSecondView;
    private RelativeLayout mThirdView;

    private static int contentViewHeight;
    private String currentSerialNum;

    public static MonthDataFragment newInstance(int page,int viewHeight) {
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_PAGE, page);
        MonthDataFragment fragment = new MonthDataFragment();
        fragment.setArguments(args);
        contentViewHeight = viewHeight;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDeviceIdLocation();
        String[] strings = PreferenceUtil.getDeviceIdLoaction(getContext());
        deviceId = strings[0];
        mChartDataManager = new ChartDataManager(getContext());
        mChartDataManager.setDeviceId(deviceId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (mWholeView == null) {
            mWholeView = inflater.inflate(R.layout.fragment_chart, container, false);
            initView(mWholeView);
        }

        initData();

        return mWholeView;
    }


    private void initView(View view) {
        mPmView = (RelativeLayout) view.findViewById(R.id.pmView);
        mTmView = (RelativeLayout) view.findViewById(R.id.temPeratureView);
        mHumView = (RelativeLayout) view.findViewById(R.id.humidityView);
        mFirstView = (RelativeLayout) view.findViewById(R.id.firstView);
        mSecondView = (RelativeLayout) view.findViewById(R.id.secondView);
        mThirdView = (RelativeLayout) view.findViewById(R.id.thirdView);
        initHeight();
        mPm25ChartView = (LineChartView) view.findViewById(R.id.line_chart_pm25);
        mPm25ChartView.setChartType(LineChartView.CHART_TYPE_PM25);

        mTemperatureChartView = (LineChartView) view.findViewById(R.id.line_chart_temperature);
        mTemperatureChartView.setChartType(LineChartView.CHART_TYPE_TEMPERATURE);

        mHumidityChartView = (LineChartView) view.findViewById(R.id.line_chart_humidity);
        mHumidityChartView.setChartType(LineChartView.CHART_TYPE_HUMIDITY);

        mPM25XUnit = (TextView) view.findViewById(R.id.pm25_day_unit);
        mPM25XUnit.setText(getResources().getString(R.string.day_unit));

        mTemperatureXUnit = (TextView) view.findViewById(R.id.temperature_day_unit);
        mTemperatureXUnit.setText(getResources().getString(R.string.day_unit));
        mTemperatureYUnit = (TextView) view.findViewById(R.id.tv_chart_temperature_unit);
        if(Utils.isCelsius(getContext())){
            mTemperatureYUnit.setText(getString(R.string.temperature_unit));
        }else {
            mTemperatureYUnit.setText(getString(R.string.temperature_f_unit));
        }
        mHumidityXUnit = (TextView) view.findViewById(R.id.humidity_day_unit);
        mHumidityXUnit.setText(getResources().getString(R.string.day_unit));

        mExtraFirstChartView = (LineChartView) view.findViewById(R.id.line_chart_extra_first);
        mExtraFirstChartView.setChartType(LineChartView.CHART_TYPE_HCHO);
        mExtraSecondhartView = (LineChartView) view.findViewById(R.id.line_chart_extra_second);
        mExtraSecondhartView.setChartType(LineChartView.CHART_TYPE_CO2);
        mExtraThirdChartView = (LineChartView) view.findViewById(R.id.line_chart_extra_third);
        mExtraThirdChartView.setChartType(LineChartView.CHART_TYPE_TVOC);
        mHCHOXUnit = (TextView) view.findViewById(R.id.extra_first_day_unit);
        mHCHOXUnit.setText(getResources().getString(R.string.day_unit));
        mCO2XUnit = (TextView) view.findViewById(R.id.extra_second_day_unit);
        mCO2XUnit.setText(getResources().getString(R.string.day_unit));
        mTVOCXUnit = (TextView) view.findViewById(R.id.extra_third_day_unit);
        mTVOCXUnit.setText(getResources().getString(R.string.day_unit));

    }

    private void initHeight(){
        //获取屏幕
        Logger.e("____________", "contentViewHeight" + contentViewHeight);
        ViewGroup.LayoutParams layoutParams;
        layoutParams =  mPmView.getLayoutParams();
        layoutParams.height = contentViewHeight/3;
        mPmView.setLayoutParams(layoutParams);
        mTmView.setLayoutParams(layoutParams);
        mHumView.setLayoutParams(layoutParams);
        mFirstView.setLayoutParams(layoutParams);
        mSecondView.setLayoutParams(layoutParams);
        mThirdView.setLayoutParams(layoutParams);
    }

    private void initData() {
        int support_type = IAQType.getSupportNewParameter(getContext());
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

        mChartDataManager.getMonthDataAndRefreshChart(chartViews);
        initOnFillingCallBack(chartViews);

    }



    private void getDeviceIdLocation() {
        String[] strings = PreferenceUtil.getDeviceIdLoaction(getContext());
        deviceId = strings[0];
        Log.d("MonthDataFragment", "deviceId=" + deviceId);
    }

    private void initOnFillingCallBack(final List<LineChartView> chartViews) {
        for (final LineChartView chart : chartViews) {
            chart.setOnScrollCallback(new LineChartView.OnScrollCallback() {
                @Override
                public void onScroll(int index) {

                    mChartDataManager.refreshLineChartView(chart, index, false);
                }
            });
            chart.setOnTouchCallback(new LineChartView.OnTouchCallback() {
                @Override
                public void onTouch() {

                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    public void onEventMainThread(IAQEvents event) {
        if (event instanceof IAQTemperatureEvent) {
            initView(getView());
            initData();
        }
    }

}