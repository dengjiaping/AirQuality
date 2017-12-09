package com.honeywell.iaq.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.honeywell.iaq.R;
import com.honeywell.iaq.bean.City;

import java.util.List;

/**
 * Created by zhujunyu on 2017/4/5.
 */
public class ResultListAdapter extends BaseAdapter {
    private Context mContext;
    private List<City> mCities;

    public ResultListAdapter(Context mContext, List<City> mCities) {
        this.mCities = mCities;
        this.mContext = mContext;
    }

    public void changeData(List<City> list){
        mCities = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mCities == null ? 0 : mCities.size();
    }

    @Override
    public City getItem(int position) {
        return mCities == null ? null : mCities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ResultViewHolder holder;
        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.cp_item_search_result_listview, parent, false);
            holder = new ResultViewHolder();
            holder.name = (TextView) view.findViewById(R.id.tv_item_result_listview_name);
            view.setTag(holder);
        }else{
            holder = (ResultViewHolder) view.getTag();
        }
        Log.d("ResultListAdapter", "mCities.get(position).getDescription():" + mCities.get(position).getDescription());
        holder.name.setText(mCities.get(position).getDescription());
        return view;
    }

    public static class ResultViewHolder{
        TextView name;
    }
}
