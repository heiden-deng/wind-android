package com.akaxin.client.site.presenter.impl;

import android.net.Uri;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.db.bean.UserFriendBean;
import com.akaxin.client.friend.presenter.impl.UserProfilePresenter;
import com.akaxin.client.im.files.IMFileUtils;
import com.akaxin.client.site.presenter.ISiteInfoPresenter;
import com.akaxin.client.site.task.ApiUserProfileTask;
import com.akaxin.client.site.task.DeleteUserToken;
import com.akaxin.client.site.view.ISiteInfoView;
import com.akaxin.client.util.SiteUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.akaxin.proto.site.ApiUserMuteProto;
import com.akaxin.proto.site.ApiUserUpdateMuteProto;
import com.akaxin.proto.site.ApiUserUpdateProfileProto;
import com.windchat.im.IMClient;
import com.windchat.im.socket.ConnectionConfig;

import java.io.File;
import java.io.FileInputStream;

import static com.akaxin.client.Configs.KEY_NEW_APPLY_FRIEND;

/**
 * Created by alexfan on 2018/4/13.
 * SiteInfoPresenter for SiteInfoActivity;
 */

public class SiteInfoPresenter implements ISiteInfoPresenter {

    private final String TAG = this.getClass().getSimpleName();
    private ISiteInfoView iView;

    private boolean messageMute;
    private String username;
    private Site site;
    private boolean muteUpdateSuccessful;
    private Site currentSite;

