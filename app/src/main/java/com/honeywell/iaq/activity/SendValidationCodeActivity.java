package com.honeywell.iaq.activity;

import android.content.Intent;
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

import com.honeywell.iaq.AndroidBug5497Workaround;
import com.honeywell.iaq.adapter.CountryAdapter;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.R;
import com.honeywell.iaq.bean.Country;
import com.honeywell.iaq.utils.PreferenceUtil;
import com.honeywell.iaq.utils.Utils;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class SendValidationCodeActivity extends IAQTitleBarActivity implements View.OnClickListener {
    private static final String TAG = SendValidationCodeActivity.class.getSimpleName();

    private EditText mPhoneNumber;

    private Button mNext;

    private Spinner mSpinner;

    private int mCountryIndex = Constants.COUNTRY_CHINA_INDEX;
    CountryAdapter mCountryAdapter;

    @Override
    protected int getContent() {
        return R.layout.activity_send_validation_code;
    }


    @Override
    protected void initTitle(TextView title) {
        title.setText(R.string.register);
    }

    @Override
    protected void initView() {
        super.initView();
//        AndroidBug5497Workaround.assistActivity(this);

        mCountryIndex = PreferenceUtil.getInt(getApplicationContext(), Constants.KEY_COUNTRY_INDEX, Constants.COUNTRY_CHINA_INDEX);

        mSpinner = (Spinner) findViewById(R.id.sp_country);
        mCountryAdapter = new CountryAdapter(this);
        mSpinner.setAdapter(mCountryAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCountryIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinner.setSelection(mCountryIndex);

        mPhoneNumber = (EditText) findViewById(R.id.register_phone_number);
        mPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mNext.setEnabled(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mNext = (Button) findViewById(R.id.btn_next);
        mNext.setOnClickListener(this);

        Utils.setListenerToRootView(this, R.id.activity_send_validation_code, mNext);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                sendValidationCode();
                break;
            default:
                break;
        }
    }

    private void sendValidationCode() {
        if (mPhoneNumber.getText().toString().length() > 0) {
            if (checkPhoneNumber(mPhoneNumber.getText().toString())) {
                showLoadingDialog();
                final AsyncHttpResponseHandler callback = new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        dismissLoadingDialog();
                        next();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        dismissLoadingDialog();
                        if (responseBody != null) {
                            String responseStr = new String(responseBody, 0, responseBody.length);
                            Log.d(TAG, "onFailure: responseStr=" + responseStr);
                        }
                        Utils.showToast(SendValidationCodeActivity.this, getString(R.string.get_validation_code_fail));
                    }
                };

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
                final Country country = (Country) mCountryAdapter.getItem(mCountryIndex);
                final String phoneNumber = country.getCode() + mPhoneNumber.getText().toString();
                Log.d(TAG, "Phone Number=" + phoneNumber + ", Language code=" + country.getLanguage() + ", Country code=" + country.getCode());
                Map<String, String> params = new HashMap<>();
                params.put(Constants.KEY_TYPE, Constants.TYPE_SEND_V_CODE);
                params.put(Constants.KEY_LANGUAGE, country.getLanguage());
                params.put(Constants.KEY_PHONE_NUMBER, phoneNumber);

                HttpClientHelper.newInstance().httpRequest(getApplicationContext(), Constants.USER_URL, params, HttpClientHelper.NO_COOKIE, callback, HttpClientHelper.POST, null);
//                    }
//                }).start();
            } else {
                Utils.showToast(SendValidationCodeActivity.this, getString(R.string.wrong_phone_number));
            }
        } else {
            Utils.showToast(SendValidationCodeActivity.this, getString(R.string.input_phone));
        }
    }

    private boolean checkPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() == Constants.CHINESE_PHONE_NUMBER_LENGHT) {
            return true;
        } else {
            return false;
        }
    }

    private void next() {
        String phoneNumber = mPhoneNumber.getText().toString();
        if (phoneNumber.length() > 0) {
            Intent intent = new Intent(SendValidationCodeActivity.this, RegisterActivity.class);
            intent.putExtra(Constants.KEY_PHONE_NUMBER, phoneNumber);
            intent.putExtra(Constants.KEY_COUNTRY_CODE, ((Country) mCountryAdapter.getItem(mCountryIndex)).getCode());
            intent.putExtra(Constants.KEY_LANGUAGE_CODE, ((Country) mCountryAdapter.getItem(mCountryIndex)).getLanguage());
            startActivity(intent);
        } else {
            Utils.showToast(SendValidationCodeActivity.this, getString(R.string.input_phone));
        }
    }

}
