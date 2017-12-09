package com.honeywell.iaq.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.honeywell.iaq.AndroidBug5497Workaround;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class RegisterActivity extends IAQTitleBarActivity implements View.OnClickListener {
    private static final String TAG = RegisterActivity.class.getSimpleName();

    private EditText mPwd, mConfirmPwd, mValidationCode;

    private TextView mGetValidationCode;

//    private EditText mNickname;

    private Button mRegister;

    private static final int REGISTE_SUCCESS = 0;

    private static final int REGISTE_FAIL = 1;

    private CountDownTimer timer;

    private static final int VALIDATION_CODE_TIMER = 60 * 1000;

    private static final int TIMER_INTERVAL = 1000;


    static class RegisterHandler extends Handler {
        private WeakReference<RegisterActivity> mActivityContent;

        private RegisterActivity mActivity;

        public RegisterHandler(RegisterActivity activity) {
            mActivityContent = new WeakReference<RegisterActivity>(activity);
            mActivity = mActivityContent.get();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REGISTE_SUCCESS:
                    mActivity.dismissLoadingDialog();

                    Utils.showToast(mActivity, mActivity.getString(R.string.register_success));

                    Intent intent = new Intent(mActivity, NetworkSetup1Activity.class);
                    mActivity.startActivity(intent);

                    mActivity.finish();
                    break;
                case REGISTE_FAIL:
                    mActivity.dismissLoadingDialog();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private RegisterHandler mHandler;

    private String languageCode = Constants.CHINA_LANGUAGE_CODE, countryCode = Constants.DEFAULT_COUNTRY_CODE;

    private String phoneNum;

    @Override
    protected int getContent() {
        return R.layout.activity_register;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.register);
    }

    @Override
    protected void initView() {
        super.initView();
//        AndroidBug5497Workaround.assistActivity(this);

        TextChange textChange = new TextChange();
        mHandler = new RegisterHandler(this);

        phoneNum = getIntent().getStringExtra(Constants.KEY_PHONE_NUMBER);
        countryCode = getIntent().getStringExtra(Constants.KEY_COUNTRY_CODE);
        languageCode = getIntent().getStringExtra(Constants.KEY_LANGUAGE_CODE);

        mPwd = (EditText) findViewById(R.id.register_pwd);
        mPwd.addTextChangedListener(textChange);
        mConfirmPwd = (EditText) findViewById(R.id.confirm_register_pwd);
        mConfirmPwd.addTextChangedListener(textChange);
//        mNickname = (EditText) findViewById(R.id.register_nickname);
        mRegister = (Button) findViewById(R.id.btn_register);
        mRegister.setOnClickListener(this);

        Utils.setListenerToRootView(this, R.id.activity_register, mRegister);

        mValidationCode = (EditText) findViewById(R.id.register_validation_code);
        mValidationCode.addTextChangedListener(textChange);
        mGetValidationCode = (TextView) findViewById(R.id.get_validation_code_again);
        mGetValidationCode.setOnClickListener(this);

        timer = new CountDownTimer(VALIDATION_CODE_TIMER, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                mGetValidationCode.setEnabled(false);
                mGetValidationCode.setText(millisUntilFinished / 1000 + "s");
                mGetValidationCode.setTextColor(getResources().getColor(R.color.button_background));
            }

            @Override
            public void onFinish() {
                mGetValidationCode.setEnabled(true);
                mGetValidationCode.setTextColor(getResources().getColor(R.color.toolbar_title_text_color));
                mGetValidationCode.setText(getString(R.string.get_validation_code_again));
            }
        };
        timer.start();
    }

    class TextChange implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (mPwd.length() > 0 &&
                    mValidationCode.length() > 0 &&
                    mConfirmPwd.length() > 0) {
                mRegister.setEnabled(true);
            }

        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        timer.cancel();
        timer = null;
        mGetValidationCode.setEnabled(true);
        mGetValidationCode.setText(getString(R.string.get_validation_code_again));
        mGetValidationCode.setTextColor(getResources().getColor(R.color.toolbar_title_text_color));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                register();
                break;
            case R.id.get_validation_code_again:
                sendValidationCode();
                break;
            default:
                break;
        }

    }

    private void register() {
        if (phoneNum != null && phoneNum.length() > 0) {
            if (checkPhoneNumber(phoneNum)) {
                doRegister();
            } else {
                Utils.showToast(RegisterActivity.this, getString(R.string.wrong_phone_number));
            }
        } else {
            Utils.showToast(RegisterActivity.this, getString(R.string.input_phone));
        }
    }

    private boolean checkPhoneNumber(String phoneNumber) {
        if (Constants.DEFAULT_COUNTRY_CODE.equals(countryCode)) {
            return phoneNumber.length() == Constants.CHINESE_PHONE_NUMBER_LENGHT;
        }
        return true;
    }

    private boolean checkPassword(String password) {
        if (password.length() >= 6 && password.length() < 20) {
            return true;
        } else {
            return false;
        }
    }

    private void sendValidationCode() {
        if (phoneNum != null && phoneNum.length() > 0) {
            if (checkPhoneNumber(phoneNum)) {
                timer.start();

                final AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Utils.showToast(RegisterActivity.this, getString(R.string.get_validation_code_success));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        if (responseBody != null) {
                            String responseStr = new String(responseBody, 0, responseBody.length);
                            Log.d(TAG, "onFailure: responseStr=" + responseStr);
                        }
                        Utils.showToast(RegisterActivity.this, getString(R.string.get_validation_code_fail));
                    }
                };

                final String phoneNumber = countryCode + phoneNum;
//                Log.d(TAG, "Phone Number=" + phoneNumber + ", Language code=" + languageCode + ", Country code=" + countryCode);
                Map<String, String> params = new HashMap<>();
                params.put(Constants.KEY_TYPE, Constants.TYPE_SEND_V_CODE);
                params.put(Constants.KEY_LANGUAGE, languageCode);
                params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);

                HttpClientHelper.newInstance().httpRequest(getApplicationContext(), Constants.USER_URL, params, HttpClientHelper.NO_COOKIE, callback, HttpClientHelper.POST, null);
            } else {
                Utils.showToast(RegisterActivity.this, getString(R.string.wrong_phone_number));
            }
        } else {
            Utils.showToast(RegisterActivity.this, getString(R.string.input_phone));
        }
    }

    private void doRegister() {
        final String validationCode = mValidationCode.getText().toString().trim();
        if (validationCode.length() > 0) {
            final String password = mPwd.getText().toString();
            if (password.length() == 0) {
                Utils.showToast(RegisterActivity.this, getString(R.string.input_password));
            } else {
                if (checkPassword(password)) {
                    String confirmPwd = mConfirmPwd.getText().toString();
                    if (password.equals(confirmPwd)) {
                        showLoadingDialog();

                        final AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                String responseStr = new String(responseBody, 0, responseBody.length);
                                Log.d(TAG, "register: responseStr=" + responseStr);
                                for (int i = 0; i < headers.length; i++) {
                                    if (headers[i].getName().equals(Constants.KEY_SET_COOKIE)) {
                                        String value = headers[i].getValue();
                                        Log.d("register", "Get server Cookie: " + value);
                                        Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_COOKIE, value);
                                    }
                                }

                                mHandler.sendEmptyMessage(REGISTE_SUCCESS);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                if (responseBody != null) {
                                    String responseStr = new String(responseBody, 0, responseBody.length);
                                    Log.d(TAG, "onFailure: responseStr=" + responseStr);
                                    if (responseStr.length() > 0) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(responseStr);
                                            String errorType = jsonObject.getString(Constants.KEY_ERROR_TYPE);
                                            Log.d(TAG, "Register error type=" + errorType);
                                            if (Constants.ERROR_TYPE_INVALID_PASSWORD.equals(errorType)) {
                                                Utils.showToast(getApplicationContext(), getString(R.string.invalid_password));
                                            } else if (Constants.ERROR_TYPE_INVALID_PHONE_NUMBER.equals(errorType)) {
                                                Utils.showToast(getApplicationContext(), getString(R.string.wrong_phone_number));
                                            } else if (Constants.ERROR_TYPE_INVALID_PHONE_V_CODE.equals(errorType)) {
                                                Utils.showToast(getApplicationContext(), getString(R.string.wrong_validation_code));
                                            } else if (Constants.ERROR_TYPE_PHONE_NUMBER_REGISTERED.equals(errorType)) {
                                                Utils.showToast(getApplicationContext(), getString(R.string.register_already));
                                            } else if (Constants.ERROR_TYPE_INVALID_LANGUAGE.equals(errorType)) {
                                                Utils.showToast(getApplicationContext(), getString(R.string.invalid_language));
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Utils.showToast(getApplicationContext(), getString(R.string.register_fail));
                                        }
                                    }
                                }
                                mHandler.sendEmptyMessage(REGISTE_FAIL);
                            }
                        };

                        final String phoneNumber = countryCode + phoneNum;
                        Log.d(TAG, "Validation code=" + validationCode + ", Phone number=" + phoneNumber);
                        Map<String, String> params = new HashMap<>();
                        params.put(Constants.KEY_TYPE, Constants.TYPE_REGISTER_USER);
                        params.put(Constants.KEY_NAME, "Home");
                        params.put(Constants.KEY_PASSWORD, password);
                        params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);
                        params.put(Constants.KEY_PHONE_V_CODE, validationCode);
                        params.put(Constants.KEY_LANGUAGE, languageCode);
                        HttpClientHelper.newInstance().httpRequest(getApplicationContext(), Constants.USER_LIST_URL, params, HttpClientHelper.NO_COOKIE, callback, HttpClientHelper.POST, null);
                    } else {
                        Utils.showToast(RegisterActivity.this, getString(R.string.password_not_same));
                    }
                } else {
                    Utils.showToast(RegisterActivity.this, getString(R.string.invalid_password));
                }
            }
        } else {
            Utils.showToast(RegisterActivity.this, getString(R.string.input_validation_code));
        }
    }
}
