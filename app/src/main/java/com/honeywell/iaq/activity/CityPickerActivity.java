package com.honeywell.iaq.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.honeywell.iaq.R;
import com.honeywell.iaq.adapter.CityListAdapter;
import com.honeywell.iaq.adapter.ResultListAdapter;
import com.honeywell.iaq.base.IAQTitleBarActivity;
import com.honeywell.iaq.bean.City;
import com.honeywell.iaq.bean.LocateState;
import com.honeywell.iaq.interfaces.IResponse;
import com.honeywell.iaq.task.TubeTask;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.HttpUtils;
import com.honeywell.iaq.utils.IAQRequestUtils;
import com.honeywell.iaq.utils.StringUtils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhujunyu on 2017/4/5.
 */
public class CityPickerActivity extends IAQTitleBarActivity implements View.OnClickListener {
    public static final String KEY_PICKED_CITY = "picked_city";
    private static String TAG = "CityPickerActivity";

    private List<City> result = new ArrayList<>();
    ;
    private ListView mListView;
    private ListView mResultListView;
    private EditText searchBox;
    private ImageView clearBtn;
    private ViewGroup emptyView;

    private CityListAdapter mCityAdapter;
    private ResultListAdapter mResultAdapter;
    private AMapLocationClient mLocationClient;

    @Override
    protected int getContent() {
        return R.layout.cp_activity_city_list;
    }

    @Override
    protected void initTitle(TextView title) {
        super.initTitle(title);
        title.setText(R.string.cp_select_city);
    }

    @Override
    protected void initView() {
        initData();
        initLocation();
        mListView = (ListView) findViewById(R.id.listview_all_city);
        mListView.setAdapter(mCityAdapter);


        searchBox = (EditText) findViewById(R.id.et_search);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString();
                if (TextUtils.isEmpty(keyword)) {
                    clearBtn.setVisibility(View.GONE);
                    emptyView.setVisibility(View.GONE);
                    mResultListView.setVisibility(View.GONE);
                } else {
                    clearBtn.setVisibility(View.VISIBLE);
                    mResultListView.setVisibility(View.VISIBLE);
                    //查询
                    searchCity(keyword, true);

                }
            }
        });

        emptyView = (ViewGroup) findViewById(R.id.empty_view);
        mResultListView = (ListView) findViewById(R.id.listview_search_result);
        mResultListView.setAdapter(mResultAdapter);
        mResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                back(mResultAdapter.getItem(position));
            }
        });

        clearBtn = (ImageView) findViewById(R.id.iv_search_clear);

        clearBtn.setOnClickListener(this);


    }

    private void refreshSeachResult() {
        if (result == null || result.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            Log.d(TAG, "发送前" + result.size());
            mResultAdapter.changeData(result);
        }
    }

    private void initLocation() {
        mLocationClient = new AMapLocationClient(this);
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setOnceLocation(true);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    Log.e(TAG,""+aMapLocation.getErrorCode());
                    if (aMapLocation.getErrorCode() == 0) {
                        String city = aMapLocation.getCity();
                        String district = aMapLocation.getDistrict();
                        String location = StringUtils.extractLocation(city, district);
                        mCityAdapter.updateLocateState(LocateState.SUCCESS, location);
                    } else {
                        //定位失败
                        mCityAdapter.updateLocateState(LocateState.FAILED, null);
                    }
                }
            }
        });
        mLocationClient.startLocation();
    }

    private void initData() {
        mCityAdapter = new CityListAdapter(this);
        mCityAdapter.setOnCityClickListener(new CityListAdapter.OnCityClickListener() {
            @Override
            public void onCityClick(String name) {
                searchCity(name, false);


            }

            @Override
            public void onLocateClick() {
                mCityAdapter.updateLocateState(LocateState.LOCATING, null);
                mLocationClient.startLocation();
            }
        });

        mResultAdapter = new ResultListAdapter(this, null);
    }


    public void searchCity(final String keyword, final boolean isSearch) {
        result.clear();
        Map<String, String> params = new HashMap<>();
        params.put(Constants.KEY_TYPE, Constants.TYPE_GET_LOCATION_INFO);
        params.put(Constants.KEY_NAME, keyword);
        HttpUtils.getString(getApplicationContext(), Constants.LOCATION_LIST_URL, IAQRequestUtils.getRequestEntity(params), new TubeTask(getApplicationContext(), Constants.GetDataFlag.HON_IAQ_GET_LOCATION_INFO, new IResponse() {
            @Override
            public void response(ArrayList resultList, int resultCode, Object... objects) throws Exception {
                if (resultCode != 0) {
                    return;
                }
                String responseStr = (String) objects[0];
                Log.d(TAG, "responseStr=" + responseStr);
                if (responseStr.contains(Constants.KEY_LOCATION_ID)) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseStr);
                        JSONArray jsonArray = jsonObject.getJSONArray(Constants.KEY_LIST);
                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONObject msgType = (JSONObject) jsonArray.get(i);
                            String locationId = msgType.optString(Constants.KEY_LOCATION_ID);
                            String cityName = msgType.optString(Constants.KEY_NAME);
                            String description = msgType.optString(Constants.KEY_DESCRIPTION);
                            Log.d(TAG, "getLocationInfo: locationID=" + locationId);

                            City city = new City(cityName, locationId, description);
                            result.add(city);
                        }
                        Log.d(TAG, "result.size" + result.size());
                        if (isSearch) {
                            refreshSeachResult();
                        } else {
                            //处理点击GridView 和定位城市 ,正常数量是1，为了防止万一设置成>=1
                            if (result.size() >= 1) {
                                City city = result.get(0);
                                back(city);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        refreshSeachResult();
                    }
                } else {
                    refreshSeachResult();
                }
            }
        }));


    }

    private void back(City city) {
        Intent data = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_PICKED_CITY, city);
        data.putExtras(bundle);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_search_clear) {
            searchBox.setText("");
            clearBtn.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            mResultListView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stopLocation();
    }


}
