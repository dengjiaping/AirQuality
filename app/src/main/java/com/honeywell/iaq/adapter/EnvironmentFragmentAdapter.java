package com.honeywell.iaq.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.honeywell.iaq.base.IAQBaseFragment;

import java.util.List;

/**
 * Created by milton_lin on 17/1/24.
 */

public class EnvironmentFragmentAdapter extends FragmentPagerAdapter {
    private List<IAQBaseFragment> mListFragments;

    public EnvironmentFragmentAdapter(FragmentManager fm, List<IAQBaseFragment> mListFragments) {
        super(fm);
        this.mListFragments = mListFragments;
    }

    @Override
    public Fragment getItem(int position) {

        return mListFragments.get(position);

//        if (position == 0) {
//            Log.e("____________","_______0__________");
//            return mListFragments.get(0);
//        } else if (position == 1) {
//            Log.e("____________","_______1__________");
//            return new EnvironmentDetialFragment3();
//        } else  {
//            Log.e("____________","_______2__________");
//            return new EnvironmentChartFragment();
//        }
    }

    @Override
    public int getCount() {
        return mListFragments.size();
    }
}
