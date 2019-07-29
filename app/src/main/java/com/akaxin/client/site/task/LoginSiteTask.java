package com.akaxin.client.site.task;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.User;
import com.akaxin.client.bean.event.AppEvent;
import com.akaxin.client.constant.ErrorCode;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.im.IMClient;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.platform.task.ApiPhoneApplyTokenTask;
import com.akaxin.client.platform.task.ApiSettingSiteMuteTask;
import com.akaxin.client.platform.task.ApiUserPushTokenTask;
import com.akaxin.client.platform.task.PushAuthTask;
import com.akaxin.client.register.RegisterActivity;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.socket.ConnectionConfig;
import com.akaxin.client.util.SiteUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.security.RSAUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.ConfigProto;
import com.akaxin.proto.site.ApiSiteConfigProto;
import com.akaxin.proto.site.ApiSiteLoginProto;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.akaxin.client.Configs.SUFFIX_USER_TOKEN;
import static com.akaxin.client.bean.Site.STATUS_SITE_ONLINE;

/**
 * Created by zhangjun on 21/06/2018.
 */

/**
 * 获取站点配置信息.
 */
public class LoginSiteTask extends ZalyTaskExecutor.Task<Void, Void, Site> {
    private static String TAG = LoginSiteTask.class.getSimpleName();

    private Site site;
    private long expireTime = 300 * 1000;////有效期5分钟
    private boolean isLoginNow = false;
    private Context mContext;

    public LoginSiteTask(Site site, Context mContext) {
        this.site = site;
        this.mContext = mContext;
    }

    @Override
    protected void onPreTask() {
        super.onPreTask();
    }

    @Override
    protected Site executeTask(Void... voids) throws Exception {
        Long prevTime = ZalyApplication.getCfgSP().getLong(site.getSiteIdentity() + SiteConfig.SITE_LOGIN_BY_AUTH_FAIL, 2021);
        Long nowTime = System.currentTimeMillis();

        if (prevTime != 2021 && (nowTime - prevTime < expireTime)) {
            return null;
        }
        ZalyApplication.getCfgSP().put(site.getSiteIdentity() + SiteConfig.SITE_LOGIN_BY_AUTH_FAIL, System.currentTimeMillis());

        ZalyLogUtils.getInstance().info(TAG, "imconnection Auth failed. Need login interval  time == " + (nowTime - prevTime));

        ApiSiteConfigProto.ApiSiteConfigResponse response = ApiClient.getInstance(site).getSiteApi().getSiteInfo();

        if (response != null) {
            site.setSiteName(response.getSiteConfig().getSiteName());
            site.setSiteIcon(response.getSiteConfig().getSiteLogo());
            site.setSiteVersion(response.getSiteConfig().getSiteVersion());
            site.setRealNameConfig(response.getSiteConfig().getRealNameConfigValue());
            site.setCodeConfig(response.getSiteConfig().getInviteCodeConfigValue());
            site.setSiteVersion(response.getSiteConfig().getSiteVersion());
            return site;
        }
        return null;
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

            //站点非实名，不调用实名接口
            if (site.getRealNameConfig() == ConfigProto.RealNameConfig.REALNAME_YES_VALUE) {
                ZalyTaskExecutor.executeUserTask(TAG, new ApiPhoneApplyTokenTask(site));
            }
            ZalyTaskExecutor.executeUserTask(TAG, new LoginTask(userSignBase64, deviceSignBase64, userToken, phoneToken, site, mContext));
        }
    }

    @Override
    protected void onTaskError(Exception e) {
        super.onTaskError(e);
        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
    }

    @Override
    protected void onAPIError(ZalyAPIException zalyApiException) {
        super.onAPIError(zalyApiException);
        ZalyLogUtils.getInstance().errorToInfo(TAG, zalyApiException.getMessage());
    }

    @Override
    protected void onTaskFinish() {
        super.onTaskFinish();
    }
}


/**
 * 登录站点.
 */
class LoginTask extends ZalyTaskExecutor.Task<Void, Void, ApiSiteLoginProto.ApiSiteLoginResponse> {

    Site site;
    String userSignBase64;
    String deviceSignBase64;
    String userToken;
    String phoneToken;
    Context mContext;

    @Override
    protected void onPreTask() {
        super.onPreTask();
    }

    public LoginTask(String userSignBase64, String deviceSignBase64, String userToken, String phoneToken, Site site, Context mContext) {
        this.userSignBase64 = userSignBase64;
        this.deviceSignBase64 = deviceSignBase64;
        this.userToken = userToken;
        this.phoneToken = phoneToken;
        this.site = site;
        this.mContext = mContext;
    }

    @Override
    protected ApiSiteLoginProto.ApiSiteLoginResponse executeTask(Void... voids) throws Exception {
        ZalyTaskExecutor.executeUserTask(TAG, new PushAuthTask(site));
        return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getSiteApi().loginSite(userSignBase64, deviceSignBase64, userToken, phoneToken);
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
        ZalyTaskExecutor.executeUserTask(TAG, new AddSiteAndChangeIdentityTask(site, mContext));
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
                    RegisterActivity.actionStart(ZalyMainActivity.zalyMainActivity, site);
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
 * 添加站点至数据库.
 */
class AddSiteAndChangeIdentityTask extends AddSiteTask {
    Context mContext;

    public AddSiteAndChangeIdentityTask(Site site, Context mContext) {
        super(site);
        this.mContext = mContext;
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
                bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, currentSite);
                EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_SWITCH_SITE, bundle));
                try {
                    IMClient.makeSureClientAlived(currentSite.toSiteAddress());
                } catch (Exception e) {
                    ZalyLogUtils.getInstance().exceptionError(e);
                }
            }
        });

        ZalyTaskExecutor.executeUserTask(TAG, new ApiUserProfileTask(site));
        ZalyTaskExecutor.executeUserTask(TAG, new ApiSettingSiteMuteTask(site));
        ZalyTaskExecutor.executeUserTask(TAG, new ApiUserPushTokenTask());

        Intent intent = new Intent(ZalyApplication.getContext(), ZalyMainActivity.class);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, site);
        mContext.startActivity(intent);
    }

    @Override
    protected void onTaskError(Exception e) {
        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());

    }

    @Override
    protected void onAPIError(ZalyAPIException zalyAPIException) {
        ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
    }
}
