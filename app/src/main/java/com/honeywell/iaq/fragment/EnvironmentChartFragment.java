package com.honeywell.iaq.fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.HomeActivity;
import com.honeywell.iaq.base.IAQBaseFragment;
import com.honeywell.iaq.events.IAQEnvironmentDetailEvent;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQTemperatureEvent;
import com.honeywell.lib.widgets.SegmentedGroup;
import com.honeywell.net.utils.Logger;

/**
 * Created by milton_lin on 17/1/24.
 */

public class EnvironmentChartFragment extends IAQBaseFragment implements RadioGroup.OnCheckedChangeListener {
    private static final String TAG = EnvironmentChartFragment.class.getSimpleName();
    private SegmentedGroup mSegmentedGroup;
    private FragmentManager mFragmentManager;
    private DayDataFragment mDayDataFragment;
    private MonthDataFragment mMonthDataFragment;
    private RadioButton mDayRadioButton;
    private RadioButton mMonthRadioButton;
    private int height = 0;
//    private TabLayout mTabs;
//    private NoScrollViewPager mViewPager;
//    private PageFragmentAdapter mFragmentAdapter;

    private boolean isSharing;

    private String currentSerialNum, room, home, deviceId;

    private LinearLayout mStatistic;

    private ImageView mPrevious;


    private boolean hasDrawer;

    @Override
    public void initView(View view) {
        super.initView(view);
        home = ((HomeActivity) getActivity()).home;
        room = ((HomeActivity) getActivity()).room;
        currentSerialNum = ((HomeActivity) getActivity()).currentSerialNum;
        deviceId = ((HomeActivity) getActivity()).deviceId;
//        mViewPager = (NoScrollViewPager) view.findViewById(R.id.weather_viewpager);
//        mViewPager.setOffscreenPageLimit(1);
//        mViewPager.setNoScroll(true);
//        mFragmentAdapter = new PageFragmentAdapter(getActivity().getSupportFragmentManager(), getActivity().getApplicationContext());
//        mViewPager.setAdapter(mFragmentAdapter);
//
//        // Give the TabLayout the ViewPager
//        mTabs = (TabLayout) view.findViewById(R.id.weather_tabs);
//        mTabs.setupWithViewPager(mViewPager);

        mPrevious = (ImageView) view.findViewById(R.id.previous);
        mPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
                ((HomeActivity) getActivity()).changePage(1);
            }
        });
        initSegmentedGroup(view);
        mFragmentManager = getActivity().getSupportFragmentManager();

        mDayRadioButton = (RadioButton) view.findViewById(R.id.statistic_day);
        mMonthRadioButton = (RadioButton) view.findViewById(R.id.statistic_month);
        final View contentView = view.findViewById(R.id.content_statistic);

//        height = unDisplayViewSize(contentView);

        ViewTreeObserver vto = contentView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                height = contentView.getHeight();
                contentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                if (mDayRadioButton.isChecked()) {
                    setSegmentedGroupSelection(0);
                }
                if (mMonthRadioButton.isChecked()) {
                    setSegmentedGroupSelection(1);
                }
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void initSegmentedGroup(View root) {
        mSegmentedGroup = (SegmentedGroup) root.findViewById(R.id.segmented2);
        if (null != mSegmentedGroup) {
            mSegmentedGroup.setOnCheckedChangeListener(this);
        }
    }

    private void setSegmentedGroupSelection(int index) {
        if (mFragmentManager == null && getActivity() != null) {
            mFragmentManager = getActivity().getSupportFragmentManager();
        }
        if (mFragmentManager == null) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        hideFragments(transaction);
        switch (index) {
            case 0:
                if (mDayDataFragment == null) {
                    mDayDataFragment = DayDataFragment.newInstance(index, height);
                    transaction.add(R.id.content_statistic, mDayDataFragment);
                } else {
                    transaction.show(mDayDataFragment);
                }
                break;
            case 1:
                if (mMonthDataFragment == null) {
                    mMonthDataFragment = MonthDataFragment.newInstance(index,height);
                    transaction.add(R.id.content_statistic, mMonthDataFragment);
                } else {
                    transaction.show(mMonthDataFragment);
                }
                break;

        }
        transaction.commitAllowingStateLoss();
    }


    private void hideFragments(FragmentTransaction transaction) {
        if (mDayDataFragment != null) {
            transaction.hide(mDayDataFragment);
        }
        if (mMonthDataFragment != null) {
            transaction.hide(mMonthDataFragment);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.statistic_day) {
            setSegmentedGroupSelection(0);
        } else if (checkedId == R.id.statistic_month) {
            setSegmentedGroupSelection(1);
        }

    }

    public void removeFragment() {
        if (mFragmentManager == null && getActivity() != null) {
            mFragmentManager = getActivity().getSupportFragmentManager();
        }
        if (mFragmentManager == null) {
            return;
        }
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (mDayDataFragment != null) {
            transaction.remove(mDayDataFragment);
            mDayDataFragment = null;
        }
        if (mMonthDataFragment != null) {
            transaction.remove(mMonthDataFragment);
            mMonthDataFragment = null;
        }
        transaction.commit();
    }

    public void addFragment() {
        setSegmentedGroupSelection(0);
        mSegmentedGroup.check(R.id.statistic_day);
    }

    @Override
    public void getData() {
        super.getData();
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_environment_chart;
    }



}
