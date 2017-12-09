package com.honeywell.iaq.bean;

import java.io.Serializable;

/**
 * Created by zhujunyu on 2017/4/5.
 */
public class City  implements Serializable {
    private String name;
    private String locationId;
    private String description;

    public City(String name, String locationId,String description) {
        this.name = name;
        this.locationId = locationId;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
