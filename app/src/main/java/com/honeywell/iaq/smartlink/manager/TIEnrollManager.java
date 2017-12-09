package com.honeywell.iaq.smartlink.manager;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.network.NetworkUtil;
import com.honeywell.iaq.utils.network.SecurityType;
import com.honeywell.iaq.utils.network.WifiNetworkUtils;
import com.integrity_project.smartconfiglib.SmartConfig;
import com.integrity_project.smartconfiglib.SmartConfigListener;


import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Jin on 04/09/2017.
 */

public class TIEnrollManager {

    private static final String TAG = "TIEnrollManager";

    private Activity mActivity;

    private static TIEnrollManager mTIEnrollManager;
    private static final long CONNECTION_TIMEOUT = 80000;

    SmartConfig smartConfig;
    SmartConfigListener smartConfigListener;
    final Handler handlerforTransmit = new Handler();
    public UdpBcastServer udpBcastServer;

    private String mSsidToAdd;
    private String mSsidToAddSecurityKey;
    private SecurityType mSsidToAddSecurityType;


    private ErrolCallback mErrolCallback;
    private SuccessCallback mSuccessCallback;

    public TIEnrollManager(Activity activity) {
        this.mActivity = activity;
    }

    public interface ErrolCallback {
        void onError(String msg);
    }

    public interface SuccessCallback {
        void onSuccess(String serial, String token);
    }

    public void setErrolCallback(ErrolCallback errolCallback) {
        this.mErrolCallback = errolCallback;
    }

    public void setSuccessCallback(SuccessCallback successCallback) {
        this.mSuccessCallback = successCallback;
    }


    public static TIEnrollManager getInstance(Activity activity) {
        if (mTIEnrollManager == null) {
            mTIEnrollManager = new TIEnrollManager(activity);
        }
        return mTIEnrollManager;
    }


    public void startSmartLink(Context context, String ssid, String password) {

        handlerforTransmit.removeCallbacks(smartConfigRunner);

        smartConfigListener = new SmartConfigListener() {
            @Override
            public void onSmartConfigEvent(SmartConfigListener.SmtCfgEvent event, Exception e) {
            }
        };

        try {
            try {
                smartConfig = new SmartConfig(smartConfigListener, null, password, null,
                        NetworkUtil.getGateway(context), ssid, (byte) 0, "");
            } catch (SocketException e) {
                Log.e(TAG, "Failed to create instance of smart config");
                if (mErrolCallback != null)
                    mErrolCallback.onError(mActivity.getString(R.string.bind_device_fail));
                return;
            }

            Log.i(TAG, "Broadcasting information to network");
            smartConfig.transmitSettings();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start smart config " + e.getMessage());
            if (mErrolCallback != null)
                mErrolCallback.onError(mActivity.getString(R.string.bind_device_fail));
            return;
        }

        handlerforTransmit.postDelayed(smartConfigRunner, CONNECTION_TIMEOUT);

    }


    public void startAp(String ssid, String password) {

        handlerforTransmit.removeCallbacks(smartConfigRunner);

        mSsidToAdd = ssid;
        mSsidToAddSecurityKey = password;
        mSsidToAddSecurityType = SecurityType.UNKNOWN;

        ArrayList<Object> passing = new ArrayList<Object>();
        passing.add("");
        passing.add(mSsidToAddSecurityType);
        passing.add(mSsidToAdd);
        passing.add(mSsidToAddSecurityKey);
        passing.add("0");
        passing.add("");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            new AddProfileAsyncTask(mAddProfileAsyncTaskCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, passing);
        } else {
            new AddProfileAsyncTask(mAddProfileAsyncTaskCallback).execute(passing);
        }

        handlerforTransmit.postDelayed(smartConfigRunner, CONNECTION_TIMEOUT);

    }

    private Runnable smartConfigRunner = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "running smartConfigRunner");

            try {
                smartConfig.stopTransmitting();
                Log.e(TAG, "Broadcasting information to network timeout");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mErrolCallback != null)
                mErrolCallback.onError(mActivity.getString(R.string.bind_device_fail));
        }
    };

    private AddProfileAsyncTask.AddProfileAsyncTaskCallback mAddProfileAsyncTaskCallback = new AddProfileAsyncTask.AddProfileAsyncTaskCallback() {
        @Override
        public void addProfileMsg(String errorMessage) {
            Log.e(TAG, errorMessage);
        }

        @Override
        public void addProfileFailed(String errorMessage) {
            Log.e(TAG, errorMessage);
            try {
                smartConfig.stopTransmitting();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mErrolCallback != null)
                mErrolCallback.onError(mActivity.getString(R.string.bind_device_fail));
        }

        @Override
        public void addProfileCompleted() {

            Log.i(TAG, "Connecting to " + mSsidToAdd + " in order to confirm device configuration has succeeded.\n DO NOT ABORT!");
            WifiNetworkUtils.getInstance(mActivity).clearCallback();
            WifiConfiguration configuration = NetworkUtil.getWifiConfigurationWithInfo(mActivity, mSsidToAdd, mSsidToAddSecurityType, mSsidToAddSecurityKey);
            WifiNetworkUtils.getInstance(mActivity).connectToWifi(configuration, mActivity, new WifiNetworkUtils.BitbiteNetworkUtilsCallback() {
                @Override
                public void successfullyConnectedToNetwork(String ssid) {
                    Log.e(TAG, "==============Connected to " + mSsidToAdd + ". Searching for new devices");
//                    restartUdp();
                }

                @Override
                public void failedToConnectToNetwork(WifiNetworkUtils.WifiConnectionFailure failure) {

                    String errorMsg = "";

                    switch (failure) {
                        case Connected_To_3G:
                            errorMsg = "There was 3G error connecting to the network";
                            Log.e(TAG, errorMsg);
                            break;
                        case Timeout:
                            errorMsg = "There was timeout connecting to the network";
                            Log.e(TAG, errorMsg);
                            break;
                        case Unknown:
                            errorMsg = "There was an unknown error connecting to the network";
                            Log.e(TAG, errorMsg);
                            break;
                        case Wrong_Password:
                            errorMsg = "The password you entered for the network is wrong please try again";
                            Log.e(TAG, errorMsg);
                            break;
                    }
                    try {
                        smartConfig.stopTransmitting();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mErrolCallback != null)
                        mErrolCallback.onError(mActivity.getString(R.string.bind_device_fail));
                }
            }, true);
        }

        @Override
        public void addProfileDeviceNameFetched(String deviceName) {

        }
    };



    public void restartUdp() {
        Log.e("GGG", "UDP restart");

        if (udpBcastServer != null) {
            udpBcastServer.cancel(true);
            udpBcastServer = null;
        }

        udpBcastServer = new UdpBcastServer(mPingCallback);
        udpBcastServer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private UdpBcastServer.PingCallback mPingCallback = new UdpBcastServer.PingCallback() {
        @Override
        public void pingDeviceFetched(String serial, String token) {
            Log.e(TAG, "Device was found via Bcast : " + serial + " " + token);
            handlerforTransmit.removeCallbacks(smartConfigRunner);
            try {
                smartConfig.stopTransmitting();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mSuccessCallback != null)
                mSuccessCallback.onSuccess(serial, token);
        }
    };

}
