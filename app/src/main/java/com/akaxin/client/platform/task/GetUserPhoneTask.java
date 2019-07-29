package com.akaxin.client.platform.task;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.platform.ApiUserPhoneProto;

/**
 * Created by zhangjun on 08/05/2018.
 */

public class GetUserPhoneTask extends ZalyTaskExecutor.Task<Void, Void, ApiUserPhoneProto.ApiUserPhoneResponse> {
    public GetUserPhoneTask() {
        super();
    }

    @Override
    protected ApiUserPhoneProto.ApiUserPhoneResponse executeTask(Void... voids) throws Exception {
        return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite())
                .getUserApi().getUserPhone();
    }

    @Override
    protected void onTaskSuccess(ApiUserPhoneProto.ApiUserPhoneResponse apiUserPhoneResponse) {
        super.onTaskSuccess(apiUserPhoneResponse);
        ZalyApplication.getCfgSP().putKey(Configs.PHONE_ID, apiUserPhoneResponse.getPhoneId());
    }

    @Override
    protected void onTaskError(Exception e) {
    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {
    }
}