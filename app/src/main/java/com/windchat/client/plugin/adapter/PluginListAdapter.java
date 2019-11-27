package com.windchat.client.plugin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.constant.SiteConfig;
import com.windchat.client.maintab.adapter.ZalyListAdapter;
import com.windchat.client.plugin.PluginUtils;
import com.windchat.client.util.file.ImageUtils;
import com.akaxin.proto.core.PluginProto;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by alexfan on 2018/3/21.
 * <p>
 * 用来单一展示plugin列表的adapter.
 */

public class PluginListAdapter extends ZalyListAdapter<PluginProto.Plugin, PluginListAdapter.PluginViewHolder> {
    Context mContext;
    private Site currentSite;

    public PluginListAdapter(Context mContext, Site site) {
        this.currentSite = site;
        this.mContext = mContext;
    }

    @Override
    public PluginViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_plugin, parent, false);
        return new PluginViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PluginViewHolder holder, int position) {
        final PluginProto.Plugin plugin = items.get(position);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PluginUtils.gotoWebActivity(currentSite, view.getContext(), plugin, SiteConfig.PLUGIN_HOME_REFERER, true);
            }
        });
        new ImageUtils(mContext, currentSite).loadImage(plugin.getIcon(), holder.pluginIcon);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(plugin.getIcon())
//                .setFileType(FileProto.FileType.SITE_PLUGIN)
//                .setSite(currentSite)
//                .build();
//        Glide.with(mContext).load(model).
//                apply(new RequestOptions()
//                        .dontAnimate()
//                        .error(R.drawable.ic_default)
//                        .fallback(R.drawable.ic_default))
//                .into(holder.pluginIcon);
        holder.pluginLabel.setText(plugin.getName());
        holder.pluginDescription.setVisibility(View.GONE);
    }

    public class PluginViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_layout)
        View itemLayout;
        @BindView(R.id.plugin_icon)
        ImageView pluginIcon;
        @BindView(R.id.plugin_label)
        TextView pluginLabel;
        @BindView(R.id.plugin_description)
        TextView pluginDescription;

        public PluginViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
