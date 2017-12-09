package com.honeywell.iaq.utils;

import android.os.Environment;

import com.honeywell.iaq.R;
import com.honeywell.iaq.db.IAQ;

import java.util.HashMap;

public class Constants {
    public static final int SDK_INT = android.os.Build.VERSION.SDK_INT;

    public static final String DEFAULT_IAQ_IP = "192.168.10.1";

    public static final String DEFAULT_AP_IP = "192.168.4.1";

    public static final String DEFAULT_IAQ_WIFI_SSID = "Honeywell+IAQ";

    public static final int DEFAULT_IAQ_PORT = 5050;


    public static final String DEFAULT_COUNTRY_CODE = "+86";

    public static final String CHINA_LANGUAGE_CODE = "zh-CN";

    public static final String PHONE_TYPE_ANDROID = "android";

    public static final String NETWORK_CONNECTED = "1";

    public static final String NETWORK_DISCONNECTED = "0";

    public static final String KEY_LANGUAGE_CODE = "language_code";

    public static final String KEY_NETWORK_CONNECT_STATUS = "network_connect_status";

    public static final String KEY_WIFI_SSID = "SSID";

    public static final String KEY_WIFI_PASSWORD = "Password";

    public static final String KEY_LENGTH = "length";

    public static final String KEY_LIST = "list";

    public static final String KEY_ACCOUNT = "account";

    public static final String KEY_TYPE = "type";

    public static final String KEY_LANGUAGE = "language";

    public static final String KEY_COUNTRY_CODE = "countryCode";

    public static final String KEY_PHONE_NUMBER = "phoneNumber";

    public static final String KEY_VALIDATION_CODE = "validationCode";

    public static final String KEY_FORECAST_DATA = "forecast_data";
    public static final String KEY_SUGGEST_DATA = "suggest_data";
    public static final String KEY_WEATHER_DATA = "weather_data";
    public static final String KEY_NAME = "name";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PASSWORD = "password";

    public static final String KEY_NEW_PASSWORD = "newPassword";

    public static final String KEY_PHONE_V_CODE = "phoneVCode";

    public static final String KEY_PHONE_UUID = "phoneUuid";

    public static final String KEY_PHONE_TYPE = "phoneType";

    public static final String KEY_DEVICE_SERIAL = "deviceSerial";

    public static final String KEY_CURRENT_DEVICE_SERIAL = "currentDeviceSerial";

    public static final String KEY_SELECT_GEN = "selectGen";
    public static final String GEN_1 = "1";
    public static final String GEN_2 = "2";

    public static final String KEY_DEVICE_ID = "deviceId";

    public static final String KEY_CLOCKS = "clocks";

    public static final String KEY_SLEEP_MODE = "sleepMode";
    public static final String KEY_SLEEP_START = "start";
    public static final String KEY_SLEEP_END = "end";

    public static final String KEY_SAVE_POWER_MODE = "energySavingMode";

    public static final String KEY_DISCONNECT = "key_disconnect";

    public static final String EXIT_DISCONNECT = "EXIT_APP";

    public static final String KEY_DEVICE_PASSWORD = "devicePassword";

    public static final String KEY_DEVICE_INFO = "deviceInfo";

    public static final String KEY_REQUEST_ID = "requestId";

    public static final String KEY_USER_ID = "userId";

    public static final String KEY_PHONE_ID = "phoneId";

    public static final String KEY_LOCATION_ID = "location";

    public static final String KEY_DEVICES = "devices";

    public static final String KEY_LATITUDE = "latitude";

    public static final String KEY_LONGITUDE = "longitude";

    public static final String KEY_WEATHER_DAY = "weatherDay";

    public static final String KEY_WEATHER_NIGHT = "weatherNight";

    public static final String KEY_TEMPERATURE_LOW = "temperatureLow";

    public static final String KEY_TEMPERATURE_HIGH = "temperatureHigh";

    public static final String KEY_DATE = "date";

    public static final String KEY_AIR_POLLUTION = "air_pollution";

    public static final String KEY_DETAILS = "details";

    // in seconds, minimum 60, maximum 300
    public static final String KEY_FREQUENCY = "frequency";

    public static final String KEY_SET_COOKIE = "Set-Cookie";

    public static final String KEY_COOKIE = "Cookie";

