package com.honeywell.iaq.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeywell.iaq.AndroidBug5497Workaround;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by E570281 on 8/17/2016.
 */
public class NetworkSetup5Activity extends IAQTitleBarActivity {

    private static final String TAG = NetworkSetup5Activity.class.getSimpleName();

    private android.support.v7.widget.Toolbar mToolbar;

    private Button mNext;

    private ProgressDialog mDialog;

    private WifiManager mWifiManager;

    private static final int RECEIVE_PORT = 5350;

    private NetworkSetupHandler mHandler;

    private static final int CONNECT_WIFI_SUCCESS = 0;

    private static final int BIND_SUCCESS = 1;

    private static final int BIND_FAIL = 2;

    private static final int CONNECT_WIFI_TIME_OUT = 3;

    private static final int BIND_IAQ = 4;

    private static final int TIMER_COUNT = 60;

    private static final int SET_WIFI_REQUEST_CODE = 123;

    private CountDownTimer timer;

    private String connectedWifiSSID;
    private TextView mTextView;

    static class NetworkSetupHandler extends Handler {
        private WeakReference<NetworkSetup5Activity> mActivityContent;

        private NetworkSetup5Activity mActivity;

        public NetworkSetupHandler(NetworkSetup5Activity activity) {
            mActivityContent = new WeakReference<>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_WIFI_SUCCESS:
                    mActivity.mDialog.setMessage(mActivity.getString(R.string.iaq_connect_wifi_success));
                    mActivity.timer.cancel();
                    sendEmptyMessageDelayed(BIND_IAQ, 3000);
                    break;
                case BIND_SUCCESS:
                    if (mActivity.mDialog != null) {
                        mActivity.mDialog.dismiss();
                    }

                    Intent intent = new Intent(mActivity, NameIAQActivity.class);
                    mActivity.startActivity(intent);
                    break;
                case BIND_FAIL:
                    if (mActivity.mDialog != null) {
                        mActivity.mDialog.dismiss();
                    }
                    Utils.showToast(mActivity, mActivity.getString(R.string.bind_device_fail));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected int getContent() {
        return R.layout.activity_network_setup5;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.connect_phone_iaq);
    }

    @Override
    protected void initView() {
        super.initView();
//        AndroidBug5497Workaround.assistActivity(this);
        mTextView = (TextView) findViewById(R.id.title_text2);
        String str = this.getResources().getString(R.string.find_iaq_wireless);
        String strTag = this.getResources().getString(R.string.find_iaq_wireless_tag);
        SpannableString spannableString = new SpannableString(str);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.body_text_blue)),str.indexOf(strTag),str.indexOf(strTag)+strTag.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTextView.setText(spannableString);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mHandler = new NetworkSetupHandler(this);
        mNext = (Button) findViewById(R.id.btn_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), SET_WIFI_REQUEST_CODE);
                    }
                }).start();

            }
        });

        mDialog = new ProgressDialog(this);
        mDialog.setTitle(getString(R.string.iaq_network));
        mDialog.setCanceledOnTouchOutside(false);

        Utils.setListenerToRootView(this, R.id.activity_network_setup5, mNext);
    }

    @Override
    public void onResume() {
        super.onResume();
        connectedWifiSSID = "";
        if (Utils.isWifiConnected(this)) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null && info.getSSID() != null) {
                connectedWifiSSID = Utils.removeDoubleQuotes(info.getSSID());
                Log.d(TAG, "Connected wifi SSID=" + connectedWifiSSID);
            } else {
                Utils.showToast(getApplicationContext(), getString(R.string.no_connect_wifi));
            }
        } else {
            Utils.showToast(getApplicationContext(), getString(R.string.no_connect_wifi));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult=" + requestCode);
        switch (requestCode) {
            case SET_WIFI_REQUEST_CODE:
                WifiInfo info = mWifiManager.getConnectionInfo();
                if (info != null && info.getSSID() != null) {
                    String connectedApSSID = Utils.removeDoubleQuotes(info.getSSID());
                    Log.d(TAG, "Connected wifi SSID=" + connectedApSSID);
                    if (Constants.DEFAULT_IAQ_WIFI_SSID.equals(connectedApSSID)) {

//                        Utils.startServiceByAction(getApplicationContext(), Constants.ACTION_DISCONNECT);

                        Intent intent = new Intent(NetworkSetup5Activity.this, NetworkSetup6Activity.class);
                        intent.putExtra(Constants.KEY_WIFI_SSID, connectedWifiSSID);
                        startActivity(intent);
                    } else {
                        showConnectIAQFailDialog();
                    }
                } else {
                    showConnectIAQFailDialog();
                }
                break;
            default:
                break;
        }
    }

    private void showConnectIAQFailDialog() {
        AlertDialog mAlertDialog = new AlertDialog.Builder(NetworkSetup5Activity.this).setTitle(getString(R.string.iaq_network)).setMessage(getString(R.string.connect_iaq_fail_message)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Utils.getSharedPreferencesValue(NetworkSetup5Activity.this, Constants.KEY_SELECT_GEN, Constants.GEN_1).equals(Constants.GEN_1)) {
                    startActivity(new Intent(NetworkSetup5Activity.this, NetworkSetup4Activity.class));
                } else {
                    finish();
                }
            }
        }).create();
        mAlertDialog.show();
    }

}
