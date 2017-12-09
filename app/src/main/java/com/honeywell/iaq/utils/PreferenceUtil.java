package com.honeywell.iaq.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferenceUtil {


    public static final String DEVICE_ID = "device_id";
    public static final String LOCATION = "location";


    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("IAQ", Context.MODE_PRIVATE);
    }

    //进入天气页面传值需要
    public static void saveDeviceIdLocation(Context context, String deviceValue, String locationValue) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString(DEVICE_ID, deviceValue);
        edit.putString(LOCATION, locationValue);
        edit.commit();
    }

    public static String[] getDeviceIdLoaction(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        String[] result = new String[2];
        result[0] = sp.getString(DEVICE_ID, "100");
        result[1] = sp.getString(LOCATION, "nj");
        return result;
    }


    public static void removeKey(Context context, String key) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.remove(key);
        edit.apply();
    }

    public static void removeAll(Context context) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.clear();
        edit.apply();
    }

    public static void saveUserLoginInfo(Context context, String username, String password) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString("username", username);
        edit.putString("password", password);
        edit.commit();
    }

    public static void clearUserLoginInfo(Context context) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString("password", "");
        edit.commit();
    }

    public static String[] getUserInfo(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        String[] result = new String[2];
        result[0] = sp.getString("username", "");
        result[1] = sp.getString("password", "");
        return result;
    }

    public static void saveXGToken(Context context, String token) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString("token", token);
        edit.commit();
    }

    public static String getXGToken(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        return sp.getString("token", "");
    }

    public static void commitString(Context context, String key, String value) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putString(key, value);
        edit.apply();
    }

    public static String getString(Context context, String key, String faillValue) {
        return getSharedPreferences(context).getString(key, faillValue);
    }

    public static void commitInt(Context context, String key, int value) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putInt(key, value);
        edit.apply();
    }

    public static int getInt(Context context, String key, int failValue) {
        return getSharedPreferences(context).getInt(key, failValue);
    }

    public static void commitLong(Context context, String key, long value) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putLong(key, value);
        edit.apply();
    }

    public static long getLong(Context context, String key, long failValue) {
        return getSharedPreferences(context).getLong(key, failValue);
    }

    public static void commitBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putBoolean(key, value);
        edit.apply();
    }

    public static Boolean getBoolean(Context context, String key, boolean failValue) {
        return getSharedPreferences(context).getBoolean(key, failValue);
    }

}
