package com.windchat.client.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.maintab.adapter.ZalyListAdapter;
import com.windchat.client.util.UIUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.file.ImageUtils;
import com.windchat.im.IMClient;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/10.
 */

public class SiteListAdapter extends ZalyListAdapter<Site, SiteListAdapter.ViewHolder> {

    private SiteListItemListener itemOnClickListener;
    Context mContext;
    private Site currentSite;

    public SiteListAdapter(Context mContext, Site site) {
        this.mContext = mContext;
        this.currentSite = site;
    }

    public void setItemOnClickListener(SiteListItemListener itemOnClickListener) {
        this.itemOnClickListener = itemOnClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_site, parent, false);
        return new SiteListAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Site site = items.get(position);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(site.getSiteIcon())
//                .setFileType(FileProto.FileType.SITE_ICON)
//                .setSite(site)
//                .build();
//        Glide.with(mContext).load(model).
//                apply(new RequestOptions().dontAnimate()
//                        .error(R.drawable.site_default_icon).fallback(R.drawable.site_default_icon)).into(holder.siteIcon);
        new ImageUtils(mContext, currentSite).loadSiteIcon(site.getSiteIcon(), holder.siteIcon);

        String siteName = site.getSiteName();
        if (StringUtils.isEmpty(siteName)) {
            siteName = "";
        }
        holder.siteName.setText(siteName);
        holder.siteAddress.setText(site.getSiteDisplayAddress());
        boolean isConnected;

        try {
            isConnected = IMClient.getInstance(site).isConnected();
        } catch (Exception e) {
            isConnected = false;
        }

        holder.siteStatus.setText(isConnected ? "已连接" : "连接断开");
        boolean isMute = site.isMute();
        holder.notificationOffIcon.setVisibility(isMute ? View.VISIBLE : View.INVISIBLE);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemOnClickListener != null) {
                    itemOnClickListener.onSiteClick(site);
                }
            }
        });
        if (site.getUnreadNum() > 0) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.unreadNum.getLayoutParams();
            if (isMute) {
                lp.width = UIUtils.getPixels(10);
                lp.height = UIUtils.getPixels(10);
                int margin = UIUtils.getPixels(2);
                lp.setMargins(margin, margin, margin, margin);
                holder.unreadNum.setText("");
            } else {
                lp.width = UIUtils.getPixels(16);
                lp.height = UIUtils.getPixels(16);
                lp.setMargins(0, 0, 0, 0);
                holder.unreadNum.setText(StringUtils.getBubbleString((int) site.getUnreadNum()));
            }
            holder.unreadNum.setLayoutParams(lp);
            holder.unreadNum.setVisibility(View.VISIBLE);
        } else {
            holder.unreadNum.setVisibility(View.INVISIBLE);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_layout)
        View itemLayout;
        @BindView(R.id.site_icon)
        ImageView siteIcon;
        @BindView(R.id.site_name)
        TextView siteName;
        @BindView(R.id.site_address)
        TextView siteAddress;
        @BindView(R.id.unread_num)
        TextView unreadNum;
        @BindView(R.id.site_status)
        TextView siteStatus;
        @BindView(R.id.notification_off_icon)
        View notificationOffIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface SiteListItemListener {
        void onSiteClick(Site site);
    }
}
