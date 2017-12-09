package com.honeywell.iaq.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.bean.Result;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.exception.TubeException;
import com.honeywell.net.listener.JSONTubeListener;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by zhujunyu on 2017/2/22.
 */

public class IAQSetPasswordActivity extends IAQTitleBarActivity implements View.OnClickListener {
    private static final String TAG = IAQSetPasswordActivity.class.getSimpleName();
    private EditText mPwd;

    private EditText mConfirmPwd;

    private Button mBtnDone;

    private String phoneNum;
    private String intentType;
    private String validationCode;
    private String countryLanguage;
    private String countryCode;

    @Override
    protected int getContent() {
        return R.layout.activity_set_password;
    }

    @Override
    protected void initView() {
        super.initView();
        TextChange textChange = new TextChange();
        mPwd = (EditText) findViewById(R.id.register_password);
        mPwd.setTypeface(Typeface.DEFAULT);
        mPwd.setTransformationMethod(new PasswordTransformationMethod());
        mPwd.addTextChangedListener(textChange);
        mConfirmPwd = (EditText) findViewById(R.id.confirm_register_pwd);
        mConfirmPwd.setTypeface(Typeface.DEFAULT);
        mConfirmPwd.setTransformationMethod(new PasswordTransformationMethod());
        mConfirmPwd.addTextChangedListener(textChange);
        mBtnDone = (Button) findViewById(R.id.btn_done);
        mBtnDone.setOnClickListener(this);
        if (Constants.FORGOT_PASSWORD.equals(intentType)) {
            mBtnDone.setText(R.string.reset);
        } else if (Constants.REGISTER_TYPE.equals(intentType)) {
            mBtnDone.setText(R.string.register);
        }

    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        initIntent();
        if (Constants.FORGOT_PASSWORD.equals(intentType)) {
            title.setText(R.string.reset_password);
        } else if (Constants.REGISTER_TYPE.equals(intentType)) {
            title.setText(R.string.set_password);
        }

    }

    @Override
    protected void getData() {
        super.getData();
    }

