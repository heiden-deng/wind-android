package com.akaxin.client.chat.view.impl;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

import com.akaxin.client.util.DateUtil;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.proto.core.DeviceProto;
import com.akaxin.client.R;
import com.akaxin.client.maintab.adapter.ZalyListAdapter;

/**
 * Created by yichao on 2017/11/9.
 */

public class DeviceListAdapter extends ZalyListAdapter<DeviceProto.SimpleDeviceProfile, DeviceListAdapter.ViewHolder> {

    public String loginTime;

    public interface FriendDeviceInterface {
        void onDeviceClick(DeviceProto.SimpleDeviceProfile deviceInfo);
    }

    private FriendDeviceInterface deviceInterface;

    public void setDeviceInterface(FriendDeviceInterface deviceInterface) {
        this.deviceInterface = deviceInterface;
    }

    @Override
    public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_device, parent, false);
        return new DeviceListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DeviceProto.SimpleDeviceProfile deviceInfo = items.get(position);
        if(StringUtils.isEmpty(deviceInfo.getDeviceName())) {
            holder.deviceNameTv.setText(R.string.unknown_device);
        } else {
            holder.deviceNameTv.setText(deviceInfo.getDeviceName());
        }
        Date date = new Date(deviceInfo.getLastLoginTime());
        loginTime = DateUtil.getTimeLineString(date);
        holder.deviceLastTimeTv.setText(loginTime);
        holder.deviceLastTimeTv.setVisibility(View.VISIBLE);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deviceInterface != null) {
                    deviceInterface.onDeviceClick(deviceInfo);
                }
            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private View itemLayout;
        private TextView deviceNameTv;
        private TextView deviceLastTimeTv;

        public ViewHolder(View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.item_layout);
            deviceNameTv = itemView.findViewById(R.id.device_name);
            deviceLastTimeTv = itemView.findViewById(R.id.device_last_time);
        }
    }
}
