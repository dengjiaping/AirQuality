package com.honeywell.iaq.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.honeywell.iaq.R;
import com.honeywell.iaq.base.IAQType;
import com.honeywell.iaq.bean.ChildItem;
import com.honeywell.iaq.bean.DeviceInformation;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.Utils;
import com.honeywell.net.utils.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by milton_lin on 17/1/23.
 */

public class DeviceListAdapter extends BaseExpandableListAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<DeviceInformation> groupTitle;

    private Map<Integer, List<ChildItem>> childMap = new HashMap<>();

    public DeviceListAdapter(Context context, List<DeviceInformation> groupTitle, Map<Integer, List<ChildItem>> childMap) {
        mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.groupTitle = groupTitle;
        this.childMap = childMap;
    }

    public void setGroupTitle(List<DeviceInformation> groupTitle) {
        this.groupTitle = groupTitle;
    }

    public void setChildMap(Map<Integer, List<ChildItem>> childMap) {
        this.childMap = childMap;
    }

    /*
     *  Gets the data associated with the given child within the given group
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childMap.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /*
     *  Gets a View that displays the data for the given child within the given group
     */
    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.device_child_item, null);
            childHolder = new ChildHolder();
            childHolder.pmCircle = (ImageView) convertView.findViewById(R.id.pm_circle);
            childHolder.pmValue = (TextView) convertView.findViewById(R.id.pm_value);
            childHolder.room = (TextView) convertView.findViewById(R.id.room);
            childHolder.pmStatus = (TextView) convertView.findViewById(R.id.pm_status);
            childHolder.temp = (ImageView) convertView.findViewById(R.id.icon_temp);
            childHolder.tempValue = (TextView) convertView.findViewById(R.id.temp_value);
            childHolder.humidity = (ImageView) convertView.findViewById(R.id.icon_humidity);
            childHolder.humidityValue = (TextView) convertView.findViewById(R.id.humidity_value);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }

        String pmValue = childMap.get(groupPosition).get(childPosition).getPm25();
        childHolder.pmValue.setText(pmValue);
        childHolder.pmStatus.setText(childMap.get(groupPosition).get(childPosition).getPmStatus());
        showPmStatus(childHolder, childMap.get(groupPosition).get(childPosition).getPmStatus());
