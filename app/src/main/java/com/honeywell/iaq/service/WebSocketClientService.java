package com.honeywell.iaq.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.honeywell.iaq.events.IAQGetDataFromWsEvent;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.lib.autobaln_websocket.WebSocket;
import com.honeywell.lib.autobaln_websocket.WebSocketConnection;
import com.honeywell.lib.autobaln_websocket.WebSocketException;
import com.honeywell.lib.autobaln_websocket.WebSocketOptions;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.greenrobot.event.EventBus;

public class WebSocketClientService extends Service {
    private final String TAG = WebSocketClientService.class.getSimpleName();

    private final WebSocketConnection mConnection = new WebSocketConnection();

    private MyHandler mHandler;

    private static final int UPDATE_IAQ_DATA = 0;

    private static final int CONNECT_WSS = 1;

    private static final int WSS_CONNECTED = 2;

    private static final int DISCONNECT_REFRESH_DATA = 3;

    private static final int UPDATE_IAQ_DATA_INTERVAL = 10 * 1000;

    private static final int CONNECT_WSS_INTERVAL = 3 * 1000;

//    private ConnectionChangeReceiver connectionChangeReceiver;

    private ArrayList<String> deviceIdList = new ArrayList<>();
    private boolean isExitDisconnect = false;
    private boolean isWebsocketConnected = false;
    private boolean isWebsocketConnecting = false;


