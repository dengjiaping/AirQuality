package com.honeywell.iaq.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;

import android.view.View;
import android.view.ViewGroup;

import com.honeywell.iaq.bean.IAQData;

import java.util.List;

/**
 * Created by zhujunyu on 2017/2/20.
 */

public class DetailViewPagerAdapter extends PagerAdapter {

    private List<View> viewList;
    private int mChildCount;

    public DetailViewPagerAdapter(List<View> viewList) {

        this.viewList = viewList;
        mChildCount = viewList.size();
    }

    @Override
    public int getCount() {
        return viewList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        // TODO Auto-generated method stub
        container.removeView(viewList.get(position));
    }

    @Override
    public void notifyDataSetChanged() {
//        mChildCount = getCount();
        super.notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(Object object) {
//        if (mChildCount > 0) {
//            mChildCount--;
//            return POSITION_NONE;
//        }
        return super.getItemPosition(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO Auto-generated method stub
        container.addView(viewList.get(position));

        return viewList.get(position);
    }
}
