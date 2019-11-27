package com.windchat.client.mvp.contract;

import android.content.Context;
import android.net.Uri;

import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.windchat.client.bean.Site;
import com.windchat.client.mvp.BasePresenter;
import com.windchat.client.mvp.BaseView;

/**
 * Created by Mr.kk on 2018/7/7.
 * This Project was client-android
 */

public class PersonalContract {
    public interface View extends BaseView {


        void onTaskStart(String content);

        void onTaskFinish();

        void onLogoutPlatformSuccess();

        void onUpdateUserProfileSuccess(Site site, UserProto.UserProfile userProfileDetails);

        void onUploadImageSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse);

        void onCleanDataSuccess();

    }

    public interface Presenter extends BasePresenter<View> {
        void getUserPhone();

        void logoutPlatform();

        void cleanData(Context mContext);

        void updateUserProfile(Site site, String userimgId, String username, String siteLoginId);

        void uploadImage(Uri uri, Site site);
    }
}
