package com.honeywell.iaq.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.adapter.CountryAdapter;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.bean.Country;
import com.honeywell.iaq.db.IAQ;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.iaq.widget.MessageBox;
import com.honeywell.net.utils.Logger;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

/**
 * Created by zhujunyu on 2017/2/21.
 */

public class RegisterAndForgotPwdActivity extends IAQTitleBarActivity implements View.OnClickListener {
    private static final String TAG = RegisterAndForgotPwdActivity.class.getSimpleName();
    private Spinner mSpinner;
    private CountryAdapter mCountryAdapter;
    private int mCountryIndex = Constants.COUNTRY_CHINA_INDEX;
    private EditText mEtPhoneNum;
    private EditText mEtValidationCode;
    private Button mBtnSend;
    private Button mBtnNext;
    private String phoneNum;
    private String validationCode;
    private String intentType;
    private String countryLanguage;
    private String countryCode;

    @Override
    protected int getContent() {
        return R.layout.activity_register_forgot;
    }

    @Override
    protected void initView() {
        super.initView();
        TextChange textChange = new TextChange();
        mEtPhoneNum = (EditText) findViewById(R.id.register_phone_number);
        mEtPhoneNum.addTextChangedListener(textChange);
        mEtValidationCode = (EditText) findViewById(R.id.register_validation_code);
        mEtValidationCode.addTextChangedListener(textChange);
        mBtnSend = (Button) findViewById(R.id.send_validation_code);
        mBtnNext = (Button) findViewById(R.id.btn_next);

        mCountryIndex = PreferenceUtil.getInt(getApplicationContext(), Constants.KEY_COUNTRY_INDEX, Constants.COUNTRY_CHINA_INDEX);
        mSpinner = (Spinner) findViewById(R.id.sp_country);
        mCountryAdapter = new CountryAdapter(this);
        mSpinner.setAdapter(mCountryAdapter);

        mSpinner.setSelection(mCountryIndex);
        setListener();
    }

    private void initIntent() {
        Intent intent = getIntent();
        intentType = intent.getStringExtra(Constants.INTENT_TYPE);
    }

