package com.windchat.client.db.helper;

import android.content.Context;

import com.windchat.client.ZalyApplication;
import com.windchat.client.db.dao.ZalyBaseDao;
import com.windchat.client.util.data.StringUtils;
import com.windchat.im.socket.SiteAddress;

import java.util.HashMap;

/**
 * Created by anguoyue on 28/02/2018.
 */

public class AkxDBManager {

    private static final String AKX_COMMON_DB = "akx-common-db.sqlite";
    private static final String AKX_SITE_DB = "akx-%s-%s-db.sqlite";

    private static final String TAG = "AkxDBManager";
    private static String userIdNum = ZalyApplication.getUserIdNum();//第几个用户身份


    //获取应用公共DB操作
    public static AkxCommonDBHelper getCommonDBHelper(Context context) {
        String dbName = AKX_COMMON_DB;
        return new AkxCommonDBHelper(context, dbName);

    }

    //获取每个站点的DB操作
    public static AkxSiteDBHelper getSiteDBHelper(Context context, SiteAddress address) {
        String globalUserId = ZalyApplication.getGlobalUserId();
        if (StringUtils.isNotEmpty(globalUserId)) {
            String dbName = String.format(AKX_SITE_DB, globalUserId, address.getSiteDBAddress());
            return new AkxSiteDBHelper(context, dbName);
        }
        return null;
    }


    //获取每个站点的DB操作
    public static AkxSiteDBHelper getSiteDBHelper(Context context, SiteAddress address, String globalUserId) {
        if (StringUtils.isNotEmpty(globalUserId)) {
            String dbName = String.format(AKX_SITE_DB, globalUserId, address.getSiteDBAddress());
            return new AkxSiteDBHelper(context, dbName);
        }
        return null;
    }

    /**
     * 删除数据库
     *
     * @param context
     */
    public static void deleteCommonDb(Context context) {
        ZalyBaseDao.daoCommonHelperMap = new HashMap<>();
        context.deleteDatabase(AKX_COMMON_DB);
    }

    /**
     * 删除某个站点内部的数据库
     */
    public static void deleteSiteInnerDB(Context context, SiteAddress address) {
        ZalyBaseDao.daoHelperMap = new HashMap<>();
        String globalUserId = ZalyApplication.getGlobalUserId();
        String dbName = String.format(AKX_SITE_DB, globalUserId, address.getSiteDBAddress());
        context.deleteDatabase(dbName);
    }

}
