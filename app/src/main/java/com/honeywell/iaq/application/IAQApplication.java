package com.honeywell.iaq.application;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.text.TextUtils;

import com.honeywell.iaq.base.IAQBaseActivity;
import com.honeywell.iaq.base.IAQBaseApplication;
import com.honeywell.iaq.events.IAQEnvironmentDetailEvent;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.lib.utils.LogUtil;
import com.honeywell.net.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class IAQApplication extends IAQBaseApplication {
    private static final String TAG = IAQApplication.class.getSimpleName();
    private static IAQApplication instance;
    protected BroadcastReceiver networkBroadcast;

    private List<Activity> activities = new ArrayList<Activity>();

    public void addActivity(Activity activity) {

        activities.add(activity);
        Logger.e(TAG, "activities.size" + activities.size());
    }

    public void removeActivity(Activity activity) {
        if (activities.contains(activity)) {
            Logger.e(TAG, "activity实例" + activity);
            activities.remove(activity);
            ;
        }
    }

    public static IAQApplication getInstance() {
        if (null == instance) {
            instance = new IAQApplication();
        }
        return instance;
    }

    public IAQApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerNetworkReceiver();
        instance = this;
//        appContext = this;
        //工具类的初始化

    }


    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        LogUtil.d("IAQApplication", "onTerminate");
        super.onTerminate();
        unRegisterNetworkReceiver();
        for (Activity activity : activities) {
            activity.finish();
        }
        System.exit(0);
    }

    private void registerNetworkReceiver() {
        networkBroadcast = new NetWorkBroadcastReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(networkBroadcast, filter);
    }

    private void unRegisterNetworkReceiver() {
        if (networkBroadcast != null) {
            this.unregisterReceiver(networkBroadcast);
            networkBroadcast = null;
        }
    }

    private class NetWorkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {

                if (Utils.isNetworkAvailable(getApplicationContext())) {
                    Logger.d("IAQBaseActivity", "--有网络-——");
                    String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
                    if (TextUtils.isEmpty(cookie)) {
                        return;
                    }
                    Logger.d("IAQBaseActivity", "start service open wss");
                    Utils.startServiceByAction(context, Constants.ACTION_OPEN_WSS);
//                    Utils.startServiceByAction(IAQBaseActivity.this, Constants.ACTION_OPEN_WSS);
//                    EventBus.getDefault().post(new IAQNetChangeEvent(IAQNetChangeEvent.NET_CONNECT));
                } else {
                    Logger.d("IAQBaseActivity", "--无网络——-");
                    EventBus.getDefault().post(new IAQEnvironmentDetailEvent(IAQEnvironmentDetailEvent.GET_DATA_FAIL, true, null));
                    Utils.startServiceByAction(context, Constants.ACTION_DISCONNECT);
                }
            }

        }
    }
}
