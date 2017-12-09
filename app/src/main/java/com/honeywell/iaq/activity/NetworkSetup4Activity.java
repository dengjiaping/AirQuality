package com.honeywell.iaq.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.Crc8PolynomialD5;
import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by E570281 on 8/17/2016.
 */
public class NetworkSetup4Activity extends IAQTitleBarActivity {

    private static final String TAG = "NetworkSetup4";

    private android.support.v7.widget.Toolbar mToolbar;

    private Button mNext;
    private ImageView mImageView;

    private ProgressDialog mDialog;

    private TextView mTitleGuide;

    private WifiManager mWifiManager;

//    private multiUdpSendTask mSendTask;
//
//    private multiUdpRecvTask mRecvTask;

    private static final int RECEIVE_PORT = 5350;

    private NetworkSetupHandler mHandler;

    private static final int CONNECT_WIFI_SUCCESS = 0;

    private static final int BIND_SUCCESS = 1;

    private static final int BIND_FAIL = 2;

    private static final int CONNECT_WIFI_TIME_OUT = 3;

    private static final int BIND_IAQ = 4;

    private boolean isIAQConnectWifi;

    private boolean isIAQConnectWifiTimeout;

    private static final int TIMER_COUNT = 60;

    private CountDownTimer timer;

    static class NetworkSetupHandler extends Handler {
        private WeakReference<NetworkSetup4Activity> mActivityContent;

        private NetworkSetup4Activity mActivity;

        public NetworkSetupHandler(NetworkSetup4Activity activity) {
            mActivityContent = new WeakReference<>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECT_WIFI_SUCCESS:
                    mActivity.mDialog.setMessage(mActivity.getString(R.string.iaq_connect_wifi_success));
//                    mActivity.mSendTask.cancel(true);
//                    mActivity.mRecvTask.cancel(true);
//                    mActivity.timer.cancel();
//                    sendEmptyMessageDelayed(BIND_IAQ, 3000);
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
                case CONNECT_WIFI_TIME_OUT:
                    if (mActivity.mDialog != null) {
                        mActivity.mDialog.dismiss();
                    }

//                    mActivity.showConnectWifiTimeoutDialog();
                    break;
                case BIND_IAQ:
                    Log.d(TAG, "BIND_IAQ");
//                    mActivity.bindDevice();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected int getContent() {
        return R.layout.activity_network_setup4;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.start_configure_mode);
    }

    @Override
    protected void initView() {
        super.initView();
//        AndroidBug5497Workaround.assistActivity(this);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mHandler = new NetworkSetupHandler(this);

        mNext = (Button) findViewById(R.id.btn_next);
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showWifiChannelDialog();
                Intent intent = new Intent(NetworkSetup4Activity.this, NetworkSetup5Activity.class);
                startActivity(intent);
            }
        });
        mImageView = (ImageView) findViewById(R.id.btn_add);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mNext.setEnabled(true);
                mImageView.setImageResource(R.mipmap.iaq_flash);
            }
        }, 5000);

        mDialog = new ProgressDialog(this);
        mDialog.setTitle(getString(R.string.iaq_network));
        mDialog.setCanceledOnTouchOutside(false);

        mTitleGuide = (TextView) findViewById(R.id.title_text2);
        mTitleGuide.setText(getSpanable(R.string.press_iaq,
                new int[]{R.string.press_iaq_1, R.string.press_iaq_2, R.string.press_iaq_3}));

        Utils.setListenerToRootView(this, R.id.activity_network_setup4, mNext);
//        unBindDevices();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            Utils.showToast(getApplicationContext(), getString(R.string.no_network));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private void unBindDevices() {

        String serialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_CURRENT_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
        Logger.e(TAG, "unbind serialNum"+serialNum);
        String deviceId = Utils.getDeviceId(this, serialNum);

        if (TextUtils.isEmpty(deviceId)) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_UNBIND_DEVICE);
        params.put(Constants.KEY_DEVICE_ID, deviceId);
        HttpUtils.getString(this, Constants.BIND_DEVICE_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_UNBIND_DEVICE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode == 0) {
                    Logger.e(TAG, "unbind success");
                } else {
                    Logger.e(TAG, "unbind failed");
                }
            }
        }));

    }

