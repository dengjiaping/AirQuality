package com.honeywell.iaq.events;

/**
 * Created by zhujunyu on 2017/3/22.
 */

public class IAQNetChangeEvent extends IAQEvents{
    public static final int NET_CONNECT = 0;

    public static final int NET_DISCONNECT = 1;
    public int type;

    public IAQNetChangeEvent(int type) {
        this.type = type;
    }
}
