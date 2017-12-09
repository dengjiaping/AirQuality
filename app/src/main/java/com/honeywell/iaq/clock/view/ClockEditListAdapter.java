package com.honeywell.iaq.clock.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.clock.model.ClockModel;

import java.util.ArrayList;

/**
 * Created by Jin on 06/09/2017.
 */

public class ClockEditListAdapter extends BaseAdapter {

    private TextView mTimeTextView;
    private TextView mNoonTextView;
    private TextView mFreqTextView;
    private ImageView mDeleteImageView;

    private Context mContext = null;
    private ArrayList<ClockModel> mClocks = new ArrayList<>();
    private DeleteClockCallback mDeleteClockCallback;


    public interface DeleteClockCallback {
        void onDelete(ClockModel clock);
    }

    public void setDeleteClockCallback(DeleteClockCallback callback) {
        this.mDeleteClockCallback = callback;
    }

    public ClockEditListAdapter(Context context, ArrayList<ClockModel> clocks) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.list_item_edit_clock, null);
        }

        mTimeTextView = (TextView) convertView.findViewById(R.id.clock_time_tv);
        mNoonTextView = (TextView) convertView.findViewById(R.id.noon_tv);
        mFreqTextView = (TextView) convertView.findViewById(R.id.clock_freq_tv);
        mDeleteImageView = (ImageView) convertView.findViewById(R.id.clock_delete);

        final ClockModel clock = getItem(position);
        mTimeTextView.setText(clock.getTime());
        mNoonTextView.setText(clock.getNoon());
        mFreqTextView.setText(clock.getFrequency());
        mDeleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteClockCallback.onDelete(clock);
            }
        });

        return convertView;
    }

}
