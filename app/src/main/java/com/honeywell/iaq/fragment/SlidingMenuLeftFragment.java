package com.honeywell.iaq.fragment;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.activity.AboutActivity;
import com.honeywell.iaq.activity.CityPickerActivity;
import com.honeywell.iaq.activity.DataExplainActivity;
import com.honeywell.iaq.activity.IAQSplashActivity;
import com.honeywell.iaq.activity.LoginActivity;
import com.honeywell.iaq.activity.NetworkSetup1Activity;
import com.honeywell.iaq.adapter.IconTextBaseAdapter;
import com.honeywell.iaq.adapter.IconTextListAdapter;
import com.honeywell.iaq.base.IAQBaseFragment;
import com.honeywell.iaq.events.IAQEvents;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.net.HttpClientHelper;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.lib.utils.ResourceUtil;
import com.honeywell.lib.utils.ToastUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import de.greenrobot.event.EventBus;


public class SlidingMenuLeftFragment extends IAQBaseFragment implements AdapterView.OnItemClickListener {
    private View rootView;// 缓存Fragment view
    private IconTextListAdapter mAdapter;
    List<IconTextBaseAdapter.ItemBean> mDataList = new ArrayList<>();

    @Override
    public int getLayout() {
        return 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_sliding_left_menu, null);
            ListView lv = (ListView) rootView.findViewById(R.id.list_menu);
            mAdapter = new IconTextListAdapter(getDataList(getResources()));
            lv.setAdapter(mAdapter);
            lv.setOnItemClickListener(this);
        }
        // 缓存的rootView需要判断是否已经被加过parent，如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    private List<IconTextBaseAdapter.ItemBean> getDataList(Resources res) {
        mDataList.clear();
        final int resourceId[] = ResourceUtil.getResourceIdArray(res, R.array.sliding_menu_left_icon);
        final String text[] = ResourceUtil.getStringArray(res, R.array.sliding_menu_left_title);
        final int length = resourceId.length;
        for (int i = 0; i < length; i++) {
            mDataList.add(new IconTextBaseAdapter.ItemBean(resourceId[i], null, text[i], "", null));
        }
        return mDataList;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            getActivity().startActivity(new Intent(getActivity(), NetworkSetup1Activity.class));
        } else if (position == 1) {
            getActivity().startActivity(new Intent(getActivity(), AboutActivity.class));
        } else if (position == 2) {
            logout();
        } else {
            ToastUtil.showShort(parent.getContext(), mAdapter.getItemList().get(position).getText() + "is clicked");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);

    }

    private void logout() {
        if (Utils.isNetworkAvailable(getActivity())) {

            Map<String, String> params = new HashMap<>();
            params.put(Constants.KEY_TYPE, Constants.TYPE_LOGOUT_USER);
            HttpUtils.getString(getContext(), Constants.USER_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getContext(), Constants.GetDataFlag.HON_IAQ_LOGOUT, new IResponse() {
                @Override
                public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                    if (resultCode == 0) {
                        doLogout();
                    }
                }
            }));


        } else {
            Utils.showToast(getActivity(), getString(R.string.no_network));
        }
    }

    public void onEventMainThread(IAQEvents event) {
    }

    private void doLogout() {
        Log.d("doLogout", "Logout");
        Utils.setSharedPreferencesValue(getActivity(), Constants.KEY_ACCOUNT, "");
        Utils.setSharedPreferencesValue(getActivity(), Constants.KEY_PASSWORD, "");

        Utils.startServiceByExitAction(getActivity(), Constants.ACTION_DISCONNECT);

        Intent intent = new Intent(getActivity(), IAQSplashActivity.class);
        getActivity().startActivity(intent);
    }
}
