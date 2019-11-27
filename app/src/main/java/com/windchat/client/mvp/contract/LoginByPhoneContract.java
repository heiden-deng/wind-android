package com.windchat.client.mvp.contract;

import com.windchat.client.bean.User;
import com.windchat.client.mvp.BasePresenter;
import com.windchat.client.mvp.BaseView;

/**
 * Created by Mr.kk on 2018/6/28.
 * This Project was client-android
 */

public class LoginByPhoneContract {
    public interface View extends BaseView {

        void onGetVerifyCodeSuccess();

        void onTaskStart(String content);

        void onTaskFinish();

        void onLoginPlatformByPhoneError();

        void onLoginPlatformByPhoneSuccess(User user);

        void onGenerateNewIdentityError();

        void onGenerateNewIdentitySuccess();

        void onGenerateLocalIdentitySuccess();

        void onGenerateLocalIdentityError();

        void onRegistPlatformSuccess();

        void onRegistPlatformChangeIdentity(String platformPubk, String platformPrik);
    }

    public interface Presenter extends BasePresenter<View> {

        void getVerifyCode(String phoneNum, int type);

        void loginPlatformByPhone(String phoneNum, String verifyCode);

        void generateNewIdentity(User user);

        void generateLocalIdentity();

        void registPlatformByPhone(String phoneNum, String code);
    }
}
