package com.akaxin.client.site.task;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.db.dao.AkxCommonDao;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;

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


