package com.honeywell.iaq.clock.model;

import android.content.Context;
import android.text.format.DateFormat;

import com.honeywell.iaq.R;
import com.honeywell.iaq.application.IAQApplication;

/**
 * Created by Jin on 08/09/2017.
 */

public class ClockFormat {

    public final static String MORNING = IAQApplication.getInstance().getResources().getString(R.string.morning);
    public final static String AFTERNOON = IAQApplication.getInstance().getResources().getString(R.string.afternoon);

    public static boolean is24HourFormat(Context context) {
        return DateFormat.is24HourFormat(context);
    }

    public static String change12to24Format(String time12, String noon) {
        if (time12 == null || !time12.contains(":"))
            return "";

        if (noon == null || noon.equals(""))
            return "";

        String hour = time12.split(":")[0];
        String minute = time12.split(":")[1];

        if (noon.equals(AFTERNOON)) {
            if (time12.startsWith("12"))
                return "12:" + minute;
            return (Integer.valueOf(hour) + 12) + ":" + minute;
        } else if (noon.equals(MORNING)) {
            if (time12.startsWith("12"))
                return "0:" + minute;
            return time12;
        } else
            return "";
    }

    public static String[] change24to12Format(String time24) {
        String[] result = new String[] {"", ""};

        if (time24 == null || !time24.contains(":"))
            return result;

        String hour = time24.split(":")[0];
        String minute = time24.split(":")[1];

        if (Integer.valueOf(hour) >= 12) {
            result[1] = AFTERNOON;
            if (hour.startsWith("12"))
                result[0] = "12:" + minute;
            else
                result[0] = (Integer.valueOf(hour) - 12) + ":" + minute;
        } else {
            result[1] = MORNING;
            if (hour.startsWith("0"))
                result[0] = "12:" + minute;
            else
                result[0] = time24;
        }

        return result;
    }

}
