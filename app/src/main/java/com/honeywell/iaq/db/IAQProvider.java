/**
 * @file Copyright (C) 2015 Honeywell Inc. All rights reserved.
 */

package com.honeywell.iaq.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class IAQProvider extends ContentProvider {
    private static final String TAG = "IAQProvider";
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    // All columns
    private static final int BIND_DEVICES = 1;
    // Single column
    private static final int BIND_DEVICE = 2;

    static {
        MATCHER.addURI(IAQ.AUTHORITY, IAQ.TABLE_BIND_DEVICE, BIND_DEVICES);
        MATCHER.addURI(IAQ.AUTHORITY, IAQ.TABLE_BIND_DEVICE + "/#", BIND_DEVICE);
    }

    private DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext(), IAQ.DATABASE_NAME, null, IAQ.DATABASE_VERSION);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (MATCHER.match(uri)) {
            case BIND_DEVICES:
                // All data
                return db.query(IAQ.TABLE_BIND_DEVICE, projection, selection, selectionArgs, null, null, sortOrder);
            case BIND_DEVICE:
                long id = ContentUris.parseId(uri);
                String where = " _id==" + id;
                if (selection != null && selection.length() > 0) {
                    where = selection + " and " + where;
                }
                return db.query(IAQ.TABLE_BIND_DEVICE, projection, where, selectionArgs, null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unknow uri" + uri.toString());
        }

    }

    @Override
    public String getType(Uri uri) {
        switch (MATCHER.match(uri)) {
            case BIND_DEVICES:
                return "vnd.android.cursor.dir/" + IAQ.TABLE_BIND_DEVICE;
            case BIND_DEVICE:
                return "vnd.android.cursor.item/" + IAQ.TABLE_BIND_DEVICE;
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (MATCHER.match(uri)) {
            case BIND_DEVICES:
                long rowId = db.insert(IAQ.TABLE_BIND_DEVICE, IAQ.BindDevice.COLUMN_ACCOUNT, values);
                if (rowId > 0) {
                    Uri insertUri = ContentUris.withAppendedId(uri, rowId);
                    getContext().getContentResolver().notifyChange(insertUri, null);
                    return insertUri;
                }
                break;
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Log.d(TAG, "Uri=" + uri);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri)) {
            case BIND_DEVICES:
                count = db.delete(IAQ.TABLE_BIND_DEVICE, selection, selectionArgs);
                break;
            case BIND_DEVICE:
                long id = ContentUris.parseId(uri);
                String where = IAQ.BindDevice.COLUMN_ID + "=" + id;
                if (selection != null && selection.length() > 0) {
                    where = selection + " and " + where;
                }
                Log.d(TAG, "where=" + where);
                count = db.delete(IAQ.TABLE_BIND_DEVICE, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (MATCHER.match(uri)) {
            case BIND_DEVICES:
                count = db.update(IAQ.TABLE_BIND_DEVICE, values, selection, selectionArgs);
                break;
            case BIND_DEVICE:
                long id = ContentUris.parseId(uri);
                String where = "_id=" + id;
                if (selection != null && selection.length() > 0) {
                    where = selection + " and " + where;
                }
                count = db.update(IAQ.TABLE_BIND_DEVICE, values, where, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
