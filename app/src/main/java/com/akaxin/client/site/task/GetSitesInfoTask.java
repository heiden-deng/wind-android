package com.akaxin.client.site.task;

import android.os.Bundle;

import com.akaxin.client.api.ApiClient;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.SiteEvent;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.site.ApiSiteConfigProto;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

/**
 * Created by zhangjun on 25/05/2018.
 */

public class GetSitesInfoTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {
    Site site;

    public GetSitesInfoTask(Site site) {
        this.site = site;
    }

    @Override
    protected Boolean executeTask(Void... voids) throws Exception {
        List<Site> siteLists = SitePresenter.getInstance().getAllSiteLists(false);
        for (int i = 0; i < siteLists.size(); i++) {
            Site site = siteLists.get(i);
            try {
                ApiSiteConfigProto.ApiSiteConfigResponse response = ApiClient.getInstance(site).getSiteApi().getSiteInfo();
                ZalyLogUtils.getInstance().info(TAG, " site name ===" + response.getSiteConfig().getSiteName());
                if (response != null) {
                    site.setSiteName(response.getSiteConfig().getSiteName());
                    site.setSiteIcon(response.getSiteConfig().getSiteLogo());
                    site.setSiteVersion(response.getSiteConfig().getSiteVersion());
                    site.setRealNameConfig(response.getSiteConfig().getRealNameConfigValue());
                    site.setCodeConfig(response.getSiteConfig().getInviteCodeConfigValue());
                    SitePresenter.getInstance().updateSiteInfo(site);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ZalyMainActivity.KEY_SITE_INFO, site);
                    EventBus.getDefault().postSticky(new SiteEvent(SiteEvent.UPDATE_SITE_INFO, bundle));
                    return true;
                }
            } catch (Exception e) {
                ZalyLogUtils.getInstance().exceptionError(e);
            }
        }
        // DBChange 获取所有站点, 以及未读消息数量
        return false;
    }
}
