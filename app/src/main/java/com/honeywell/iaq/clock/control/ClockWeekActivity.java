package com.honeywell.iaq.clock.control;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;

import java.util.ArrayList;

/**
 * Created by Jin on 06/09/2017.
 */

public class ClockWeekActivity extends IAQTitleBarActivity implements View.OnClickListener {

    private RelativeLayout mMondayLayout;
    private RelativeLayout mTuesdayLayout;
    private RelativeLayout mWednesdayLayout;
    private RelativeLayout mThursdayLayout;
    private RelativeLayout mFridayLayout;
    private RelativeLayout mSaturdayLayout;
    private RelativeLayout mSundayLayout;
    private ImageView mMondayImageView;
    private ImageView mTuesdayImageView;
    private ImageView mWednesdayImageView;
    private ImageView mThursdayImageView;
    private ImageView mFridayImageView;
    private ImageView mSaturdayImageView;
    private ImageView mSundayImageView;

    private boolean mIsMondayOn;
    private boolean mIsTuesdayOn;
    private boolean mIsWednesdayOn;
    private boolean mIsThursdayOn;
    private boolean mIsFridayOn;
    private boolean mIsSaturdayOn;
    private boolean mIsSundayOn;
    private ArrayList<Integer> mFreqList;


    @Override
    protected int getContent() {
        return R.layout.activity_clock_week;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(getString(R.string.repeat));
    }


    @Override
    protected void initView() {
        super.initView();

        mMondayLayout = (RelativeLayout) findViewById(R.id.monday_layout);
        mTuesdayLayout = (RelativeLayout) findViewById(R.id.tuesday_layout);
        mWednesdayLayout = (RelativeLayout) findViewById(R.id.wednesday_layout);
        mThursdayLayout = (RelativeLayout) findViewById(R.id.thursday_layout);
        mFridayLayout = (RelativeLayout) findViewById(R.id.friday_layout);
        mSaturdayLayout = (RelativeLayout) findViewById(R.id.saturday_layout);
        mSundayLayout = (RelativeLayout) findViewById(R.id.sunday_layout);
        mMondayImageView = (ImageView) findViewById(R.id.monday_iv);
        mTuesdayImageView = (ImageView) findViewById(R.id.tuesday_iv);
        mWednesdayImageView = (ImageView) findViewById(R.id.wednesday_iv);
        mThursdayImageView = (ImageView) findViewById(R.id.thursday_iv);
        mFridayImageView = (ImageView) findViewById(R.id.friday_iv);
        mSaturdayImageView = (ImageView) findViewById(R.id.saturday_iv);
        mSundayImageView = (ImageView) findViewById(R.id.sunday_iv);

        mMondayLayout.setOnClickListener(this);
        mTuesdayLayout.setOnClickListener(this);
        mWednesdayLayout.setOnClickListener(this);
        mThursdayLayout.setOnClickListener(this);
        mFridayLayout.setOnClickListener(this);
        mSaturdayLayout.setOnClickListener(this);
        mSundayLayout.setOnClickListener(this);

        mFreqList = getIntent().getIntegerArrayListExtra(ClockHourActivity.INTENT_CLOCK_FREQ);

        if (mFreqList.contains(0))
            mIsMondayOn = true;
        if (mFreqList.contains(1))
            mIsTuesdayOn = true;
        if (mFreqList.contains(2))
            mIsWednesdayOn = true;
        if (mFreqList.contains(3))
            mIsThursdayOn = true;
        if (mFreqList.contains(4))
            mIsFridayOn = true;
        if (mFreqList.contains(5))
            mIsSaturdayOn = true;
        if (mFreqList.contains(6))
            mIsSundayOn = true;

        displaySelectImageView();
    }

    @Override
    public void onClick(View v) {
        if (v == mMondayLayout) {
            mIsMondayOn = !mIsMondayOn;
        } else if (v == mTuesdayLayout) {
            mIsTuesdayOn = !mIsTuesdayOn;
        } else if (v == mWednesdayLayout) {
            mIsWednesdayOn = !mIsWednesdayOn;
        } else if (v == mThursdayLayout) {
            mIsThursdayOn = !mIsThursdayOn;
        } else if (v == mFridayLayout) {
            mIsFridayOn = !mIsFridayOn;
        } else if (v == mSaturdayLayout) {
            mIsSaturdayOn = !mIsSaturdayOn;
        } else if (v == mSundayLayout) {
            mIsSundayOn = !mIsSundayOn;
        }

        displaySelectImageView();
    }

    private void displaySelectImageView() {
        mMondayImageView.setVisibility(mIsMondayOn ? View.VISIBLE : View.INVISIBLE);
        mTuesdayImageView.setVisibility(mIsTuesdayOn ? View.VISIBLE : View.INVISIBLE);
        mWednesdayImageView.setVisibility(mIsWednesdayOn ? View.VISIBLE : View.INVISIBLE);
        mThursdayImageView.setVisibility(mIsThursdayOn ? View.VISIBLE : View.INVISIBLE);
        mFridayImageView.setVisibility(mIsFridayOn ? View.VISIBLE : View.INVISIBLE);
        mSaturdayImageView.setVisibility(mIsSaturdayOn ? View.VISIBLE : View.INVISIBLE);
        mSundayImageView.setVisibility(mIsSundayOn ? View.VISIBLE : View.INVISIBLE);
    }

    private ArrayList<Integer> calcFreqList() {
        ArrayList<Integer> result = new ArrayList<>();

        if (mIsMondayOn)
            result.add(0);
        if (mIsTuesdayOn)
            result.add(1);
        if (mIsWednesdayOn)
            result.add(2);
        if (mIsThursdayOn)
            result.add(3);
        if (mIsFridayOn)
            result.add(4);
        if (mIsSaturdayOn)
            result.add(5);
        if (mIsSundayOn)
            result.add(6);

        return result;
    }

    protected void initLeftIcon(ImageView left) {
        left.setImageResource(R.mipmap.ic_arrow_back_white);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putIntegerArrayListExtra(ClockHourActivity.INTENT_CLOCK_FREQ_BACK, calcFreqList());
                setResult(ClockHourActivity.CHANGE_CLOCK_FREQ, intent);
                finish();
            }
        });
    }

}
