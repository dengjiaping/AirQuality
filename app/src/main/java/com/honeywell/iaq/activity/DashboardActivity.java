package com.honeywell.iaq.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.adapter.DeviceListAdapter;
import com.honeywell.iaq.application.IAQApplication;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.bean.ChildItem;
import com.honeywell.iaq.bean.DeviceInformation;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.events.IAQTemperatureEvent;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.service.WebSocketClientService;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nineoldandroids.view.ViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.util.TextUtils;

public class DashboardActivity extends IAQTitleBarActivity {

    private static final String TAG = "Dashboard";

    private DrawerLayout mDrawerLayout;

    private ExpandableListView mDeviceList;

    private DeviceListAdapter mAdapter;

    private ArrayList<DeviceInformation> mDeviceInfoList;

    private Map<Integer, List<ChildItem>> childMap;

    private static final int GET_DATA_SUCCESS = 0;

    private static final int GET_DATA_FAIL = 1;

    private static final int CHECK_NETWORK = 2;

//    private ProgressDialog mDialog;

    private DashboardHandler mHandler;

    private DashboardReceiver mReceiver;

    // minimum 300 seconds
    public static final int FREQUENCY_INTERVAL = 30 * 60;

    private static final int CHECK_NETWORK_INTERVAL = 3 * 1000;

    private IntentFilter filter;

    private boolean isLoacationSuccess = false;

    private static int totalDeviveCount = 0;
    private int deviceCount = 0;

    static class DashboardHandler extends Handler {
        private WeakReference<DashboardActivity> mActivityContent;

        private DashboardActivity mActivity;

        public DashboardHandler(DashboardActivity activity) {
            mActivityContent = new WeakReference<>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA_SUCCESS:
                    //地址获取成功
                    mActivity.setData();
                    mActivity.refresh();
                    mActivity.dismissLoadingDialog();
                    mActivity.isLoacationSuccess = true;
                    break;
                case GET_DATA_FAIL:
                    mActivity.dismissLoadingDialog();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    class DashboardReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: Action=" + action);
            if (Constants.ACTION_WSS_CONNECTED.equals(action)) {
                refresh();

            } else if (Constants.ACTION_WSS_CONNECT_FAIL.equals(action)) {
//                if (mDialog != null) {
//                    mDialog.dismiss();
//                }
                Utils.showToast(getApplicationContext(), getString(R.string.connect_cloud_fail));
            } else if (Constants.ACTION_GET_IAQ_DATA_SUCCESS.equals(action)) {
//                Logger.e("boolean", "" + isLoacationSuccess);
                if (isLoacationSuccess) {
                    setData();
                } else {
//                    refresh();
                }
            } else if (Constants.ACTION_LOGOUT_FAIL.equals(action)) {
                Utils.showToast(getApplicationContext(), getString(R.string.logout_fail));
            } else if (Constants.ACTION_INVALID_NETWORK.equals(action)) {
                Utils.showToast(getApplicationContext(), getString(R.string.no_network));
            }
        }
    }

