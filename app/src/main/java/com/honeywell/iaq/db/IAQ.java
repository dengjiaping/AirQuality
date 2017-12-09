/**
 * @file Copyright (C) 2015 Honeywell Inc. All rights reserved.
 */

package com.honeywell.iaq.db;

import android.net.Uri;
import android.provider.BaseColumns;

import com.honeywell.iaq.utils.Constants;

public class IAQ {
    public static final String DATABASE_NAME = "iaq.db";

    public static final String TABLE_BIND_DEVICE = "bind_device";

    public static final int DATABASE_VERSION = 4;

    public static final String AUTHORITY = "com.honeywell.iaq.db.IAQProvider";

    public IAQ() {
    }

    public static final class BindDevice implements BaseColumns {
        public static final String COLUMN_ID = "_id";

        public static final String COLUMN_ACCOUNT = Constants.KEY_ACCOUNT;

        public static final String COLUMN_DEVICE_ID = Constants.KEY_DEVICE_ID;

        public static final String COLUMN_DEVICE_SERIAL_NUMBER = Constants.KEY_DEVICE_SERIAL;

        public static final String COLUMN_DEVICE_HOME = Constants.KEY_HOME;

        public static final String COLUMN_DEVICE_ROOM = Constants.KEY_ROOM;

        public static final String COLUMN_LOCATION = Constants.KEY_LOCATION;

        public static final String COLUMN_ONLINE_STATUS = Constants.KEY_ONLINE_STATUS;

        // IAQ column
        public static final String COLUMN_DEVICE_PM25 = Constants.KEY_DEVICE_PM25;

        public static final String COLUMN_DEVICE_TEMPERATURE = Constants.KEY_DEVICE_TEMPERATURE;

        public static final String COLUMN_DEVICE_HUMIDITY = Constants.KEY_DEVICE_HUMIDITY;

        public static final String COLUMN_DEVICE_TVOC = Constants.KEY_DEVICE_TVOC;

        public static final String COLUMN_DEVICE_CO2 = Constants.KEY_DEVICE_CO2;
        public static final String COLUMN_DEVICE_HCHO = Constants.KEY_DEVICE_HCHO;
        public static final String COLUMN_DEVICE_SLEEP = Constants.KEY_DEVICE_SLEEP_MODE;

        // GEN2_DATA
        public static final String COLUMN_DEVICE_SLEEP_START = Constants.KEY_DEVICE_SLEEP_START;
        public static final String COLUMN_DEVICE_SLEEP_STOP = Constants.KEY_DEVICE_SLEEP_STOP;
        public static final String COLUMN_DEVICE_SAVE_POWER = Constants.KEY_DEVICE_SAVE_POWER;
        public static final String COLUMN_DEVICE_STANDBY_SCREEN = Constants.KEY_DEVICE_STANDBY_SCREEN;
        public static final String COLUMN_DEVICE_TEMPERATURE_UNIT = Constants.KEY_DEVICE_TEMPERATURE_UNIT;

        // Weather column
        public static final String COLUMN_WEATHER = Constants.KEY_WEATHER;

        public static final String COLUMN_TEMPERATURE = Constants.KEY_TEMPERATURE;

        public static final String COLUMN_HUMIDITY = Constants.KEY_HUMIDITY;

        public static final String COLUMN_PM25 = Constants.KEY_PM25;

        public static final String COLUMN_PM10 = Constants.KEY_PM10;

        public static final String COLUMN_AQI = Constants.KEY_AQI;

        public static final String COLUMN_TIME = Constants.KEY_TIME;

        public static final Uri DICT_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_BIND_DEVICE);

        public static final Uri COLUMN_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_BIND_DEVICE + "/" + COLUMN_ACCOUNT);

    }

}
