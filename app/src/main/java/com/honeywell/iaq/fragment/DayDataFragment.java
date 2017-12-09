package com.honeywell.iaq.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.HomeActivity;
import com.honeywell.iaq.base.IAQBaseFragment;
import com.honeywell.iaq.base.IAQType;
import com.honeywell.iaq.chart.manager.ChartDataManager;
import com.honeywell.iaq.chart.view.LineChartView;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQTemperatureEvent;
import com.honeywell.iaq.events.IAQWeatherEvent;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.widget.CustomScrollView;
import com.honeywell.iaq.widget.CustomViewPager;
import com.honeywell.lib.widgets.PercentLinearLayout;
import com.honeywell.lib.widgets.directionalviewpager.DirectionalViewPager;
import com.honeywell.net.utils.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qian Jin on 2/16/2017.
 */
public class DayDataFragment extends IAQBaseFragment {

    protected ChartDataManager mChartDataManager;

    private View mWholeView;
    private LineChartView mPm25ChartView, mTemperatureChartView, mHumidityChartView;
    private LineChartView lineChartView;
    private LineChartView mExtraFirstChartView;
    private LineChartView mExtraSecondhartView;
    private LineChartView mExtraThirdChartView;
    private TextView mPM25XUnit, mTemperatureXUnit, mHumidityXUnit;
    private TextView mHCHOXUnit;
    private TextView mCO2XUnit;
    private TextView mTVOCXUnit;
    private TextView mTemperatureYUnit;
    private String currentSerialNum;
    private String deviceId;

    private RelativeLayout mPmView;
    private RelativeLayout mTmView;
    private RelativeLayout mHumView;
    private RelativeLayout mFirstView;
    private RelativeLayout mSecondView;
    private RelativeLayout mThirdView;

    private static int contentViewHeight;

    public static DayDataFragment newInstance(int page, int viewHeight) {
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_PAGE, page);
        DayDataFragment fragment = new DayDataFragment();
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
        return R.layout.fragment_chart;
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
//        if (mWholeView == null) {
//            mWholeView = inflater.inflate(R.layout.fragment_chart, container, false);
//            initView(mWholeView);
//        }
//
//        initData();
//
//        return mWholeView;
//    }

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
        mPm25ChartView.setChartType(LineChartView.CHART_TYPE_PM25);

        mTemperatureChartView = (LineChartView) view.findViewById(R.id.line_chart_temperature);
        mTemperatureChartView.setChartType(LineChartView.CHART_TYPE_TEMPERATURE);

        mHumidityChartView = (LineChartView) view.findViewById(R.id.line_chart_humidity);
        mHumidityChartView.setChartType(LineChartView.CHART_TYPE_HUMIDITY);

        mPM25XUnit = (TextView) view.findViewById(R.id.pm25_day_unit);
        mPM25XUnit.setText(getResources().getString(R.string.hour_unit));

        mTemperatureXUnit = (TextView) view.findViewById(R.id.temperature_day_unit);
        mTemperatureXUnit.setText(getResources().getString(R.string.hour_unit));
        mTemperatureYUnit = (TextView) view.findViewById(R.id.tv_chart_temperature_unit);
        if (Utils.isCelsius(getContext())) {
            mTemperatureYUnit.setText(getString(R.string.temperature_unit));
        } else {
            mTemperatureYUnit.setText(getString(R.string.temperature_f_unit));
        }
        mHumidityXUnit = (TextView) view.findViewById(R.id.humidity_day_unit);
        mHumidityXUnit.setText(getResources().getString(R.string.hour_unit));
        /****************/
        mExtraFirstChartView = (LineChartView) view.findViewById(R.id.line_chart_extra_first);
        mExtraFirstChartView.setChartType(LineChartView.CHART_TYPE_HCHO);
        mExtraSecondhartView = (LineChartView) view.findViewById(R.id.line_chart_extra_second);
        mExtraSecondhartView.setChartType(LineChartView.CHART_TYPE_CO2);
        mExtraThirdChartView = (LineChartView) view.findViewById(R.id.line_chart_extra_third);
        mExtraThirdChartView.setChartType(LineChartView.CHART_TYPE_TVOC);
        mHCHOXUnit = (TextView) view.findViewById(R.id.extra_first_day_unit);
        mHCHOXUnit.setText(getResources().getString(R.string.hour_unit));
        mCO2XUnit = (TextView) view.findViewById(R.id.extra_second_day_unit);
        mCO2XUnit.setText(getResources().getString(R.string.hour_unit));
        mTVOCXUnit = (TextView) view.findViewById(R.id.extra_third_day_unit);
        mTVOCXUnit.setText(getResources().getString(R.string.hour_unit));

//        CustomViewPager parentPager = ((HomeActivity) getActivity()).getViewPager();
//        CustomScrollView customScrollView = (CustomScrollView) view.findViewById(R.id.scrollView);
//        customScrollView.setParentPager(parentPager);

        initData();
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
        int support_type = IAQType.getSupportNewParameter(getContext());
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

        mChartDataManager.getDayDataAndRefreshChart(chartViews);
        initOnFillingCallBack(chartViews);

    }


    private void getDeviceIdLocation() {
        String[] strings = PreferenceUtil.getDeviceIdLoaction(getContext());
        deviceId = strings[0];
//        Log.d("DayDataFragment", "deviceId=" + deviceId);
    }

    private void initOnFillingCallBack(final List<LineChartView> chartViews) {
        for (final LineChartView chart : chartViews) {
            chart.setOnScrollCallback(new LineChartView.OnScrollCallback() {
                @Override
                public void onScroll(int index) {
                    mChartDataManager.refreshLineChartView(chart, index, true);
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
        if (event instanceof IAQTemperatureEvent) {
            initView(getView());
            initData();
        }
    }


}
