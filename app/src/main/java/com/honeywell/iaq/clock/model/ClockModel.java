package com.honeywell.iaq.clock.model;

import com.honeywell.iaq.R;
import com.honeywell.iaq.application.IAQApplication;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jin on 06/09/2017.
 */

public class ClockModel implements Serializable {

    private String mTime;
    private String m24Time;
    private String mNoon;
    private String mFrequency;
    private ArrayList<Integer> mFreqList;
    private boolean mIsActive;
    private boolean mIs24Format;
    private int mId;

    public ClockModel(int id, String time24, ArrayList<Integer> freq, boolean isActive, boolean is24Format) {
        this.mId = id;
        this.m24Time = time24;
        this.mFreqList = freq;
        this.mFrequency = ClockFrequency.changeFreqListToString(freq);
        this.mIsActive = isActive;
        this.mIs24Format = is24Format;

        if (is24Format) {
            mTime = time24;
            mNoon = "";
        } else {
            mTime = ClockFormat.change24to12Format(time24)[0];
            mNoon = ClockFormat.change24to12Format(time24)[1];
        }

    }

    public ClockModel(String time, String noon, ArrayList<Integer> freq, boolean isActive, boolean is24Format) {
        this.mTime = time;
        this.mNoon = noon;
        this.mFreqList = freq;
        this.mFrequency = ClockFrequency.changeFreqListToString(freq);
        this.mIsActive = isActive;
        this.mIs24Format = is24Format;

        if (is24Format) {
            m24Time = time;
        } else {
            m24Time = ClockFormat.change12to24Format(mTime, mNoon);
        }
    }

    public void setId(int id) {
        this.mId = id;
    }

    public int getId() {
        return mId;
    }

    public void setTime(String time) {
        this.mTime = time;

        if (mIs24Format) {
            m24Time = time;
        } else {
            m24Time = ClockFormat.change12to24Format(mTime, mNoon);
        }
    }

    public String getTime() {
        return mTime;
    }

    public String get24Time() {
        return m24Time;
    }

    public void setNoon(String noon) {
        this.mNoon = noon;

        if (mIs24Format) {
            m24Time = mTime;
        } else {
            m24Time = ClockFormat.change12to24Format(mTime, mNoon);
        }
    }

    public String getNoon() {
        return mNoon;
    }

    public void setFreqList(ArrayList<Integer> freqList) {
        this.mFreqList = freqList;
    }

    public ArrayList<Integer> getFreqList() {
        return mFreqList;
    }

    public void setFrequency(String frequency) {
        this.mFrequency = frequency;
    }

    public String getFrequency() {
        return mFrequency;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setActive(boolean isActive) {
        this.mIsActive = isActive;
    }

    public boolean isRepeat() {
        return !mFrequency.equals(IAQApplication.getInstance().getResources().getString(R.string.once));
    }

    public void switch12or24Format(boolean is24FormatNew) {
        if (is24FormatNew) {
            // 如果原来时间格式为12小时，现在系统时间格式为24小时，则改为24小时
            if (!mIs24Format) {
                mTime = ClockFormat.change12to24Format(mTime, mNoon);
                mNoon = "";
                mIs24Format = true;
            }
        } else {
            // 如果原来时间格式为24小时，现在系统时间格式为12小时，则改为12小时
            if (mIs24Format) {
                mNoon = ClockFormat.change24to12Format(mTime)[1];
                mTime = ClockFormat.change24to12Format(mTime)[0];
                mIs24Format = false;
            }
        }

    }

}
