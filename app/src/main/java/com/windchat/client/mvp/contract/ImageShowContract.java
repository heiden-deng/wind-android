package com.windchat.client.mvp.contract;

import com.windchat.client.mvp.BasePresenter;
import com.windchat.client.mvp.BaseView;

/**
 * Created by Mr.kk on 2018/7/9.
 * This Project was client-android
 */

public class ImageShowContract {
    public interface View extends BaseView {
        void onTaskStart(String content);

        void onTaskFinish();

        void onTaskError();

    }

    public interface Presenter extends BasePresenter<View> {

    }
}
