package com.honeywell.iaq.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.honeywell.iaq.Pins;
import com.honeywell.iaq.R;

import java.util.ArrayList;
import java.util.List;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ShortcutViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private Activity mContext;
    private List<Pins.Item> mItems;
    private int mCheckedPos = -1;
    private ClickListener mListener;

    public NavigationDrawerAdapter(Activity context, ClickListener listener) {
        mContext = context;
        mItems = new ArrayList<>();
        mListener = listener;
        if (Pins.getAll(context).size() == 0) {
            Pins.add(context, new Pins.Item(mContext.getString(R.string.add_iaq), "Add"));
            Pins.add(context, new Pins.Item(mContext.getString(R.string.about), "About"));
            Pins.add(context, new Pins.Item(mContext.getString(R.string.logout), "Logout"));
        }
        reload(context);
    }

    @Override
    public void onClick(View view) {
        mListener.onClick((Integer) view.getTag());
    }

    @Override
    public boolean onLongClick(View view) {
        mListener.onLongClick((Integer) view.getTag());
        return false;
    }

    public void reload(Context context) {
        set(Pins.getAll(context));
    }

    public void set(List<Pins.Item> items) {
        mItems.clear();
        for (Pins.Item i : items)
            mItems.add(i);
        notifyDataSetChanged();
    }

    public int setCheckedItem(String name) {
        int index = -1;
        for (int i = 0; i < mItems.size(); i++) {
            Pins.Item item = mItems.get(i);
            if (item.getName().equals(name)) {
                index = i;
                break;
            }
        }
        setCheckedPos(index);
        return index;
    }

    public void setCheckedPos(int index) {
        mCheckedPos = index;
        notifyDataSetChanged();
    }

    public Pins.Item getItem(int index) {
        return mItems.get(index);
    }

    @Override
    public ShortcutViewHolder onCreateViewHolder(ViewGroup parent, int index) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_drawer, parent, false);
        return new ShortcutViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ShortcutViewHolder holder, int index) {
        Pins.Item item = mItems.get(index);
        holder.title.setTag(index);
        holder.title.setOnClickListener(this);
        holder.title.setOnLongClickListener(this);
        holder.title.setActivated(mCheckedPos == index);
        if (mCheckedPos == index) {
            holder.title.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        } else {
            holder.title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        }

        holder.title.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface ClickListener {
        public abstract void onClick(int index);

        public abstract boolean onLongClick(int index);
    }

    public static class ShortcutViewHolder extends RecyclerView.ViewHolder {

        TextView title;

        public ShortcutViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView;
        }
    }
}
