package com.honeywell.iaq.base;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.honeywell.iaq.R;
import com.honeywell.lib.dialogs.LoadingDialog;
import com.honeywell.lib.utils.ToastUtil;


public class IAQBaseFragmentActivity extends FragmentActivity {
    protected int activityCloseEnterAnimation;
    protected int activityCloseExitAnimation;
    protected LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EventBus.getDefault().register(this);
        setTheme();
        initAnimation();
    }

    protected void setTheme() {
        setTheme(R.style.IAQBaseTheme);
    }

    public void initAnimation() {
        TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowAnimationStyle
        });

        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);

        activityStyle.recycle();

        activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId, new int[]{
                android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation
        });

        activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);

        activityCloseExitAnimation = activityStyle.getResourceId(1, 0);

        activityStyle.recycle();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }

    public void showLoadingDialog() {
        if (null == mLoadingDialog) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setCanceledOnTouchOutside(false);
        }
        mLoadingDialog.show();
    }

    public void dismissLoadingDialog() {
        if (null != mLoadingDialog && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }
//
//    public void onEventMainThread(CubeEvents event) {
//        if (event instanceof CubeBasicEvent) {
//            final CubeBasicEvent cubeBasicEvent = (CubeBasicEvent) event;
//            if (cubeBasicEvent.getType() == CubeEvents.CubeBasicEventType.TIME_OUT) {
////                ToastUtil.showShort(this, cubeBasicEvent.getMessage());
//                dismissLoadingDialog();
//            }
//        }
//    }

    public void showToastShort(int res) {
        showToastShort(getString(res));
    }

    public void showToastShort(String text) {
        ToastUtil.showShort(this, text, true);
    }

    public void showToastLong(int res) {
        showToastLong(getString(res));
    }

    public void showToastLong(String text) {
        ToastUtil.showLong(this, text, true);
    }
}
