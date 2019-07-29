package com.akaxin.client.mvp.contract;

import com.akaxin.client.bean.Site;
import com.akaxin.client.mvp.BasePresenter;
import com.akaxin.client.mvp.BaseView;
import com.akaxin.proto.site.ApiPluginListProto;

/**
 * Created by Mr.kk on 2018/7/4.
 * This Project was client-android
 */

public class AppContract {
    public interface View extends BaseView {
        void onGetPluginsOnSiteSuccess(ApiPluginListProto.ApiPluginListResponse apiPluginListResponse, boolean isRefresh);

        void onGetPluginsOnSiteError();

        void onTaskStart(String content);

        void onTaskFinish();
    }

    public interface Presenter extends BasePresenter<View> {
        void getPluginsOnSite(Site currentSite, boolean isRefresh);
    }
}
