package com.windchat.client.site.task;

import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.Site;
import com.windchat.client.db.dao.AkxCommonDao;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;

/**
 * Created by zhangjun on 2018/3/3.
 */

public class AddSiteTask extends ZalyTaskExecutor.Task<Void, Void, Long> {
    public Site site;
    public static final String TAG = AddSiteTask.class.getSimpleName();

    public AddSiteTask(Site site) {
        this.site = site;
    }

    @Override
    protected Long executeTask(Void... voids) throws Exception {
        site.setGlobalUserId(ZalyApplication.getGlobalUserId());
        Site siteInfo = SitePresenter.getInstance().getSiteUser(site.getHostAndPort());
        if (siteInfo != null && StringUtils.isNotEmpty(siteInfo.getSiteUserId()) && siteInfo.getSiteUserId().length() > 0) {
            AkxCommonDao.getInstance().updateUserSiteSessionId(site.getSiteHost(), site.getSitePort(), site);
            return null;
        }
        return AkxCommonDao.getInstance().insertSite(site);
    }

    @Override
    protected void onAPIError(ZalyAPIException e) {
        ZalyLogUtils.getInstance().apiError(TAG, e);
    }

    @Override
    protected void onTaskError(Exception e) {
        ZalyLogUtils.getInstance().exceptionError(e);
    }
}


