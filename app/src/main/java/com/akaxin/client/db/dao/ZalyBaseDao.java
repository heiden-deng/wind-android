package com.akaxin.client.db.dao;

import android.database.sqlite.SQLiteDatabase;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.db.helper.AkxCommonDBHelper;
import com.akaxin.client.db.helper.AkxDBManager;
import com.akaxin.client.db.helper.AkxSiteDBHelper;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.windchat.im.socket.SiteAddress;


import java.util.HashMap;

/**
 * Created by zhangjun on 2018/3/28.
 */

public class ZalyBaseDao {
    private static final String TAG = ZalyBaseDao.class.getSimpleName();

    public volatile static HashMap<String, SQLiteDatabase> daoHelperMap = new HashMap<>();
    public volatile static HashMap<String, SQLiteDatabase> daoCommonHelperMap = new HashMap<>();

    protected static AkxSiteDBHelper dbHelper;
    protected static SiteAddress siteAddress;
    protected static String siteUserId;
    protected static String globalUserId;
    protected static SQLiteDatabase database;
    protected static AkxCommonDBHelper dbCommonHelper;
    protected static SQLiteDatabase commonDatabase;
    private static ZalyBaseDao instance = null;

    public SQLiteDatabase getSiteDatabase(SiteAddress address) {

        SQLiteDatabase database = daoHelperMap.get(address.getSiteDBAddress());
        if (database == null) {
            openSiteDb(address);
        }
        database = daoHelperMap.get(address.getSiteDBAddress());
        if (database.isReadOnly()) {
            ZalyLogUtils.getInstance().info(TAG, "db is read only , address is " + address);
            openSiteDb(address);
        }
        database = daoHelperMap.get(address.getSiteDBAddress());
        return database;
    }

    public SQLiteDatabase getCommonDatabase() {
        SQLiteDatabase commonDb = daoCommonHelperMap.get(SiteConfig.DB_COMMON_HELPER);
        if (commonDb == null) {
            openCommonDb();
        }
        commonDb = daoCommonHelperMap.get(SiteConfig.DB_COMMON_HELPER);
        if (commonDb.isReadOnly()) {
            ZalyLogUtils.getInstance().info(TAG, "db is read only ");
            openCommonDb();
        }
        commonDb = daoCommonHelperMap.get(SiteConfig.DB_COMMON_HELPER);
        return commonDb;
    }

    private ZalyBaseDao(SiteAddress address) {
        if (!daoHelperMap.containsKey(address.getSiteDBAddress())) {
            openSiteDb(address);
        }
    }

    private static void openSiteDb(SiteAddress siteAddress) {
        synchronized (ZalyBaseDao.class) {
            //查询当前站点的siteUserId
            String host = siteAddress.getHost();
            int port = siteAddress.getPort();
            Site site = AkxCommonDao.getInstance().querySiteInfo(siteAddress);
            siteUserId = site.getSiteUserId();
            globalUserId = site.getGlobalUserId();

            dbHelper = AkxDBManager.getSiteDBHelper(ZalyApplication.getContext(), siteAddress, globalUserId);
            database = dbHelper.getWritableDatabase();
            daoHelperMap.put(siteAddress.getSiteDBAddress(), database);
        }
    }

    public static synchronized ZalyBaseDao getInstance(SiteAddress siteAddress) {
        if (instance == null) {
            instance = new ZalyBaseDao(siteAddress);
        }
        return instance;
    }

    private ZalyBaseDao() {
        if (!daoHelperMap.containsKey(SiteConfig.DB_COMMON_HELPER)) {
            openCommonDb();
        }
    }

    private static void openCommonDb() {
        synchronized (ZalyBaseDao.class) {
            dbCommonHelper = AkxDBManager.getCommonDBHelper(ZalyApplication.getContext());
            commonDatabase = dbCommonHelper.getWritableDatabase();
            daoCommonHelperMap.put(SiteConfig.DB_COMMON_HELPER, commonDatabase);
        }
    }

    public static ZalyBaseDao getInstance() {
        return ZalyBaseDao.SingletonCommonHolder.instance;
    }

    static class SingletonCommonHolder {
        private static ZalyBaseDao instance = new ZalyBaseDao();
    }

}
