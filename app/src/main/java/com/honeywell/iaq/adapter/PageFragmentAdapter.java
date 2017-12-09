//package com.honeywell.iaq.adapter;
//
//import android.content.Context;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.util.Log;
//
//import com.honeywell.iaq.R;
//import com.honeywell.iaq.fragment.DayDataFragment;
//import com.honeywell.iaq.fragment.MonthDataFragment;
//
///**
// * Created by E570281 on 10/14/2015.
// */
//public class PageFragmentAdapter extends FragmentPagerAdapter {
//
//    private final int PAGE_COUNT = 2;
//
//    private String[] tabTitles = null;
//
//    private Context mContext;
//
//    public PageFragmentAdapter(FragmentManager fm, Context context) {
//        super(fm);
//        this.mContext = context;
//        tabTitles = new String[]{mContext.getString(R.string.statistic_day), mContext.getString(R.string.statistic_month)};
//    }
//
//    @Override
//    public int getCount() {
//        return PAGE_COUNT;
//    }
//
//    @Override
//    public Fragment getItem(int position) {
//        Log.d("getItem", "position=" + position);
//        if (position == 0) {
//            return DayDataFragment.newInstance(position);
//        } else {
//            return MonthDataFragment.newInstance(position);
//        }
//    }
//
//    @Override
//    public CharSequence getPageTitle(int position) {
//        return tabTitles[position];
//    }
//}
