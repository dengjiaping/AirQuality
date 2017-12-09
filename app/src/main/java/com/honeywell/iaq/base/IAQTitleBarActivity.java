package com.honeywell.iaq.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.application.IAQApplication;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.widget.LoadingDialog;
import com.honeywell.net.utils.Logger;

import de.greenrobot.event.EventBus;


public abstract class IAQTitleBarActivity extends IAQBaseActivity {
    protected ImageView mLeft;
    protected ImageView mRight;
    protected TextView mTextViewTitle;
    private LoadingDialog mLoadingDialog;
    protected String mTitle;
    protected int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.e("************","注册EventBus");
        EventBus.getDefault().register(this);
        initIntentValue();
        setContentView(getContent());
        initTitleBar();
        initView();
        getData();
    }

    protected void initView() {
    }

    protected void initIntentValue() {
        mTitle = getIntent().getStringExtra(Constants.TITLE);
        mType = getIntent().getIntExtra(Constants.TYPE, -1);
    }

    protected void getData() {
    }

    protected abstract int getContent();

    protected void initTitleBar() {
        mLeft = (ImageView) findViewById(R.id.iv_left);
        mRight = (ImageView) findViewById(R.id.iv_right);
        mTextViewTitle = (TextView) findViewById(R.id.tv_title);
        initLeftIcon(mLeft);
        initRightIcon(mRight);
        initTitle(mTextViewTitle);
    }

    protected void initLeftIcon(ImageView left) {
        left.setImageResource(R.mipmap.ic_arrow_back_white);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected void initRightIcon(ImageView right) {

    }

    protected void initTitle(TextView title) {
        if (!TextUtils.isEmpty(mTitle)) {
            title.setText(mTitle);
        }
    }

    public void showLoadingDialog() {
        if (null == mLoadingDialog) {
            initLoadingDialog();
        }
        mLoadingDialog.show();
    }


    private void initLoadingDialog() {
        mLoadingDialog = new LoadingDialog(this);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.setCancelable(false);
    }

    public void dismissLoadingDialog() {
        if (null != mLoadingDialog && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.e("************","反注册EventBus");
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(IAQEvents event) {

        }

    protected void showExitDialog() {
        AlertDialog mAlertDialog = new AlertDialog.Builder(this).setMessage(getString(R.string.exit_confirm)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                IAQApplication.getInstance().onTerminate();
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        mAlertDialog.show();
    }

}
