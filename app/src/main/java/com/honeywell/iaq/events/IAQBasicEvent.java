package com.honeywell.iaq.events;

import android.support.annotation.Nullable;

import java.util.Map;


public class IAQBasicEvent extends IAQEvents {
    public IAQEvents.IAQBasicEventType type;
    public String message;
    public boolean isSuccessed;

    /**
     * 普通消息
     * @param type
     * @param isSuccessed
     * @param message
     */
    public IAQBasicEvent(IAQEvents.IAQBasicEventType type, @Nullable Boolean isSuccessed, @Nullable String message){
        this.type = type;
        this.isSuccessed = isSuccessed;
        this.message = message;
    }


    /**
     * 升级使用
     */
    private Map<String, String> info;
    public IAQBasicEvent(IAQEvents.IAQBasicEventType type, Map<String, String> updateInfo)
    {
        this.type = type;
        this.info = updateInfo;
    }


    public Boolean getIsSuccessed() {
        return isSuccessed;
    }

    public String getMessage() {
        return message;
    }

    public IAQBasicEventType getType() {
        return type;
    }

    public Map<String, String> getInfo(){return info;}

}
