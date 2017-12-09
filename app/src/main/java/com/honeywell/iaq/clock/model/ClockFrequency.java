package com.honeywell.iaq.clock.model;

import com.honeywell.iaq.R;
import com.honeywell.iaq.application.IAQApplication;

import java.util.ArrayList;
import java.util.Collections;


/**
 * Created by Jin on 07/09/2017.
 */

public class ClockFrequency {

    private final static int MON = 0;
    private final static int TUE = 1;
    private final static int WED = 2;
    private final static int THU = 3;
    private final static int FRI = 4;
    private final static int SAT = 5;
    private final static int SUN = 6;
    private final static String MONDAY = IAQApplication.getInstance().getResources().getString(R.string.one);
    private final static String TUESDAY = IAQApplication.getInstance().getResources().getString(R.string.two);
    private final static String WEDNESDAY = IAQApplication.getInstance().getResources().getString(R.string.three);
    private final static String THURSDAY = IAQApplication.getInstance().getResources().getString(R.string.four);
    private final static String FRIDAY = IAQApplication.getInstance().getResources().getString(R.string.five);
    private final static String SATURDAY = IAQApplication.getInstance().getResources().getString(R.string.six);
    private final static String SUNDAY = IAQApplication.getInstance().getResources().getString(R.string.seven);
    private final static String EVERYDAY = IAQApplication.getInstance().getResources().getString(R.string.everyday);
    private final static String WORKDAY = IAQApplication.getInstance().getResources().getString(R.string.workday);
    private final static String WEEKEND = IAQApplication.getInstance().getResources().getString(R.string.weekend);
    private final static String[] DAYS = new String[] {MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY};


    public static String changeFreqListToString(ArrayList<Integer> days) {
        if (days == null || days.size() == 0)
            return IAQApplication.getInstance().getResources().getString(R.string.once);

        if (days.size() == 7)
            return IAQApplication.getInstance().getResources().getString(R.string.everyday);

        Collections.sort(days);

        if (days.size() == 5 && days.get(0) == MON && days.get(4) == FRI)
            return IAQApplication.getInstance().getResources().getString(R.string.workday);

        if (days.size() == 2 && days.get(0) == SAT && days.get(1) == SUN)
            return IAQApplication.getInstance().getResources().getString(R.string.weekend);


        String result = "";
        for (int i = 0; i < days.size(); i++) {
            result += getDay(days.get(i));
            if (i != days.size() - 1)
                result += ", ";
        }

        return result;
    }

    public static String[] changeFreqStringToList(String freq) {
        if (EVERYDAY.equals(freq))
            return DAYS;

        return null;
    }

    private static String getDay(int day) {
        return DAYS[day];
    }


}