    public SiteInfoPresenter(ISiteInfoView iView) {
        super();
        this.iView = iView;
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public void setSite(Site site) {
        this.site = site;
    }

    @Override
    public void getPlatformSiteSetting() {

    }

    @Override
    public void updateSiteMute(boolean mute) {
        muteUpdateSuccessful = false;
    }

    @Override
    public void connectSite() {
        ZalyTaskExecutor.executeUserTask(TAG, new ConnectSiteTask(site));
    }

    @Override
    public void disconnectSite() {
        ZalyTaskExecutor.executeUserTask(TAG, new DisconnectSiteTask(site));
    }

    @Override
    public void delSite() {
        ZalyTaskExecutor.executeUserTask(TAG, new DeleteSiteTask(site));
    }

    @Override
    public void updateUserImage(Uri uri) {
        ZalyTaskExecutor.executeUserTask(TAG, new UploadUserImageTask(uri));
    }

    @Override
    public void updateUsername(String username) {
        ZalyTaskExecutor.executeUserTask(TAG, new UpdateProfile(site.getSiteUserImage(), username, site.getSiteLoginId()));
    }

    @Override
    public void updateSiteLoginId(String siteLoginId) {
        ZalyTaskExecutor.executeUserTask(TAG, new UpdateProfile(site.getSiteUserImage(), site.getSiteUserName(), siteLoginId));
    }

    /**
     * 上传用户资料
     */
    class UpdateProfile extends ZalyTaskExecutor.Task<Void, Void, ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse> {

        private String userimgId;
        private String username;
        private String siteLoginId;
        UserProto.UserProfile userProfileDetails;

        public UpdateProfile(String userimgId, String username, String siteLoginId) {
            this.userimgId = userimgId;
            this.username = username;
            this.siteLoginId = siteLoginId;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onUpdateUserProfileStart();
        }

        @Override
        protected ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse executeTask(Void... voids) throws Exception {
            if (siteLoginId != "" && siteLoginId != null && siteLoginId.length() > 2) {
                userProfileDetails = UserProto.UserProfile.newBuilder()
                        .setSiteUserId(site.getSiteUserId())
                        .setUserPhoto(userimgId)
                        .setUserName(username)
                        .setSiteLoginId(siteLoginId)
                        .build();
            } else {
                userProfileDetails = UserProto.UserProfile.newBuilder()
                        .setSiteUserId(site.getSiteUserId())
                        .setUserPhoto(userimgId)
                        .setUserName(username)
                        .build();
            }
            return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getUserApi().updateProfile(userProfileDetails);

        }

        @Override
        protected void onTaskSuccess(ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse userProfileUpdateResponse) {
            super.onTaskSuccess(userProfileUpdateResponse);

            //更新站点用户的信息
            site.setSiteLoginId(siteLoginId);
            site.setSiteUserName(username);
            site.setSiteUserImage(userimgId);

            SitePresenter.getInstance().updateSiteUserInfo(site);

            UserFriendBean userFriendBean = new UserFriendBean();
            userFriendBean.setSiteUserId(site.getSiteUserId());
            userFriendBean.setUserName(username);
            userFriendBean.setUserImage(userimgId);
            userFriendBean.setSiteLoginId(siteLoginId);
            userFriendBean.setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY));

            UserProfilePresenter.getInstance(site).updateSiteUserProfile(userFriendBean);
            iView.onUpdateUserProfileSuccess();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
            iView.onUpdateUserProfileError();
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onUpdateUserProfileError();
        }
    }

    class GetSiteMuteTask extends ZalyTaskExecutor.Task<Void, Void, ApiUserMuteProto.ApiUserMuteResponse> {

        @Override
        protected ApiUserMuteProto.ApiUserMuteResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getUserApi().getSiteMute();
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        @Override
        protected void onTaskSuccess(ApiUserMuteProto.ApiUserMuteResponse apiUserMuteResponse) {
            super.onTaskSuccess(apiUserMuteResponse);
            messageMute = apiUserMuteResponse.getMute();
            // update db
            SitePresenter.getInstance().updateSiteMute(site.getSiteHost(), site.getSitePort() + "", messageMute);

            iView.onGetPlatformSiteSettingSuccess(messageMute);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onGetPlatformSiteSettingError();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            iView.onGetPlatformSiteSettingError();
        }
    }


    class UpdateSiteMuteTask extends ZalyTaskExecutor.Task<Void, Void, ApiUserUpdateMuteProto.ApiUserUpdateMuteResponse> {

        boolean mute;

        public UpdateSiteMuteTask(boolean mute) {
            super();
            this.mute = mute;
        }

        @Override
        protected ApiUserUpdateMuteProto.ApiUserUpdateMuteResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getUserApi().updateSiteMute(mute);
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onUpdateSiteSettingStart();
        }

        @Override
        protected void onTaskSuccess(ApiUserUpdateMuteProto.ApiUserUpdateMuteResponse apiUserUpdateMuteResponse) {
            super.onTaskSuccess(apiUserUpdateMuteResponse);
            SitePresenter.getInstance().updateSiteMute(site.getSiteHost(), site.getSitePort() + "", mute);
            messageMute = mute;
            muteUpdateSuccessful = true;
            SitePresenter.getInstance().updateSiteMute(site.getSiteHost(), site.getSitePort() + "", messageMute);

            iView.onUpdateSiteSettingSuccess();
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            // 如果此前平台更改静音状态也未成功，才恢复按钮。
            if (!muteUpdateSuccessful) {
                messageMute = !mute;
                iView.onUpdateSiteSettingError(messageMute);
            }
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            if (!muteUpdateSuccessful) {
                messageMute = !mute;
                iView.onUpdateSiteSettingError(messageMute);
            }
        }

    }

    /**
     * 上传用户头像
     */
    class UploadUserImageTask extends ZalyTaskExecutor.Task<Void, Void, ApiFileUploadProto.ApiFileUploadResponse> {

        private Uri imageUri;

        public UploadUserImageTask(Uri imageUri) {
            this.imageUri = imageUri;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onUpdateUserProfileStart();
        }

        @Override
        protected ApiFileUploadProto.ApiFileUploadResponse executeTask(Void... voids) throws Exception {
            File file = new File(imageUri.getPath());
            byte[] bytesArray = new byte[(int) file.length()];
            try {
                FileInputStream fis = new FileInputStream(file);
                fis.read(bytesArray); //read file into bytes[]
                fis.close();
            } catch (Exception e) {
                throw e;
            }

            byte[] resizedImage = IMFileUtils.resizeImageByWidth(bytesArray, 256);
            return IMFileUtils.uploadFile(resizedImage, FileProto.FileType.USER_PORTRAIT, site);
        }

        @Override
        protected void onTaskSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
            super.onTaskSuccess(apiFileUploadResponse);
            if (apiFileUploadResponse == null) {
                iView.onUpdateUserProfileError();
                return;
            }
            ZalyTaskExecutor.executeUserTask(TAG, new UpdateProfile(apiFileUploadResponse.getFileId(), site.getSiteUserName(), site.getSiteLoginId()));
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onUpdateUserProfileError();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            iView.onUpdateUserProfileError();
            ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
        }
    }


    class DisconnectSiteTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        Site site;

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onDisconnectStart();
        }

        public DisconnectSiteTask(Site site) {
            this.site = site;
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            ////手动断开连接
            SitePresenter.getInstance().updateSiteConnStatus(Site.MANUAL_CONTROL_DISCONNECT_STATUS, site.getSiteHost(), site.getSitePort() + "");
            IMClient.getInstance(site).disconnect();
            site.setConnStatus(Site.MANUAL_CONTROL_DISCONNECT_STATUS);

            Thread.sleep(1000); // 有意sleep，等待IM断开
            return true;
        }

        @Override
        protected void onTaskSuccess(Boolean b) {
            super.onTaskSuccess(b);
            iView.onDisconnectSuccess();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
            iView.onDisconnectError();
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            iView.onDisconnectError();
        }
    }


    class ConnectSiteTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        Site site;

        public ConnectSiteTask(Site site) {
            this.site = site;
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            SitePresenter.getInstance().updateSiteConnStatus(Site.AUTO_DISCONNECT_STATUS, site.getSiteHost(), site.getSitePort() + "");
            site.setConnStatus(Site.AUTO_DISCONNECT_STATUS);
            IMClient.makeSureClientAlived(site);
            iView.onConnectStart();
            Thread.sleep(2000);//有意sleep，等待IM断开
            return true;
        }

        @Override
        protected void onTaskSuccess(Boolean b) {
            super.onTaskSuccess(b);
            iView.onConnectSuccess();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            iView.onConnectError();
        }
    }

    class DeleteSiteTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        Site site;

        @Override
        protected void onPreTask() {
            super.onPreTask();
            iView.onDelSiteStart();
        }

        public DeleteSiteTask(Site site) {
            this.site = site;
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            /////TODO add logout platform

            ZalyTaskExecutor.executeUserTask(TAG, new DeleteUserToken(site));
            ////手动断开连接
            SitePresenter.getInstance().updateSiteConnStatus(Site.MANUAL_CONTROL_DISCONNECT_STATUS, site.getSiteHost(), site.getSitePort() + "");
            IMClient.getInstance(site).disconnect();
            Thread.sleep(1000); // 有意sleep，等待IM断开
            IMClient.removeClient(site.getSiteAddress());
            SitePresenter.getInstance().deleteSiteDB(ZalyApplication.getSiteAddressObj(site.toString()));
            SitePresenter.getInstance().delSiteInfo(site.getSiteHost(), site.getSitePort() + "");
            ZalyApplication.getCfgSP().put(site.getSiteIdentity() + KEY_NEW_APPLY_FRIEND, false);

            if (SiteUtils.removeCurrent(site)) {
                ZalyLogUtils.getInstance().info(TAG, " site list ==" + ZalyApplication.siteList.toString());
                if (SiteUtils.currentContains(site)) {
                    // 当前站点仍然在站点列表内说明没被删除
                    iView.onDelSiteSuccessAtAnotherSite(site);
                } else if (ZalyApplication.siteList.size() > 0) {
                    // 当前站点被删除但仍有其他站点
                    switchTo(site, ZalyApplication.siteList.get(0));
                } else {
                    // 当前站点被删除且无其他站点
                    iView.onDelSiteSuccessAtCurrentSite(site, null);
                }
            }

            ZalyLogUtils.getInstance().errorToInfo(TAG, "deleting a non-existed site");

            return true;
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            iView.onDelSiteSuccessAtAnotherSite(site);

        }

        @Override
        protected void onAPIError(ZalyAPIException zalyException) {
            ZalyLogUtils.getInstance().apiError(TAG, zalyException);
            iView.onDelSiteSuccessAtAnotherSite(site);

        }
    }


    private void switchTo(final Site fromSite, final Site toSite) {
        currentSite = getSite();
        if (toSite.getSiteIdentity().equals(currentSite.getSiteIdentity())) {
            return;
        }
        ZalyLogUtils.getInstance().info(TAG, " site list toSite ==" + toSite.toString());

        ZalyApplication.getCfgSP().put(Configs.KEY_CUR_SITE, toSite.getSiteIdentity());

        new SiteUtils().prepareDo(new SiteUtils.SiteUtilsListener() {
            @Override
            public void onPrepareSiteMsg(String msg) {
            }

            @Override
            public void onPrepareSiteSuccess(Site currentSite) {
                ZalyLogUtils.getInstance().info(TAG, " site list currentSite==" + currentSite.toString());

                ZalyTaskExecutor.executeUserTask(TAG, new ApiUserProfileTask(currentSite));
                iView.onDelSiteSuccessAtCurrentSite(fromSite, currentSite);
            }
        });
    }

}