    @Override
    protected void getData() {
        super.getData();
        Country country = (Country) mCountryAdapter.getItem(mCountryIndex);
        countryCode = country.getCode();
        countryLanguage = country.getLanguage();
    }


    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        initIntent();
        Log.e("---intentType---", "" + intentType);
        if (Constants.FORGOT_PASSWORD.equals(intentType)) {
            title.setText(R.string.forget_password);
        } else if (Constants.REGISTER_TYPE.endsWith(intentType)) {
            title.setText(R.string.register);
        }
    }

    private void setListener() {
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCountryIndex = position;
                Country country = (Country) mCountryAdapter.getItem(mCountryIndex);
                countryCode = country.getCode();
                countryLanguage = country.getLanguage();
                Logger.e("----", "" + countryCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mBtnSend.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnSend) {
            if (TextUtils.isEmpty(mEtPhoneNum.getText())) {
                Utils.showToast(getApplicationContext(), getString(R.string.input_phone));
                return;
            }
            phoneNum = mEtPhoneNum.getText().toString();

            if (Constants.FORGOT_PASSWORD.equals(intentType)) {
                getValidateCode(phoneNum);
            } else if (Constants.REGISTER_TYPE.endsWith(intentType)) {
                checkRegister(phoneNum);
            }

        } else if (v == mBtnNext) {

            if (TextUtils.isEmpty(mEtPhoneNum.getText())) {
                Utils.showToast(getApplicationContext(), getString(R.string.input_phone));
                return;
            }
            phoneNum = mEtPhoneNum.getText().toString();

            if (TextUtils.isEmpty(mEtValidationCode.getText())) {
                Utils.showToast(getApplicationContext(), getString(R.string.input_validation_code));
                return;
            }
            validationCode = mEtValidationCode.getText().toString();

            doNext(phoneNum, validationCode);
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
            if (mEtPhoneNum.length() > 0 &&
                    mEtValidationCode.length() > 0) {
                mBtnNext.setEnabled(true);
            } else {
                mBtnNext.setEnabled(false);
            }

        }
    }

    private void getValidateCode(String phoneNum) {


        String phoneNumber = countryCode + phoneNum;
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_SEND_V_CODE);
        params.put(Constants.KEY_LANGUAGE, countryLanguage);
        params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);
        HttpUtils.getString(this, Constants.USER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_GET_VALIDATE_CODE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode == 0) {
                    new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            int sec = (int) (millisUntilFinished / 1000);
                            mBtnSend.setText(getResources().getString(R.string.resend_validation_code) + "(" + sec + ")");
                            mBtnSend.setEnabled(false);
                        }

                        @Override
                        public void onFinish() {
                            mBtnSend.setText(R.string.send_validation_code);
                            mBtnSend.setEnabled(true);
                        }
                    }.start();
                } else {
                    Utils.showToast(RegisterAndForgotPwdActivity.this, getString(R.string.get_validation_code_fail));
                }
            }
        }));
    }


    private void doNext(final String phoneNum, final String validationCode) {
        if (!Utils.checkPhoneNumber(phoneNum, countryCode)) {
            Utils.showToast(getApplicationContext(), getString(R.string.wrong_phone_number));
            return;
        }
        showLoadingDialog();
        final String phoneNumber = countryCode + phoneNum;
        Log.d(TAG, "Validation code=" + validationCode + ", Phone number=" + phoneNumber);
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);
        params.put(Constants.KEY_LANGUAGE, countryLanguage);
        params.put(Constants.KEY_PHONE_V_CODE, validationCode);
        params.put(Constants.KEY_TYPE, Constants.TYPE_REGISTER_USER);
        params.put(Constants.KEY_NAME, "Home");
        params.put(Constants.KEY_PASSWORD, "");
        HttpUtils.getString(this, Constants.USER_LIST_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_CHECK_VALIDATE_CODE, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode != 0) {

                    return;
                }
                String responseStr = (String) objects[0];
                JSONObject jsonObject = new JSONObject(responseStr);
                String errorType = jsonObject.optString(Constants.KEY_ERROR_TYPE);
                if (Constants.ERROR_TYPE_INVALID_PHONE_V_CODE.equals(errorType)) {
                    Utils.showToast(getApplicationContext(), getString(R.string.wrong_validation_code));
                } else {
                    //获取成功 跳转到输入密码页面
                    Intent intent = new Intent(RegisterAndForgotPwdActivity.this, IAQSetPasswordActivity.class);
                    intent.putExtra(Constants.INTENT_TYPE, intentType);
                    intent.putExtra(Constants.VALIDATE_CODE, validationCode);
                    intent.putExtra(Constants.COUNTRY_LANGUAGE, countryLanguage);
                    intent.putExtra(Constants.COUNTRY_CODE, countryCode);
                    intent.putExtra(Constants.PHONE_NUMBER, phoneNum);
                    Log.e("phone", "num" + phoneNum);
                    startActivity(intent);
                }


            }
        }));

    }

    private void checkRegister(final String phoneNum) {
        showLoadingDialog();

        String phoneNumber = countryCode + phoneNum;

        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_CHECK_REGISTER);
        params.put("phoneNumber", phoneNumber);
        HttpUtils.getString(this, Constants.USER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(this, Constants.GetDataFlag.HON_IAQ_CHECK_REGISTER, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                dismissLoadingDialog();
                if (resultCode == 0) {
                    JSONObject jsonObject = new JSONObject((String) objects[0]);
                    String isRegister = jsonObject.getString("isRegister");
                    if ("0".equals(isRegister)) {
                        getValidateCode(phoneNum);
                    } else if ("1".equals(isRegister)) {
                        MessageBox.createSimpleDialog(RegisterAndForgotPwdActivity.this, null, getString(R.string.phone_already_register), null, null);
                    }
                } else {
                    MessageBox.createSimpleDialog(RegisterAndForgotPwdActivity.this, null, getString(R.string.setting_fail), null, null);
                }

            }
        }));


    }


}