    @Override
    protected int getContent() {
        return R.layout.activity_dashboard;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.dashboard_title);
    }

    @Override
    protected void initLeftIcon(ImageView left) {
        super.initLeftIcon(left);
        left.setImageResource(R.mipmap.icon_menu);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLeftMenu();
            }
        });
    }

    @Override
    protected void initView() {
        super.initView();
        showLoadingDialog();
        mHandler = new DashboardHandler(this);
        mReceiver = new DashboardReceiver();
//        mDialog = new ProgressDialog(this);
//        mDialog.setTitle(getString(R.string.app_name));
//        mDialog.setCanceledOnTouchOutside(false);

        filter = new IntentFilter();
        filter.addAction(Constants.ACTION_WSS_CONNECTED);
        filter.addAction(Constants.ACTION_WSS_CONNECT_FAIL);
        filter.addAction(Constants.ACTION_GET_IAQ_DATA_SUCCESS);
        filter.addAction(Constants.ACTION_LOGOUT_FAIL);
        filter.addAction(Constants.ACTION_INVALID_NETWORK);
        registerReceiver(mReceiver, filter);

        mDeviceInfoList = new ArrayList<>();
        childMap = new HashMap<>();
        mDeviceList = (ExpandableListView) findViewById(R.id.device_list);
        mDeviceList.setGroupIndicator(null);
        mAdapter = new DeviceListAdapter(getApplicationContext(), mDeviceInfoList, childMap);
        mDeviceList.setAdapter(mAdapter);
        mDeviceList.setOnItemLongClickListener(onItemLongClickListener);
        mDeviceList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });
        mDeviceList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    String serialNum = childMap.get(groupPosition).get(childPosition).getSerialNum();
                    if (serialNum != null) {
                        Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, serialNum);


                        if (isDeviceInfoComplete(serialNum)) {
                            Intent intent = new Intent(DashboardActivity.this, HomeActivity.class);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(DashboardActivity.this, NameIAQActivity.class);
                            startActivity(intent);
                        }

                    }
                } else {
                    Utils.showToast(getApplicationContext(), getString(R.string.no_network));
                }

                return false;
            }
        });

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_layout);
        setListener();

        getDatas();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_SEARCH)
            return true;

        return super.onKeyDown(keyCode, event);
    }


    //请求地址等信息
    private void getDatas() {
        if (Utils.isNetworkAvailable(getApplicationContext())) {

            //获取本账户下面所有设备的 地址
            String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
            String[] selectionArgs = new String[]{Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "")};
            Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
            final int dbCount = cur.getCount();
            Log.d(TAG, "refresh: Count=" + dbCount);
            if (dbCount > 0) {
//                mDialog.setMessage(getString(R.string.iaq_cloud_data));
                totalDeviveCount = dbCount;
                deviceCount = 0;

                cur.moveToFirst();
                while (!cur.isAfterLast()) {
                    String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
                    String serialNum = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER));
                    Log.d(TAG, "refresh: serialNum=" + serialNum);
                    getDeviceLocation(deviceId, serialNum, cur.isLast());
                    Logger.d(TAG, "start service open wss get data");
//                    Utils.startServiceByAction(getApplicationContext(), Constants.ACTION_OPEN_WSS);
                    cur.moveToNext();
                }
            } else {
                dismissLoadingDialog();
                startActivity(new Intent(this, NetworkSetup1Activity.class));
            }

            cur.close();


        } else {
            dismissLoadingDialog();
            Utils.showToast(getApplicationContext(), getString(R.string.no_network));
        }
    }


    private void initIAQData() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{account};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        int count = cur.getCount();
        Log.d(TAG, "initIAQData: Account count=" + count);


        for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
            String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
            initIAQDataByDeviceId(account, deviceId);
            cur.moveToNext();
        }

        cur.close();
    }

    private void initIAQDataByDeviceId(String account, String deviceId) {
        if (deviceId != null && deviceId.length() > 0) {
            String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_ID + "=?";
            String[] selectionArgs = new String[]{account, deviceId};
            Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
            int count = cur.getCount();
            Log.d(TAG, "initIAQDataByDeviceId: Account count=" + count);
            ContentValues cv = new ContentValues();
            cv.put(IAQ.BindDevice.COLUMN_ACCOUNT, account);
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_ID, deviceId);

            cv.put(IAQ.BindDevice.COLUMN_DEVICE_PM25, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_TVOC, "");
            cv.put(IAQ.BindDevice.COLUMN_DEVICE_HCHO, "");

            if (count == 0) {
                getContentResolver().insert(IAQ.BindDevice.DICT_CONTENT_URI, cv);
            } else {
                getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);
            }

            cur.close();
        }
    }

    //    private void refreshData() {
