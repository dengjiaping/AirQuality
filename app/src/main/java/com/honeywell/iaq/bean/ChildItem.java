package com.honeywell.iaq.bean;

import com.honeywell.iaq.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by E570281 on 9/19/2016.
 */
public class ChildItem {

    private Map<String, String> temperatureUnitMap = new HashMap<>();

    private int pmImgId;

    private int tempImgId;

    private int humidityImgId;

    private String room;

    private String temperature;

    private String humidity;

    private String pm25;

    private String pmStatus;

    private int pmLevel = Constants.DEVICE_DISCONNECT;

    private int onlineStatus;


    private String weather;

    private String aqi;

    private String time;

    private String pm10;

    private String serialNum;

    private String deviceId;

    public ChildItem(int pmImgId, int tempImgId, int humidityImgId) {
        this.pmImgId = pmImgId;
        this.tempImgId = tempImgId;
        this.humidityImgId = humidityImgId;
    }

    public Map<String, String> getTemperatureUnit() {
        return temperatureUnitMap;
    }

    public void setTemperatureUnit(Map<String, String> temperatureUnitMap) {
        this.temperatureUnitMap = temperatureUnitMap;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getPmStatus() {
        return pmStatus;
    }

    public void setPmStatus(String pmStatus) {
        this.pmStatus = pmStatus;
    }

    public int getPmLevel() {
        return pmLevel;
    }

    public void setPmLevel(int pmLevel) {
        this.pmLevel = pmLevel;
    }


    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPm10() {
        return pm10;
    }

    public void setPm10(String pm10) {
        this.pm10 = pm10;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public int getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
