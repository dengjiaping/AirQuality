package com.honeywell.iaq.chart.model;

import java.io.Serializable;

/**
 * Created by Qian Jin on 2/16/17.
 */

public class HistoryDataModel implements Serializable {

    private String date = "";

    private String indoorData = "0";

    private String timestamp = "";

    private String pm25 = "0.0";

    private String temperature = "0.0";

    private String humidity = "0.0";

    private String tvoc = "0.0";

    private String co2 = "0.0";
    private String hcho = "0.0";

//    public HistoryDataModel(float indoorData) {
//        this.indoorData = indoorData;
//    }

    public HistoryDataModel() {
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
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

    public String getTvoc() {
        return tvoc;
    }

    public void setTvoc(String tvoc) {
        this.tvoc = tvoc;
    }

    public String getCo2() {
        return co2;
    }

    public void setCo2(String co2) {
        this.co2 = co2;
    }

    public void setHcho(String hcho) {
        this.hcho = hcho;
    }

    public String getHcho() {
        return hcho;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
