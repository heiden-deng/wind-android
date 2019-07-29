package com.akaxin.client.util;

import android.text.TextUtils;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.db.ZalyDbHelper;
import com.akaxin.client.db.dao.AkxCommonDao;
import com.akaxin.client.group.presenter.impl.GroupPresenter;
import com.akaxin.client.im.IMClient;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.site.task.ApiUserProfileTask;
import com.akaxin.client.socket.SiteAddress;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.site.ApiGroupListProto;
import com.akaxin.proto.site.ApiSiteConfigProto;
import com.orhanobut.logger.Logger;

import java.util.List;

/**
 * Created by yichao on 2017/11/2.
 */

public class SiteUtils {

    public static final String TAG = "SiteUtils";

    public static final int DEFAULT_PORT = 2021;

    private SiteUtilsListener siteUtilsListener;
    GroupPresenter groupPresenter;
    SitePresenter sitePresenter;

    public interface SiteUtilsListener {
        void onPrepareSiteMsg(String msg);

        void onPrepareSiteSuccess(Site currentSite);
    }

    /**
     * 检测站点信息是否完全，如果不完全从库中重新获取返回
     *
     * @param site
     * @return
     */
    public static Site getAndCheckLegalSite(Site site) {
        if (site == null || TextUtils.isEmpty(site.getSiteHost())
                || TextUtils.isEmpty(site.getSitePort()) || TextUtils.isEmpty(site.getSiteUserId()) || TextUtils.isEmpty(site.getGlobalUserId())) {
            return SitePresenter.getInstance().getSiteByHostAndPort(site.getSiteHost(), site.getSitePort());
        }
        return site;
    }


