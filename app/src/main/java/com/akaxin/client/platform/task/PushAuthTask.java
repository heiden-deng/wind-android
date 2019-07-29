package com.akaxin.client.platform.task;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.platform.ApiPushAuthProto;

/**
 * 发送pushToken到平台
 * Created by zhangjun on 2018/3/3.
 */

public class PushAuthTask extends ZalyTaskExecutor.Task<Void, Void, ApiPushAuthProto.ApiPushAuthResponse> {
    private static final String TAG = PushAuthTask.class.getSimpleName();
    private Site site;

    public PushAuthTask(Site site) {
        this.site = site;
    }

    @Override
    protected ApiPushAuthProto.ApiPushAuthResponse executeTask(Void... voids) throws Exception {
        String userToken = ZalyApplication.getCfgSP().getString(site.getSiteIdentity() + Configs.SUFFIX_USER_TOKEN);
        return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite()).getPlatformApi().pushAuth(site, userToken);

    }


    @Override
    protected void onTaskError(Exception e) {
        super.platformLoginByError(e);
        ZalyLogUtils.getInstance().exceptionError(e);
    }

    @Override
    protected void onAPIError(ZalyAPIException e) {
        super.platformLoginByApiError(e);
        ZalyLogUtils.getInstance().exceptionError(e);
    }
}
