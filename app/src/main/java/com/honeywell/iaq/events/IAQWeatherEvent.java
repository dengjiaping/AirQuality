package com.honeywell.iaq.events;

/**
 * Created by zhujunyu on 2017/2/21.
 */

public class IAQWeatherEvent extends IAQEvents {

    public static final int GET_DATA_SUCCESS = 0;

    public static final int GET_DATA_FAIL = 1;

    public static final int GET_SUGGESTION_SUCCESS = 2;

    public static final int GET_WEATHER_DATA_SUCCESS = 3;
    public static final int GET_LOCATION_SUCCESS = 4;
    public int type;

    public IAQWeatherEvent(int type) {
        this.type = type;
    }
}
