package com.honeywell.iaq.clock.model;

import java.util.ArrayList;

/**
 * Created by Jin on 21/09/2017.
 */

public class ClockJson {

    public static final String SWITCH_ON = "on";
    public static final String SWITCH_OFF = "off";

    String time;

    String activate;

    ArrayList<Integer> day;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setActivate(String activate) {
        this.activate = activate;
    }

    public ArrayList<Integer> getDay() {
        return day;
    }

    public void setDay(ArrayList<Integer> day) {
        this.day = day;
    }
}
