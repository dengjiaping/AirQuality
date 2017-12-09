
package com.honeywell.iaq.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;


import com.honeywell.iaq.R;

import java.util.List;


public abstract class IconTextBaseAdapter extends BaseAdapter {
    List<ItemBean> mItemList;

    private LayoutInflater mInflater;

    protected int mRows = -1;
    protected int mColumns = -1;

    public IconTextBaseAdapter(List<ItemBean> list) {
        mItemList = list;
    }

    public abstract int getItemLayout();

    @Override
    public int getCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    @Override
    public Object getItem(int i) {
        return mItemList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemHolder holder;
        if (convertView == null) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(parent.getContext());
            }
            holder = new ItemHolder();
            convertView = mInflater.inflate(getItemLayout(), null);
            if (mRows > 0 && mColumns > 0) {
                int totalHeight = parent.getMeasuredHeight() - parent.getPaddingTop() - parent.getPaddingBottom() - ((GridView) parent).getVerticalSpacing() * (mRows - 1);
                int totalWidth = parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight() - ((GridView) parent).getHorizontalSpacing() * (mColumns - 1);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(totalWidth / mColumns, totalHeight / mRows);
                convertView.setLayoutParams(params);
            }
            holder.icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.text = (TextView) convertView.findViewById(R.id.tv_text);
            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }
        holder.icon.setImageResource(mItemList.get(position).getIconId());
//        holder.icon.setImageDrawable(DrawableUtil.tintDrawable(parent.getContext(), mItemList.get(position).getIconId(), Color.BLUE));
        holder.text.setText(mItemList.get(position).getText());
        return convertView;
    }

    public void setRows(int rows) {
        this.mRows = rows;
    }

    public void setColumns(int columns) {
        this.mColumns = columns;
    }

    public List<ItemBean> getItemList() {
        return mItemList;
    }

    public void setItemList(List<ItemBean> itemList) {
        this.mItemList = itemList;
    }

    protected static class ItemHolder {
        public ImageView icon;
        public TextView text;
    }

    public static class ItemBean {
        public int mIconId;
        public Drawable mIcon;
        public String mText;
        public String mType;
        public Object mObject;

        public ItemBean(int iconId, Drawable icon, String text, String type, Object object) {
            mIconId = iconId;
            mIcon = icon;
            mText = text;
            mType = type;
            mObject = object;
        }

        public int getIconId() {
            return mIconId;
        }

        public void setIconId(int iconId) {
            this.mIconId = iconId;
        }

        public Drawable getIcon() {
            return mIcon;
        }

        public void setIcon(Drawable icon) {
            this.mIcon = icon;
        }

        public String getText() {
            return mText;
        }

        public void setText(String text) {
            this.mText = text;
        }

        public String getType() {
            return mType;
        }

        public void setType(String type) {
            this.mType = type;
        }
    }

}
