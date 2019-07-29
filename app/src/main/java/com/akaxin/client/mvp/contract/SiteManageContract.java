package com.akaxin.client.mvp.contract;

import com.akaxin.client.mvp.BasePresenter;
import com.akaxin.client.mvp.BaseView;

/**
 * Created by Mr.kk on 2018/7/10.
 * This Project was client-android
 */

public class SiteManageContract {
    public interface View extends BaseView {

        void onTaskStart(String content);

        void onTaskFinish();
    }

    public interface Presenter extends BasePresenter<View> {

    }
}
