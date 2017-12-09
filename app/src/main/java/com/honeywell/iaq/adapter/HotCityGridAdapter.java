package com.honeywell.iaq.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.honeywell.iaq.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhujunyu on 2017/4/5.
 */
public class HotCityGridAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mCities;

    public HotCityGridAdapter(Context context) {
        this.mContext = context;
        mCities = new ArrayList<>();
        mCities.add(mContext.getResources().getString(R.string.hot_city_beijing));
        mCities.add(mContext.getResources().getString(R.string.hot_city_shanghai));
        mCities.add(mContext.getResources().getString(R.string.hot_city_guangzhou));
        mCities.add(mContext.getResources().getString(R.string.hot_city_shenzhen));
//        mCities.add(mContext.getResources().getString(R.string.hot_city_delhi));
        mCities.add(mContext.getResources().getString(R.string.hot_city_suzhou));
        mCities.add(mContext.getResources().getString(R.string.hot_city_bombay));
        mCities.add(mContext.getResources().getString(R.string.hot_city_calcutta));
        mCities.add(mContext.getResources().getString(R.string.hot_city_chennai));
//        mCities.add(mContext.getResources().getString(R.string.hot_city_bangalore));
        mCities.add(mContext.getResources().getString(R.string.hot_city_delhi));

    }

    @Override
    public int getCount() {
        return mCities == null ? 0 : mCities.size();
    }

    @Override
    public String getItem(int position) {
        return mCities == null ? null : mCities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        HotCityViewHolder holder;
        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.cp_item_hot_city_gridview, parent, false);
            holder = new HotCityViewHolder();
            holder.name = (TextView) view.findViewById(R.id.tv_hot_city_name);
            view.setTag(holder);
        }else{
            holder = (HotCityViewHolder) view.getTag();
        }
        holder.name.setText(mCities.get(position));
        return view;
    }

    public static class HotCityViewHolder{
        TextView name;
    }
}