//    private void showInputWifiPasswordDialog() {
//        LayoutInflater mInflater = LayoutInflater.from(this);
//        View view = mInflater.inflate(R.layout.input_wifi_password, null);
//        final EditText pwdInput = (EditText) view.findViewById(R.id.wifi_pwd);
//
//        AlertDialog mAlertDialog = new AlertDialog.Builder(this).setTitle(getString(R.string.iaq_network)).setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                String password = pwdInput.getText().toString();
//                sendWifiPassword(password);
//                startConnectWifiTimer();
//
//                mDialog.setMessage(getString(R.string.iaq_connect_wifi));
//                mDialog.show();
//            }
//        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // TODO Auto-generated method stub
//
//            }
//        }).create();
//        mAlertDialog.show();
//    }

//    private void startConnectWifiTimer() {
//        isIAQConnectWifiTimeout = false;
//        timer = new CountDownTimer(TIMER_COUNT * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//
//            }
//
//            @Override
//            public void onFinish() {
//                Log.d(TAG, "Timer Finish: isIAQConnectWifi=" + isIAQConnectWifi);
//                if (!isIAQConnectWifi) {
//                    isIAQConnectWifiTimeout = true;
//                    Message message = Message.obtain(mHandler, CONNECT_WIFI_TIME_OUT);
//                    mHandler.sendMessage(message);
//
//                    mSendTask.cancel(true);
//                    mRecvTask.cancel(true);
//                }
//            }
//        };
//        timer.start();
//    }
//
//
//    private void sendWifiPassword(String password) {
//        isIAQConnectWifi = false;
//        if (((mSendTask != null)) && (!mSendTask.isCancelled())) {
//            mSendTask.cancel(true);
//            mRecvTask.cancel(true);
//            Log.e(getResources().getString(R.string.app_name), "onClick " + mSendTask.isCancelled());
//        } else if (mSendTask == null) {
//            mSendTask = new multiUdpSendTask();
//            mRecvTask = new multiUdpRecvTask();
//
//            mSendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, password);
//            mRecvTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
//    }




