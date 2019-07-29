package com.akaxin.client.platform.task;

import android.content.Context;
import android.content.Intent;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.User;
import com.akaxin.client.bean.event.LoginEvent;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.activitys.LoginByPhoneActivity;
import com.akaxin.client.register.LoginSiteActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.platform.ApiPhoneLoginProto;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by zhangjun on 07/05/2018.
 */

public class LoginPlatformByPhoneTask extends ZalyTaskExecutor.Task<Void, Void, ApiPhoneLoginProto.ApiPhoneLoginResponse> {

    private String phoneNum;
    private String verifyCode;
    protected User user;
    Context mContext;

    public LoginPlatformByPhoneTask(String phoneNum, String verifyCode, Context mContext) {
        this.phoneNum = phoneNum;
        this.verifyCode = verifyCode;
        this.mContext = mContext;
    }

    @Override
    protected ApiPhoneLoginProto.ApiPhoneLoginResponse executeTask(Void... voids) throws Exception {
        return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite()).getPhoneApi().loginPlatformByPhone(phoneNum,verifyCode);
    }

    @Override
    protected void onTaskSuccess(ApiPhoneLoginProto.ApiPhoneLoginResponse phoneLoginResponse) {
        super.onTaskSuccess(phoneLoginResponse);

        String pubkey = phoneLoginResponse.getUserIdPubk();
        String prikey = phoneLoginResponse.getUserIdPrik();

        if (pubkey == null || prikey == null || pubkey.equals("null") || prikey.equals("null")) {
            Toaster.showInvalidate("请稍候再试");
            mContext.startActivity(new Intent(mContext,LoginByPhoneActivity.class));
            return;
        }

        ZalyApplication.getCfgSP().putKey(Configs.USER_PUB_KEY, pubkey);
        ZalyApplication.getCfgSP().putKey(Configs.USER_PRI_KEY, prikey);
        ZalyApplication.getCfgSP().putKey(Configs.PHONE_ID, phoneNum);

        //生成本机设备公钥
        user = new User();
        user.setUserIdPrik(prikey);
        user.setUserIdPuk(pubkey);
        user.setGlobalUserId(StringUtils.getGlobalUserIdHash(pubkey));
        ZalyTaskExecutor.executeUserTask(TAG, new GenerateNewIdentityTask(user));
    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {
        Toaster.showInvalidate("手机号或验证码有误");
    }

    @Override
    protected void onTaskError(Exception e) {
        Toaster.showInvalidate("手机号或验证码有误");
    }






    class GenerateNewIdentityTask extends BaseGenerateNewIdentityTask {

        public GenerateNewIdentityTask(User user) {
            super(user, ServerConfig.LOGIN_WITH_PHONE, ServerConfig.LOGIN_WITH_PHONE_NAME);
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        @Override
        protected void onTaskSuccess(Boolean aBoolean) {
            super.onTaskSuccess(aBoolean);
            if (aBoolean) {
                mContext.startActivity(new Intent(mContext, LoginSiteActivity.class));
                EventBus.getDefault().post(new LoginEvent(-1, null));
            }
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
        }
    }
}