    @Override
    public void onClick(View v) {
        if (TextUtils.isEmpty(mPwd.getText())) {

            return;
        }
        if (TextUtils.isEmpty(mConfirmPwd.getText())) {
            return;
        }
        String password = mPwd.getText().toString();
        String confirmPassword = mConfirmPwd.getText().toString();
        if (!password.equals(confirmPassword)) {
            Utils.showToast(this, this.getResources().getString(R.string.password_not_same));
            return;
        }

        if (password.length() < 6 || password.length() > 30) {
            Utils.showToast(this, this.getResources().getString(R.string.invalid_password));
            return;
        }

        if (Constants.FORGOT_PASSWORD.equals(intentType)) {
            doForgotPwd(password);
        } else if (Constants.REGISTER_TYPE.equals(intentType)) {
            doRegister(password);
        }

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
                    mConfirmPwd.length() >= 8) {
                mBtnDone.setEnabled(true);
            } else {
                mBtnDone.setEnabled(false);
            }
        }
    }

    private void initIntent() {
        Intent intent = getIntent();
        intentType = intent.getStringExtra(Constants.INTENT_TYPE);
        validationCode = intent.getStringExtra(Constants.VALIDATE_CODE);
        countryLanguage = intent.getStringExtra(Constants.COUNTRY_LANGUAGE);
        countryCode = intent.getStringExtra(Constants.COUNTRY_CODE);
        phoneNum = intent.getStringExtra(Constants.PHONE_NUMBER);
        Logger.e("phone", "num" + phoneNum);
    }

    private void doRegister(String password) {
        final String phoneNumber = countryCode + phoneNum;
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);
        params.put(Constants.KEY_LANGUAGE, countryLanguage);
        params.put(Constants.KEY_PHONE_V_CODE, validationCode);
        params.put(Constants.KEY_TYPE, Constants.TYPE_REGISTER_USER);
        params.put(Constants.KEY_NAME, "Home");
        params.put(Constants.KEY_PASSWORD, password);
        showLoadingDialog();
        HttpUtils.getString(this, Constants.USER_LIST_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_REGISTER, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();


                doResponse(resultList, resultCode, new DataSecondaryProcessing() {
                    @Override
                    public void JSONOKCallback(JSONObject jsonObject) {
                        //错误排查完毕
                        if (Constants.FORGOT_PASSWORD.equals(intentType)) {
                            Utils.showToast(getApplicationContext(), getString(R.string.reset_password_success));
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        } else if (Constants.REGISTER_TYPE.equals(intentType)) {

                            Utils.showToast(getApplicationContext(), getString(R.string.register_success));
                            Intent intent = new Intent(getApplicationContext(), NetworkSetup1Activity.class);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void ListOKCallback(ArrayList resultList) {

                    }

                    @Override
                    public void NoneOKCallback() {

                    }

                    @Override
                    public void ErrorCallback(JSONObject jsonObject) {
                        String errorType = jsonObject.optString(Constants.KEY_ERROR_TYPE);
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
                        } else {
                            Utils.showToast(getApplicationContext(), getString(R.string.register_fail));
                        }
                    }

                    @Override
                    public void ExceptionCallback() {
                        Utils.showToast(getApplicationContext(), getString(R.string.register_fail));
                    }
                }, objects);

            }
        }));

    }
    private void doRegister2(String password) {
        final String phoneNumber = countryCode + phoneNum;
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);
        params.put(Constants.KEY_LANGUAGE, countryLanguage);
        params.put(Constants.KEY_PHONE_V_CODE, validationCode);
        params.put(Constants.KEY_TYPE, Constants.TYPE_REGISTER_USER);
        params.put(Constants.KEY_NAME, "Home");
        params.put(Constants.KEY_PASSWORD, password);
        showLoadingDialog();


        HttpUtils.getJSON(this, Constants.USER_LIST_URL, IAQRequestUtils.getRequestEntity(params), new JSONTubeListener<JSONObject>() {

            @Override
            public JSONObject doInBackground(JSONObject water) throws Exception {
                return water;
            }

            @Override
            public void onSuccess(JSONObject jsonObject) {
                dismissLoadingDialog();
                String errorType = jsonObject.optString(Constants.KEY_ERROR_TYPE);
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
                } else {
                    if(jsonObject.has(Constants.KEY_COOKIE)){
                        String cookie = jsonObject.optString(Constants.KEY_COOKIE);
                        Log.d(TAG, "Get server Cookie: " + cookie);
                        Utils.setSharedPreferencesValue(IAQSetPasswordActivity.this, Constants.KEY_COOKIE, cookie);
                        Utils.showToast(getApplicationContext(), getString(R.string.register_success));
                        Intent intent = new Intent(getApplicationContext(), NetworkSetup1Activity.class);
                        startActivity(intent);
                    }else {
                        Utils.showToast(getApplicationContext(), getString(R.string.register_fail));
                    }
                }




            }

            @Override
            public void onFailed(TubeException e) {
                dismissLoadingDialog();
                Utils.showToast(getApplicationContext(), getString(R.string.register_fail));
            }
        });

