package com.windchat.client.api;

import com.windchat.client.plugin.task.GetMsgPluginListTask;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.site.ApiPluginListProto;
import com.akaxin.proto.site.ApiPluginPageProto;
import com.akaxin.proto.site.ApiPluginProxyProto;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForPlugin {
    private static final String API_PLUGIN_LIST = "api.plugin.list";
    private static final String API_PLUGIN_PAGE = "api.plugin.page";
    private static final String API_PLUGIN_PROXY = "api.plugin.proxy";
    private ApiClient client = null;
    private String logTag = "";

    public ApiClientForPlugin(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }

    /**
     * 获取主页插件列表
     *
     * @param referer
     * @return
     * @throws Exception
     */
    public ApiPluginListProto.ApiPluginListResponse getPluginList(String referer) throws Exception {
        ApiPluginListProto.ApiPluginListRequest request = ApiPluginListProto.ApiPluginListRequest.newBuilder()
                .setPositionValue(PluginProto.PluginPosition.HOME_PAGE_VALUE)
                .setPageNumber(GetMsgPluginListTask.FIRST_PAGE_NUMBER)
                .setPageSize(GetMsgPluginListTask.PAGE_SIZE)
                .build();
        TransportPackageForResponse response = this.client.sendRequestWithReferer(API_PLUGIN_LIST, request, referer);

        return ApiPluginListProto.ApiPluginListResponse.parseFrom(response.data.getData());
    }


    /**
     * 获取聊天插件列表
     *
     * @param referer
     * @return
     * @throws Exception
     */
    public ApiPluginListProto.ApiPluginListResponse getChatPluginList(String referer) throws Exception {
        ApiPluginListProto.ApiPluginListRequest request = ApiPluginListProto.ApiPluginListRequest.newBuilder()
                .setPositionValue(PluginProto.PluginPosition.MSG_PAGE_VALUE)
                .setPageNumber(GetMsgPluginListTask.FIRST_PAGE_NUMBER)
                .setPageSize(GetMsgPluginListTask.PAGE_SIZE)
                .build();
        TransportPackageForResponse response = this.client.sendRequestWithReferer(API_PLUGIN_LIST, request, referer);
        return ApiPluginListProto.ApiPluginListResponse.parseFrom(response.data.getData());
    }


    /**
     * 获取web页内容
     * @param pluginId
     * @param referer
     * @return
     * @throws Exception
     */
    public ApiPluginPageProto.ApiPluginPageResponse getPage(String pluginId, String referer) throws Exception {
        ApiPluginPageProto.ApiPluginPageRequest request = ApiPluginPageProto.ApiPluginPageRequest.newBuilder()
                .setPluginId(pluginId)
                .build();
        TransportPackageForResponse response = this.client.sendRequestWithReferer(API_PLUGIN_PAGE, request, referer);
        return ApiPluginPageProto.ApiPluginPageResponse.parseFrom(response.data.getData());

    }


    /**
     * 插件代理请求
     * @param apiName
     * @param params
     * @param pluginId
     * @param referer
     * @return
     * @throws Exception
     */
    public ApiPluginProxyProto.ApiPluginProxyResponse pluginProxy(String apiName,String params,String pluginId,String referer) throws Exception {
        ApiPluginProxyProto.ApiPluginProxyRequest request = ApiPluginProxyProto.ApiPluginProxyRequest.newBuilder()
                .setApi(apiName)
                .setParams(params)
                .setPluginId(pluginId)
                .build();
        TransportPackageForResponse response = this.client.sendRequestWithReferer(API_PLUGIN_PROXY, request, referer);
        return ApiPluginProxyProto.ApiPluginProxyResponse.parseFrom(response.data.getData());
    }

}
