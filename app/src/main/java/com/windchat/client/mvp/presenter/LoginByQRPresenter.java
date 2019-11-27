package com.windchat.client.mvp.presenter;

import com.windchat.client.Configs;
import com.windchat.client.ZalyApplication;
import com.windchat.client.bean.User;
import com.windchat.client.constant.ServerConfig;
import com.windchat.client.mvp.contract.LoginByQRContract;
import com.windchat.client.mvp.BasePresenterImpl;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.util.SPUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.security.RSAUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;

/**
 * Created by Mr.kk on 2018/6/28.
 * This Project was client-android
 */

public class LoginByQRPresenter extends BasePresenterImpl<LoginByQRContract.View> implements LoginByQRContract.Presenter {
    @Override
    public void getTempSpaceContent(final String spaceKey, final byte[] tsk) {

    }

    @Override
    public void generateNewIdentityTask(final User user) {
        ZalyTaskExecutor.executeUserTask("", new ZalyTaskExecutor.Task<Void, Void, Long>() {
            @Override
            protected void onPreTask() {
                super.onPreTask();
                if (mView != null)
                    mView.onTaskStart("授权中...");
            }

            @Override
            protected Long executeTask(Void... voids) throws Exception {
                String[] deviceKeyPair = RSAUtils.getInstance().generateNewKeyPairPEMStr();
                ZalyLogUtils.getInstance().info(TAG, "device info is " + deviceKeyPair.toString());
                if (StringUtils.isEmpty(deviceKeyPair[0]) || StringUtils.isEmpty(deviceKeyPair[1])) {
                    Toaster.showInvalidate("生成全新身份失败，请稍候再试");
                    return Long.valueOf(-1);
                }
                //一部设备对应一用户密钥对，一设备密钥对，所需要存储在上层配置项
                SPUtils spUtils = ZalyApplication.getCfgSP();
                spUtils.putKey(Configs.DEVICE_PRI_KEY, deviceKeyPair[0]);
                spUtils.putKey(Configs.DEVICE_PUB_KEY, deviceKeyPair[1]);
                user.setDeviceIdPrik(deviceKeyPair[0]);
                user.setDeviceIdPubk(deviceKeyPair[1]);
                user.setIdentitySource(ServerConfig.LOGIN_WITH_AUTH);
                user.setIdentityName(ServerConfig.LOGIN_WITH_AUTH_NAME);
                return SitePresenter.getInstance().insertUserIdentity(user);
            }

            @Override
            protected void onTaskSuccess(Long aLong) {
                super.onTaskSuccess(aLong);
                if (aLong != null && aLong != -1) {
                    if (mView != null)
                        mView.generateNewIdentityTaskSuccess();
                }
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                if (mView != null)
                    mView.onTaskFinish();
            }
        });
    }
}
