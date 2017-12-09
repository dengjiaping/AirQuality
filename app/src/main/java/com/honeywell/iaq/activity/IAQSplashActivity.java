package com.honeywell.iaq.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.application.IAQApplication;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.utils.AES;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by zhujunyu on 2017/2/21.
 */

public class IAQSplashActivity extends IAQTitleBarActivity {
    private TextView mTextViewLogin;
    private Button mBtnRegister;


    @Override
    protected int getContent() {
        return R.layout.activity_spash;
    }

    @Override
    protected void initView() {
        mTextViewLogin = (TextView) findViewById(R.id.tv_login);
        mTextViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IAQSplashActivity.this, LoginActivity.class);
                startActivity(intent);
//                startActivity(new Intent(IAQSplashActivity.this, CityPickerActivity.class));
            }
        });
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IAQSplashActivity.this, RegisterAndForgotPwdActivity.class);
                intent.putExtra(Constants.INTENT_TYPE, Constants.REGISTER_TYPE);
                startActivity(intent);
            }
        });
        initData();
//        {
//            JSONObject jsonObject = new JSONObject();
//
//
//            Logger.e("============", "" + AES.generateKey());
//            byte[] bytes = AES.shaEncrypt1("00100002900000000004");
//
//            String string = Arrays.toString(Arrays.copyOfRange(bytes, 0, 15));
//
//            String last = Arrays.toString(Arrays.copyOfRange(bytes, 16, 31));
//
//            try {
//                jsonObject.put("KEY", string);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//
//
//            String key = jsonObject.optString("KEY");
//
//            Logger.e("##########", "" + key);
//            Logger.e("-------------", "" + last);
//            AES.setKey(last);
//            String wifi = AES.encrypt("Honeywell");
//           String passward = AES.encrypt("1234567");
//
//            Logger.e("***********",""+ wifi);
//            Logger.e("***********",""+ passward);
//
////            AES.decrypt(wifi);
////            AES.decrypt(passward);
//
//            Logger.e("@@@@@@@@@@@",""+ AES.decrypt(wifi));
//            Logger.e("@@@@@@@@@@@",""+ AES.decrypt(passward));
//
//        }




    }


    private void initData() {
        String account = Utils.getSharedPreferencesValue(getApplicationContext(), Constants.KEY_ACCOUNT, "");
        if (account.length() > 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