    private WebSocket.ConnectionHandler connectionHandler = new WebSocket.ConnectionHandler() {
        @Override
        public void onOpen() {
            Logger.d(TAG, "WSS opened" + mConnection.isConnected());
            isWebsocketConnected = true;
            isWebsocketConnecting = false;
//            Utils.setSharedPreferencesValue(getApplicationContext(), Const.KEY_WSS_CONNECT_STATUS, Const.WSS_CONNECTED);
            getOnlineStatus();
//            getIAQData();
            mHandler.sendEmptyMessageDelayed(WSS_CONNECTED, 1000);
            mHandler.removeMessages(CONNECT_WSS);
        }

        @Override
        public void onClose(WebSocketCloseNotification i, String s) {
            isWebsocketConnected = false;
            isWebsocketConnecting = false;
            Logger.e(TAG, "stop isWebsocketConnected=" + isWebsocketConnected + "isWebsocketConnecting=" + isWebsocketConnecting + "isExitDisconnect：" + isExitDisconnect);
            Logger.d(TAG, "WSS closed" + i);
//            Utils.setSharedPreferencesValue(getApplicationContext(), Const.KEY_WSS_CONNECT_STATUS, Const.WSS_DISCONNECTED);
            mHandler.removeMessages(UPDATE_IAQ_DATA);
            mHandler.removeMessages(CONNECT_WSS);

            //延迟一秒 主要是解决Preferance中账户字段更新慢的问题
            mHandler.sendEmptyMessageDelayed(DISCONNECT_REFRESH_DATA, 1000);

            //断开 网络正常 非主动退出 则执行重连
            if (i == WebSocketCloseNotification.CONNECTION_LOST && Utils.isNetworkAvailable(getApplicationContext()) && !isExitDisconnect) {
                connectWebSocket();
            }
        }

        @Override
        public void onTextMessage(String s) {
            Logger.e(TAG, "onTextMessage receive message=" + s);
            parseMessage(getApplicationContext(), s);
        }

        @Override
        public void onRawTextMessage(byte[] bytes) {
            Logger.d(TAG, "onRawTextMessage receive message=");
            parseMessage(getApplicationContext(), new String(bytes));
        }

        @Override
        public void onBinaryMessage(byte[] bytes) {
            Logger.d(TAG, "onBinaryMessage receive message=");
            parseMessage(getApplicationContext(), new String(bytes));
        }
    };

//    public class ConnectionChangeReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(final Context context, Intent intent) {
//            Log.d(TAG, "onReceive: Action=" + intent.getAction());
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    if (Utils.isNetworkConnected(context)) {
//                        Intent getIAQData = new Intent(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
//                        sendBroadcast(getIAQData);
//
//                        Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_NETWORK_CONNECT_STATUS, Constants.NETWORK_CONNECTED);
//                    } else {
//                        Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_NETWORK_CONNECT_STATUS, Constants.NETWORK_DISCONNECTED);
//                    }
//                }
//            }).start();
//        }
//    }

    static class MyHandler extends Handler {
        private WeakReference<WebSocketClientService> mInstanceContent;

        private WebSocketClientService mInstance;

        public MyHandler(WebSocketClientService instance) {
            mInstanceContent = new WeakReference<WebSocketClientService>(instance);
            mInstance = mInstanceContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_IAQ_DATA:
                    String deviceId = (String) msg.obj;
                    mInstance.sendGetIaqDataMessage(deviceId);
                    break;
                case CONNECT_WSS:
                    mInstance.connectWebSocket();
                    break;
                case WSS_CONNECTED:
                    Utils.sendBroadcast(mInstance.getApplicationContext(), Constants.ACTION_WSS_CONNECTED);
                    break;
                case DISCONNECT_REFRESH_DATA:
                    mInstance.disConnectRefreshData();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new MyHandler(this);
//        connectionChangeReceiver = new ConnectionChangeReceiver();
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(connectionChangeReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Logger.d(TAG, "onStartCommand: Action=" + action);
            if (Constants.ACTION_OPEN_WSS.equals(action)) {

                connectWebSocket();
            } else if (Constants.ACTION_GET_IAQ_DATA.equals(action)) {
                sendGetIaqDataMessage(intent.getStringExtra(Constants.KEY_DEVICE_ID));
            } else if (Constants.ACTION_DISCONNECT.equals(action)) {
                String str = intent.getStringExtra(Constants.KEY_DISCONNECT);
                Logger.e(TAG, "disconnect type" + str);
                if (Constants.EXIT_DISCONNECT.equalsIgnoreCase(str)) {
                    isExitDisconnect = true;
                } else {
                    isExitDisconnect = false;
                }
                Logger.e(TAG, "isExitDisconnect" + isExitDisconnect);
                disconnect();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG, "Service Destroy");
        disconnect();
        mHandler = null;
//        unregisterReceiver(connectionChangeReceiver);
        super.onDestroy();
    }

    private void connectWebSocket() {
        Logger.e(TAG, " start   isWebsocketConnected=" + isWebsocketConnected + "isWebsocketConnecting=" + isWebsocketConnecting + "isExitDisconnect：" + isExitDisconnect);
        if (!mConnection.isConnected() && !isWebsocketConnected && !isWebsocketConnecting) {
            isWebsocketConnecting = true;
            //执行连接 isExitDisconnect 初始化，以便退出时候使用
            isExitDisconnect = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (Utils.isNetworkConnected(getApplicationContext())) {
                        try {
                            Logger.e(TAG, "start connecting_start");
                            String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
                            if (cookie.length() > 0) {
                                List<BasicNameValuePair> extraHeaders = Arrays.asList(new BasicNameValuePair(Constants.KEY_COOKIE, cookie));
                                mConnection.connect(Constants.WSS_CONNECT_URL, null, connectionHandler, new WebSocketOptions(), extraHeaders);
                                Logger.e(TAG, "start connecting_end");
                            }
                        } catch (WebSocketException e) {
                            isWebsocketConnecting = false;
                            e.printStackTrace();
                            Utils.sendBroadcast(getApplicationContext(), Constants.ACTION_WSS_CONNECT_FAIL);
                        }

                        mHandler.sendEmptyMessageDelayed(CONNECT_WSS, CONNECT_WSS_INTERVAL);
                    } else {
                        isWebsocketConnecting = false;
                        Logger.d(TAG, "Network is not available");
                    }
                }
            }).start();
        } else {
            Logger.d(TAG, "WSS is connected");
            mHandler.sendEmptyMessageDelayed(WSS_CONNECTED, 1000);
            mHandler.removeMessages(CONNECT_WSS);
        }
    }


