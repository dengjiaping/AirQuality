package com.honeywell.iaq.adapter;

import com.honeywell.iaq.R;

import java.util.List;


public class IconTextListAdapter extends IconTextBaseAdapter {
    public IconTextListAdapter(List<ItemBean> list) {
        super(list);
    }

    @Override
    public int getItemLayout() {
        return R.layout.list_icon_text;
    }

}
