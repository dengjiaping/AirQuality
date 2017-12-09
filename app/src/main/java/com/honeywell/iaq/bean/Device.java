package com.honeywell.iaq.bean;

/**
 * Created by E570281 on 8/25/2016.
 */
public class Device {

    private String deviceId;

    private String deviceSerial;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceSerial(String deviceSerial) {
        this.deviceSerial = deviceSerial;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }
}
