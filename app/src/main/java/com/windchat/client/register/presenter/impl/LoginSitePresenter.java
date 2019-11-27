package com.windchat.client.register.presenter.impl;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.windchat.client.Configs;
import com.windchat.client.ZalyApplication;
import com.windchat.client.register.LoginSiteActivity;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.Site;
import com.windchat.client.bean.User;
import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.constant.ErrorCode;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.maintab.ZalyMainActivity;
import com.windchat.client.register.RegisterActivity;
import com.windchat.client.register.presenter.ILoginSitePresenter;
import com.windchat.client.register.view.ILoginSiteView;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.site.task.AddSiteTask;
import com.windchat.client.site.task.ApiUserProfileTask;
import com.windchat.client.site.task.GetSitesTask;
import com.windchat.client.util.DeviceUtils;
import com.windchat.client.util.SiteUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.security.RSAUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.site.ApiSiteConfigProto;
import com.akaxin.proto.site.ApiSiteLoginProto;
import com.akaxin.proto.site.ApiSiteRegisterProto;
import com.orhanobut.logger.Logger;
import com.windchat.im.IMClient;
import com.windchat.im.socket.ConnectionConfig;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.windchat.client.Configs.SUFFIX_USER_TOKEN;
import static com.windchat.client.bean.Site.STATUS_SITE_ONLINE;


public class LoginSitePresenter implements ILoginSitePresenter {

    private static final String TAG = "LoginSitePresenter";
    private static final int MAX_DELAY_LOGIN = 5000; // 5 seconds
    private boolean isLoggingIn = false;
    private ILoginSiteView loginSiteView;

    public LoginSitePresenter(ILoginSiteView loginSiteView) {
        this.loginSiteView = loginSiteView;
    }

    @Override
    public void loadCurrentSites(boolean needUnreadNum) {
        // 根据是否需要展示消息数量(needUnreadNum)加载多站点列表.
        ZalyTaskExecutor.executeUserTask(TAG, new GetSiteListsTask(needUnreadNum));
    }

