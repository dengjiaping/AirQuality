package com.honeywell.iaq.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.R;

/**
 * Created by E570281 on 7/26/2016.
 */
public class WeekDataFragment extends Fragment {

    private static final String TAG = "DayDataFragment";

    private int mPage = 0;

    public static WeekDataFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_PAGE, page);
        WeekDataFragment fragment = new WeekDataFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(Constants.ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Page=" + mPage);
        View view = inflater.inflate(R.layout.fragment_week_data, container, false);
        return view;
    }
}
