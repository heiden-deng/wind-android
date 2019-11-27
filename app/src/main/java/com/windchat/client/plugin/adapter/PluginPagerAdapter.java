package com.windchat.client.plugin.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.util.file.ImageUtils;
import com.akaxin.proto.core.PluginProto;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by alexfan on 2018/3/20.
 */

public class PluginPagerAdapter extends PagerAdapter {
    public static final String TAG = "PluginPagerAdapter";
    private Context mContext;
    private int maxNumPlugins;
    private Site currentSite;

    private OnAppClickListener appClickListener = null;

    private List<PluginProto.Plugin> pluginProfiles = new ArrayList<>();

    public PluginPagerAdapter(Context context, int maxNumPlugins, Site currentSite) {
        super();
        this.mContext = context;
        this.maxNumPlugins = maxNumPlugins;
        this.currentSite = currentSite;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View layout = inflater.inflate(R.layout.item_plugin, container, false);
        final ImageView pluginIcon = layout.findViewById(R.id.plugin_icon);
        TextView pluginLabel = layout.findViewById(R.id.plugin_label);
        layout.setOnClickListener(new itemClick());
        layout.setTag(position);
        if (getAllCount() > maxNumPlugins && position == maxNumPlugins - 1) {
            pluginIcon.setImageDrawable(container.getContext().getDrawable(R.drawable.ic_more_plugins_48dp));
            pluginLabel.setText(R.string.more_plugins);
        } else {
            final PluginProto.Plugin item = pluginProfiles.get(position);
            new ImageUtils(mContext, currentSite).loadImage(item.getIcon(), pluginIcon, R.drawable.ic_default);
//            ZalyGlideModel model = new ZalyGlideModel.Builder()
//                    .setImageID(item.getIcon())
//                    .setFileType(FileProto.FileType.SITE_PLUGIN)
//                    .setSite(currentSite)
//                    .build();
//            GlideApp.with(mContext)
//                    .load(model)
//                    .into(new SimpleTarget<Drawable>() {
//                        @Override
//                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                            pluginIcon.setImageDrawable(resource);
//                        }
//                    });
            pluginLabel.setText(item.getName());
        }

        container.addView(layout);
        return layout;
    }

    @Override
    public float getPageWidth(int position) {
        return (float) 1.0 / maxNumPlugins;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        /////必须写上，不然会导致item 可以左右滑动
        return Math.min(pluginProfiles.size(), maxNumPlugins);
    }


    public int getAllCount() {
        return pluginProfiles.size();
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void updatePlugins(List<PluginProto.Plugin> pluginProfiles, Site site) {
        this.pluginProfiles = pluginProfiles;
        currentSite = site;
        notifyDataSetChanged();
    }

    public void removePlugins() {
        this.pluginProfiles = new ArrayList<>();
        notifyDataSetChanged();
    }

    public interface OnAppClickListener {
        void onAppClick(View v, int position, List<PluginProto.Plugin> pluginProfiles);
    }

    public void setOnAppClickListener(OnAppClickListener listener) {
        this.appClickListener = listener;
    }

    class itemClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (appClickListener != null) {
                //注意这里使用getTag方法获取数据
                appClickListener.onAppClick(v, Integer.parseInt(v.getTag().toString()), pluginProfiles);
            }
        }
    }
}