//    private void bindDevice() {
//        mDialog.setMessage(getString(R.string.binding));
//        String cookie = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, Constants.DEFAULT_COOKIE_VALUE);
//        if (cookie.length() > 0) {
//            Map<String, String> params = new HashMap<>();
//            params.put(Constants.KEY_TYPE, Constants.TYPE_BIND_DEVICE);
//            params.put(Constants.KEY_DEVICE_SERIAL, Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER));
//            params.put(Constants.KEY_DEVICE_PASSWORD, "000");
//            AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                    String responseStr = new String(responseBody, 0, responseBody.length);
//                    Log.d(TAG, "bindDevice: responseStr=" + responseStr);
//                    if (responseStr.contains(Constants.KEY_DEVICE_ID)) {
//                        String serialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_DEVICE_SERIAL, Constants.DEFAULT_SERIAL_NUMBER);
//                        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
//                        String selection = IAQ.BindDevice.COLUMN_ACCOUNT + "=?" + " and " + IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER + "=?";
//                        String[] selectionArgs = new String[]{account, serialNum};
//                        Cursor cur = getContentResolver().query(IAQ.BindDevice.DICT_CONTENT_URI, Constants.BIND_DEVICE_PROJECTION, selection, selectionArgs, null);
//                        int count = cur.getCount();
//                        Log.d(TAG, "bindDevice: Account count=" + count);
//
//                        ContentValues cv = new ContentValues();
//                        cv.put(IAQ.BindDevice.COLUMN_ACCOUNT, account);
//                        cv.put(IAQ.BindDevice.COLUMN_DEVICE_SERIAL_NUMBER, serialNum);
//                        try {
//                            JSONObject jsonObject = new JSONObject(responseStr);
//                            cv.put(IAQ.BindDevice.COLUMN_DEVICE_ID, jsonObject.getString(Constants.KEY_DEVICE_ID));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//
//                        if (count == 0) {
//                            getContentResolver().insert(IAQ.BindDevice.DICT_CONTENT_URI, cv);
//                        } else {
//                            getContentResolver().update(IAQ.BindDevice.DICT_CONTENT_URI, cv, selection, selectionArgs);
//                        }
//
//                        cur.close();
//
//                        mHandler.sendEmptyMessage(BIND_SUCCESS);
//                    } else {
//                        mHandler.sendEmptyMessage(BIND_FAIL);
//                    }
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                    if (responseBody != null) {
//                        String responseStr = new String(responseBody, 0, responseBody.length);
//                        Log.d(TAG, "bindDevice: onFailure=" + responseStr);
//                        try {
//                            JSONObject jsonObject = new JSONObject(responseStr);
//                            Utils.showToast(getApplicationContext(), jsonObject.getString(Constants.KEY_ERROR_DETAIL));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    mHandler.sendEmptyMessageDelayed(BIND_FAIL, 2000);
//                }
//            };
//            HttpClientHelper.newInstance().httpRequest(getApplicationContext(), Constants.BIND_DEVICE_URL, params, HttpClientHelper.COOKIE, callback, HttpClientHelper.POST, cookie);
//        }
//    }

//    private void showConnectWifiTimeoutDialog() {
//        AlertDialog mAlertDialog = new AlertDialog.Builder(NetworkSetup4Activity.this).setTitle(getString(R.string.iaq_network)).setMessage(getString(R.string.iaq_connect_wifi_timeout)).setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        }).create();
//        mAlertDialog.show();
//    }

//    private class multiUdpSendTask extends AsyncTask<String, Integer, Void> {
//
//        private MulticastSocket socket = null;
//
//        /**
//         * ui task
//         */
//        @Override
//        protected void onPreExecute() {
////            ((Button)getActivity().findViewById(R.id.button_start)).setText(R.string.find_stop);
////            getActivity().findViewById(R.id.button_start).
////                    setBackgroundColor(getResources().getColor(R.color.dark));
//            Log.d("multiUdpSendTask", "Call onPreExecute");
//        }
//
//        private boolean send24BitByMultiUdp(byte first, byte second, byte third, int length) {
//            byte[] da = {(byte) 239, 0, 0, 1};
//            da[0] = (byte) 239;
//            da[1] = first;
//            da[2] = second;
//            da[3] = third;
//            length++;/* It's very important. */
//
//            Inet4Address destAddress = null;
//            try {
//                destAddress = (Inet4Address) Inet4Address.getByAddress(da);
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//                return false;
//            }
//
//            if (!destAddress.isMulticastAddress()) {
//                Log.e(getResources().getString(R.string.app_name), "ip " + destAddress.toString());
//                destAddress = null;
//                return false;
//            }
//            try {
//                if (socket == null) {
//                    socket = new MulticastSocket();
//                    socket.setTimeToLive(4);
//                    //socket.setSendBufferSize(0);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//                if (socket != null) {
//                    socket.close();
//                    socket = null;
//                }
//            }
//            if (socket != null) {
//
//                try {
//                    byte[] msg = new byte[length];
//                    DatagramPacket dp = new DatagramPacket(msg, msg.length, destAddress, 9999);
//                    socket.send(dp);
//                    //Log.e("udp", "send24BitByMultiUdp " + msg.length + " " + destAddress.toString());
//                    dp = null;
//                    msg = null;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    socket.close();
//                    socket = null;
//                    return false;
//                }
//                destAddress = null;
//            }
//            return true;
//        }
//
//        private byte[] getChacha20Key() {
//            /* this is chacha20 key. chacha20 key = md5(source key) */
//            return new byte[]{(byte) 0xC6, (byte) 0xD0, (byte) 0xFD, (byte) 0xE4, (byte) 0x6F, (byte) 0x20, (byte) 0x3D, (byte) 0x6F, (byte) 0x4D, (byte) 0x27, (byte) 0x41, (byte) 0x9B, (byte) 0x23, (byte) 0x83, (byte) 0x38, (byte) 0xEC};
//        }
//
//        private byte[] byteArrayResize(byte[] input) {
//            if (input == null) {
//                return null;
//            }
//            int length = (input.length % 3 == 0) ? (input.length) : (input.length - (input.length % 3) + 3);
//            byte[] output = new byte[length];
//            System.arraycopy(input, 0, output, 0, input.length);
//            SecureRandom sr = new SecureRandom(input);
//            switch (length - input.length) {
//                case 0:
//                    break;
//                case 1:
//                    output[input.length] = (byte) (sr.nextInt() & 0xFF);
//                    sr = null;
//                    break;
//                case 2:
//                    output[input.length] = (byte) (sr.nextInt() & 0xFF);
//                    output[input.length + 1] = (byte) (sr.nextInt() & 0xFF);
//                    sr = null;
//                    break;
//                default:
//                    break;
//            }
//            return output;
//        }
//
//        private byte[] encrypt_chacha20(String passphase) {
//            byte[] password = passphase.getBytes();
//            byte[] plaintext = new byte[password.length + 3];
//
//            //length
//            plaintext[0] = (byte) (password.length + 2);
//            //Nonce
//            do {
//                //plaintext[1] = (byte)System.currentTimeMillis();
//                plaintext[1] = 0x55;
//            } while (plaintext[1] == 0 || plaintext[1] == -1);
//            //crc8
//            plaintext[2] = 0;
//            //password
//            System.arraycopy(password, 0, plaintext, 3, password.length);
//
//            //calc crc8
//            plaintext[2] = Crc8PolynomialD5.getCrc8(plaintext, 0, plaintext.length);
//            //calc chacha20
//
//            return plaintext;
//        }
//
//        /**
//         * background task
//         *
//         * @param params
//         */
//        @Override
//        protected Void doInBackground(String... params) {
//            WifiManager.MulticastLock lock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).
//                    createMulticastLock(getResources().getString(R.string.app_name));
//            byte[] passwordData = encrypt_chacha20(params[0]);
//            if (passwordData == null) {
//                Log.e(TAG, "encrypt passwordData fail");
//                return null;
//            }
//            //decryptAES128(passwdData);//for test
//            passwordData = byteArrayResize(passwordData);
//            boolean exceptionFlag = false;
//            lock.acquire();
//            if (lock.isHeld()) {
//                long endTime = System.currentTimeMillis() + 60 * 1000;
//                while (System.currentTimeMillis() < endTime) {
//                    if (isCancelled() || exceptionFlag) {
//                        break;
//                    }
//
//                    for (int j = 0; j < 4; j++) {
//                        if (isCancelled() || exceptionFlag) {
//                            break;
//                        }
//                        if (!send24BitByMultiUdp((byte) 0x7e, (byte) 0x6e, (byte) 0x6e, j)) {
//                            exceptionFlag = true;
//                        }
//                        try {
//                            Thread.sleep(1);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                            exceptionFlag = true;
//                        }
//                    }
//                    /*
//                    if (!send24BitByMultiUdp((byte) 0x6e, (byte) 0x6e, (byte) 0x6e, passedLength)) {
//                        exceptionFlag = true;
//                    }
//                    try {
//                        Thread.sleep(1);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        exceptionFlag = true;
//                    }
//                    */
//
//                    for (int i = 0; i < passwordData.length; i += 3) {
//                        if (isCancelled() || exceptionFlag) {
//                            break;
//                        }
//                        if (!send24BitByMultiUdp((byte) (0x40 + i / 3), passwordData[i], passwordData[i + 1], (passwordData[i + 2]) & 0xFF)) {
//                            exceptionFlag = true;
//                        }
//                        try {
//                            Thread.sleep(1);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                            exceptionFlag = true;
//                        }
//                    }
//                }
//            }
//            if (socket != null) {
//                socket.close();
//                socket = null;
//            }
//            lock.release();
//            return null;
//        }
//
//        /**
//         * ui task
//         *
//         * @param progress
//         */
//        @Override
//        protected void onProgressUpdate(Integer... progress) {
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
////            ((Button)getActivity().findViewById(R.id.button_start)).setText(R.string.find_start);
////            getActivity().findViewById(R.id.button_start).
////                    setBackgroundColor(getResources().getColor(R.color.dblue));
//            Log.d("multiUdpSendTask", "onPostExecute");
//            mSendTask = null;
//        }
//
//        @Override
//        protected void onCancelled() {
////            ((Button)getActivity().findViewById(R.id.button_start)).setText(R.string.find_start);
////            getActivity().findViewById(R.id.button_start).
////                    setBackgroundColor(getResources().getColor(R.color.dblue));
//            mSendTask = null;
//        }
//    }

//    private class multiUdpRecvTask extends AsyncTask<Void, String, Void> {
//
//        @Override
//        protected void onPreExecute() {
//            Log.d("multiUdpRecvTask", "onPreExecute");
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            WifiManager.MulticastLock lock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).
//                    createMulticastLock(getResources().getString(R.string.app_name) + "recv");
//            lock.acquire();
//            byte[] receiveBuffer = new byte[2048];
//            DatagramPacket dp = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//            MulticastSocket ms;
//            InetAddress receiveAddress;
//            try {
//                ms = new MulticastSocket(RECEIVE_PORT);
//                receiveAddress = InetAddress.getByName("239.102.103.104");
//                ms.joinGroup(receiveAddress);
//            } catch (IOException e) {
//                e.printStackTrace();
//                lock.release();
//                return null;
//            }
//            while (!isIAQConnectWifiTimeout) {
//                try {
//                    ms.receive(dp);
//                    byte[] data = dp.getData();
//                    String dataStr = new String(data, 0, data.length);
////                    Log.d("multiUdpRecvTask", "doInBackground: Received data=" + dataStr);
////                    String serialNum = Utils.getSharedPreferencesValue(getApplicationContext(), Const.KEY_DEVICE_SERIAL, Const.DEFAULT_SERIAL_NUMBER);
//                    if (dataStr.contains(Constants.IAQ_REGISTER_SUCCESS)) {
//                        isIAQConnectWifi = true;
//                        isIAQConnectWifiTimeout = true;
//                        mHandler.sendEmptyMessage(CONNECT_WIFI_SUCCESS);
//                    }
//                    StringBuilder sb = new StringBuilder(100);
//                    sb.append(String.format("MAC %02X:%02X:%02X:%02X:%02X:%02X.\n", data[0] & 0xFF, data[1] & 0xFF, data[2] & 0xFF, data[3] & 0xFF, data[4] & 0xFF, data[5] & 0xFF));
//                    sb.append(String.format("IP %d.%d.%d.%d.\n", data[8] & 0xFF, data[9] & 0xFF, data[10] & 0xFF, data[11] & 0xFF));
//                    sb.append(String.format("CPUID %02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X%02X.\n", data[12] & 0xFF, data[13] & 0xFF, data[14] & 0xFF, data[15] & 0xFF, data[16] & 0xFF, data[17] & 0xFF, data[18] & 0xFF, data[19] & 0xFF, data[20] & 0xFF, data[21] & 0xFF, data[22] & 0xFF, data[23] & 0xFF, data[24] & 0xFF, data[25] & 0xFF, data[26] & 0xFF, data[27] & 0xFF));
//                    publishProgress(sb.toString());
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//
//            try {
//                ms.leaveGroup(receiveAddress);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            lock.release();
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(String... progress) {
////            mAdapter.add(progress[0]);
//            Log.d("multiUdpRecvTask", "onProgressUpdate: data=" + progress[0]);
//        }
//    }

    public SpannableString getSpanable(int msgId, int[] spanableStrId) {
        String str = getString(msgId);
        SpannableString ssTitle = new SpannableString(str);

        for (int spanItem : spanableStrId) {
            String spanItemStr = getString(spanItem);
            ssTitle.setSpan(new StyleSpan(Typeface.BOLD), str.indexOf(spanItemStr), str.indexOf(spanItemStr) + spanItemStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ssTitle.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.body_text_blue)), str.indexOf(spanItemStr), str.indexOf(spanItemStr) + spanItemStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        }

        return ssTitle;
    }

}
