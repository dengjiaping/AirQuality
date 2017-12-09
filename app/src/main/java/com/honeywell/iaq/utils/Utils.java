package com.honeywell.iaq.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.honeywell.iaq.R;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.net.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static String getJsonString(Map<String, String> params) {
        try {
            JSONObject message = new JSONObject();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                message.put(entry.getKey(), entry.getValue());
            }
            return message.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static void showToast(Context context, String msg) {
        Toast mToast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void startServiceByAction(Context context, String action) {
        Intent installPkg = new Intent();
        installPkg.setAction(action);
        installPkg.setPackage(context.getPackageName());
        context.startService(installPkg);
    }

    public static void startServiceByExitAction(Context context, String action) {
        Intent installPkg = new Intent();

        installPkg.setAction(action);
        installPkg.setPackage(context.getPackageName());
        installPkg.putExtra(Constants.KEY_DISCONNECT, Constants.EXIT_DISCONNECT);
        context.startService(installPkg);
    }

    public static void tcpClientConnect(String dstName, int dstPort, String message) {
        Socket socket = null;
        OutputStream outputStream = null;
        BufferedReader input = null;
        try {
            socket = new Socket();

            SocketAddress socketAddress = new InetSocketAddress(dstName, dstPort);

            socket.connect(socketAddress);

            outputStream = socket.getOutputStream();

            byte[] buffer = message.getBytes("UTF-8");

            outputStream.write(buffer, 0, buffer.length);
            outputStream.flush();

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // read line(s)
            String result = input.readLine();
            Log.d("tcpClientConnect", "Message From Server:" + result);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return Constants.SECURITY_WEP_STR;
        } else if (result.capabilities.contains("PSK")) {
            return Constants.SECURITY_PSK_STR;
        } else if (result.capabilities.contains("EAP")) {
            return Constants.SECURITY_EAP_STR;
        } else return Constants.SECURITY_NONE_STR;
    }

    public static void setSharedPreferencesValue(Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences("IAQ", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getSharedPreferencesValue(Context context, String key, String defValue) {
        SharedPreferences settings = context.getSharedPreferences("IAQ", 0);
        return settings.getString(key, defValue);
    }

    public static String replaceBlank(String str) {
        Pattern p = Pattern.compile("\\s*|\t|\r|\n");
        Matcher m = p.matcher(str);
        String after = m.replaceAll("");
        return after;
    }

    public static void sendBroadcast(Context context, String action) {
        Log.d("sendBroadcast", "action=" + action);
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES | Intent.FLAG_RECEIVER_FOREGROUND);
        context.sendBroadcast(intent);
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            if (networkInfo.isConnected() && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                        return ping();
                    }
                    return true;
                }
            }
        }
        return false;
    }

