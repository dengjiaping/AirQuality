package com.honeywell.iaq.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.smartlink.control.Gen2SetupActivity;
import com.honeywell.iaq.smartlink.control.SmartLinkSetupActivity;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.db.IAQ;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Jin on 8/17/2016.
 */
public class NetworkSetup1Activity extends IAQTitleBarActivity implements View.OnClickListener {

    private static final String TAG = "NetworkSetup1";
    private static final String NET_WORK_ERROR = "NetworkError";
    private static final String NETWORK_CONNECT_SERVER_WELL = "NetworkWell";

    private ImageView mGen1ImageView;
    private ImageView mGen2ImageView;
    private ImageView mChooseGen1ImageView;
    private ImageView mChooseGen2ImageView;
    private TextView mChooseGen1TextView;
    private TextView mChooseGen2TextView;
    private TextView mWifiNotConnectTextView;
    private Button mNextButton;
    private boolean mIsChooseGen2 = false;

    private DrawerLayout mDrawerLayout;

    private static final int HAD_BOUND = 0;

    private static final int NO_BIND = 1;

    private boolean hasBound;

    private NetworkSetup1Handler mHandler;

    private NetWorkBroadcastReceiver networkBroadcast;


    static class NetworkSetup1Handler extends Handler {
        private WeakReference<NetworkSetup1Activity> mActivityContent;

        private NetworkSetup1Activity mActivity;

        public NetworkSetup1Handler(NetworkSetup1Activity activity) {
            mActivityContent = new WeakReference<>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HAD_BOUND:
                    mActivity.hasBound = true;
                    mActivity.setBackArrow();
                    break;
                case NO_BIND:
                    mActivity.hasBound = false;
                    mActivity.setAddIAQ();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected int getContent() {
        return R.layout.activity_network_setup1;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.config_iaq);
    }

    @Override
    protected void initView() {
        super.initView();
        mHandler = new NetworkSetup1Handler(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_layout);

        mGen1ImageView = (ImageView) findViewById(R.id.gen_1_iv);
        mGen2ImageView = (ImageView) findViewById(R.id.gen_2_iv);
        mChooseGen1ImageView = (ImageView) findViewById(R.id.choose_gen_1_iv);
        mChooseGen2ImageView = (ImageView) findViewById(R.id.choose_gen_2_iv);
        mChooseGen1TextView = (TextView) findViewById(R.id.choose_gen_1_tv);
        mChooseGen2TextView = (TextView) findViewById(R.id.choose_gen_2_tv);
        mWifiNotConnectTextView = (TextView) findViewById(R.id.wifi_not_open_tv);
        mNextButton = (Button) findViewById(R.id.btn_connect);

        mChooseGen1ImageView.setBackgroundResource(R.mipmap.ic_radio_selected);
        mChooseGen2ImageView.setBackgroundResource(R.mipmap.ic_radio_nomal);

        mGen1ImageView.setOnClickListener(this);
        mGen2ImageView.setOnClickListener(this);
        mChooseGen1ImageView.setOnClickListener(this);
        mChooseGen2ImageView.setOnClickListener(this);
        mChooseGen1TextView.setOnClickListener(this);
        mChooseGen2TextView.setOnClickListener(this);
        mNextButton.setOnClickListener(this);

        isAccountBound();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWifiNotConnectTextView.setVisibility(Utils.isWifiConnected(this) ? View.INVISIBLE : View.VISIBLE);
        mNextButton.setEnabled(Utils.isWifiConnected(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerNetworkReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unRegisterNetworkReceiver();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("NetworkSetup1", "onKeyDown: hasBound=" + hasBound);
        if (hasBound) {
            doExit();
        } else {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SEARCH) {
                showExitDialog();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void isAccountBound() {
        Log.d(TAG, "isAccountBound");
        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?";
        String[] selectionArgs = new String[]{Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "")};
        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
        final int dbCount = cur.getCount();
        Log.d(TAG, "isAccountBound: DBCount=" + dbCount);
        if (dbCount > 0) {
            cur.moveToFirst();
            ArrayList<String> deviceIds = new ArrayList<>();
            while (!cur.isAfterLast()) {
                String deviceId = cur.getString(cur.getColumnIndex(IAQ.BindDevice.COLUMN_DEVICE_ID));
                if (deviceId != null && deviceId.length() > 0) {
                    deviceIds.add(deviceId);
                }

                cur.moveToNext();
            }

            if (deviceIds.size() > 0) {
                mHandler.sendEmptyMessage(HAD_BOUND);
            } else {
                mHandler.sendEmptyMessage(NO_BIND);
            }
        } else {
            mHandler.sendEmptyMessage(NO_BIND);
        }
        cur.close();
    }

    private void setBackArrow() {
        mLeft.setImageResource(R.mipmap.ic_arrow_back_white);
        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doExit();
            }
        });
    }

    private void setAddIAQ() {
        mLeft.setImageResource(R.mipmap.icon_menu);

        mLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLeftMenu();
            }
        });
    }

    public void openLeftMenu() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
    }

    private void doExit() {
        if (Utils.getDeviceCount(NetworkSetup1Activity.this) > 1) {
            Intent intent = new Intent(NetworkSetup1Activity.this, DashboardActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(NetworkSetup1Activity.this, HomeActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == mChooseGen1ImageView || v == mChooseGen1TextView || v == mGen1ImageView) {
            if (mIsChooseGen2) {
                mIsChooseGen2 = false;
                mChooseGen1ImageView.setBackgroundResource(R.mipmap.ic_radio_selected);
                mChooseGen2ImageView.setBackgroundResource(R.mipmap.ic_radio_nomal);
            }
        } else if (v == mChooseGen2ImageView || v == mChooseGen2TextView || v == mGen2ImageView) {
            if (!mIsChooseGen2) {
                mIsChooseGen2 = true;
                mChooseGen1ImageView.setBackgroundResource(R.mipmap.ic_radio_nomal);
                mChooseGen2ImageView.setBackgroundResource(R.mipmap.ic_radio_selected);
            }
        } else if (v == mNextButton) {
            if (mIsChooseGen2) {
                Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_SELECT_GEN, Constants.GEN_2);
//                Intent intent = new Intent(NetworkSetup1Activity.this, SmartLinkSetupActivity.class);
                Intent intent = new Intent(NetworkSetup1Activity.this, Gen2SetupActivity.class);
                startActivity(intent);
            } else {
                Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_SELECT_GEN, Constants.GEN_1);
                Intent intent = new Intent(NetworkSetup1Activity.this, NetworkSetup2Activity.class);
                startActivity(intent);
            }
        }
    }


    private void registerNetworkReceiver() {
        NetWorkBroadcastReceiver networkBroadcast = new NetWorkBroadcastReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(NET_WORK_ERROR);
        filter.addAction(NETWORK_CONNECT_SERVER_WELL);
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
        public void onReceive(Context context, final Intent intent) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String action = intent.getAction();
                    if (NET_WORK_ERROR.equals(action) || ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                            || NETWORK_CONNECT_SERVER_WELL.equals(action)) {
                        mWifiNotConnectTextView.setVisibility(Utils.isWifiConnected(NetworkSetup1Activity.this)
                                ? View.INVISIBLE : View.VISIBLE);
                        mNextButton.setEnabled(Utils.isWifiConnected(NetworkSetup1Activity.this));
                    }

                }
            });

        }
    }

}
