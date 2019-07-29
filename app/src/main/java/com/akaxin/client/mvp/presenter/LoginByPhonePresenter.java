package com.akaxin.client.mvp.presenter;

import android.util.Base64;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.User;
import com.akaxin.client.constant.ErrorCode;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.mvp.BasePresenterImpl;
import com.akaxin.client.mvp.contract.LoginByPhoneContract;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.SPUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.security.RSAUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.PhoneProto;
import com.akaxin.proto.platform.ApiPhoneLoginProto;
import com.akaxin.proto.platform.ApiPhoneVerifyCodeProto;
import com.akaxin.proto.site.ApiPlatformRegisterByPhoneProto;

import java.security.KeyPair;
import java.security.Signature;

/**
 * Created by Mr.kk on 2018/6/28.
 * This Project was client-android
 */

public class LoginByPhonePresenter extends BasePresenterImpl<LoginByPhoneContract.View> implements LoginByPhoneContract.Presenter {
    public static final String LOGIN_WITH_GENERATE = "login_with_generate";
    String userPrivKeyPem = "";
    String userPubKeyPem = "";

    @Override
    public void getVerifyCode(final String phoneNum, final int type) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse>() {
            @Override
            protected ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite()).getPhoneApi().getVerifyCode(phoneNum, type, ServerConfig.CHINA_COUNTRY_CODE);
            }

            @Override
            protected void onPreTask() {
                super.onPreTask();
                mView.onTaskStart("正在获取验证码...");
            }

            @Override
            protected void onTaskSuccess(ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse apiPhoneVerifyCodeResponse) {
                super.onTaskSuccess(apiPhoneVerifyCodeResponse);
                mView.onGetVerifyCodeSuccess();
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                mView.onTaskFinish();
            }


        });
    }

    @Override
    public void loginPlatformByPhone(final String phoneNum, final String verifyCode) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiPhoneLoginProto.ApiPhoneLoginResponse>() {
            @Override
            protected ApiPhoneLoginProto.ApiPhoneLoginResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite()).getPhoneApi().loginPlatformByPhone(phoneNum, verifyCode);
            }

            @Override
            protected void onPreTask() {
                super.onPreTask();
                mView.onTaskStart("登录中");
            }

            @Override
            protected void onTaskSuccess(ApiPhoneLoginProto.ApiPhoneLoginResponse apiPhoneLoginResponse) {
                super.onTaskSuccess(apiPhoneLoginResponse);
                String pubkey = apiPhoneLoginResponse.getUserIdPubk();
                String prikey = apiPhoneLoginResponse.getUserIdPrik();
                if (pubkey == null || prikey == null || pubkey.equals("null") || prikey.equals("null")) {
                    mView.onLoginPlatformByPhoneError();
                    return;
                }
                ZalyApplication.getCfgSP().putKey(Configs.USER_PUB_KEY, pubkey);
                ZalyApplication.getCfgSP().putKey(Configs.USER_PRI_KEY, prikey);
                ZalyApplication.getCfgSP().putKey(Configs.PHONE_ID, phoneNum);

                //生成本机设备公钥
                User user = new User();
                user.setUserIdPrik(prikey);
                user.setUserIdPuk(pubkey);
                user.setGlobalUserId(StringUtils.getGlobalUserIdHash(pubkey));
                mView.onLoginPlatformByPhoneSuccess(user);
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                mView.onTaskFinish();
            }
        });
    }

    @Override
    public void generateNewIdentity(final User user) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, Boolean>() {
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
                user.setIdentitySource(ServerConfig.LOGIN_WITH_PHONE);
                user.setIdentityName(ServerConfig.LOGIN_WITH_PHONE_NAME);
                SitePresenter.getInstance().insertUserIdentity(user);
                return true;
            }

            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {
                super.onAPIError(zalyAPIException);
                mView.onGenerateNewIdentityError();
            }

            @Override
            protected void onTaskError(Exception e) {
                super.onTaskError(e);
                mView.onGenerateNewIdentityError();
            }

            @Override
            protected void onTaskSuccess(Boolean aBoolean) {
                super.onTaskSuccess(aBoolean);
                if (aBoolean)
                    mView.onGenerateNewIdentitySuccess();
            }
        });
    }

    @Override
    public void generateLocalIdentity() {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, Boolean>() {
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
                return true;
            }

            @Override
            protected void onPreTask() {
                super.onPreTask();
                mView.onTaskStart("本地生成账户中....");
            }

            @Override
            protected void onTaskSuccess(Boolean aBoolean) {
                super.onTaskSuccess(aBoolean);
                if (aBoolean) {
                    mView.onGenerateLocalIdentitySuccess();
                } else {
                    mView.onGenerateLocalIdentityError();
                }
            }

            @Override
            protected void onTaskError(Exception e) {
                super.onTaskError(e);
                mView.onGenerateLocalIdentityError();
            }

            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {
                super.onAPIError(zalyAPIException);
                mView.onGenerateLocalIdentityError();
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                mView.onTaskFinish();
            }
        });
    }

    @Override
    public void registPlatformByPhone(final String phoneNum, final String code) {

        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneResponse>() {
            @Override
            protected ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneResponse executeTask(Void... voids) throws Exception {
                ZalyLogUtils.getInstance().info(TAG, " ApiPlatformRegisterTask phoneNum is " + phoneNum);

                SPUtils spUtils = ZalyApplication.getCfgSP();

                //一部设备对应一用户密钥对，一设备密钥对，所需要存储在上层配置项
                String[] deviceKeyPair = RSAUtils.getInstance().generateNewKeyPairPEMStr();
                spUtils.putKey(Configs.DEVICE_PRI_KEY, deviceKeyPair[0]);
                spUtils.putKey(Configs.DEVICE_PUB_KEY, deviceKeyPair[1]);

                //用户身份密钥对
                String[] userKeyPair = RSAUtils.getInstance().generateNewKeyPairPEMStr();
                spUtils.putKey(Configs.USER_PRI_KEY, userKeyPair[0]);
                spUtils.putKey(Configs.USER_PUB_KEY, userKeyPair[1]);
                userPrivKeyPem = userKeyPair[0];
                userPubKeyPem = userKeyPair[1];
                return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite())
                        .getPlatformApi()
                        .registerPlatform(phoneNum, userPrivKeyPem, userPubKeyPem, code, PhoneProto.VCType.PHONE_REGISTER_VALUE);
            }

            @Override
            protected void onTaskSuccess(ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneResponse apiPlatformRegisterByPhoneResponse) {
                super.onTaskSuccess(apiPlatformRegisterByPhoneResponse);
                User user = new User();
                user.setGlobalUserId(StringUtils.getGlobalUserIdHash(userPubKeyPem));
                user.setIdentityName(ServerConfig.LOGIN_WITH_PHONE_NAME);
                user.setIdentitySource(ServerConfig.LOGIN_WITH_PHONE);
                ZalyApplication.getCfgSP().putKey(Configs.PHONE_ID, phoneNum);
                SitePresenter.getInstance().insertUserIdentity(user);
                mView.onRegistPlatformSuccess();
            }

            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {
                super.onAPIError(zalyAPIException);
                String errorCode = zalyAPIException.getErrorInfoCode();
                byte[] result = zalyAPIException.getZalyResult();
                if (errorCode.equals(ErrorCode.PHONE_HAS_USER)) {
                    try {
                        ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneResponse response = ApiPlatformRegisterByPhoneProto.ApiPlatformRegisterByPhoneResponse.parseFrom(result);
                        final String platformPubk = response.getUserIdPubk();
                        final String platformPrik = response.getUserIdPrik();

                        String platformPubkBase64 = Base64.encodeToString(platformPubk.getBytes(), Base64.NO_WRAP);
                        String localPubkBase64 = Base64.encodeToString(userPubKeyPem.getBytes(), Base64.NO_WRAP);

                        if (!platformPubkBase64.equals(localPubkBase64)) {
                            mView.onRegistPlatformChangeIdentity(platformPubk, platformPrik);
                        }
                    } catch (Exception e) {
                        ZalyLogUtils.getInstance().exceptionError(e);
                        Toaster.showInvalidate("请稍候再试");
                    }
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