    @Override
    public void tryLogin(String siteAddress) {
        loginSiteView.showProgressDialog();
        final Site site = new Site();
        if (siteAddress.contains(":")) {
            String[] siteInfo = siteAddress.split(":");
            site.setSiteHost(siteInfo[0]);
            site.setSitePort(Integer.valueOf(siteInfo[1]));
        } else {
            site.setSiteHost(siteAddress);
            site.setSitePort(SiteUtils.DEFAULT_PORT);
        }


        final ZalyTaskExecutor.Task getSiteInfoTask = new GetSiteInfoTask(site);
        ZalyTaskExecutor.executeUserTask(TAG, getSiteInfoTask);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLoggingIn) {
                    loginSiteView.hideProgressDialog();
                    ZalyTaskExecutor.cancleSpecificTask(TAG, getSiteInfoTask);
                    Toaster.showInvalidate("请稍候再试");
                }
            }
        }, MAX_DELAY_LOGIN);
    }

    @Override
    public void loginOrRegisterSite(Site site) {
        String userToken = UUID.randomUUID().toString();
        ZalyApplication.getCfgSP().put(site.getSiteIdentity() + SUFFIX_USER_TOKEN, userToken);

        String userPrivateKeyPem = ZalyApplication.getCfgSP().getKey(Configs.USER_PRI_KEY);
        String userPubKeyPem = ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY);
        String devicePubKeyPem = ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY);

        String userSignBase64 = RSAUtils.getInstance().signInBase64String(userPrivateKeyPem, userPubKeyPem);
        String deviceSignBase64 = RSAUtils.getInstance().signInBase64String(userPrivateKeyPem, devicePubKeyPem);
        String phoneToken = ZalyApplication.getCfgSP().getKey(Configs.PHONE_TOKEN + "_" + site.getSiteAddress());

        ApiSiteLoginProto.ApiSiteLoginRequest request = ApiSiteLoginProto.ApiSiteLoginRequest.newBuilder()
                .setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY))
                .setUserIdSignBase64(userSignBase64)
                .setUserDeviceIdPubk(ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY))
                .setUserDeviceIdSignBase64(deviceSignBase64)
                .setUserDeviceName(DeviceUtils.getDeviceName())
                .setUserToken(userToken)
                .setPhoneToken(phoneToken)
                .build();

        loginSiteView.hideProgressDialog();

        ZalyTaskExecutor.executeUserTask(TAG, new LoginOrRegisterTask(userSignBase64, deviceSignBase64, phoneToken, site));
    }

    /**
     * 初始化站点任务. 根据needUnreadNum确认是否需要获取站点未读消息数量.
     */
    class GetSiteListsTask extends GetSitesTask {

        public GetSiteListsTask(boolean needUnreadNum) {
            super(needUnreadNum);
        }

        @Override
        protected void onTaskSuccess(List<Site> sites) {
            super.onTaskSuccess(sites);
            ZalyApplication.siteList = sites;
        }
    }

    /**
     * 获取站点配置信息.
     */
    class GetSiteInfoTask extends ZalyTaskExecutor.Task<Void, Void, Site> {

        private Site site;

        public GetSiteInfoTask(Site site) {
            this.site = site;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            isLoggingIn = true;
        }

        @Override
        protected Site executeTask(Void... voids) throws Exception {
            ApiSiteConfigProto.ApiSiteConfigResponse response = ApiClient.getInstance(site).getSiteApi().getSiteInfo();
            if (response != null) {
                site.setSiteName(response.getSiteConfig().getSiteName());
                site.setSiteIcon(response.getSiteConfig().getSiteLogo());
                site.setSiteVersion(response.getSiteConfig().getSiteVersion());
                site.setRealNameConfig(response.getSiteConfig().getRealNameConfigValue());
                site.setCodeConfig(response.getSiteConfig().getInviteCodeConfigValue());
                site.setSiteVersion(response.getSiteConfig().getSiteVersion());
                return site;
            } else return null;
        }

        @Override
        protected void onTaskSuccess(Site site) {
            if (site != null) {
                String userToken = UUID.randomUUID().toString();
                ZalyApplication.getCfgSP().put(site.getSiteIdentity() + SUFFIX_USER_TOKEN, userToken);

                User user = SitePresenter.getInstance().getUserIdentity();
                if (user == null) {
                    ZalyLogUtils.getInstance().info(TAG, "platform login is error, user is null");
                    return;
                }
                ////用户私钥
                String userPrivateKeyPem = user.getUserIdPrik();
                ////用户公钥
                String userPubKeyPem = user.getUserIdPuk();
                /////用户设备公钥
                String devicePubKeyPem = user.getDeviceIdPubk();

                String userSignBase64 = RSAUtils.getInstance().signInBase64String(userPrivateKeyPem, userPubKeyPem);
                String deviceSignBase64 = RSAUtils.getInstance().signInBase64String(userPrivateKeyPem, devicePubKeyPem);
                String phoneToken = ZalyApplication.getCfgSP().getKey(Configs.PHONE_TOKEN + "_" + site.getSiteAddress());
                loginSiteView.hideProgressDialog();

                ZalyTaskExecutor.executeUserTask(TAG, new LoginTask(userSignBase64, deviceSignBase64, userToken, phoneToken, site));
            }
        }

        @Override
        protected void onTaskError(Exception e) {
            loginSiteView.hideProgressDialog();
            super.onTaskError(e);
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyApiException) {
            loginSiteView.hideProgressDialog();
            super.onAPIError(zalyApiException);
            ZalyLogUtils.getInstance().errorToInfo(TAG, zalyApiException.getMessage());

        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            loginSiteView.hideProgressDialog();
            isLoggingIn = false;
        }
    }


    /**
     * 登录站点.
     */
    class LoginTask extends ZalyTaskExecutor.Task<Void, Void, ApiSiteLoginProto.ApiSiteLoginResponse> {

        Site site;
        String userSignBase64;
        String deviceSignBase64;
        String pushToken;
        String phoneToken;

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        public LoginTask(String userSignBase64, String deviceSignBase64, String pushToken, String phoneToken, Site site) {
            this.userSignBase64 = userSignBase64;
            this.deviceSignBase64 = deviceSignBase64;
            this.pushToken = pushToken;
            this.phoneToken = phoneToken;
            this.site = site;
        }

        @Override
        protected ApiSiteLoginProto.ApiSiteLoginResponse executeTask(Void... voids) throws Exception {
            // push auth
            return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getSiteApi().loginSite(userSignBase64, deviceSignBase64, pushToken, phoneToken);
        }

        @Override
        protected void onTaskSuccess(ApiSiteLoginProto.ApiSiteLoginResponse apiLoginResponse) {
            super.onTaskSuccess(apiLoginResponse);
            if (StringUtils.isEmpty(apiLoginResponse.getSiteUserId()) || StringUtils.isEmpty(apiLoginResponse.getUserSessionId())) {
                return;
            }
            // 站点有该身份数据，但是本地客户端没有站点记录，需要先入库，然后并切换至该站点
            site.setSiteUserId(apiLoginResponse.getSiteUserId());
            site.setLastLoginTime(new Date().getTime());
            site.setSiteStatus(STATUS_SITE_ONLINE);
            site.setSiteSessionId(apiLoginResponse.getUserSessionId());
            //TODO DBChange 用户名和图片不能为null
            site.setSiteUserName("");
            site.setSiteUserImage("");

            ////登录站点成功之后，进行跳转
            ZalyTaskExecutor.executeUserTask(TAG, new AddSiteAndChangeIdentityTask(site));
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
            if (zalyAPIException.getExceptionType() == ZalyAPIException.TYPE_ERRINFO_NULL) {
            } else {
                String errorCode = zalyAPIException.getErrorInfoCode();
                switch (errorCode) {
                    case ErrorCode.LOGIN_NEED_REGISTER1:
                    case ErrorCode.LOGIN_NEED_REGISTER2:
                    case ErrorCode.LOGIN_NEED_REGISTER3:
                        RegisterActivity.actionStart(LoginSiteActivity.loginSiteActivity, site);
                        break;
                    case ErrorCode.REQUEST_ERROR_ALTER:
                    case ErrorCode.REQUEST_ERROR_ALERT:
                        Toaster.showInvalidate(zalyAPIException.getErrorInfoStr());
                        break;
                    case ErrorCode.REQUEST_ERROR:
                        Toaster.showInvalidate("请求失败");
                        break;
                }
            }
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }

    /**
     * 登录站点.
     */
    class LoginOrRegisterTask extends ZalyTaskExecutor.Task<Void, Void, ApiSiteLoginProto.ApiSiteLoginResponse> {
        Site site;
        ApiSiteRegisterProto.ApiSiteRegisterRequest registerReguest;
        String userSignBase64;
        String deviceSignBase64;
        String phoneToken;

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        public LoginOrRegisterTask(String userSignBase64, String deviceSignBase64, String phoneToken, Site site) {
            this.userSignBase64 = userSignBase64;
            this.deviceSignBase64 = deviceSignBase64;
            this.phoneToken = phoneToken;
            this.site = site;
        }

        @Override
        protected ApiSiteLoginProto.ApiSiteLoginResponse executeTask(Void... voids) throws Exception {
            // push auth
            return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getSiteApi().loginSite(userSignBase64, deviceSignBase64, "", phoneToken);
        }

        @Override
        protected void onTaskSuccess(ApiSiteLoginProto.ApiSiteLoginResponse apiLoginResponse) {
            super.onTaskSuccess(apiLoginResponse);
            if (StringUtils.isEmpty(apiLoginResponse.getSiteUserId()) || StringUtils.isEmpty(apiLoginResponse.getUserSessionId())) {
                return;
            }

            // 站点有该身份数据，但是本地客户端没有站点记录，需要先入库，然后并切换至该站点
            site.setSiteUserId(apiLoginResponse.getSiteUserId());
            site.setLastLoginTime(new Date().getTime());
            site.setSiteStatus(STATUS_SITE_ONLINE);
            site.setSiteSessionId(apiLoginResponse.getUserSessionId());
            site.setSiteUserName("");
            //TODO DBChange 用户名和图片不能为null
            site.setSiteUserImage("");
            ////登录站点成功之后，进行跳转
            ZalyTaskExecutor.executeUserTask(TAG, new AddSiteAndChangeIdentityTask(site));
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            Logger.e(zalyAPIException);
            ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
        }
    }

    /**
     * 添加站点至数据库.
     */
    class AddSiteAndChangeIdentityTask extends AddSiteTask {

        public AddSiteAndChangeIdentityTask(Site site) {
            super(site);
        }

        @Override
        protected void onTaskSuccess(Long l) {
            super.onTaskSuccess(l);
            // 存入内存
            if (ZalyApplication.siteList == null) {
                ZalyApplication.siteList = new ArrayList<>();
            }
            ZalyApplication.siteList.add(site);
            // 切换至该站点
            ZalyApplication.getCfgSP().put(Configs.KEY_CUR_SITE, site.getSiteIdentity());
            new SiteUtils().prepareDo(new SiteUtils.SiteUtilsListener() {
                @Override
                public void onPrepareSiteMsg(String msg) {
                }

                @Override
                public void onPrepareSiteSuccess(Site currentSite) {

                    Bundle bundle = new Bundle();
                    bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, site);
                    EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_SWITCH_SITE, bundle));
                    try {
                        IMClient.getInstance(site).checkConnection();
                    } catch (Exception e) {
                        ZalyLogUtils.getInstance().exceptionError(e);
                    }
                }
            });

            ZalyTaskExecutor.executeUserTask(TAG, new ApiUserProfileTask(site));

            Intent intent = new Intent(loginSiteView.getAppContext(), ZalyMainActivity.class);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, site);
            loginSiteView.getAppContext().startActivity(intent);
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            if (StringUtils.isNotEmpty(ZalyApplication.getGotoUrl())) {
                ZalyApplication.setGotoUrl("");
            }
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {

            ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
            if (StringUtils.isNotEmpty(ZalyApplication.getGotoUrl())) {
                ZalyApplication.setGotoUrl("");
            }
        }
    }

}
