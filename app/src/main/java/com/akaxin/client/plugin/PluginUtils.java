package com.akaxin.client.plugin;

import android.content.Context;
import android.content.Intent;

import com.akaxin.client.bean.Site;
import com.akaxin.client.bridge.PluginWebActivity;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.proto.core.PluginProto;

/**
 * Created by alexfan on 2018/3/21.
 */

public class PluginUtils {
    /**
     * Go to the corresponding web view for a plugin.
     */
    public static void gotoWebActivity(Site site, Context mContext, PluginProto.Plugin plugin, String referrer, boolean isAddCookie) {
        Intent intent = new Intent(mContext, PluginWebActivity.class);
        intent.putExtra(PluginWebActivity.KEY_WEB_VIEW_DATA, plugin.toByteArray());
        intent.putExtra(PluginWebActivity.REFERER, referrer);
        intent.putExtra(PluginWebActivity.IS_ADD_COOKIE, isAddCookie);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, site);
        mContext.startActivity(intent);
    }
}
