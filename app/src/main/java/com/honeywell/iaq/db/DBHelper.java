/**
 * @file Copyright (C) 2015 Honeywell Inc. All rights reserved.
 */

package com.honeywell.iaq.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";

    // table creation sql statement
    private static final String BIND_DEVICE_TABLE_CREATE = "CREATE TABLE " + IAQ.TABLE_BIND_DEVICE + " ("
            + IAQ.BindDevice.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + IAQ.BindDevice.COLUMN_DEVICE_ID + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_ROOM + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_HOME + " TEXT,"
            + IAQ.BindDevice.COLUMN_ONLINE_STATUS + " INTEGER,"
            + IAQ.BindDevice.COLUMN_DEVICE_PM25 + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_SLEEP + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_TVOC + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_CO2 + " TEXT,"
            + IAQ.BindDevice.COLUMN_DEVICE_HCHO + " TEXT,"
            + IAQ.BindDevice.COLUMN_LOCATION + " TEXT,"
            + IAQ.BindDevice.COLUMN_WEATHER + " TEXT,"
            + IAQ.BindDevice.COLUMN_TEMPERATURE + " TEXT,"
            + IAQ.BindDevice.COLUMN_HUMIDITY + " TEXT,"
            + IAQ.BindDevice.COLUMN_PM25 + " TEXT,"
            + IAQ.BindDevice.COLUMN_PM10 + " TEXT,"
            + IAQ.BindDevice.COLUMN_AQI + " TEXT,"
            + IAQ.BindDevice.COLUMN_TIME + " TEXT,"
            + IAQ.BindDevice.COLUMN_ACCOUNT + " TEXT NOT NULL);";

    private static final String ADD_SLEEP_START = "ALTER TABLE " + IAQ.TABLE_BIND_DEVICE + " ADD COLUMN " + IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START + " TEXT";
    private static final String ADD_SLEEP_STOP = "ALTER TABLE " + IAQ.TABLE_BIND_DEVICE + " ADD COLUMN " + IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP + " TEXT";
    private static final String ADD_SAVE_POWER = "ALTER TABLE " + IAQ.TABLE_BIND_DEVICE + " ADD COLUMN " + IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER + " TEXT";
    private static final String ADD_STANDBY = "ALTER TABLE " + IAQ.TABLE_BIND_DEVICE + " ADD COLUMN " + IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN + " TEXT";
    private static final String ADD_TEMPERATURE_UNIT = "ALTER TABLE " + IAQ.TABLE_BIND_DEVICE + " ADD COLUMN " + IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT + " TEXT";

    /**
     * @param context
     * @param name    table name
     * @param factory
     * @param version current database version
     */
    public DBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "create database");

//      若不是第一个版本安装，直接执行数据库升级
//      请不要修改FIRST_DATABASE_VERSION的值，其为第一个数据库版本大小
        final int FIRST_DATABASE_VERSION = 3;
        db.execSQL(BIND_DEVICE_TABLE_CREATE);
        onUpgrade(db, FIRST_DATABASE_VERSION, IAQ.DATABASE_VERSION);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "upgrade database");

        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 3:
                    upgradeToVersion4(db);
                    break;

                case 4:
                    upgradeToVersion5(db);
                    break;

                default:
                    break;
            }
        }
    }

    private void upgradeToVersion4(SQLiteDatabase db) {
        db.execSQL(ADD_SLEEP_START);
        db.execSQL(ADD_SLEEP_STOP);
        db.execSQL(ADD_SAVE_POWER);
        db.execSQL(ADD_STANDBY);
        db.execSQL(ADD_TEMPERATURE_UNIT);
    }

    private void upgradeToVersion5(SQLiteDatabase db) {
//        db.execSQL(BIND_DEVICE_TABLE_CREATE);
    }


}
