package com.honeywell.iaq.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.lib.utils.ResourceUtil;
import com.honeywell.lib.utils.ToastUtil;

import de.greenrobot.event.EventBus;


public abstract class IAQBaseFragment extends Fragment {
    private static final String TAG = IAQBaseFragment.class.getSimpleName();
    private View rootView;// 缓存Fragment view
    protected ImageView mLeft;
    protected ImageView mRight;
    protected TextView mTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);
//        mLeft = (ImageView) view.findViewById(R.id.iv_left);
//        mRight = (ImageView) view.findViewById(R.id.iv_right);
//        mTitle = (TextView) view.findViewById(R.id.tv_title);
//        final int index = getIndex();
//        final int leftId = ResourceUtil.getResourceIdArray(getResources(), R.array.main_header_icon_left)[index];
//        final int rightId = ResourceUtil.getResourceIdArray(getResources(), R.array.main_header_icon_right)[index];
//        final String title = ResourceUtil.getStringArray(getResources(), R.array.main_header_title)[index];
//        if (leftId > 0) {
//            mLeft.setImageResource(leftId);
//        }
//        if (rightId > 0) {
//            mRight.setImageResource(rightId);
//        }
//        mTitle.setText(title);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        initView(view);
        getData();
        return view;
    }

    public void initView(View view) {

    }

    public abstract int getLayout();

//    public abstract int getIndex();

    public void getData() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    public void onEventMainThread(IAQEvents event) {

    }

    public void showToastShort(int res) {
        showToastShort(getString(res));
    }

    public void showToastShort(String text) {
        ToastUtil.showShort(getContext(), text, true);
    }

    public void showToastLong(int res) {
        showToastLong(getString(res));
    }

    public void showToastLong(String text) {
        ToastUtil.showLong(getContext(), text, true);
    }
}
