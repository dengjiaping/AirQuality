package com.honeywell.iaq.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.bean.Country;
import com.honeywell.iaq.utils.DataHelper;
import com.honeywell.lib.utils.LogUtil;
import com.honeywell.lib.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by E570281 on 12/9/2016.
 */
public class CountryAdapter extends BaseAdapter {
    private final static String TAG = CountryAdapter.class.getSimpleName();
    private List<Country> mList = new ArrayList<>();

    private LayoutInflater inflater;

    public CountryAdapter(Context context) {
        mList = DataHelper.getCountryList(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_country, null);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.ic_country);
            holder.country = (TextView) convertView.findViewById(R.id.country);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.icon.setImageResource(mList.get(position).getIconId());
        holder.country.setText(mList.get(position).getName());
        return convertView;
    }

    static class ViewHolder {
        ImageView icon;
        TextView country;
    }
}