    public static final String KEY_HOME = "home";

    public static final String KEY_ROOM = "room";

    public static final String KEY_LOCATION = "location";

    public static final String KEY_ONLINE_STATUS = "online";

    public static final String KEY_STANDBY_MODE = "homeScreenMode";


    // Data from IAQ data
    public static final String KEY_DEVICE_TEMPERATURE = "device_temperature";

    public static final String KEY_DEVICE_PM25 = "device_pm25";

    public static final String KEY_DEVICE_HUMIDITY = "device_humidity";

    public static final String KEY_DEVICE_TVOC = "tvoc";

    public static final String KEY_DEVICE_CO2 = "co2";

    public static final String KEY_DEVICE_HCHO = "hcho";

    public static final String KEY_DEVICE_SLEEP_MODE = "sleep_mode";

    // GEN2
    public static final String KEY_DEVICE_SLEEP = "sleep";
    public static final String KEY_DEVICE_SLEEP_START = "sleepStart";
    public static final String KEY_DEVICE_SLEEP_STOP = "sleepEnd";
    public static final String KEY_DEVICE_SAVE_POWER = "energySaving";
    public static final String KEY_DEVICE_STANDBY_SCREEN= "homeScreen";
    public static final String KEY_DEVICE_TEMPERATURE_UNIT= "temperatureUnit";

    // Data from outdoor weather
    public static final String KEY_WEATHER = "weather";

    public static final String KEY_TEMPERATURE = "temperature";

    public static final String KEY_HUMIDITY = "humidity";

    public static final String KEY_SLEEP = "sleep";

    public static final String KEY_TIMESTAMP = "timestamp";

    public static final String KEY_PM25 = "pm25";

    public static final String KEY_PM10 = "pm10";

    public static final String KEY_AQI = "aqi";

    public static final String KEY_TIME = "time";

    public static final String KEY_GRANULARITY = "granularity";

    public static final String KEY_DATA = "data";

    public static final String KEY_ERROR_DETAIL = "errorDetail";

    public static final String KEY_ERROR_TYPE = "errorType";

    public static final String KEY_DEFAULT = "default";

    public static final String ERROR_TYPE_INVALID_NAME = "InvalidName";

    public static final String ERROR_TYPE_INVALID_PASSWORD = "InvalidPassword";

    public static final String ERROR_TYPE_INVALID_PHONE_NUMBER = "InvalidPhoneNumber";

    public static final String ERROR_TYPE_PHONE_NUMBER_REGISTERED = "PhoneNumberAlreadyRegistered";

    public static final String ERROR_TYPE_INVALID_PHONE_V_CODE = "InvalidPhoneVCode";

    public static final String ERROR_TYPE_INVALID_LANGUAGE = "InvalidLanguage";

    public static final String ERROR_DETAIL_LOGIN_UNREGISTER = "cannot get user by phone number";

    public static final String ERROR_DETAIL_LOGIN_WRONG_PASSWORD = "wrong phone number or password";

    public static final String GRANULARITY_HOUR = "h";

    public static final String GRANULARITY_DAY = "d";

    public static final String GRANULARITY_START = "start";

    public static final String GRANULARITY_END = "end";

    public static final String TYPE_LOGIN_USER = "LoginUser";

    public static final String TYPE_LOGOUT_USER = "LogoutUser";

    public static final String TYPE_SEND_V_CODE = "SendVCode";

    public static final String TYPE_RESET_PASSWORD = "ResetPassword";

    public static final String TYPE_REGISTER_USER = "RegisterUser";

    public static final String TYPE_BIND_DEVICE = "BindDevice";

    public static final String TYPE_UNBIND_DEVICE = "UnbindDevice";

    public static final String TYPE_SET_SLEEPMODE = "IAQSleepMode";

    public static final String TYPE_SET_SAVE_POWER = "IAQEnergySavingMode";

    public static final String TYPE_SET_TEMPERATURE = "IAQTemperatureUnit";

    public static final String TYPE_ONLINE_STATUS = "OnlineStatus";

    public static final String TYPE_SET_REPORT_FREQUENCY = "IAQSetReportFrequency";

    public static final String TYPE_GET_IAQ_DATA = "IAQGetData";

    public static final String TYPE_IAQ_HISTORY = "IAQHistory";