//        HttpUtils.getString(this, Constants.USER_LIST_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_REGISTER, new IResponse() {
//            @Override
//            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
//                dismissLoadingDialog();
//
//
//                doResponse(resultList, resultCode, new DataSecondaryProcessing() {
//                    @Override
//                    public void JSONOKCallback(JSONObject jsonObject) {
//                        //错误排查完毕
//                        if (Constants.FORGOT_PASSWORD.equals(intentType)) {
//                            Utils.showToast(getApplicationContext(), getString(R.string.reset_password_success));
//                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                            startActivity(intent);
//                        } else if (Constants.REGISTER_TYPE.equals(intentType)) {
//
//
//
//                            Utils.showToast(getApplicationContext(), getString(R.string.register_success));
//                            Intent intent = new Intent(getApplicationContext(), NetworkSetup1Activity.class);
//                            startActivity(intent);
//                        }
//                    }
//
//                    @Override
//                    public void ListOKCallback(ArrayList resultList) {
//
//                    }
//
//                    @Override
//                    public void NoneOKCallback() {
//
//                    }
//
//                    @Override
//                    public void ErrorCallback(JSONObject jsonObject) {
//                        String errorType = jsonObject.optString(Constants.KEY_ERROR_TYPE);
//                        if (Constants.ERROR_TYPE_INVALID_PASSWORD.equals(errorType)) {
//                            Utils.showToast(getApplicationContext(), getString(R.string.invalid_password));
//                        } else if (Constants.ERROR_TYPE_INVALID_PHONE_NUMBER.equals(errorType)) {
//                            Utils.showToast(getApplicationContext(), getString(R.string.wrong_phone_number));
//                        } else if (Constants.ERROR_TYPE_INVALID_PHONE_V_CODE.equals(errorType)) {
//                            Utils.showToast(getApplicationContext(), getString(R.string.wrong_validation_code));
//                        } else if (Constants.ERROR_TYPE_PHONE_NUMBER_REGISTERED.equals(errorType)) {
//                            Utils.showToast(getApplicationContext(), getString(R.string.register_already));
//                        } else if (Constants.ERROR_TYPE_INVALID_LANGUAGE.equals(errorType)) {
//                            Utils.showToast(getApplicationContext(), getString(R.string.invalid_language));
//                        } else {
//                            Utils.showToast(getApplicationContext(), getString(R.string.register_fail));
//                        }
//                    }
//
//                    @Override
//                    public void ExceptionCallback() {
//                        Utils.showToast(getApplicationContext(), getString(R.string.register_fail));
//                    }
//                }, objects);
//
//            }
//        }));

    }




    private void doForgotPwd(final String password) {

        final String phoneNumber = countryCode + phoneNum;
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);
        params.put(Constants.KEY_LANGUAGE, countryLanguage);
        params.put(Constants.KEY_PHONE_V_CODE, validationCode);
        params.put(Constants.KEY_TYPE, Constants.TYPE_RESET_PASSWORD);
        params.put(Constants.KEY_NAME, "Home");
        params.put(Constants.KEY_NEW_PASSWORD, password);
        showLoadingDialog();
        HttpUtils.getString(this, Constants.USER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_FORGOT_PWD, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {

                dismissLoadingDialog();
                doResponse(resultList, resultCode, new DataSecondaryProcessing() {
                    @Override
                    public void JSONOKCallback(JSONObject jsonObject) {
                        if (Constants.FORGOT_PASSWORD.equals(intentType)) {
                            Utils.showToast(getApplicationContext(), getString(R.string.reset_password_success));
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        } else if (Constants.REGISTER_TYPE.equals(intentType)) {
                            if (jsonObject.has(Constants.KEY_PHONE_ID)) {
                                String phoneId = jsonObject.optString(Constants.KEY_PHONE_ID);
                                Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, phoneNum);
//                                Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_PASSWORD, password);
                                Utils.setSharedPreferencesValue(getApplicationContext(), Constants.KEY_PHONE_ID, phoneId);
                                Log.d(TAG, "register: PhoneId=" + phoneId);
                                Utils.showToast(getApplicationContext(), getString(R.string.register_success));
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            }

                        }
                    }

                    @Override
                    public void ListOKCallback(ArrayList resultList) {

                    }

                    @Override
                    public void NoneOKCallback() {
                        Utils.showToast(getApplicationContext(), getString(R.string.reset_password_success));
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void ErrorCallback(JSONObject jsonObject) {
                        if (jsonObject.has(Constants.KEY_ERROR_TYPE)) {
                            String string = jsonObject.optString(Constants.KEY_ERROR_TYPE);
                            if (Constants.ERROR_TYPE_INVALID_PASSWORD.equals(string)) {
                                Utils.showToast(getApplicationContext(), getString(R.string.invalid_password));
                            } else if (Constants.ERROR_TYPE_INVALID_PHONE_NUMBER.equals(string)) {
                                Utils.showToast(getApplicationContext(), getString(R.string.wrong_phone_number));
                            } else if (Constants.ERROR_TYPE_INVALID_PHONE_V_CODE.equals(string)) {
                                Utils.showToast(getApplicationContext(), getString(R.string.wrong_validation_code));
                            }
                        } else {
                            Utils.showToast(getApplicationContext(), getString(R.string.reset_fail));
                        }
                    }

                    @Override
                    public void ExceptionCallback() {
                        Utils.showToast(getApplicationContext(), getString(R.string.reset_fail));
                    }
                }, objects);
//
//
//                dismissLoadingDialog();
//                if (resultCode != 0) {
//                    if (Constants.FORGOT_PASSWORD.equals(intentType)) {
//                        Utils.showToast(getApplicationContext(), getString(R.string.reset_password_success));
//                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                        startActivity(intent);
//                    } else if (Constants.REGISTER_TYPE.equals(intentType)) {
//                        Utils.showToast(getApplicationContext(), getString(R.string.register_success));
//                        Intent intent = new Intent(getApplicationContext(), NetworkSetup1Activity.class);
//                        startActivity(intent);
//                    }
//                    return;
//                }
//                if (Constants.FORGOT_PASSWORD.equals(intentType)) {
//                    Utils.showToast(getApplicationContext(), getString(R.string.reset_password_success));
//                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//                    startActivity(intent);
//                } else if (Constants.REGISTER_TYPE.equals(intentType)) {
//                    Utils.showToast(getApplicationContext(), getString(R.string.register_success));
//                    Intent intent = new Intent(getApplicationContext(), NetworkSetup1Activity.class);
//                    startActivity(intent);
//                }
            }
        }));
    }


    private void doResponse(ArrayList resultList, int resultCode, DataSecondaryProcessing listener, Object... objects) {

        Logger.e("-response data---", "resultCode" + resultCode);
        if (objects != null && objects.length != 0) {
            String re = (String) objects[0];
            Logger.e("-response data---", "objects" + re + "" + re.length());
        }
        if (resultList != null && resultList.size() != 0) {
            Logger.e("-response data---", "resultList.size" + resultList.size());
        }

        switch (resultCode) {
            case Result.RESULT_OK:
                if (objects != null && objects.length != 0) {
                    try {
                        String responseStr = (String) objects[0];

                        if ("null".equalsIgnoreCase(responseStr) || TextUtils.isEmpty(responseStr)) {
                            listener.NoneOKCallback();
                        } else {
                            JSONObject jsonObject = new JSONObject(responseStr);
                            listener.JSONOKCallback(jsonObject);
                        }

                    } catch (Exception e) {
                        listener.ExceptionCallback();
                    }

                } else if (resultList != null && resultList.size() != 0) {
                    listener.ListOKCallback(resultList);
                } else {
                    listener.NoneOKCallback();
                }

                break;
            case Result.RESULT_ERROR:
                try {
                    String responseStr = (String) objects[0];
                    JSONObject jsonObject = new JSONObject(responseStr);
                    listener.ErrorCallback(jsonObject);
                } catch (Exception e) {

                }
                break;
            case Result.RESULT_EXCEPTION:
                listener.ExceptionCallback();
                break;
        }

    }


    public interface DataSecondaryProcessing {
        void JSONOKCallback(JSONObject jsonObject);

        void ListOKCallback(ArrayList resultList);

        void NoneOKCallback();

        void ErrorCallback(JSONObject jsonObject);

        void ExceptionCallback();
    }


}
