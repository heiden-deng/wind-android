package com.akaxin.client.platform.task;

import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.platform.ApiPhoneVerifyCodeProto;

/**
 * Created by zhangjun on 07/05/2018.
 */

public class GetPhoneCode extends ZalyTaskExecutor.Task<Void, Void, ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse> {

    private String phoneNum;
    private String countryCode;
    private int vcType;

    public GetPhoneCode(String phoneNum, int vcType, String countryCode) {
        this.phoneNum = phoneNum;
        this.vcType = vcType;
        this.countryCode = countryCode;
    }

    @Override
    protected void onPreTask() {
        super.onPreTask();
    }

    @Override
    protected ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse executeTask(Void... voids) throws Exception {
        return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite()).getPhoneApi().getVerifyCode(phoneNum, vcType, ServerConfig.CHINA_COUNTRY_CODE);
    }

    @Override
    protected void onTaskSuccess(ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse phoneVerifyCodeResponse) {
        super.onTaskSuccess(phoneVerifyCodeResponse);
        Toaster.show("发送成功，请注意查收");
    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {
        super.onAPIError(zalyAPIException);
        ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
    }

    @Override
    protected void onTaskError(Exception e) {
        super.onTaskError(e);
        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
    }
}