    public static boolean currentContains(Site site) {
        ZalyLogUtils.getInstance().info(TAG, "login site is " + site.getSiteIdentity());

        if (ZalyApplication.siteList != null && ZalyApplication.siteList.size() > 0) {
            for (Site existingSite : ZalyApplication.siteList) {
                ZalyLogUtils.getInstance().info(TAG, " existing site is " + existingSite.getSiteIdentity());
                if (existingSite.getSiteIdentity().equals(site.getSiteIdentity())) {
                    site.setSiteUserId(existingSite.getSiteUserId());
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean removeCurrent(Site site) {
        int index = -1;
        if (ZalyApplication.siteList != null && ZalyApplication.siteList.size() > 0) {
            for (Site existingSite : ZalyApplication.siteList) {
                if (existingSite.getSiteIdentity().equals(site.getSiteIdentity())) {
                    index = ZalyApplication.siteList.indexOf(existingSite);
                }
            }
            if (index >= 0) {
                ZalyApplication.siteList.remove(index);
                return true;
            }
        }

        return false;
    }

    /**
     * 准备工作：
     * 1. 检查数据库
     * 2. 准备站点数据
     */
    public void prepareDo(SiteUtilsListener siteUtilsListener) {
        //检查该siteUserId的数据库
        this.siteUtilsListener = siteUtilsListener;
        ZalyTaskExecutor.executeTask(TAG, new CheckDBTask());
    }

    /**
     * 数据库检查任务
     */
    public class CheckDBTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {
        @Override
        protected void onPreTask() {
            super.onPreTask();
            log("开始检查数据库");
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            //TODO DBChange
            //1.存放任务表 imConnection
            ZalyDbHelper zalyDbHelper = new ZalyDbHelper(ZalyApplication.getContext());
            zalyDbHelper.checkBaseTable();

            SitePresenter.getInstance().checkCommonBaseTable();

            if (StringUtils.isEmpty(ZalyApplication.getGlobalUserId())) {
                //无身份不检测
                return false;
            }

            List<Site> sites = SitePresenter.getInstance().getAllSiteLists(false);
//          //////List<Site> sites = ZalyDAO.getInstance().queryAllSite(false);

            if (sites != null) {
                //////TODO DBChange 检查所有站点的数据库
                for (Site site : sites) {
                    SitePresenter.getInstance().checkSiteBaseTable(site.getSiteAddress());
                }
                return true;
            } else {
                ZalyLogUtils.getInstance().info(TAG, "检测当前数据库站点为空=" + sites.size());
            }
            return true;
        }


        @Override
        protected void onTaskSuccess(Boolean aBoolean) {
            super.onTaskSuccess(aBoolean);

            if (aBoolean) {
                ZalyTaskExecutor.executeUserTask(TAG, new InitSiteTask());
            }

        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
            Toaster.showInvalidate("检查数据库失败");
        }
    }

    /**
     * 初始化站点任务
     */
    public class InitSiteTask extends ZalyTaskExecutor.Task<Void, Void, List<Site>> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            log("初始化站点信息");
        }

        @Override
        protected List<Site> executeTask(Void... voids) throws Exception {
            //TODO DBChange
            return SitePresenter.getInstance().getAllSiteLists();
        }

        @Override
        protected void onTaskSuccess(List<Site> sites) {
            super.onTaskSuccess(sites);
            log("初始化站点完成");
            if (sites == null || sites.size() == 0) {
                if (siteUtilsListener != null) {
                    Site site = new Site();
                    siteUtilsListener.onPrepareSiteSuccess(site);
                }
                return;
            }
            //获取所有站点
            ZalyApplication.siteList = sites;
            Site currentSite = new Site();

            //查找当前站点
            String curSiteIndentity = ZalyApplication.getCfgSP().getString(Configs.KEY_CUR_SITE, "");
            if (StringUtils.isEmpty(curSiteIndentity)) {
                for (Site site : sites) {
                    if (site.getSiteStatus() == Site.STATUS_SITE_ONLINE) {
                        ZalyApplication.getCfgSP().put(Configs.KEY_CUR_SITE, site.getSiteIdentity());
                        currentSite = site;
                        break;
                    }
                }
            } else {
                for (Site site : sites) {
                    if (curSiteIndentity.equals(site.getSiteIdentity())) {
                        currentSite = site;
                        break;
                    }
                }
                //说明数据库被篡改异常
                if (currentSite == null) {
                    log("应用数据被篡改，请卸载并重装！");
                    //todo return
//                    return;
                    for (Site site : sites) {
                        if (site.getSiteStatus() == Site.STATUS_SITE_ONLINE) {
                            ZalyApplication.getCfgSP().put(Configs.KEY_CUR_SITE, site.getSiteIdentity());
                            currentSite = site;
                            break;
                        }
                    }
                }
            }

            if (siteUtilsListener != null) {
                siteUtilsListener.onPrepareSiteSuccess(currentSite);
            }

            ZalyTaskExecutor.executeUserTask(TAG, new GetSiteInfoTask(currentSite));
        }
    }


    /**
     * 获取群通讯录
     */
    public static class GetGroupListTask extends ZalyTaskExecutor.Task<Void, Void, ApiGroupListProto.ApiGroupListResponse> {

        private Site site;

        public GetGroupListTask(Site site) {
            this.site = site;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        @Override
        protected ApiGroupListProto.ApiGroupListResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(site).getGroupApi().getGroupFromSite(site.getSiteUserId());
        }

        @Override
        protected void onTaskSuccess(ApiGroupListProto.ApiGroupListResponse apiGetGroupsResponse) {
            super.onTaskSuccess(apiGetGroupsResponse);
            List<GroupProto.SimpleGroupProfile> groupsimpleProfiles = apiGetGroupsResponse.getListList();
            if (groupsimpleProfiles != null && groupsimpleProfiles.size() > 0) {
                GroupPresenter.getInstance(site).batchInsertGroup(groupsimpleProfiles);
            }
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {

        }

        @Override
        protected void onTaskError(Exception e) {

        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
        }
    }

    /**
     * 获取站点信息并更新数据库
     */
    class GetSiteInfoTask extends ZalyTaskExecutor.Task<Void, Void, ApiSiteConfigProto.ApiSiteConfigResponse> {

        private Site site;

        public GetSiteInfoTask(Site site) {
            this.site = site;
        }

        @Override
        protected ApiSiteConfigProto.ApiSiteConfigResponse executeTask(Void... voids) throws Exception {
            Logger.i(TAG, "GetSiteInfoTask execute");
            return ApiClient.getInstance(site).getSiteApi().getSiteInfo();
        }


        @Override
        protected void onTaskSuccess(ApiSiteConfigProto.ApiSiteConfigResponse response) {
            Logger.i(TAG, "GetSiteInfoTask onDownloadSuccess");
            if (response != null) {
                site.setSiteName(response.getSiteConfig().getSiteName());
                site.setSiteIcon(response.getSiteConfig().getSiteLogo());
                site.setSiteVersion(response.getSiteConfig().getSiteVersion());
                Site curSite = null;
                for (Site s : ZalyApplication.siteList) {
                    if (s.getSiteIdentity().equals(site.getSiteIdentity())) {
                        curSite = s;
                        break;
                    }
                }
                if (curSite != null) {
                    curSite.setSiteName(site.getSiteName());
                }

            }
        }

        @Override
        protected void onTaskError(Exception e) {
            Logger.w(TAG, "GetSiteInfoTask onTaskError");
        }
    }

    private void log(String message) {
        Logger.i(TAG, message);
        if (siteUtilsListener != null) {
            siteUtilsListener.onPrepareSiteMsg(message);
        }
    }


    public static void syncSiteInfo(final Site site) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                ZalyTaskExecutor.executeUserTask(TAG, new ApiUserProfileTask(site));
                ZalyTaskExecutor.executeUserTask(TAG, new SiteUtils.GetGroupListTask(site));
                //todo 该步骤需要移动至IM进程处理
                //主动sync当前站点信息
                ZalyLogUtils.getInstance().info(TAG, " SYNC CUR SITE MSG ");

                try {
                    String curSiteIdentity = site.getSiteIdentity();
                    IMClient.getInstance(new SiteAddress(curSiteIdentity)).syncMessage();
                } catch (Exception e) {
                    ZalyLogUtils.getInstance().info(TAG, " SYNC CUR SITE MSG FAIL!" + e.getMessage());
                }
            }
        }).start();
    }

}
