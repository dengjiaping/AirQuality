package com.honeywell.iaq.clock.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.clock.model.ClockModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jin on 06/09/2017.
 */

public class ClockListAdapter extends BaseAdapter {

    private TextView mTimeTextView;
    private TextView mNoonTextView;
    private TextView mFreqTextView;
    private CheckBox mActiveCheckbox;

    private Context mContext = null;
    private ArrayList<ClockModel> mClocks = new ArrayList<>();
    private Map<Integer, Boolean> mClockCheckedMap = new HashMap<>();
    private ToggleClockCallback mToggleClockCallback;

    public interface ToggleClockCallback {
        void onSet(int clockId, boolean isOn);
    }

    public void setToggleClockCallback(ToggleClockCallback callback) {
        this.mToggleClockCallback = callback;
    }

    public ClockListAdapter(Context context, ArrayList<ClockModel> clocks) {
        this.mContext = context;
        this.mClocks = clocks;
    }


    @Override
    public int getCount() {
        return mClocks == null ? 0 : mClocks.size();
    }

    @Override
    public ClockModel getItem(int position) {
        return mClocks == null ? null : mClocks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.list_item_clock, null);
        }

        mTimeTextView = (TextView) convertView.findViewById(R.id.clock_time_tv);
        mNoonTextView = (TextView) convertView.findViewById(R.id.noon_tv);
        mFreqTextView = (TextView) convertView.findViewById(R.id.clock_freq_tv);
        mActiveCheckbox = (CheckBox) convertView.findViewById(R.id.clock_checkbox);

        ClockModel clock = getItem(position);
        mTimeTextView.setText(clock.getTime());
        mNoonTextView.setText(clock.getNoon());
        mFreqTextView.setText(clock.getFrequency());
        mActiveCheckbox.setChecked(clock.isActive());
        mClockCheckedMap.put(position, clock.isActive());

        mActiveCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mClockCheckedMap.put(position, isChecked);
            }
        });

        mActiveCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mToggleClockCallback.onSet(position, mClockCheckedMap.get(position));
            }
        });

        return convertView;
    }

}
