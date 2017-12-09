package com.honeywell.iaq.bean;

public class Country {
    private String name;
    private String code;
    private String language;
    private int iconId;

    public Country(String name, String code, String language, int iconId) {
        this.name = name;
        this.code = code;
        this.language = language;
        this.iconId = iconId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }
}
