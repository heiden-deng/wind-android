package com.windchat.client.register.presenter.impl;

import android.content.Intent;
import android.util.Base64;

import com.windchat.client.Configs;
import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.User;

import com.windchat.client.register.LoginSiteActivity;
import com.windchat.client.register.presenter.ILoginPresenter;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.register.view.ILoginView;
import com.windchat.client.util.SPUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.security.RSAUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;

import java.security.KeyPair;
import java.security.Signature;

/**
 * Created by yichao on 2017/10/16.
 */

public class LoginPresenter implements ILoginPresenter {
    public static final String TAG = LoginPresenter.class.getSimpleName();

    public static final String LOGIN_WITH_GENERATE = "login_with_generate";

    private ILoginView loginView;

    public LoginPresenter(ILoginView loginView) {
        this.loginView = loginView;
    }

    public LoginPresenter() {
    }

    @Override
    public boolean generateNewIdentity() {
        ZalyTaskExecutor.executeUserTask(TAG, new GenerateNewIdentityTask());
        return true;
    }

    /**
     * 生成本地新身份
     */
    class GenerateNewIdentityTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {
        User user;

        @Override
        protected void onPreTask() {
            super.onPreTask();
            if (loginView != null)
                loginView.showProgressDialog();
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            //用户身份密钥对
            KeyPair userKeyPair = RSAUtils.getInstance().generateNewKeyPair();
            //设备密钥对
            KeyPair deviceKeyPair = RSAUtils.getInstance().generateNewKeyPair();

            String userPEMPriKeyStr = RSAUtils.getPEMStringFromRSAKey(userKeyPair.getPrivate());
            String userPEMPubKeyStr = RSAUtils.getPEMStringFromRSAKey(userKeyPair.getPublic());

            RSAUtils.extractPublicRSAKey(userPEMPubKeyStr);

            String devicePEMPriKeyStr = RSAUtils.getPEMStringFromRSAKey(deviceKeyPair.getPrivate());
            String devicePEMPubKeyStr = RSAUtils.getPEMStringFromRSAKey(deviceKeyPair.getPublic());

            if (StringUtils.isEmpty(userPEMPriKeyStr) || StringUtils.isEmpty(userPEMPubKeyStr) ||
                    StringUtils.isEmpty(devicePEMPriKeyStr) || StringUtils.isEmpty(devicePEMPubKeyStr)) {
                return false;
            }
            User user = new User();
            //一部设备对应一用户密钥对，一设备密钥对，所需要存储在上层配置项
            SPUtils spUtils = ZalyApplication.getCfgSP();
            spUtils.putKey(Configs.USER_PRI_KEY, userPEMPriKeyStr);
            spUtils.putKey(Configs.USER_PUB_KEY, userPEMPubKeyStr);

            spUtils.putKey(Configs.DEVICE_PRI_KEY, devicePEMPriKeyStr);
            spUtils.putKey(Configs.DEVICE_PUB_KEY, devicePEMPubKeyStr);

            user.setUserIdPrik(userPEMPriKeyStr);
            user.setUserIdPuk(userPEMPubKeyStr);
            user.setDeviceIdPrik(devicePEMPriKeyStr);
            user.setDeviceIdPubk(devicePEMPubKeyStr);

            //用户签名
            Signature userSign = Signature.getInstance("SHA512withRSA");
            userSign.initSign(userKeyPair.getPrivate());
            userSign.update(userPEMPubKeyStr.getBytes());
            String userSignBase64 = Base64.encodeToString(userSign.sign(), Base64.NO_WRAP);

            Signature deviceSign = Signature.getInstance("SHA512withRSA");
            deviceSign.initSign(userKeyPair.getPrivate());
            deviceSign.update(devicePEMPubKeyStr.getBytes());
            String deviceSignBase64 = Base64.encodeToString(deviceSign.sign(), Base64.NO_WRAP);

            //这里虽然生成了Sign，但是可能有些情况获取不到，使用的时候还是自己获取吧。
            //这段代码应该挪到Config.get里面去更合适一些。
            if (StringUtils.isNotEmpty(userSignBase64) && StringUtils.isNotEmpty(deviceSignBase64)) {
                spUtils.put(Configs.USER_SIGN, userSignBase64);//设置用户签名
                spUtils.put(Configs.DEVICE_SIGN, deviceSignBase64);//设置设备签名
            } else {
                return false;
            }

            user.setGlobalUserId(StringUtils.getGlobalUserIdHash(userPEMPubKeyStr));
            user.setIdentitySource(LOGIN_WITH_GENERATE);
            user.setIdentityName("本地生成账户");

            SitePresenter.getInstance().insertUserIdentity(user);
            if (loginView != null)
                loginView.hideProgressDialog();
            return true;
        }

        @Override
        protected void onTaskSuccess(Boolean aBoolean) {
            super.onTaskSuccess(aBoolean);
            if (aBoolean) {
                Intent intent = new Intent(loginView.getAppContext(), LoginSiteActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                loginView.getAppContext().startActivity(intent);
            } else {
                ZalyApplication.setGotoUrl("");
                Toaster.showInvalidate("生成匿名账户失败，请稍候再试");
            }
            if (loginView != null)
                loginView.hideProgressDialog();
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            if (StringUtils.isNotEmpty(ZalyApplication.getGotoUrl())) {
                ZalyApplication.setGotoUrl("");
            }
            if (loginView != null)
                loginView.hideProgressDialog();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
            if (StringUtils.isNotEmpty(ZalyApplication.getGotoUrl())) {
                ZalyApplication.setGotoUrl("");
            }
            if (loginView != null)
                loginView.hideProgressDialog();
        }

    }
}