    public static final String TYPE_GET_LOCATION_INFO = "GetLocationInfo";

    public static final String TYPE_SET_LOCATION = "SetLocation";

    public static final String TYPE_GET_LOCATION = "GetLocation";

    public static final String TYPE_GET_WEATHER = "GetWeather";

    public static final String TYPE_GET_FORECAST = "GetForecast";

    public static final String TYPE_GET_LIFE_SUGGESTION = "GetLifeSuggestion";

    public static final String TYPE_UPDATE_DEVICE = "UpdateDevice";

    public static final String TYPE_QUERY_ONLINE = "QueryOnline";

    public static final String TYPE_STANDBY_SCREEN = "IAQHomeScreenMode";

    public static final String TYPE_SET_CLOCK = "IAQSetClock";
    public static final String TYPE_GET_CLOCK = "IAQGetClock";

    public static final String TYPE_CHECK_REGISTER = "CheckRegister";


//    public static final String DEFAULT_URL = "https://dev.acscloud.honeywell.com.cn/v1/00100002";
//    public static final String DEFAULT_WSS_URL = "wss://dev.acscloud.honeywell.com.cn/v1/00100002";

//    public static final String DEFAULT_URL = "https://acscloud.honeywell.com.cn/v1/00100002";
//    public static final String DEFAULT_URL = "http://115.159.225.195:8084/v1/00100002";
//    public static final String DEFAULT_URL = "http://qaiaq.honcloud.honeywell.com.cn/v1/00100002";
//    public static final String DEFAULT_URL = "https://iaq.honcloud.honeywell.com.cn/v2/00100002";
    public static final String DEFAULT_URL = "https://acscloud.honeywell.com.cn/v2/00100002";

//    public static final String DEFAULT_WSS_URL = "wss://acscloud.honeywell.com.cn/v1/00100002";
//    public static final String DEFAULT_WSS_URL = "ws://115.159.225.195:8087";
//    public static final String DEFAULT_WSS_URL = "ws://qaiaq.honcloud.honeywell.com.cn";
//    public static final String DEFAULT_WSS_URL = "wss://iaq.honcloud.honeywell.com.cn";
    public static final String DEFAULT_WSS_URL = "wss://acscloud.honeywell.com.cn/v1/00100002";


    public static final String USER_URL = DEFAULT_URL + "/user";

    public static final String USER_LIST_URL = DEFAULT_URL + "/user/list";

    public static final String BIND_DEVICE_URL = DEFAULT_URL + "/user/device/list";

    public static final String DEVICE_URL = DEFAULT_URL + "/user/device";

    public static final String HTTP_CONNECT_URL = DEFAULT_URL + "/phone/connect";

    public static final String WSS_CONNECT_URL = DEFAULT_WSS_URL + "/phone/connect";
//    public static final String WSS_CONNECT_URL = DEFAULT_WSS_URL + "/connect";

    public static final String LOCATION_LIST_URL = DEFAULT_URL + "/location/list";

    public static final String DEVICE_LOCATION_URL = DEFAULT_URL + "/user/device/location";

    public static final String WEATHER_LIST_URL = DEFAULT_URL + "/weather/list";

    public static final String DEVICE_WEATHER_URL = DEFAULT_URL + "/user/device/weather";

    public static final String DEVICE_LIFE_URL = DEFAULT_URL + "/user/device/life";

    public static final String DEVICE_ONLINE_URL = DEFAULT_URL + "/user/device/online";


    public static final String COMMAND_SEND_SSID = "AT+APSSID=";

    public static final String COMMAND_SEND_PASSWORD = "AT+APPASS=";

    public static final String COMMAND_SWITCH_MODE = "AT+SWITCH=1\r\n";

    public static final String ACTION_OPEN_WSS = "com.honeywell.iaq.ACTION_OPEN_WSS";

    public static final String ACTION_WSS_CONNECTED = "com.honeywell.iaq.ACTION_WSS_CONNECTED";

    public static final String ACTION_BIND_DEVICE = "com.honeywell.iaq.ACTION_BIND_DEVICE";

    public static final String ACTION_UNBIND_DEVICE = "com.honeywell.iaq.ACTION_UNBIND_DEVICE";

    public static final String ACTION_GET_IAQ_DATA = "com.honeywell.iaq.ACTION_GET_IAQ_DATA";

