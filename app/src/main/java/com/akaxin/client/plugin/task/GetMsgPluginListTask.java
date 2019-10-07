package com.akaxin.client.plugin.task;

/**
 * Created by zhangjun on 2018/3/20.
 */


import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.chat.view.IGroupMsgView;
import com.akaxin.client.chat.view.IMessageView;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.site.ApiPluginListProto;
import com.blankj.utilcode.util.CacheDiskUtils;

import java.util.List;

/**
 * 获取插件列表
 */
public class GetMsgPluginListTask extends ZalyTaskExecutor.Task<Void, Void, ApiPluginListProto.ApiPluginListResponse> {

    public static final String TAG = "GetMsgPluginListTask";
    public static final String GROUP_MSG_PLUGIN = "group";
    public static final String U2_MSG_PLUGIN = "u2";
    public static final int PAGE_SIZE = 32;
    public static final int FIRST_PAGE_NUMBER = 1;

    protected IGroupMsgView iGroupMsgView;
    protected IMessageView iMessageView;
    protected String type;
    protected String chatSessionId;
    protected String referer;
    protected Site site;
    List<PluginProto.Plugin> plugins = null;

    public GetMsgPluginListTask(IGroupMsgView iView, String type, String chatSessionId, Site site) {
        this.type = type;
        this.site = site;

        this.iGroupMsgView = iView;
        this.chatSessionId = chatSessionId;
    }

    public GetMsgPluginListTask(IMessageView iView, String type, String chatSessionId, Site site) {
        this.type = type;
        this.site = site;
        this.iMessageView = iView;
        this.chatSessionId = chatSessionId;
    }

    @Override
    protected void onPreTask() {
        super.onPreTask();
    }

    protected void onCacheTask() {
        byte[] cache = CacheDiskUtils.getInstance().getBytes(site.getSiteIdentity() + SiteConfig.PLUGIN_MSG_LIST);
        if (cache == null)
            return;
        try {
            ApiPluginListProto.ApiPluginListResponse apiPluginListResponse = ApiPluginListProto.ApiPluginListResponse.parseFrom(cache);
            if (type.equals(GROUP_MSG_PLUGIN)) {
                iGroupMsgView.setExpandViewData(apiPluginListResponse.getPluginList());
            } else {
                iMessageView.setExpandViewData(apiPluginListResponse.getPluginList());
            }
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            if (type.equals(GROUP_MSG_PLUGIN)) {
                iGroupMsgView.setExpandViewData(plugins);
            } else {
                iMessageView.setExpandViewData(plugins);
            }
        }

//        String cachePluginList = ZalyApplication.getCfgSP().getString(site.getSiteIdentity() + SiteConfig.PLUGIN_MSG_LIST);
//        if (!StringUtils.isEmpty(cachePluginList)) {
//            byte[] data = Base64.decode(cachePluginList, Base64.NO_WRAP);
//            try {
//                ApiPluginListProto.ApiPluginListResponse apiPluginListResponse = ApiPluginListProto.ApiPluginListResponse.parseFrom(data);
//                if (type.equals(GROUP_MSG_PLUGIN)) {
//                    iGroupMsgView.setExpandViewData(apiPluginListResponse.getPluginList());
//                } else {
//                    iMessageView.setExpandViewData(apiPluginListResponse.getPluginList());
//                }
//            } catch (Exception e) {
//                WindLogger.getInstance().exceptionError(e);
//                if (type.equals(GROUP_MSG_PLUGIN)) {
//                    iGroupMsgView.setExpandViewData(plugins);
//                } else {
//                    iMessageView.setExpandViewData(plugins);
//                }
//            }
//        }
    }

    @Override
    protected ApiPluginListProto.ApiPluginListResponse executeTask(Void... voids) throws Exception {
        if (type.equals(GROUP_MSG_PLUGIN)) {
            referer = SiteConfig.PLUGIN_GROUP_REFERER.replace("siteAddress", site.getHostAndPort());
        } else {
            referer = SiteConfig.PLUGIN_U2_REFERER.replace("siteAddress", site.getHostAndPort());
        }
        referer = referer.replace("chatSessionId", chatSessionId);
        return ApiClient.getInstance(site).getPluginApi().getChatPluginList(referer);
    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {
        ZalyLogUtils.getInstance().exceptionError(zalyAPIException);

        if (type.equals(GROUP_MSG_PLUGIN)) {
            iGroupMsgView.setExpandViewData(plugins);
        } else {
            iMessageView.setExpandViewData(plugins);
        }
    }

    @Override
    protected void onTaskError(Exception e) {
        ZalyLogUtils.getInstance().exceptionError(e);
        if (type.equals(GROUP_MSG_PLUGIN)) {
            iGroupMsgView.setExpandViewData(plugins);
        } else {
            iMessageView.setExpandViewData(plugins);
        }
    }

    @Override
    protected void onTaskSuccess(ApiPluginListProto.ApiPluginListResponse apiPluginListResponse) {
        super.onTaskSuccess(apiPluginListResponse);
        if (type.equals(GROUP_MSG_PLUGIN)) {
            iGroupMsgView.setExpandViewData(apiPluginListResponse.getPluginList());
        } else {
            iMessageView.setExpandViewData(apiPluginListResponse.getPluginList());
        }
        CacheDiskUtils.getInstance().put(site.getSiteIdentity() + SiteConfig.PLUGIN_MSG_LIST, apiPluginListResponse.toByteArray());
//        ZalyApplication.getCfgSP().put(site.getSiteIdentity() + SiteConfig.PLUGIN_MSG_LIST,
//                Base64.encodeToString(apiPluginListResponse.toByteArray(), Base64.NO_WRAP));
    }
}
