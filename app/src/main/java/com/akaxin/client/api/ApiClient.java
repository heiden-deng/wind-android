package com.akaxin.client.api;

import com.akaxin.client.Configs;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.site.presenter.impl.PlatformPresenter;
import com.akaxin.client.site.presenter.impl.SitePresenter;

import com.akaxin.client.util.ClientTypeHepler;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.google.protobuf.AbstractMessageLite;
import com.google.protobuf.ByteString;
import com.windchat.im.socket.IMConnection;
import com.windchat.im.socket.ConnectionConfig;
import com.windchat.im.socket.SiteAddress;
import com.windchat.im.socket.TransportPackageForRequest;
import com.windchat.im.socket.TransportPackageForResponse;
import com.windchat.proto.core.CoreProto;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sssl on 08/06/2018.
 */

public class ApiClient {
    private static final String TAG = "ApiClient";
    private ApiClientForUser userApi = null;
    private ApiClientForFile fileApi = null;
    private ApiClientForSite siteApi = null;
    private ApiClientForFriend friendApi = null;
    private ApiClientForGroup groupApi = null;
    private ApiClientForPhone phoneApi = null;
    private ApiClientForDevice deviceApi = null;
    private ApiClientForPlugin pluginApi = null;
    private ApiClientForSetting settingApi = null;
    private ApiClientForTemp tempApi = null;
    private SiteAddress address = null;


    public static ApiClient getInstance(ConnectionConfig config) {
        SiteAddress address = new SiteAddress(config);
        return new ApiClient(address);
    }

    public static ApiClient getInstance(com.akaxin.client.bean.Site beanSite) {
        SiteAddress address = new SiteAddress(beanSite);
        return new ApiClient(address);
    }


    public ApiClient(SiteAddress addr) {
        this.address = addr;
        this.userApi = new ApiClientForUser(this);
        this.fileApi = new ApiClientForFile(this);
        this.siteApi = new ApiClientForSite(this);
        this.friendApi = new ApiClientForFriend(this);
        this.groupApi = new ApiClientForGroup(this);
        this.phoneApi = new ApiClientForPhone(this);
        this.pluginApi = new ApiClientForPlugin(this);
        this.deviceApi = new ApiClientForDevice(this);
        this.settingApi = new ApiClientForSetting(this);
        this.tempApi = new ApiClientForTemp(this);
    }

    public ApiClientForUser getUserApi() {
        return this.userApi;
    }

    public ApiClientForSite getSiteApi() {
        return this.siteApi;
    }

    public ApiClientForFile getFileApi() {
        return this.fileApi;
    }

    public ApiClientForGroup getGroupApi() {
        return this.groupApi;
    }

    public ApiClientForFriend getFriendApi() {
        return this.friendApi;
    }

    public ApiClientForPlugin getPluginApi() {
        return this.pluginApi;
    }

    public ApiClientForPhone getPhoneApi() {
        return this.phoneApi;
    }

    public ApiClientForDevice getDeviceApi() {
        return this.deviceApi;
    }

    public ApiClientForTemp getTempApi() {
        return this.tempApi;
    }

    public ApiClientForSetting getSettingApi() {
        return this.settingApi;
    }

    /**
     * 执行Api请求 <- 默认请使用这个方法
     *
     * @param action
     * @param request
     * @return
     * @throws Exception
     */
    public TransportPackageForResponse sendRequest(String action, AbstractMessageLite request) throws Exception {
        return this.sendRequestWithHeader(action, request, null);
    }


    public TransportPackageForResponse sendRequestWithReferer(String action, AbstractMessageLite request, String referer) throws Exception {
        Map<Integer, String> header = new HashMap<>();
        header.put(CoreProto.HeaderKey.CLIENT_REQUEST_REFERER_VALUE, referer);
        return this.sendRequestWithHeader(action, request, header);
    }

    public TransportPackageForResponse sendRequestWithHeader(String action, AbstractMessageLite request, Map<Integer, String> header) throws Exception {
        Map<Integer, String> finalHeader = new HashMap<>();

        // 这个地方要改成根据站点获取
        String sessionId = "";
        // 需要判断赋予哪一个Session
        if (this.address.getHost() == ServerConfig.PLATFORM_ADDRESS) {
            sessionId = PlatformPresenter.getInstance().getPlatformSessionId();
        } else {
            Site siteInfo = SitePresenter.getInstance().getSiteUser(this.address.getFullUrl());
            sessionId = siteInfo.getSiteSessionId();
        }

        ZalyLogUtils.getInstance().info(TAG, "ApiClient action:" + action + " siteAddress:" + this.address.getFullUrl() + " session:" + sessionId);
        finalHeader.put(CoreProto.HeaderKey.CLIENT_SOCKET_SITE_SESSION_ID_VALUE, sessionId == null ? "" : sessionId);
        finalHeader.put(CoreProto.HeaderKey.CLIENT_REQUEST_SERVER_HOST_VALUE, this.address.getHost());
        finalHeader.put(CoreProto.HeaderKey.CLIENT_REQUEST_SERVER_PORT_VALUE, "" + this.address.getPort());
        finalHeader.put(CoreProto.HeaderKey.CLIENT_SOCKET_VERSION_VALUE, Configs.APP_VERSION);
        finalHeader.put(CoreProto.HeaderKey.CLIENT_SOCKET_TYPE_VALUE, ClientTypeHepler.getClientTypeString());
        if (null != header && header.size() > 0) {
            finalHeader.putAll(header);
        }

        CoreProto.TransportPackageData packageData = CoreProto.TransportPackageData.newBuilder().
                setData(ByteString.copyFrom(request.toByteArray())).
                putAllHeader(finalHeader).
                build();

        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            return null;
        }

        IMConnection imConnection = new IMConnection(this.address.toConnectionConfig());
        imConnection.logTag = "ApiClient.IMConnection";
        TransportPackageForRequest realRequest = new TransportPackageForRequest(action, packageData);
        TransportPackageForResponse response = imConnection.requestAndResponse(realRequest);

        CoreProto.ErrorInfo errorInfo = response.data.getErr();
        String errorCode = errorInfo.getCode();
        if (StringUtils.isEmpty(errorCode) || !errorCode.equals("success")) {
            throw new ZalyAPIException(ZalyAPIException.TYPE_ERRINFO_CODE, errorInfo.getCode(), errorInfo.getInfo(), response);
        }
        return response;
    }

}