//        if (Utils.isNetworkAvailable(getApplicationContext())) {
//            Utils.startServiceByAction(getApplicationContext(), Constants.ACTION_OPEN_WSS);
//            mHandler.removeMessages(CHECK_NETWORK);
//        } else {
//            mHandler.sendEmptyMessageDelayed(CHECK_NETWORK, CHECK_NETWORK_INTERVAL);
//            initIAQData();
//        }
//    }
//对一个在线设备发送获取数据请求
    private void refresh() {
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "")};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Log.d(TAG, "refresh: Count=" + dbCount);
        if (dbCount > 0) {
//            mDialog.setMessage(getString(R.string.iaq_cloud_data));

            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
                String serialNum = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER));
                int status = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
                Logger.d(TAG, "refresh: serialNum=" + serialNum + "deviceId:" + deviceId + "status:" + status);
                if (Constants.DEVICE_ONLINE == status) {
                    startServiceByAction(getApplicationContext(), Constants.ACTION_GET_IAQ_DATA, deviceId);
                }
                cur.moveToNext();
            }
        }
        cur.close();
    }

    private void startServiceByAction(Context context, String action, String deviceId) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setPackage(context.getPackageName());
        intent.putExtra(Constants.KEY_DEVICE_ID, deviceId);
        context.startService(intent);
    }

    private void getDeviceLocation(final String deviceId, final String serialNum, final boolean isLast) {


        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_LOCATION);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(this, Constants.DEVICE_LOCATION_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_GET_DEVICE_LOCATION, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {

                if (resultCode != 0) {
                    mHandler.sendEmptyMessage(GET_DATA_FAIL);
                    return;
                }
                String responseStr = (String) objects[0];
                if (!responseStr.contains(Constants.KEY_NAME)) {
                    mHandler.sendEmptyMessage(GET_DATA_FAIL);
                    return;
                }
                try {
                    JSONObject jsonObject = new JSONObject(responseStr);
                    Log.d(TAG, "location=" + jsonObject.getString(Constants.KEY_NAME));

                    String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
                    String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
                    String[] selectionArgs = new String[]{account, serialNum};

                    ContentValues cv = new ContentValues();
                    cv.put(IAQ.BindDevice.COLUMN_LOCATION, jsonObject.getString(Constants.KEY_NAME));
                    getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);


                    deviceCount = deviceCount + 1;
                    if (deviceCount == totalDeviveCount) {
                        mHandler.sendEmptyMessage(GET_DATA_SUCCESS);
                    }


//                    if (isLast) {
//                        //发送获取成功标识
////                        mHandler.sendEmptyMessageDelayed(GET_DATA_SUCCESS, 1000);
//                        isLoacationSuccess = true;
//                        setData();
//                    }
                } catch (JSONException e) {
                    mHandler.sendEmptyMessage(GET_DATA_FAIL);
                    e.printStackTrace();
                }

            }
        }));

    }


    private void setData() {
        dismissLoadingDialog();
        childMap.clear();
        mDeviceInfoList.clear();

        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "")};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        final int dbCount = cur.getCount();
        Log.d(TAG, "setData: DBCount=" + dbCount);
        if (dbCount == 0) {
            //没有设备，进入添加设备页面
            startActivity(new Intent(this, NetworkSetup1Activity.class));
            cur.close();
            return;
        }

        ArrayList<DeviceInformation> homeList = new ArrayList<>(dbCount);
        while (!cur.isAfterLast()) {
            DeviceInformation information = new DeviceInformation();
            String homeName = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HOME));
            String location = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_LOCATION));
            if (homeName == null) {
                homeName = "";
            }
            if (location == null) {
                location = "";
            }
            information.setHome(homeName);
            information.setLocation(location);
            homeList.add(information);
            cur.moveToNext();
        }

        cur.close();

        ArrayList<String> groupList = new ArrayList<>();

        ArrayList<String> groupListLocation = new ArrayList<>();


        for (DeviceInformation deviceInformation : homeList) {
            if (groupList.contains(deviceInformation.getHome()) && groupListLocation.contains(deviceInformation.getLoacation())) {

            } else {
                groupList.add(deviceInformation.getHome());
                groupListLocation.add(deviceInformation.getLoacation());
                Logger.e(TAG, "home:" + deviceInformation.getHome() + " city:" + deviceInformation.getLoacation());
                mDeviceInfoList.add(deviceInformation);

            }

        }

        for (int i = 0; i < mDeviceInfoList.size(); i++) {
            setChildItemData(i, mDeviceInfoList.get(i).getHome(), mDeviceInfoList.get(i).getLoacation());
        }

        mAdapter.setGroupTitle(mDeviceInfoList);
        mAdapter.setChildMap(childMap);
        mAdapter.notifyDataSetChanged();

        int groupCount = mDeviceList.getCount();
        for (int i = 0; i < groupCount; i++) {
            mDeviceList.expandGroup(i);
        }
    }

    private void setDataReportFrequency(final String deviceId) {

        IAQRequestUtils.setDataReportFrequency(this, deviceId, callback);

    }

    final AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Log.d(TAG, "setDataReportFrequency: onSuccess");
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            if (responseBody != null) {
                String responseStr = new String(responseBody, 0, responseBody.length);
                Log.d(TAG, "setDataReportFrequency: onFailure: responseStr=" + responseStr);
            }
        }
    };

    private void setChildItemData(int index, String homeName, String location) {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_HOME + "=?" + " and " + IAQ.BindDevice.COLUMN_LOCATION + "=?";
        String[] selectionArgs = new String[]{account, homeName, location};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        List<ChildItem> childItems = new ArrayList<>();
        while (!cur.isAfterLast()) {
            ChildItem childItem = new ChildItem(R.mipmap.circle1, R.mipmap.icon_tep, R.mipmap.icon_humidity);
            childItem.setRoom(cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ROOM)));
            int onlineStatus = cur.getInt(cur.getColumnIndex(IAQ.BindDevice.COLUMN_ONLINE_STATUS));
            if (onlineStatus == Constants.DEVICE_ONLINE) {
                String temperature = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE));
                if (temperature == null || temperature.length() == 0) {
                    temperature = getString(R.string.unknown);
                }
                childItem.setTemperature(temperature);

                String room = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ROOM));
                String temperatureUnit = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TEMPERATURE_UNIT));
                Map<String, String> tempMap = new HashMap<>();
                tempMap.put(room, temperatureUnit);
                childItem.setTemperatureUnit(tempMap);

                String humidity = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HUMIDITY));
                if (humidity == null || humidity.length() == 0) {
                    humidity = getString(R.string.unknown);
                }
                childItem.setHumidity(humidity);

                String tvoc = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_TVOC));
                String hcho = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HCHO));
                String co2 = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_CO2));

                String pm25 = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_PM25));
                String pmStatus = getString(R.string.getting_data);
                if (pm25 == null || pm25.length() == 0) {
                    if (Utils.isNetworkAvailable(getApplicationContext())) {
                        pmStatus = getString(R.string.getting_data);
                    } else {
                        pmStatus = getString(R.string.get_data_fail);
                    }
                    pm25 = "";
                } else {
                    try {
                        pmStatus = Utils.getPMStatus(getApplicationContext(), Integer.parseInt(pm25));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "setChildItemData: PM25=" + pm25);
                childItem.setPm25(pm25);
                childItem.setPmStatus(pmStatus);
                if (pm25.length() == 0) {
                    childItem.setPmLevel(Constants.PM_LEVEL_UNKNOWN);
                } else {
                    try {
                        if (Utils.getPMLevel(getApplicationContext(), Integer.parseInt(pm25)) == Constants.PM_LEVEL_1) {
                            childItem.setPmLevel(Constants.PM_LEVEL_1);
                        } else if (Utils.getPMLevel(getApplicationContext(), Integer.parseInt(pm25)) == Constants.PM_LEVEL_2) {
                            childItem.setPmLevel(Constants.PM_LEVEL_2);
                        } else if (Utils.getPMLevel(getApplicationContext(), Integer.parseInt(pm25)) == Constants.PM_LEVEL_3) {
                            childItem.setPmLevel(Constants.PM_LEVEL_3);
                        } else if (Utils.getPMLevel(getApplicationContext(), Integer.parseInt(pm25)) == Constants.PM_LEVEL_4) {
                            childItem.setPmLevel(Constants.PM_LEVEL_4);
                        } else if (Utils.getPMLevel(getApplicationContext(), Integer.parseInt(pm25)) == Constants.PM_LEVEL_5) {
                            childItem.setPmLevel(Constants.PM_LEVEL_5);
                        } else if (Utils.getPMLevel(getApplicationContext(), Integer.parseInt(pm25)) == Constants.PM_LEVEL_6) {
                            childItem.setPmLevel(Constants.PM_LEVEL_6);
                        } else {
                            childItem.setPmLevel(Constants.PM_LEVEL_UNKNOWN);
                        }

                        if (!isHCHOLevelNormal(hcho) || !isCO2LevelNormal(co2) || !isTVOCLevelnormal(tvoc)) {
                            childItem.setPmLevel(Constants.PM_LEVEL_5);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                childItem.setTemperature(getString(R.string.unknown));
                childItem.setHumidity(getString(R.string.unknown));
                childItem.setPm25("");
                childItem.setPmStatus(getString(R.string.iaq_disconnect));
                childItem.setPmLevel(Constants.PM_LEVEL_UNKNOWN);
            }
            childItem.setSerialNum(cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER)));
            childItem.setDeviceId(cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID)));
            childItems.add(childItem);

            cur.moveToNext();
        }

        cur.close();

        childMap.put(index, childItems);
    }

    private boolean isDeviceInfoComplete(String currentSerialNum) {
        String roomName = null;
        String homeName = null;
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        Log.d(TAG, "getDeviceInformation: serialNum=" + currentSerialNum);
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, currentSerialNum};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            roomName = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ROOM));
            homeName = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_HOME));
            Log.d(TAG, "Room=" + roomName + ", home=" + homeName);

            cur.moveToNext();
        }
        cur.close();
        if (TextUtils.isEmpty(roomName) || TextUtils.isEmpty(homeName)) {
            return false;
        }
        return true;
    }

    public void openLeftMenu() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                Gravity.LEFT);
    }

    public void setListener() {
        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                View mContent = mDrawerLayout.getChildAt(0);
                View mMenu = drawerView;
                float scale = 1 - slideOffset;
                float rightScale = 0.8f + scale * 0.2f;

                if (drawerView.getTag().equals("START")) {

                    ViewHelper.setAlpha(mMenu, 0.6f + 0.4f * (1 - scale));
                    ViewHelper.setTranslationX(mContent, mMenu.getMeasuredWidth() * (1 - scale));
                    ViewHelper.setPivotX(mContent, 0);
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                } else {
                    ViewHelper.setTranslationX(mContent, -mMenu.getMeasuredWidth() * slideOffset);
                    ViewHelper.setPivotX(mContent, mContent.getMeasuredWidth());
                    ViewHelper.setPivotY(mContent, mContent.getMeasuredHeight() / 2);
                    mContent.invalidate();
                }

            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            }
        });
    }


    @Override
    public void onEventMainThread(IAQEvents event) {
        super.onEventMainThread(event);
        if (event instanceof IAQTemperatureEvent) {
            //先清空数据库
//            removeAccountDataFromDB();
            getDatas();
        }
    }


    private boolean isHCHOLevelNormal(String hchoValue) {

        if (android.text.TextUtils.isEmpty(hchoValue)) {
            hchoValue = "0";
        }

        try {
            int hchoLevel = Utils.getHCHOLevel(Float.parseFloat(hchoValue));
            if (hchoLevel == Constants.HCHO_NORMAL) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return true;
        }

    }

    private boolean isCO2LevelNormal(String co2Value) {
        if (android.text.TextUtils.isEmpty(co2Value)) {
            co2Value = "0";
        }
        try {
            int co2Level = Utils.getCO2Level(Integer.parseInt(co2Value));
            if (co2Level == Constants.CO2_NORMAL) {
                return true;
            } else {
                return false;

            }
        } catch (NumberFormatException e) {
            return true;
        }

    }

    private boolean isTVOCLevelnormal(String tvocValue) {
        if (android.text.TextUtils.isEmpty(tvocValue)) {
            tvocValue = "0";
        }
        try {
            int tvocLevel = Utils.getTVOCLevel(Float.parseFloat(tvocValue));
            if (tvocLevel == Constants.TVOC_NORMAL) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return true;
        }

    }

    private void unbindDevice(final String serialNum, String deviceId) {
        showLoadingDialog();

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_UNBIND_DEVICE);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(DashboardActivity.this, Constants.BIND_DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(DashboardActivity.this, Constants.GetDataFlag.HON_IAQ_UNBIND_DEVICE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode == 0) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            removeDeviceFromDb(serialNum);
                            Intent intent = new Intent(DashboardActivity.this, DashboardActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 1000);
                } else {
                    if (objects != null) {
                        String responseStr = (String) objects[0];
                        if (responseStr.contains(Constants.KEY_ERROR_TYPE)) {
                            JSONObject jsonObject = new JSONObject(responseStr);
                            String errorType = jsonObject.getString(Constants.KEY_ERROR_TYPE);
                            if (errorType != null) {
                                if (errorType.equals(Constants.ERROR_TYPE_DEVICE_NOT_BIND)) {
                                    Utils.showToast(DashboardActivity.this, getString(R.string.remove_iaq_fail_2));
                                } else {
                                    Utils.showToast(DashboardActivity.this, getString(R.string.remove_iaq_fail));
                                }
                            } else {
                                Utils.showToast(DashboardActivity.this, getString(R.string.remove_iaq_fail));
                            }
                        } else {
                            Utils.showToast(DashboardActivity.this, getString(R.string.remove_iaq_fail));
                        }
                    } else {
                        Utils.showToast(DashboardActivity.this, getString(R.string.remove_iaq_fail));
                    }

                }
            }
        }));
    }

    private void removeDeviceFromDb(String serialNum) {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
        String[] selectionArgs = new String[]{account, serialNum};
        getContentResolver().delete(IAQ.BindDevice.DICT_CONTENT_URI, selection, selectionArgs);
    }

    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final long packedPosition = mDeviceList.getExpandableListPosition(position);
            final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
            final int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
            if (childPosition != -1) {
                final String serialNum = childMap.get(groupPosition).get(childPosition).getSerialNum();
                final String deviceId = childMap.get(groupPosition).get(childPosition).getDeviceId();

                AlertDialog mAlertDialog = new AlertDialog.Builder(DashboardActivity.this).setTitle(getString(R.string.iaq_settings)).setMessage(getString(R.string.confirm_remove_iaq)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Utils.isNetworkAvailable(DashboardActivity.this)) {
                            unbindDevice(serialNum, deviceId);
                        } else {
                            Utils.showToast(DashboardActivity.this, getString(R.string.no_network));
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
                mAlertDialog.show();

            }
            return true;
        }
    };

}
