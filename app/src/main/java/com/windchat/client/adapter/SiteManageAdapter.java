package com.windchat.client.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.maintab.adapter.ZalyListAdapter;
import com.windchat.client.util.file.ImageUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/31.
 */

public class SiteManageAdapter extends ZalyListAdapter<Site, SiteManageAdapter.ViewHolder> {

    private SiteManageListener manageListener;
    Context mContext;

    public SiteManageAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_site_manage, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Site site = items.get(position);
        holder.siteName.setText(site.getSiteName());
        holder.siteAddress.setText(site.getHostAndPort());

//              ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(site.getSiteIcon())
//                .setFileType(FileProto.FileType.SITE_ICON)
//                .setSite(site)
//                .build();
//        Glide.with(mContext).load(model).
//                apply(new RequestOptions().dontAnimate().error(R.drawable.site_default_icon).fallback(R.drawable.site_default_icon)).into(holder.siteIcon);
        new ImageUtils(mContext, site).loadSiteIcon(site.getSiteIcon(), holder.siteIcon);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (manageListener != null) {
                    manageListener.onSiteClick(site);
                }
            }
        });
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

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setManageListener(SiteManageListener manageListener) {
        this.manageListener = manageListener;
    }

    public interface SiteManageListener {
        void onSiteClick(Site site);
    }

}