//    public static boolean isNetworkCanUse(Context context) {
//        return getSharedPreferencesValue(context, Const.KEY_NETWORK_CONNECT_STATUS, Const.NETWORK_CONNECTED).equals(Const.NETWORK_DISCONNECTED);
//    }

    public static boolean ping() {
        String result = null;
        try {
            String ip = "www.baidu.com";
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + ip);

            int status = p.waitFor();
            if (status == 0) {
                result = "success";
                return true;
            } else {
                result = "failed";
            }
        } catch (IOException e) {
            result = "IOException";
        } catch (InterruptedException e) {
            result = "InterruptedException";
        } finally {
            Log.d("ping", "result=" + result);
        }
        return false;
    }

    public static Map<String, String> toMap(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        Map result = new HashMap();
        Iterator iterator = jsonObject.keys();
        String key = null;
        String value = null;
        while (iterator.hasNext()) {
            key = (String) iterator.next();
            value = jsonObject.getString(key);
            result.put(key, value);

        }
        return result;
    }

    public static String getPMStatus(Context context, int pmValue) {
//        if (pmValue >= 0 && pmValue < 50) {
//            return context.getString(R.string.pm_level_1);
//        } else if (pmValue >= 50 && pmValue < 100) {
//            return context.getString(R.string.pm_level_2);
//        } else if (pmValue >= 100 && pmValue < 150) {
//            return context.getString(R.string.pm_level_3);
//        } else if (pmValue >= 150 && pmValue < 200) {
//            return context.getString(R.string.pm_level_4);
//        } else if (pmValue >= 200 && pmValue < 300) {
//            return context.getString(R.string.pm_level_5);
//        } else if (pmValue >= 300) {
//            return context.getString(R.string.pm_level_6);
//        }

        if (pmValue >= 0 && pmValue < 80) {
            return context.getString(R.string.pm_level_2);
        } else if (pmValue >= 80 && pmValue < 200) {
            return context.getString(R.string.pm_level_3);
        } else if (pmValue >= 200) {
            return context.getString(R.string.pm_level_5);
        }
        return context.getString(R.string.unknown);
    }

    public static int getPMLevel(Context context, int pmValue) {
//        if (pmValue >= 0 && pmValue < 50) {
//            return Constants.PM_LEVEL_1;
//        } else if (pmValue >= 50 && pmValue < 100) {
//            return Constants.PM_LEVEL_2;
//        } else if (pmValue >= 100 && pmValue < 150) {
//            return Constants.PM_LEVEL_3;
//        } else if (pmValue >= 150 && pmValue < 200) {
//            return Constants.PM_LEVEL_4;
//        } else if (pmValue >= 200 && pmValue < 300) {
//            return Constants.PM_LEVEL_5;
//        } else if (pmValue >= 300) {
//            return Constants.PM_LEVEL_6;
//        }

        if (pmValue >= 0 && pmValue < 80) {
            return Constants.PM_LEVEL_2;
        } else if (pmValue >= 80 && pmValue < 200) {
            return Constants.PM_LEVEL_4;
        } else if (pmValue >= 200) {
            return Constants.PM_LEVEL_5;
        }
        return Constants.PM_LEVEL_UNKNOWN;
    }


    public static int getHCHOLevel(float hchoValue) {
        if (hchoValue >= 80) {
            return Constants.HCHO_ABNORMAL;
        } else {
            return Constants.HCHO_NORMAL;
        }
    }

    public static int getTVOCLevel(float tvocValue) {
        if (tvocValue >= 380) {
            return Constants.TVOC_ABNORMAL;
        } else {
            return Constants.TVOC_NORMAL;
        }
    }


    public static int getCO2Level(int co2Value) {
        if (co2Value >= 750) {
            return Constants.CO2_ABNORMAL;
        } else {
            return Constants.CO2_NORMAL;
        }
    }


    public static String removeDoubleQuotes(String string) {
        int length = string.length();
        if ((length > 1) && (string.charAt(0) == '"') && (string.charAt(length - 1) == '"')) {
            return string.substring(1, length - 1);
        }
        return string;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * @param resId layout
     * @param view  view id which want to hidden
     */
    public static void setListenerToRootView(final Activity activity, int resId, final View view) {
        final View rootView = activity.getWindow().getDecorView().findViewById(resId);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int navigationBarHeight = 0;
                int resourceId = activity.getApplicationContext().getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    navigationBarHeight = activity.getApplicationContext().getResources().getDimensionPixelSize(resourceId);
                }

                // status bar height
                int statusBarHeight = 0;
                resourceId = activity.getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    statusBarHeight = activity.getApplicationContext().getResources().getDimensionPixelSize(resourceId);
                }

                // display window size for the app layout
                Rect rect = new Rect();
                activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

                // screen height - (user app height + status + nav) ..... if non-zero, then there is a soft keyboard
                int keyboardHeight = rootView.getRootView().getHeight() - (statusBarHeight + navigationBarHeight + rect.height());
                ViewPropertyAnimator viewPropertyAnimator = view.animate();

                if (keyboardHeight >= Utils.dip2px(activity.getApplicationContext(), 50)) {
                    view.setVisibility(View.GONE);
                    viewPropertyAnimator.translationY(keyboardHeight).setDuration(10).start();
                } else {
                    view.setVisibility(View.VISIBLE);
                    viewPropertyAnimator.translationY(0).setDuration(10).start();

                }

            }
        });
    }

    public static int getOnlineStatusFromDB(Context context, String deviceId) {
        String account = Utils.getSharedPreferencesValue(context, Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};
        Cursor cur = context.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        int dbCount = cur.getCount();
        int onlineStatus = Constants.DEVICE_OFFLINE;
        if (dbCount > 0) {
            cur.moveToFirst();
            onlineStatus = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
        }
        cur.close();

        Log.d("getOnlineStatusFromDB", "Online Status=" + onlineStatus);
        return onlineStatus;
    }

    public static CharSequence getLocalUTCTime() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(java.util.Calendar.DST_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return DateFormat.format("yyyy'-'MM'-'dd'T'kk':'mm':'ss'Z'", cal);
    }

    public static CharSequence getUTCTime() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -zoneOffset);
        return DateFormat.format("yyyy'-'MM'-'dd'T'kk':'mm':'ss'Z'", cal);
    }

    public static CharSequence getUTCDayBefore(String utcFormatter) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        int zoneOffset = cal.get(java.util.Calendar.ZONE_OFFSET);
        cal.add(java.util.Calendar.MILLISECOND, -zoneOffset);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return DateFormat.format(utcFormatter, cal);
    }

    public static CharSequence getUTCMonthBefore(String utcFormatter) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.add(Calendar.DAY_OF_MONTH, -30);
        return DateFormat.format(utcFormatter, cal);
    }


    /**
     * @param num N days before
     * @return
     */
    public static String getDate(int num, String utcFormatter) {
        long time = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * num);
        Date date = new Date();
        if (time > 0) {
            date.setTime(time);
        }
        SimpleDateFormat format = new SimpleDateFormat(utcFormatter, Locale.getDefault());
        return format.format(date);
    }

    /**
     * @param num N hours before
     * @return
     */
    public static String getHour(int num, String utcFormatter) {
        long time = System.currentTimeMillis() - (1000L * 60 * 60 * num);
        Date date = new Date();
        if (time > 0) {
            date.setTime(time);
        }
        SimpleDateFormat format = new SimpleDateFormat(utcFormatter, Locale.getDefault());
        return format.format(date);
    }

    /**
     * @param num N hours before
     * @return
     */
    public static String getUCHour(int num, String utcFormatter) {
        long time = System.currentTimeMillis() - (1000L * 60 * 60 * num);
        Date date = new Date();
        if (time > 0) {
            date.setTime(time);
        }
        SimpleDateFormat format = new SimpleDateFormat(utcFormatter, Locale.getDefault());
        return format.format(date);
    }

    public static int getHourDay(String utcFormatter) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static String getUTCDate(String utcTime) {
        if (utcTime != null && utcTime.length() > 0) {
            return utcTime.substring(0, utcTime.indexOf('T'));
        }
        return "";
    }

    public static String utc2Local(String utcTime, String utcTimePatten, String localTimePatten) {
        if (utcTime != null) {
            SimpleDateFormat utcFormatter = new SimpleDateFormat(utcTimePatten, Locale.getDefault());
            utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date gpsUTCDate = null;
            try {
                gpsUTCDate = utcFormatter.parse(utcTime);
                SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten, Locale.getDefault());
                localFormater.setTimeZone(TimeZone.getDefault());
                if (gpsUTCDate != null) {
                    return localFormater.format(gpsUTCDate.getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return getUTCTime().toString();
        } else {
            return getUTCTime().toString();
        }
    }

    public static String utcToLocal(String utcTime, String utcTimePatten, String localTimePatten) {
        SimpleDateFormat utcFormatter = new SimpleDateFormat(utcTimePatten);
        utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormatter.parse(utcTime);
            SimpleDateFormat localFormater = new SimpleDateFormat(Constants.LOCAL_TIME_FORMATTER);
            localFormater.setTimeZone(TimeZone.getDefault());

            return localFormater.format(gpsUTCDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";

    }


    public static double getMaxDouble(double[] doubles) {
        double max = doubles[0];
        for (int i = 1; i < doubles.length; i++) {
            if (doubles[i] > max) max = doubles[i];
        }
        return max;
    }

    public static String getMinString(ArrayList<String> strings) {
        if (strings != null && strings.size() > 0) {
            String min = strings.get(0);
            for (int i = 1; i < strings.size(); i++) {
                if (strings.get(i).compareTo(min) < 0) {
                    min = strings.get(i);
                }
            }
            return min;
        } else {
            return "";
        }
    }

    public static String getMaxString(ArrayList<String> strings) {
        if (strings != null && strings.size() > 0) {
            String max = strings.get(0);
            for (int i = 1; i < strings.size(); i++) {
                if (strings.get(i).compareTo(max) > 0) {
                    max = strings.get(i);
                }
            }
            return max;
        } else {
            return "";
        }
    }

    public static int getDeviceCount(Context context) {
        String account = Utils.getSharedPreferencesValue(context.getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{account};
        Cursor cur = context.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        int dbCount = cur.getCount();
        cur.close();
        return dbCount;
    }

    public static String getDeviceId(Context context) {
        String deviceId = null;
        String account = Utils.getSharedPreferencesValue(context.getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, Utils.getSharedPreferencesValue(context.getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER)};
        Cursor cur = context.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
            cur.moveToNext();
        }
        cur.close();
        Log.d("getDeviceId", "deviceId=" + deviceId);
        return deviceId;
    }

    public static String getDeviceId(Context context, String serialNum) {
        String deviceId = null;
        String account = Utils.getSharedPreferencesValue(context.getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, serialNum};
        Cursor cur = context.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
            cur.moveToNext();
        }
        cur.close();
        Log.d("getDeviceId", "deviceId=" + deviceId);
        return deviceId;
    }


    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.1";
        }
    }

    public static boolean checkPhoneNumber(String phoneNumber, String countryCode) {
        if (Constants.DEFAULT_COUNTRY_CODE.equals(countryCode)) {
            return phoneNumber.length() == Constants.CHINESE_PHONE_NUMBER_LENGHT;
        }
        return true;
    }


    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return true;
        } else {
            return false;
        }

    }

    public static String getLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh")) {
            return "zh-CN";
        } else {
            return "en-US";
        }

    }

    public static boolean isCelsius(Context context) {
        String str = Utils.getSharedPreferencesValue(context, Constants.KEY_TYPE_TEMP, Constants.KEY_CELSIUS);
        if (Constants.KEY_CELSIUS.equals(str)) {
            return true;
        } else if (Constants.KEY_FAHRENHEIT.equals(str)) {
            return false;
        }
        return true;
    }

    public static boolean isCelsius(Context context, String serial) {
        String tempUnit = "";
        String account = getSharedPreferencesValue(context, Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, serial};
        Cursor cur = context.getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        int count = cur.getCount();
        if (count > 0) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                tempUnit = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT));
                cur.moveToNext();
            }
        }
        cur.close();

        return (tempUnit.equals("1") || tempUnit.equals(""));
    }

    /**
     * 华氏温度转摄氏温度
     *
     * @param tW 华氏温度
     * @return 摄氏温度
     */
    public static float W2C(float tW) {
        return (tW - 32) * 5 / 9;
    }

    /**
     * 摄氏温度转华氏温度
     *
     * @param tC 摄氏温度
     * @return 华氏温度
     */
    public static int C2W(float tC) {
        return (int) ((9 * tC / 5 + 32) + 0.5);
    }


    /**
     * 判断某一时间是否在一个区间内
     *
     * @param sourceTime 时间区间,半闭合,如[10:00-20:00)
     * @param curTime    需要判断的时间 如10:00
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean isInTime(String sourceTime, String curTime) {
        if (sourceTime == null || !sourceTime.contains("-") || !sourceTime.contains(":")) {
            throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
        }
        if (curTime == null || !curTime.contains(":")) {
            throw new IllegalArgumentException("Illegal Argument arg:" + curTime);
        }
        String[] args = sourceTime.split("-");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            long now = sdf.parse(curTime).getTime();
            long start = sdf.parse(args[0]).getTime();
            long end = sdf.parse(args[1]).getTime();
            if (args[1].equals("00:00")) {
                args[1] = "24:00";
            }
            if (end < start) {
                if (now >= end && now < start) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (now >= start && now < end) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
        }

    }

    public static float getFloat(float sourceFloat) {
        Logger.e("*************",""+sourceFloat);
        float result = Float.parseFloat(String.format("%.2f", sourceFloat));
        Logger.e("*************",""+String.format("%.2f", sourceFloat));
        return result;
    }

}