//            Log.d(TAG, "getChildView: groupPosition=" + groupPosition + ", childPosition=" + childPosition);
        int pmLevel = childMap.get(groupPosition).get(childPosition).getPmLevel();
        if (pmLevel == Constants.PM_LEVEL_UNKNOWN) {
            childHolder.pmCircle.setBackgroundResource(R.mipmap.not_connected);
        } else if (pmLevel == Constants.PM_LEVEL_1) {
            childHolder.pmCircle.setBackgroundResource(R.mipmap.circle_1);
        } else if (pmLevel == Constants.PM_LEVEL_2) {
            childHolder.pmCircle.setBackgroundResource(R.mipmap.circle_2);
        } else if (pmLevel == Constants.PM_LEVEL_3) {
            childHolder.pmCircle.setBackgroundResource(R.mipmap.circle_3);
        } else if (pmLevel == Constants.PM_LEVEL_4) {
            childHolder.pmCircle.setBackgroundResource(R.mipmap.circle_4);
        } else if (pmLevel == Constants.PM_LEVEL_5) {
            childHolder.pmCircle.setBackgroundResource(R.mipmap.circle_5);
        } else if (pmLevel == Constants.PM_LEVEL_6) {
            childHolder.pmCircle.setBackgroundResource(R.mipmap.circle_6);
        } else {
            childHolder.pmCircle.setBackgroundResource(R.mipmap.not_connected);
        }

        String room = childMap.get(groupPosition).get(childPosition).getRoom();
        if (room != null && room.length() > 0) {
            childHolder.room.setText(room);

            Logger.e("-----", "" + room.toLowerCase());
            if (room != null) {
                if (room.toLowerCase().contains(getString(R.string.bedroom).toLowerCase())) {
                    convertView.setBackgroundResource(R.mipmap.list_bedroom1);
                } else if (room.toLowerCase().contains(getString(R.string.bathroom).toLowerCase())) {
                    convertView.setBackgroundResource(R.mipmap.list_bathroom1);
                } else if (room.toLowerCase().contains(getString(R.string.kitchen).toLowerCase())) {
                    convertView.setBackgroundResource(R.mipmap.list_kitchen1);
                } else {
                    convertView.setBackgroundResource(R.mipmap.list_livingroom1);
                }
            }

        }

        String tempValue = childMap.get(groupPosition).get(childPosition).getTemperature();
        if (getString(R.string.unknown).equals(tempValue)) {
            childHolder.tempValue.setText(tempValue);
        } else {

//            if (Utils.isCelsius(mContext)) {
//                childHolder.tempValue.setText(tempValue + getString(R.string.temperature_unit));
//            }else {
//                float temp = Float.parseFloat(tempValue);
//                childHolder.tempValue.setText(String.valueOf(Utils.C2W(temp)) + getString(R.string.temperature_f_unit));
//            }

            String serialNum = childMap.get(groupPosition).get(childPosition).getSerialNum();

            if (Constants.GEN_1.equals(IAQType.getGeneration(serialNum))) {

                if (Utils.isCelsius(mContext)) {
                    childHolder.tempValue.setText(tempValue + getString(R.string.temperature_unit));
                }else {
                    float temp = Float.parseFloat(tempValue);
                    childHolder.tempValue.setText(String.valueOf(Utils.C2W(temp)) + getString(R.string.temperature_f_unit));
                }

            } else if (Constants.GEN_2.equals(IAQType.getGeneration(serialNum))) {

                Map<String, String> tempMap = childMap.get(groupPosition).get(childPosition).getTemperatureUnit();
                if (tempMap != null && tempMap.size() != 0) {
                    String temperatureUnit = tempMap.get(room);
                    if (Constants.TEMPERATURE_UNIT_C.equals(temperatureUnit)) {
                        childHolder.tempValue.setText(tempValue + getString(R.string.temperature_unit));
                    } else if (Constants.TEMPERATURE_UNIT_F.equals(temperatureUnit)) {
                        float temp = Float.parseFloat(tempValue);
                        childHolder.tempValue.setText(String.valueOf(Utils.C2W(temp)) + getString(R.string.temperature_f_unit));
                    } else {
                        childHolder.tempValue.setText(tempValue + getString(R.string.temperature_unit));
                    }
                }
            }

        }
        String humidityValue = childMap.get(groupPosition).get(childPosition).getHumidity();
        if (getString(R.string.unknown).equals(humidityValue)) {
            childHolder.humidityValue.setText(humidityValue);
        } else {
            childHolder.humidityValue.setText(humidityValue + getString(R.string.percent));
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (childMap == null || childMap.get(groupPosition) == null) return 0;
        return childMap.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupTitle.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groupTitle.size();
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /*
     *Gets a View that displays the given group
     *return: the View corresponding to the group at the specified position
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.device_group_item, null);
            holder = new GroupHolder();
            holder.deviceHomeName = (TextView) convertView.findViewById(R.id.home_name);
            holder.deviceLocation = (TextView) convertView.findViewById(R.id.location);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }

        if (groupTitle != null && groupTitle.size() > 0) {
            holder.deviceHomeName.setText(groupTitle.get(groupPosition).getHome());
            holder.deviceLocation.setText(groupTitle.get(groupPosition).getLoacation());
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        // Indicates whether the child and group IDs are stable across changes to the underlying data
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // Whether the child at the specified position is selectable
        return true;
    }

    /**
     * show the text on the child and group item
     */
    private class GroupHolder {
        TextView deviceLocation;

        TextView deviceHomeName;
    }

    private String getString(int res) {
        return mContext.getString(res);
    }

    private class ChildHolder {
        ImageView pmCircle;

        TextView pmValue;

        TextView room;

        TextView pmStatus;

        ImageView temp;

        TextView tempValue;

        ImageView humidity;

        TextView humidityValue;
    }

    /**
     * 如果正在获取数据或者设备离线则显示状态，否则隐藏状态
     */
    private void showPmStatus(ChildHolder childHolder, String status) {
        if (status.equals(getString(R.string.getting_data)) || status.equals(getString(R.string.get_data_fail))
                || status.equals(getString(R.string.iaq_disconnect)))
            childHolder.pmStatus.setVisibility(View.VISIBLE);
        else
            childHolder.pmStatus.setVisibility(View.GONE);
    }

}
