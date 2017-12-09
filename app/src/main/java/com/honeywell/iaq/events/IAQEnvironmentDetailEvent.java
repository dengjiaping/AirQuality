package com.honeywell.iaq.events;

/**
 * Created by milton_lin on 17/1/24.
 */

public class IAQEnvironmentDetailEvent extends IAQEvents {
    public static final int GET_DATA_SUCCESS = 0;
    public static final int GET_DATA_FAIL = 1;
    public static final int CHECK_NETWORK = 2;
    public static final int REFRESH_LOCATION = 3;
    public static final int ACTION_GET_IAQ_DATA_SUCCESS = 4;
    public static final int ACTION_WSS_CONNECTED = 5;
    public static final int MODIFY_HOME_ROME_NAME=6;

    public int type;
    public boolean success; //记录事件是否成功或者失败
    public Object item;//如果需要，则在这个数据里携带需要的参数，成功了，可能是需要显示的字段，失败了用它携带对应的错误信息

    public IAQEnvironmentDetailEvent(int type, boolean state, Object item) {
        this.type = type;
        this.success = state;
        this.item = item;
    }
}