    private void parseMessage(Context context, String message) {
        try {
            JSONObject jsonParser = new JSONObject(message);
            String type = jsonParser.getString(Constants.KEY_TYPE);
            if (Constants.TYPE_GET_IAQ_DATA.equals(type)) {
                updateIAQData(jsonParser);
            } else if (Constants.TYPE_ONLINE_STATUS.equals(type)) {
                updateOnlineStatus(jsonParser);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

//    private void getIAQData() {
//        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
//        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
//        String[] selectionArgs = new String[]{account};
//        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
//        int dbCount = cur.getCount();
//        Log.d(TAG, "getIAQData: DBCount=" + dbCount);
//        if (dbCount > 0) {
//            cur.moveToFirst();
//
//            while (!cur.isAfterLast()) {
//                String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
//
//                sendGetIaqDataMessage(deviceId);
//
//                cur.moveToNext();
//            }
//        }
//        cur.close();
//    }

    private void disConnectRefreshData() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{account};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        int dbCount = cur.getCount();
        Logger.d(TAG, "initIAQData: DBCount=" + dbCount);
        if (dbCount > 0) {
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
                cleanIAQData(account, deviceId);

                cur.moveToNext();
            }
        }
        cur.close();
    }

    private void cleanIAQData(String account, String deviceId) {
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};

        ContentValues cv = new ContentValues();
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_PM25, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_TVOC, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_CO2, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_HCHO, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN, "");
        cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT, "");
        getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

        Intent intent = new Intent(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
        sendBroadcast(intent);
    }

    private void updateIAQData(JSONObject jsonObject) {
        try {
            String deviceId = jsonObject.getString(Constants.KEY_DEVICE_ID);
            String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
            String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
            String[] selectionArgs = new String[]{account, deviceId};
            Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
            int dbCount = cur.getCount();
            Logger.d(TAG, "updateIAQData: DB Count=" + dbCount);
            Logger.e(TAG, "updateIAQData: " + jsonObject.toString());
            if (dbCount > 0) {
                cur.moveToFirst();

                String currentSerialNum = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER));
                Logger.d(TAG, "updateIAQData: currentSerialNum=" + currentSerialNum);

                ContentValues cv = new ContentValues();
                int onlineStatus = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
                Logger.d(TAG, "updateIAQData: onlineStatus=" + onlineStatus);
                if (onlineStatus == Constants.DEVICE_ONLINE) {

//                    if (jsonObject.toString().contains(Constants.KEY_PM25)) {
//                        String pm25 = jsonObject.optString(Constants.KEY_PM25);
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_PM25, replaceZero(pm25));
//                    }
//                    if (jsonObject.toString().contains(Constants.KEY_HUMIDITY)) {
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, jsonObject.getString(Constants.KEY_HUMIDITY));
//                    }
//                    if (jsonObject.toString().contains(Constants.KEY_TEMPERATURE)) {
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE, jsonObject.getString(Constants.KEY_TEMPERATURE));
//                    }
//                    if (jsonObject.toString().contains(Constants.KEY_DEVICE_CO2)) {
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_CO2, replaceZero(jsonObject.getString(Constants.KEY_DEVICE_CO2)));
//                    }
//                    if (jsonObject.toString().contains(Constants.KEY_DEVICE_TVOC)) {
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_TVOC, replaceZero(jsonObject.getString(Constants.KEY_DEVICE_TVOC)));
//                    }
//                    if (jsonObject.toString().contains(Constants.KEY_DEVICE_HCHO)) {
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_HCHO, replaceZero(jsonObject.getString(Constants.KEY_DEVICE_HCHO)));
//                    }

                    if (jsonObject.has(Constants.KEY_PM25)) {
                        String pm25 = jsonObject.optString(Constants.KEY_PM25);
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_PM25, replaceZero(pm25));
                    }
                    if (jsonObject.has(Constants.KEY_HUMIDITY)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, jsonObject.getString(Constants.KEY_HUMIDITY));
                    }
                    if (jsonObject.has(Constants.KEY_TEMPERATURE)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE, jsonObject.getString(Constants.KEY_TEMPERATURE));
                    }
                    if (jsonObject.has(Constants.KEY_DEVICE_CO2)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_CO2, replaceZero(jsonObject.getString(Constants.KEY_DEVICE_CO2)));
                    }
                    if (jsonObject.has(Constants.KEY_DEVICE_TVOC)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_TVOC, replaceZero(jsonObject.getString(Constants.KEY_DEVICE_TVOC)));
                    }
                    if (jsonObject.has(Constants.KEY_DEVICE_HCHO)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_HCHO, replaceZero(jsonObject.getString(Constants.KEY_DEVICE_HCHO)));
                    }

                    if (jsonObject.has(Constants.KEY_DEVICE_SLEEP)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP, jsonObject.optString(Constants.KEY_DEVICE_SLEEP));
                    }

                    if (jsonObject.has(Constants.KEY_DEVICE_SLEEP_START)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START, jsonObject.optString(Constants.KEY_DEVICE_SLEEP_START));
                    }

                    if (jsonObject.has(Constants.KEY_DEVICE_SLEEP_STOP)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP, jsonObject.optString(Constants.KEY_DEVICE_SLEEP_STOP));
                    }

                    if (jsonObject.has(Constants.KEY_DEVICE_SAVE_POWER)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER, jsonObject.optString(Constants.KEY_DEVICE_SAVE_POWER));
                    }

                    if (jsonObject.has(Constants.KEY_DEVICE_STANDBY_SCREEN)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN, jsonObject.optString(Constants.KEY_DEVICE_STANDBY_SCREEN));
                    }

                    if (jsonObject.has(Constants.KEY_DEVICE_TEMPERATURE_UNIT)) {
                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT, jsonObject.optString(Constants.KEY_DEVICE_TEMPERATURE_UNIT));
                    }

                } else {
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_PM25, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TVOC, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_CO2, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_HCHO, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT, "");

                }
                getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

                Intent intent = new Intent(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
                sendBroadcast(intent);
                EventBus.getDefault().post(new IAQGetDataFromWsEvent());

                mHandler.removeMessages(UPDATE_IAQ_DATA);
            }

            cur.close();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private String replaceZero(String string) {
        if ("0".equals(string))
            return "0";

        String newStr = string.replaceFirst("^0*", "");
        return newStr;
    }

    private void getOnlineStatus() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{account};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        int dbCount = cur.getCount();
