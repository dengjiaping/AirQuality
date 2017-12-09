package com.honeywell.iaq.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.honeywell.iaq.R;
import com.honeywell.iaq.application.IAQApplication;
import com.honeywell.iaq.events.IAQEnvironmentDetailEvent;
import com.honeywell.iaq.events.IAQNetChangeEvent;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.utils.Logger;

import de.greenrobot.event.EventBus;


public class IAQBaseActivity extends AppCompatActivity {
    protected int activityCloseEnterAnimation;

    protected int activityCloseExitAnimation;

    protected BroadcastReceiver networkBroadcast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme();
        initAnimation();
        Logger.e("************","注册广播");
//        registerNetworkReceiver();
        IAQApplication.getInstance().addActivity(this);
    }

    protected void setTheme() {
        setTheme(R.style.IAQBaseTheme);
    }

    protected void setOtherTheme() {
        setTheme(R.style.IAQOtherBaseTheme);
        initAnimation();
    }
    public void initAnimation() {
        TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowAnimationStyle
        });

        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);

        activityStyle.recycle();

        activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId, new int[]{
                android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation
        });

        activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);

        activityCloseExitAnimation = activityStyle.getResourceId(1, 0);

        activityStyle.recycle();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getBooleanExtra(Constants.FINISH_ALL, false)) {
            finishAll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.e("************","反注册广播");
//        unRegisterNetworkReceiver();
        IAQApplication.getInstance().removeActivity(this);
    }

    private void registerNetworkReceiver(){
        networkBroadcast = new NetWorkBroadcastReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(networkBroadcast, filter);
    }
    private void unRegisterNetworkReceiver(){
        if (networkBroadcast != null) {
            this.unregisterReceiver(networkBroadcast);
            networkBroadcast = null;
        }
    }

    public void finishAll() {
        Intent intent = new Intent();
        intent.putExtra(Constants.FINISH_ALL, true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private class NetWorkBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
             if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {

                if(Utils.isNetworkAvailable(IAQBaseActivity.this)){
                    Logger.d("IAQBaseActivity","--有网络-——");
                    String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
                    if(TextUtils.isEmpty(cookie)){
                        return;
                    }
                    Logger.d("IAQBaseActivity","start service open wss");
                    Utils.startServiceByAction(context,Constants.ACTION_OPEN_WSS);
//                    Utils.startServiceByAction(IAQBaseActivity.this, Constants.ACTION_OPEN_WSS);
//                    EventBus.getDefault().post(new IAQNetChangeEvent(IAQNetChangeEvent.NET_CONNECT));
                }else {
                    Logger.d("IAQBaseActivity","--无网络——-");
                    EventBus.getDefault().post(new IAQEnvironmentDetailEvent(IAQEnvironmentDetailEvent.GET_DATA_FAIL,true,null));
                    Utils.startServiceByAction(context,Constants.ACTION_DISCONNECT);
                }
            }

        }
    }

}
