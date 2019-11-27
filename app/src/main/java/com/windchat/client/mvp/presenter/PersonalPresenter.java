package com.windchat.client.mvp.presenter;

import android.content.Context;
import android.net.Uri;

import com.windchat.client.Configs;
import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.Site;
//import com.akaxin.client.im.files.IMFileUtils;
import com.windchat.client.im.files.IMFileUtils;
import com.windchat.client.mvp.BasePresenterImpl;
import com.windchat.client.mvp.contract.PersonalContract;
import com.windchat.client.util.DataCleanManager;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.akaxin.proto.site.ApiUserUpdateProfileProto;
import com.orhanobut.logger.Logger;

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

    }

    @Override
    public void logoutPlatform() {
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
