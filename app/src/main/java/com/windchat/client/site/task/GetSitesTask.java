package com.windchat.client.site.task;

import com.windchat.client.bean.Site;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.util.task.ZalyTaskExecutor;

import java.util.List;

/**
 * Created by zhangjun on 2018/3/3.
 */

public class GetSitesTask extends ZalyTaskExecutor.Task<Void, Void, List<Site>> {

    private Boolean needUnreadNum;

    public GetSitesTask(Boolean needUnreadNum){
        this.needUnreadNum = needUnreadNum;
    }

    @Override
    protected List<Site> executeTask(Void... voids) throws Exception {
        // DBChange 获取所有站点, 以及未读消息数量
        return SitePresenter.getInstance().getAllSiteLists(this.needUnreadNum);
    }
}
