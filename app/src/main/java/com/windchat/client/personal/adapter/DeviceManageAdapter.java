package com.windchat.client.personal.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.windchat.client.bean.Site;
import com.windchat.client.maintab.adapter.ZalyListAdapter;
import com.windchat.client.util.data.StringUtils;
import com.akaxin.proto.core.DeviceProto;
import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.util.DateUtil;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/31.
 */

public class DeviceManageAdapter extends ZalyListAdapter<DeviceProto.SimpleDeviceProfile, DeviceManageAdapter.ViewHolder> {

    private DeviceManageListener deviceManageListener;
    private Site currentSite;

    public DeviceManageAdapter(Site site) {
        this.currentSite = site;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_device_manage, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final DeviceProto.SimpleDeviceProfile simpleDeviceInfo = items.get(position);
        String deviceName = simpleDeviceInfo.getDeviceName();
        if (StringUtils.isEmpty(deviceName))
            deviceName = holder.deviceNameTv.getContext().getString(R.string.unknown_device);
        holder.deviceNameTv.setText(deviceName);
        holder.deviceInfo.setText(String.format(holder.deviceInfo.getContext().getString(R.string.device_info),
                currentSite.getSiteName(),
                currentSite.getSiteAddress(),
                DateUtil.formateDateTime(new Date(simpleDeviceInfo.getLastLoginTime()))));
        holder.deviceLoginTimeDescTv.setText(DateUtil.getTimeLineString(new Date(simpleDeviceInfo.getLastLoginTime())));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_layout) View itemLayout;
        @BindView(R.id.device_name) TextView deviceNameTv;
        @BindView(R.id.device_login_time_desc) TextView deviceLoginTimeDescTv;
        @BindView(R.id.device_info) TextView deviceInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setDeviceManageListener(DeviceManageListener deviceManageListener) {
        this.deviceManageListener = deviceManageListener;
    }

    public interface DeviceManageListener {
        void onDeviceCancelfBind(int pos, DeviceProto.SimpleDeviceProfile simpleDeviceInfo);
    }

}
