package com.akaxin.client.platform.task;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.User;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.SPUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.security.RSAUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.orhanobut.logger.Logger;

/**
 * Created by zhangjun on 07/05/2018.
 */

public class BaseGenerateNewIdentityTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

    public User user;
    protected String registerType;
    protected String registerTypeName;

    public BaseGenerateNewIdentityTask(User userInfo, String registerType, String registerTypeName) {
        this.user = userInfo;
        this.registerType = registerType;
        this.registerTypeName = registerTypeName;
    }

    @Override
    protected void onPreTask() {
        super.onPreTask();
    }

    @Override
    protected Boolean executeTask(Void... voids) throws Exception {
        String[] deviceKeyPair = RSAUtils.getInstance().generateNewKeyPairPEMStr();
        ZalyLogUtils.getInstance().info(TAG, "device info is " + deviceKeyPair.toString());
        if (StringUtils.isEmpty(deviceKeyPair[0]) || StringUtils.isEmpty(deviceKeyPair[1])) {
            Toaster.showInvalidate("生成全新身份失败，请稍候再试");
            return false;
        }
        //一部设备对应一用户密钥对，一设备密钥对，所需要存储在上层配置项
        SPUtils spUtils = ZalyApplication.getCfgSP();
        spUtils.putKey(Configs.DEVICE_PRI_KEY, deviceKeyPair[0]);
        spUtils.putKey(Configs.DEVICE_PUB_KEY, deviceKeyPair[1]);

        user.setDeviceIdPrik(deviceKeyPair[0]);
        user.setDeviceIdPubk(deviceKeyPair[1]);
        user.setIdentitySource(registerType);
        user.setIdentityName(registerTypeName);
        SitePresenter.getInstance().insertUserIdentity(user);
        return true;
    }

    @Override
    protected void onTaskSuccess(Boolean aBoolean) {
        super.onTaskSuccess(aBoolean);
    }

    @Override
    protected void onTaskError(Exception e) {
        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        if (StringUtils.isNotEmpty(ZalyApplication.getGotoUrl())) {
            ZalyApplication.setGotoUrl("");
        }
        Logger.e(e);
    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {
        ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
        if (StringUtils.isNotEmpty(ZalyApplication.getGotoUrl())) {
            ZalyApplication.setGotoUrl("");
        }
        Logger.e(zalyAPIException);
    }

    @Override
    protected void onTaskFinish() {
        super.onTaskFinish();
    }
}