package com.akaxin.client.platform.task;

import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.platform.ApiSettingSiteMuteProto;
import com.akaxin.proto.site.ApiUserMuteProto;
import com.windchat.im.socket.ConnectionConfig;

/**
 * Created by zhangjun on 04/06/2018.
 */

/**
 * 从平台获取站点是否静音。
 * <p>
 * 获取平台接口时，
 * 1. 如果结果为静音状态，开关设置为静音状态，站点接口不需要访问
 * 2. 如果平台获取结果为非静音，需要调用站点的接口，进行依次校验
 */
public class ApiSettingSiteMuteTask extends ZalyTaskExecutor.Task<Void, Void, ApiSettingSiteMuteProto.ApiSettingSiteMuteResponse> {

    private Site site;

    public ApiSettingSiteMuteTask(Site site) {
        this.site = site;
    }

    @Override
    protected ApiSettingSiteMuteProto.ApiSettingSiteMuteResponse executeTask(Void... voids) throws Exception {
        return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite()).getSettingApi().getSiteSetting(site);
    }

    @Override
    protected void onTaskSuccess(ApiSettingSiteMuteProto.ApiSettingSiteMuteResponse apiSettingSiteMuteResponse) {
        super.onTaskSuccess(apiSettingSiteMuteResponse);
        boolean messageMute = apiSettingSiteMuteResponse.getMute();
        SitePresenter.getInstance().updateSiteMute(site.getSiteHost(), site.getSitePort() + "", messageMute);
        if (!messageMute) {
            ZalyTaskExecutor.executeUserTask(TAG, new GetSiteMuteTask());
        }
    }

    @Override
    protected void onTaskError(Exception e) {
        ZalyLogUtils.getInstance().exceptionError(e);
        ZalyTaskExecutor.executeUserTask(TAG, new GetSiteMuteTask());
    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {
        ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
        ZalyTaskExecutor.executeUserTask(TAG, new GetSiteMuteTask());
    }

    class GetSiteMuteTask extends ZalyTaskExecutor.Task<Void, Void, ApiUserMuteProto.ApiUserMuteResponse> {

        @Override
        protected ApiUserMuteProto.ApiUserMuteResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getUserApi().getSiteMute();
        }

        @Override
        protected void onTaskSuccess(ApiUserMuteProto.ApiUserMuteResponse apiUserMuteResponse) {
            super.onTaskSuccess(apiUserMuteResponse);
            boolean messageMute = apiUserMuteResponse.getMute();
            SitePresenter.getInstance().updateSiteMute(site.getSiteHost(), site.getSitePort() + "", messageMute);
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
        }
    }
}
