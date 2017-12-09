package com.honeywell.iaq.bean;

/**
 * Created by E570281 on 9/19/2016.
 */
public class DeviceInformation {

    private String home;
    private String loacation;

    public String getHome() {
        return home;
    }

    public String getLoacation(){
        return loacation;
    }
    public void setHome(String home) {
        this.home = home;
    }

    public void setLocation(String loacation){
        this.loacation = loacation;
    }
}