    public static final String ACTION_DISCONNECT = "com.honeywell.iaq.ACTION_DISCONNECT";

    public static final String ACTION_SEND_WIFI_PWD = "com.honeywell.iaq.ACTION_SEND_PWD";

    public static final String ACTION_WSS_CONNECT_FAIL = "com.honeywell.iaq.ACTION_WSS_CONNECT_FAIL";

    public static final String ACTION_GET_IAQ_DATA_SUCCESS = "com.honeywell.iaq.ACTION_GET_IAQ_DATA_SUCCESS";

    public static final String ACTION_GET_IAQ_DATA_FAIL = "com.honeywell.iaq.ACTION_GET_IAQ_DATA_FAIL";

    public static final String ACTION_INVALID_NETWORK = "com.honeywell.iaq.ACTION_INVALID_NETWORK";

    public static final String ACTION_LOGOUT_FAIL = "com.honeywell.iaq.ACTION_LOGOUT_FAIL";

    public static final String ARG_PAGE = "ARG_PAGE";

    public static final String DEFAULT_COOKIE_VALUE = "";

    public static final String DEFAULT_DEVICE_ID = "";

    public static final String DEFAULT_LOCATION_ID = "";

    public static final String DEFAULT_SERIAL_NUMBER = "";

    public static final int SECURITY_NONE = 0;

    public static final int SECURITY_WEP = 1;

    public static final int SECURITY_PSK = 2;

    public static final int SECURITY_EAP = 3;

    public static final String SECURITY_NONE_STR = "NONE";

    public static final String SECURITY_PSK_STR = "PSK";

    public static final String SECURITY_WEP_STR = "WEP";

    public static final String SECURITY_EAP_STR = "EAP";

    public static final String KEY_TYPE_TEMP = "TYPE_TEMP";
    public static final String KEY_CELSIUS = "CELSTUS";
    public static final String KEY_FAHRENHEIT = "FAHRENHEIT";

    public static final String TEMPERATURE_UNIT_C = "1";
    public static final String TEMPERATURE_UNIT_F = "2";

    public static final int CHINESE_PHONE_NUMBER_LENGHT = 11;

    public static final int CHART_TYPE_PM25 = 201;
    public static final int CHART_TYPE_TEMPERATURE = 202;
    public static final int CHART_TYPE_HUMIDITY = 203;
    public static final int CHART_TYPE_HCHO = 204;
    public static final int CHART_TYPE_CO2 = 205;
    public static final int CHART_TYPE_TVOC = 206;


    public static final String[] BIND_DEVICE_PROJECTION = new String[]{IAQ.BindDevice.COLUMN_ID, IAQ.BindDevice.COLUMN_DEVICE_ID,
            IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER, IAQ.BindDevice.COLUMN_DEVICE_ROOM, IAQ.BindDevice.COLUMN_DEVICE_HOME,
            IAQ.BindDevice.COLUMN_ONLINE_STATUS, IAQ.BindDevice.COLUMN_DEVICE_PM25, IAQ.BindDevice.COLUMN_DEVICE_SLEEP,IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE,
            IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, IAQ.BindDevice.COLUMN_DEVICE_TVOC, IAQ.BindDevice.COLUMN_DEVICE_CO2,IAQ.BindDevice.COLUMN_DEVICE_HCHO,
            IAQ.BindDevice.COLUMN_LOCATION, IAQ.BindDevice.COLUMN_WEATHER, IAQ.BindDevice.COLUMN_TEMPERATURE,
            IAQ.BindDevice.COLUMN_HUMIDITY, IAQ.BindDevice.COLUMN_PM25, IAQ.BindDevice.COLUMN_PM10,
            IAQ.BindDevice.COLUMN_AQI, IAQ.BindDevice.COLUMN_TIME,
            IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START, IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP,
            IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER, IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN,
            IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT, IAQ.BindDevice.COLUMN_ACCOUNT};

    public static final int DEVICE_DISCONNECT = 0;

    public static final int PM_LEVEL_UNKNOWN = 0;

    public static final int PM_LEVEL_1 = 1;

    public static final int PM_LEVEL_2 = 2;

    public static final int PM_LEVEL_3 = 3;

    public static final int PM_LEVEL_4 = 4;

    public static final int PM_LEVEL_5 = 5;

