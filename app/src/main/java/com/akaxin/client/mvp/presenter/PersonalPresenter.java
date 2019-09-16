package com.akaxin.client.mvp.presenter;

import android.content.Context;
import android.net.Uri;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.ErrorCode;
//import com.akaxin.client.im.files.IMFileUtils;
import com.akaxin.client.im.files.IMFileUtils;
import com.akaxin.client.mvp.BasePresenterImpl;
import com.akaxin.client.mvp.contract.PersonalContract;
import com.akaxin.client.util.DataCleanManager;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.platform.ApiPlatformLogoutProto;
import com.akaxin.proto.platform.ApiUserPhoneProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.akaxin.proto.site.ApiUserUpdateProfileProto;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.ConnectionConfig;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Mr.kk on 2018/7/7.
 * This Project was client-android
 */

public class PersonalPresenter extends BasePresenterImpl<PersonalContract.View> implements PersonalContract.Presenter {
    UserProto.UserProfile userProfileDetails;

    @Override
    public void getUserPhone() {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiUserPhoneProto.ApiUserPhoneResponse>() {

            @Override
            protected ApiUserPhoneProto.ApiUserPhoneResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite())
                        .getUserApi().getUserPhone();
            }

            @Override
            protected void onTaskSuccess(ApiUserPhoneProto.ApiUserPhoneResponse apiUserPhoneResponse) {
                super.onTaskSuccess(apiUserPhoneResponse);
                ZalyApplication.getCfgSP().putKey(Configs.PHONE_ID, apiUserPhoneResponse.getPhoneId());
                if (mView != null)
                    mView.onGetUserPhoneSuccess(apiUserPhoneResponse);
            }

            @Override
            protected void onTaskError(Exception e) {

            }

            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {

            }
        });
    }

    @Override
    public void logoutPlatform() {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiPlatformLogoutProto.ApiPlatformLogoutResponse>() {
            @Override
            protected void onPreTask() {
                super.onPreTask();
                if (mView != null)
                mView.onTaskStart("注销平台连接");
            }

            @Override
            protected ApiPlatformLogoutProto.ApiPlatformLogoutResponse executeTask(Void... voids) throws Exception {
                return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(ApiClientForPlatform.getPlatformSite()))
                        .getPlatformApi().platformLogout();
            }

            @Override
            protected void onTaskSuccess(ApiPlatformLogoutProto.ApiPlatformLogoutResponse apiPlatformLogoutResponse) {
                super.onTaskSuccess(apiPlatformLogoutResponse);
                if (mView != null)
                mView.onLogoutPlatformSuccess();
            }

            @Override
            protected void onTaskError(Exception e) {
                super.onTaskError(e);
                if (e instanceof ZalyAPIException && ((ZalyAPIException) e).getErrorInfoCode().equals(ErrorCode.REQUEST_SESSION_ERROR)) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ZalyLogUtils.getInstance().exceptionError(ex);
                    }
                    Toaster.showInvalidate("请稍候再试");
                    return;
                }
            }

            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {
                super.onAPIError(zalyAPIException);

                if (zalyAPIException != null && zalyAPIException.getErrorInfoCode().equals(ErrorCode.REQUEST_SESSION_ERROR)) {
                    /////TODO session 过期 需要重新登录login
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ZalyLogUtils.getInstance().exceptionError(ex);
                    }
                    Toaster.showInvalidate("请稍候再试");
                    return;
                }

                //    ZalyTaskExecutor.executeUserTask(TAG, new com.akaxin.client.personal.presenter.impl.PersonalPresenter.DeleteIdentityTask());
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                if (mView != null)
                mView.onTaskFinish();
            }
        });
    }

    @Override
    public void cleanData(Context mContext) {
        DataCleanManager.cleanApplicationData(mContext);
        if (mView != null)
            mView.onCleanDataSuccess();
    }

    @Override
    public void updateUserProfile(final Site site, final String userimgId, final String username, final String siteLoginId) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse>() {
            @Override
            protected void onPreTask() {
                super.onPreTask();
                if (mView != null)
                mView.onTaskStart("上传资料...");

            }

            @Override
            protected ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse executeTask(Void... voids) throws Exception {
                if (siteLoginId != null) {
                    userProfileDetails = UserProto.UserProfile.newBuilder()
                            .setSiteUserId(site.getSiteUserId())
                            .setUserPhoto(userimgId == null ? "" : userimgId)
                            .setUserName(username)
                            .setSiteLoginId(siteLoginId)
                            .build();
                } else {
                    userProfileDetails = UserProto.UserProfile.newBuilder()
                            .setSiteUserId(site.getSiteUserId())
                            .setUserPhoto(userimgId == null ? "" : userimgId)
                            .setUserName(username)
                            .build();
                }
                ZalyLogUtils.getInstance().info(TAG, "userImgId: " + userimgId + ", username: " + username);
                return ApiClient.getInstance(site).getUserApi().updateProfile(userProfileDetails);
            }

            @Override
            protected void onTaskSuccess(ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse apiUserUpdateProfileResponse) {
                super.onTaskSuccess(apiUserUpdateProfileResponse);
                if (mView != null)
                mView.onUpdateUserProfileSuccess(site, userProfileDetails);
            }


            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {
                super.onAPIError(zalyAPIException);
                ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                if (mView != null)
                mView.onTaskFinish();
            }
        });
    }

    @Override
    public void uploadImage(final Uri uri, final Site site) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiFileUploadProto.ApiFileUploadResponse>() {
            @Override
            protected ApiFileUploadProto.ApiFileUploadResponse executeTask(Void... voids) throws Exception {
                File file = new File(uri.getPath());
                byte[] bytesArray = new byte[(int) file.length()];
                try {
                    FileInputStream fis = new FileInputStream(file);
                    fis.read(bytesArray); //read file into bytes[]
                    fis.close();
                } catch (Exception e) {
                    throw e;
                }
                byte[] resizedImage = IMFileUtils.resizeImageByWidth(bytesArray, 256);
                return IMFileUtils.uploadImg(site, resizedImage, FileProto.FileType.USER_PORTRAIT);
            }

            @Override
            protected void onTaskSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
                super.onTaskSuccess(apiFileUploadResponse);
                if (apiFileUploadResponse == null) {
                    Toaster.showInvalidate("上传失败，请稍候再试");
                    return;
                }
                if (mView != null)
                mView.onUploadImageSuccess(apiFileUploadResponse);
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                if (mView != null)
                mView.onTaskFinish();
            }

            @Override
            protected void onPreTask() {
                super.onPreTask();
                if (mView != null)
                mView.onTaskStart("上传图片中");
            }

            @Override
            protected void onTaskError(Exception e) {
                super.onTaskError(e);
                Logger.e(e);
            }
        });
    }
}