//        Log.d(TAG, "getOnlineStatus: DBCount=" + dbCount);
        if (dbCount > 0) {
            cur.moveToFirst();

            while (!cur.isAfterLast()) {
                String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
                getOnlineStatusByDeviceId(deviceId);

                cur.moveToNext();
            }
        }
        cur.close();
    }

    private void getOnlineStatusByDeviceId(final String deviceId) {

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_QUERY_ONLINE);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(getApplicationContext(), Constants.DEVICE_ONLINE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getApplicationContext(), Constants.GetDataFlag.HON_IAQ_QUERY_ONLINE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode != 0) {
                    return;
                }

                String responseStr = (String) objects[0];
                try {
                    JSONObject jsonObject = new JSONObject(responseStr);
                    boolean status = jsonObject.getBoolean(Constants.KEY_ONLINE_STATUS);
                    int onlineStatus = Constants.DEVICE_OFFLINE;
                    if (status) {
                        onlineStatus = Constants.DEVICE_ONLINE;
                    }
                    Logger.d(TAG, "getOnlineStatusByDeviceId: onSuccess: Online Status=" + onlineStatus);
                    updateOnlineStatus(deviceId, onlineStatus);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }));

    }


    private void updateOnlineStatus(String deviceId, int onlineStatus) {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{account, deviceId};

        Logger.d(TAG, "updateOnlineStatus1: onlineStatus=" + onlineStatus);
        ContentValues cv = new ContentValues();
        cv.put(IAQ.BindDevice.COLUMN_ONLINE_STATUS, onlineStatus);
        if (onlineStatus == Constants.DEVICE_OFFLINE) {
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_PM25, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_TVOC, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_CO2, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_HCHO, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT, "");

            getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

            Intent intent = new Intent(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
            sendBroadcast(intent);
        } else {
            getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

            Map<String, String> params = new HashMap<>();
            params.put(Constants.KEY_TYPE, Constants.TYPE_GET_IAQ_DATA);
            params.put(Constants.KEY_DEVICE_ID, deviceId);
            String message = Utils.getJsonString(params);
            mConnection.sendTextMessage(message);
        }
    }

    private void updateOnlineStatus(JSONObject jsonObject) {
        try {
            if (jsonObject.toString().contains(Constants.KEY_ONLINE_STATUS)) {
                String deviceId = jsonObject.getString(Constants.KEY_DEVICE_ID);
                String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
                String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
                String[] selectionArgs = new String[]{account, deviceId};

                boolean status = jsonObject.getBoolean(Constants.KEY_ONLINE_STATUS);
                int onlineStatus = Constants.DEVICE_OFFLINE;
                if (status) {
                    onlineStatus = Constants.DEVICE_ONLINE;
                }
                Logger.d(TAG, "updateOnlineStatus2: onlineStatus=" + onlineStatus);
                ContentValues cv = new ContentValues();
                cv.put(IAQ.BindDevice.COLUMN_ONLINE_STATUS, onlineStatus);
                if (onlineStatus == Constants.DEVICE_OFFLINE) {
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_PM25, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TVOC, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_CO2, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_HCHO, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_START, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SLEEP_STOP, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_SAVE_POWER, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_STANDBY_SCREEN, "");
                    cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT, "");
                    getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

                    Intent intent = new Intent(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
                    sendBroadcast(intent);
                } else {
                    getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);

                    Map<String, String> params = new HashMap<>();
                    params.put(Constants.KEY_TYPE, Constants.TYPE_GET_IAQ_DATA);
                    params.put(Constants.KEY_DEVICE_ID, deviceId);
                    String message = Utils.getJsonString(params);
                    mConnection.sendTextMessage(message);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendGetIaqDataMessage(String deviceId) {
        if (deviceId != null && deviceId.length() > 0) {
            if (!deviceIdList.contains(deviceId)) {
                deviceIdList.add(deviceId);
            }
            if (mConnection.isConnected()) {
                Map<String, String> params = new HashMap<>();
                params.put(Constants.KEY_TYPE, Constants.TYPE_GET_IAQ_DATA);
                params.put(Constants.KEY_DEVICE_ID, deviceId);
                String message = Utils.getJsonString(params);
                mConnection.sendTextMessage(message);
                Logger.d(TAG, "sendGetIaqDataMessage: DeviceId=" + deviceId);

//                if (Constants.DEVICE_ONLINE == Utils.getOnlineStatusFromDB(getApplicationContext(), deviceId)) {
//                    Message msg = Message.obtain(mHandler, UPDATE_IAQ_DATA, deviceId);
//                    mHandler.sendMessageDelayed(msg, UPDATE_IAQ_DATA_INTERVAL);
//                }
            } else {
//                connectWebSocket();
            }

//            Message msg = Message.obtain(mHandler, UPDATE_IAQ_DATA, deviceId);
//            mHandler.sendMessageDelayed(msg, UPDATE_IAQ_DATA_INTERVAL);
        } else {
            Logger.d(TAG, "Invalid device id");
        }
    }

    private void disconnect() {
        if (mConnection.isConnected()) {
            mConnection.disconnect();
        }
    }
}