    public static final int PM_LEVEL_6 = 6;

    public static final int HCHO_ABNORMAL = 1;

    public static final int HCHO_NORMAL = 2;

    public static final int CO2_ABNORMAL = 1;

    public static final int CO2_NORMAL = 2;

    public static final int TVOC_ABNORMAL = 1;

    public static final int TVOC_NORMAL = 2;

    public static final String IAQ_CONNECT_WIFI_SUCCESS = "CONNECTWIFISUCCESS";

    public static final String IAQ_REGISTER_SUCCESS = "REGISTERCLOUDSUCCESS";

    public static final String WEATHER_TYPE_BLUSTERY = "Blustery";

    public static final String WEATHER_TYPE_CLEAR = "Clear";

    public static final String WEATHER_TYPE_CLOUD = "Cloudy";

    public static final String WEATHER_TYPE_COLD = "Cold";

    public static final String WEATHER_TYPE_DUST = "Dust";

    public static final String WEATHER_TYPE_DUST_STORM = "Duststorm";

    public static final String WEATHER_TYPE_FAIR_DAY = "FairDay";

    public static final String WEATHER_TYPE_FAIR_NIGHT = "FairNight";

    public static final String WEATHER_TYPE_FOGGY = "Foggy";

    public static final String WEATHER_TYPE_HAZE = "Haze";

    public static final String WEATHER_TYPE_HEAVY_RAIN = "HeavyRain";

    public static final String WEATHER_TYPE_HEAVY_SNOW = "HeavySnow";

    public static final String WEATHER_TYPE_HEAVY_STORM = "HeavyStorm";

    public static final String WEATHER_TYPE_HOT = "Hot";

    public static final String WEATHER_TYPE_HURRICANE = "Hurricane";

    public static final String WEATHER_TYPE_ICE_RAIN = "IceRain";

    public static final String WEATHER_TYPE_LIGHT_RAIN = "LightRain";

    public static final String WEATHER_TYPE_LIGHT_SNOW = "LightSnow";

    public static final String WEATHER_TYPE_MODERATE_RAIN = "ModerateRain";

    public static final String WEATHER_TYPE_MODERATE_SNOW = "ModerateSnow";

    public static final String WEATHER_TYPE_MOSTLY_CLOUDY_DAY = "MostlyCloudyDay";

    public static final String WEATHER_TYPE_MOSTLY_CLOUDY_NIGHT = "MostlyCloudyNight";

    public static final String WEATHER_TYPE_OVERCAST = "Overcast";

    public static final String WEATHER_TYPE_PARTLY_CLOUDY_DAY = "PartlyCloudyDay";

    public static final String WEATHER_TYPE_PARTLY_CLOUDY_NIGHT = "PartlyCloudyNight";

    public static final String WEATHER_TYPE_SAND = "Sand";

    public static final String WEATHER_TYPE_SAND_STORM = "Sandstorm";

    public static final String WEATHER_TYPE_SEVERE_STORM = "SevereStorm";

    public static final String WEATHER_TYPE_SHOWER = "Shower";

    public static final String WEATHER_TYPE_SLEET = "Sleet";

    public static final String WEATHER_TYPE_SNOW_FLURRY = "SnowFlurry";

    public static final String WEATHER_TYPE_SNOW_STORM = "Snowstorm";

    public static final String WEATHER_TYPE_STORM = "Storm";

    public static final String WEATHER_TYPE_SUNNY = "Sunny";

    public static final String WEATHER_TYPE_THUNDERSHOWER = "Thundershower";

    public static final String WEATHER_TYPE_THUNDERSHOWER_WITH_HAIL = "ThundershowerWithHail";

    public static final String WEATHER_TYPE_TORNADO = "Tornado";

    public static final String WEATHER_TYPE_TROPICAL_STORM = "TropicalStorm";

    public static final String WEATHER_TYPE_WINDY = "Windy";

    public static final String WEATHER_TYPE_UNKNOWN = "Unknown";

    public static final String ERROR_TYPE_CAN_NOT_BIND = "CannotBind";

    public static final String ERROR_TYPE_PASSWORD_ERROR = "PasswordError";

    public static final String ERROR_TYPE_DEVICE_NOT_BIND = "DeviceNotBind";

    public static final String ZERO = "0";

