package com.akaxin.client.mvp.presenter;

import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.mvp.BasePresenterImpl;
import com.akaxin.client.mvp.contract.AppContract;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.site.ApiPluginListProto;
import com.blankj.utilcode.util.CacheDiskUtils;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Created by Mr.kk on 2018/7/4.
 * This Project was client-android
 */

public class AppPresenter extends BasePresenterImpl<AppContract.View> implements AppContract.Presenter {
    @Override
    public void getPluginsOnSite(final Site currentSite, final boolean isRefresh) {
        ZalyTaskExecutor.executeUserTask(TAG, new ZalyTaskExecutor.Task<Void, Void, ApiPluginListProto.ApiPluginListResponse>() {
            @Override
            protected void onCacheTask() {
                ZalyLogUtils.getInstance().info(TAG, " current site ==" + currentSite.getSiteIdentity());
                byte[] cache = CacheDiskUtils.getInstance().getBytes(currentSite.getSiteIdentity() + SiteConfig.PLUGIN_HOME_LIST);
                if (cache == null) {
                    return;
                }
                try {
                    ApiPluginListProto.ApiPluginListResponse apiPluginListResponse = ApiPluginListProto.ApiPluginListResponse.parseFrom(cache);
                    mView.onGetPluginsOnSiteSuccess(apiPluginListResponse, isRefresh);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                }
            }

            @Override
            protected ApiPluginListProto.ApiPluginListResponse executeTask(Void... voids) throws Exception {
                ZalyLogUtils.getInstance().info(TAG, " current site ==" + currentSite.getSiteIdentity());
                String referer = SiteConfig.PLUGIN_HOME_REFERER.replace("siteAddress", currentSite.getHostAndPort());
                return ApiClient.getInstance(currentSite).getPluginApi().getPluginList(referer);
            }

            @Override
            protected void onTaskSuccess(ApiPluginListProto.ApiPluginListResponse apiPluginListResponse) {
                super.onTaskSuccess(apiPluginListResponse);
                CacheDiskUtils.getInstance().put(currentSite.getSiteIdentity() + SiteConfig.PLUGIN_HOME_LIST, apiPluginListResponse.toByteArray());
                mView.onGetPluginsOnSiteSuccess(apiPluginListResponse, isRefresh);
            }

            @Override
            protected void onAPIError(ZalyAPIException zalyAPIException) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
                super.platformLoginByError(zalyAPIException);
                mView.onGetPluginsOnSiteError();
            }

            @Override
            protected void onTaskError(Exception e) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                super.platformLoginByError(e);
                mView.onGetPluginsOnSiteError();
            }

            @Override
            protected void onTaskFinish() {
                super.onTaskFinish();
                mView.onTaskFinish();
            }
        });
    }
}
