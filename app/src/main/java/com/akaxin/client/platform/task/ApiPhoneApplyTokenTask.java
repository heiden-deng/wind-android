package com.akaxin.client.platform.task;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.platform.ApiPhoneApplyTokenProto;

/**
 * Created by zhangjun on 09/05/2018.
 */

public class ApiPhoneApplyTokenTask extends ZalyTaskExecutor.Task<Void, Void, ApiPhoneApplyTokenProto.ApiPhoneApplyTokenResponse> {
    private Site site;
    public static final String TAG = ApiPhoneApplyTokenTask.class.getSimpleName();

    @Override
    protected void onPreTask() {
        super.onPreTask();
    }
    public ApiPhoneApplyTokenTask(Site site) {
        this.site = site;
    }
    @Override
    protected ApiPhoneApplyTokenProto.ApiPhoneApplyTokenResponse executeTask(Void... voids) throws Exception {
        ZalyLogUtils.getInstance().info(TAG, " get platform token");
        return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite())
                .getPhoneApi().getPlatformToken(site.getSiteAddress());
    }

    @Override
    protected void onTaskSuccess(ApiPhoneApplyTokenProto.ApiPhoneApplyTokenResponse apiPhoneApplyTokenResponse) {
        super.onTaskSuccess(apiPhoneApplyTokenResponse);
        //写入phoneId
        ZalyApplication.getCfgSP().putKey(Configs.PHONE_ID, apiPhoneApplyTokenResponse.getPhoneId());
        ZalyApplication.getCfgSP().putKey(Configs.PHONE_TOKEN+"_"+site.getSiteAddress(), apiPhoneApplyTokenResponse.getPhoneToken());
    }

    @Override
    protected void onTaskError(Exception e) {
        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {
        ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
    }

    @Override
    protected void onTaskFinish() {
        super.onTaskFinish();
    }
}