    public static final String ZERO_ZERO = "00";

    public static final int DEVICE_ONLINE = 1;

    public static final int DEVICE_OFFLINE = 0;

    public static final char UTC_TIME_SYMBOL = 'T';

    public static final String UTC_TIME_FORMATTER = "yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'";
    public static final String LOCAL_TIME_FORMATTER = "yyyy'-'MM'-'dd'T'HH':'";
    public static final String AD_URL = "https://shop.m.jd.com/?shopId=652214&utm_source=iosapp&utm_medium=appshare&utm_campaign=t_335139774&utm_term";

    public static final String SERIAL_NUMBER_PREFIX = "00100002";

    public static final int TYPE_PM25 = 0;

    public static final int TYPE_TEMPERATURE = 1;

    public static final int TYPE_HUMIDITY = 2;


    public static final String KEY_COUNTRY_INDEX = "country_index";
    public static final int COUNTRY_CHINA_INDEX = 0;

    public static final int COUNTRY_BRAZIL_INDEX = 1;

    public static final int COUNTRY_BRUNEI_INDEX = 2;

    public static final int COUNTRY_DUBAI_INDEX = 3;

    public static final int COUNTRY_INDIA_INDEX = 4;

    public static final int COUNTRY_INDONESIA_INDEX = 5;

    public static final int COUNTRY_MALAYSIA_INDEX = 6;

    public static final int COUNTRY_PHILIPPINES_INDEX = 7;

    public static final int COUNTRY_SINGAPORE_INDEX = 8;

    public static final int COUNTRY_TURKEY_INDEX = 9;

    public static final int COUNTRY_US_INDEX = 10;

    public static final String REGISTER_TYPE = "register_type";
    public static final String FORGOT_PASSWORD = "forgot_password";
    public static final String INTENT_TYPE = "intent_type";
    public static final String VALIDATE_CODE = "validate_code";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String COUNTRY_LANGUAGE = "country_language";
    public static final String COUNTRY_CODE = "country_code";


    public static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/cube/";
    public static final String FINISH_ALL = "finish_all";
    public static final String TITLE = "title11";
    public static final String CONTENT = "content11";
    public static final String TYPE = "type111";
    public static final String OPERATION_TYPE = "type";
    public static final String OPERATION_ADD = "add";
    public static final String OPERATION_EDIT = "edit";
    public static final String RESULT = "result";
    public static final String SUCCESS = "success";

    public static final HashMap<String, Integer> WEATHER_MAP = new HashMap<String, Integer>() {
        {
            put(Constants.WEATHER_TYPE_SUNNY, R.string.sunny);
            put(Constants.WEATHER_TYPE_CLEAR, R.string.sunny);
            put(Constants.WEATHER_TYPE_FAIR_DAY, R.string.sunny);
            put(Constants.WEATHER_TYPE_FAIR_NIGHT, R.string.sunny);
            put(Constants.WEATHER_TYPE_CLOUD, R.string.cloudy);
            put(Constants.WEATHER_TYPE_PARTLY_CLOUDY_DAY, R.string.partly_cloudy);
            put(Constants.WEATHER_TYPE_PARTLY_CLOUDY_NIGHT, R.string.partly_cloudy);
            put(Constants.WEATHER_TYPE_MOSTLY_CLOUDY_DAY, R.string.mostly_cloudy);
            put(Constants.WEATHER_TYPE_MOSTLY_CLOUDY_NIGHT, R.string.mostly_cloudy);
            put(Constants.WEATHER_TYPE_OVERCAST, R.string.overcast);
            put(Constants.WEATHER_TYPE_SHOWER, R.string.shower);
            put(Constants.WEATHER_TYPE_THUNDERSHOWER, R.string.thundershower);
            put(Constants.WEATHER_TYPE_THUNDERSHOWER_WITH_HAIL, R.string.thundershower_with_hail);
            put(Constants.WEATHER_TYPE_LIGHT_RAIN, R.string.light_rain);
            put(Constants.WEATHER_TYPE_MODERATE_RAIN, R.string.moderate_rain);
            put(Constants.WEATHER_TYPE_HEAVY_RAIN, R.string.heavy_rain);
            put(Constants.WEATHER_TYPE_STORM, R.string.storm);
            put(Constants.WEATHER_TYPE_HEAVY_STORM, R.string.heavy_storm);
            put(Constants.WEATHER_TYPE_SEVERE_STORM, R.string.severe_storm);
            put(Constants.WEATHER_TYPE_ICE_RAIN, R.string.ice_rain);
            put(Constants.WEATHER_TYPE_SLEET, R.string.sleet);
            put(Constants.WEATHER_TYPE_SNOW_FLURRY, R.string.snow_flurry);
            put(Constants.WEATHER_TYPE_LIGHT_SNOW, R.string.light_snow);
            put(Constants.WEATHER_TYPE_MODERATE_SNOW, R.string.moderate_snow);
            put(Constants.WEATHER_TYPE_HEAVY_SNOW, R.string.heavy_snow);
            put(Constants.WEATHER_TYPE_SNOW_STORM, R.string.snowstorm);
            put(Constants.WEATHER_TYPE_DUST, R.string.dust);
            put(Constants.WEATHER_TYPE_SAND, R.string.sand);
            put(Constants.WEATHER_TYPE_DUST_STORM, R.string.dust_storm);
            put(Constants.WEATHER_TYPE_SAND_STORM, R.string.sandstorm);
            put(Constants.WEATHER_TYPE_FOGGY, R.string.foggy);
            put(Constants.WEATHER_TYPE_HAZE, R.string.haze);
            put(Constants.WEATHER_TYPE_WINDY, R.string.windy);
            put(Constants.WEATHER_TYPE_BLUSTERY, R.string.blustery);
            put(Constants.WEATHER_TYPE_HURRICANE, R.string.hurricane);
            put(Constants.WEATHER_TYPE_TROPICAL_STORM, R.string.tropical_storm);
            put(Constants.WEATHER_TYPE_TORNADO, R.string.tornado);
            put(Constants.WEATHER_TYPE_COLD, R.string.cold);
            put(Constants.WEATHER_TYPE_UNKNOWN, R.string.unknown);
        }
    };


    public class GetDataFlag {
        public static final int HON_IAQ_LOGIN = 1; // 登录
        public static final int HON_IAQ_REGISTER = 2; // 注册
        public static final int HON_IAQ_FORGOT_PWD = 3; // 忘记密码
        public static final int HON_IAQ_GET_WEATHER = 4; // 获取天气
        public static final int HON_IAQ_GET_VALIDATE_CODE = 5; // 获取天气
        public static final int HON_IAQ_GET_DEVICE_LOCATION = 6; // 获取地理位置
        public static final int HON_IAQ_CHECK_DEVICE_BOUND = 7; // 绑定设备
        public static final int HON_IAQ_GET_FORECAST = 8; // 获取预报
        public static final int HON_IAQ_LIFE_SUGGEST = 9; // 获取生活建议
        public static final int HON_IAQ_CHECK_VALIDATE_CODE = 10; // 检查验证码
        public static final int HON_IAQ_UPDATE_DEVICE = 11; // 更新设备
        public static final int HON_IAQ_UNBIND_DEVICE = 12; // 解绑
        public static final int HON_IAQ_GET_DAY_HISTORY = 13; // 日历史
        public static final int HON_IAQ_GET_MONTH_HISTORY = 14; // 月历史
        public static final int HON_IAQ_QUERY_ONLINE = 15; // 查询是否在线
        public static final int HON_IAQ_LOGOUT = 16; // 登出
        public static final int HON_IAQ_GET_LOCATION_INFO = 17; // 获取地址
        public static final int HON_IAQ_SET_DEVICE_LOCATION = 18; // 设置设备地址
        public static final int HON_IAQ_BIND_DEVICE = 19; // 绑定账号
        public static final int HON_IAQ_SLEEP_MODE = 20; // 设定sleep mode
        public static final int HON_IAQ_SET_STANDBY_SCREEN = 21; // 设置待机界面
        public static final int HON_IAQ_SAVE_POWER_MODE = 22; // 设定节能模式
        public static final int HON_IAQ_SET_CLOCK = 23; // 设定闹钟
        public static final int HON_IAQ_GET_CLOCK = 24; // 获取闹钟
        public static final int HON_IAQ_CHECK_REGISTER = 25; // 检查账户是否已存在
        public static final int HON_IAQ_SET_TEMPERATURE = 26; // 设置温度单位


    }
}
