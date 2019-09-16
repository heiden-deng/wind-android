package com.akaxin.client.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.User;
import com.akaxin.client.db.sql.DBSQL;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.SiteAddress;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端登陆不同站点，针对站点的相关数据库操作
 * Created by anguoyue on 28/02/2018.
 */

public class AkxCommonDao {
    private static final String TAG = AkxCommonDao.class.getSimpleName();

    private static final String AKX_SITE_TABLE = DBSQL.AKX_SITE_TABLE;
    private static final String AKX_USER_IDENTITY_TABLE = DBSQL.AKX_USER_IDENTITY_TABLE;
    private static volatile AkxCommonDao dao;
    protected SQLiteDatabase database;

    private AkxCommonDao() {
    }

    public static AkxCommonDao getInstance() {
        if (dao == null) {
            synchronized (AkxCommonDao.class) {
                if (dao == null) {
                    dao = new AkxCommonDao();
                }
            }
        }
        dao.database = ZalyBaseDao.getInstance().getCommonDatabase();

        return dao;
    }

    public void removeDaoObject() {
        dao = null;
    }

    /**
     * 新增用户身份,这里不能使用replace into
     *
     * @return
     */
    public synchronized Long insertUserIdentity(User user) {
        long startTime = System.currentTimeMillis();
        long _id;

        try {
            String sql = "INSERT INTO " + AKX_USER_IDENTITY_TABLE +
                    "(global_user_id," +
                    "user_id_prik," +
                    "user_id_pubk," +
                    "device_id_prik," +
                    "device_id_pubk," +
                    "name," +
                    "source) VALUES(?,?,?,?,?,?,?);";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, user.getGlobalUserId());    //身份全局ID
            statement.bindString(2, ZalyApplication.getCfgSP().getKey(Configs.USER_PRI_KEY));  //加密身份公钥
            statement.bindString(3, ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY));  //加密身份私钥
            statement.bindString(4, ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PRI_KEY));  //加密设备公钥
            statement.bindString(5, ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY));  //加密设备公钥
            statement.bindString(6, user.getIdentityName());    //身份名称
            statement.bindString(7, user.getIdentitySource());  //身份来源
            ZalyLogUtils.getInstance().info(TAG, "user_pub_key is " + ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY));
            _id = statement.executeInsert();//返回插入的_id
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;

        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, " error msg is " + e.getMessage());
            String sql = " UPDATE " + AKX_USER_IDENTITY_TABLE +
                    " SET user_id_prik=?, user_id_pubk=?, device_id_prik=?, device_id_pubk=?, name=?,  source=? " +
                    " WHERE global_user_id=?";

            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, user.getGlobalUserId());    //身份全局ID
            statement.bindString(2, ZalyApplication.getCfgSP().getKey(Configs.USER_PRI_KEY));  //加密身份公钥
            statement.bindString(3, ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY));  //加密身份私钥
            statement.bindString(4, ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PRI_KEY));  //加密设备公钥
            statement.bindString(5, ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY));  //加密设备公钥
            statement.bindString(6, user.getIdentityName());    //身份名称
            statement.bindString(7, user.getIdentitySource());  //身份来源
            statement.executeUpdateDelete();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

        }
        return null;
    }

    public synchronized int delUserIdentity() {
        long startTime = System.currentTimeMillis();
        long _id;
        try {
            String sql = "delete from " + AKX_USER_IDENTITY_TABLE;
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            SQLiteStatement statement = database.compileStatement(sql);
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return statement.executeUpdateDelete();
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }
        return User.DEL_USER_FAILED;
    }


    /**
     * 更新用户在平台的认证ID
     *
     * @return
     */
    public synchronized void updateUserPlatformSessionId(String globalUserId, String sessionID) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE " + AKX_USER_IDENTITY_TABLE +
                " SET platform_session_id=?  " +
                " WHERE global_user_id=?  ;";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, sessionID);
        statement.bindString(2, globalUserId);
        statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql + "session id platform_session_id is " + sessionID + " globalUserId is " + globalUserId);
    }

    /**
     * 更新用户在平台的认证ID
     *
     * @return
     */
    public synchronized void updateGlobalUserId(String globalUserId) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE " + AKX_SITE_TABLE +
                " SET global_user_id=? ; ";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, globalUserId);
        statement.executeUpdateDelete();
    }

    /**
     * 查询用户身份
     *
     * @return
     */
    public synchronized User getUserIdentity() {
        long startTime = System.currentTimeMillis();

        String sql = "SELECT * FROM " + AKX_USER_IDENTITY_TABLE + ";";
        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            User user = new User();
            do {
                user.setGlobalUserId(cursor.getString(cursor.getColumnIndex("global_user_id")));
                user.setDeviceIdPubk(cursor.getString(cursor.getColumnIndex("device_id_pubk")));
                user.setUserIdPuk(cursor.getString(cursor.getColumnIndex("user_id_pubk")));
                user.setUserIdPrik(cursor.getString(cursor.getColumnIndex("user_id_prik")));
                user.setPlatformSessionId(cursor.getString(cursor.getColumnIndex("platform_session_id")));
            } while (cursor.moveToNext());
            return user;
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

        return null;
    }

    /**
     * 更新用户在平台的认证ID
     *
     * @return
     */
    public synchronized int updateUserSiteSessionId(String siteUserId, String userSessionId) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE " + AKX_SITE_TABLE +
                " SET site_session_id = ?  " +
                " WHERE site_user_id  = ?  ;";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, userSessionId);
        statement.bindString(2, siteUserId);
        int num = statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return num;
    }

    /**
     * 更新用户在平台的认证ID
     *
     * @return
     */
    public synchronized int updateUserSiteSessionId(String host, int port, Site site) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE " + AKX_SITE_TABLE +
                " SET site_session_id = ? , site_user_id = ? " +
                " WHERE site_host  = ? AND site_port = ?  ;";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, site.getSiteSessionId());
        statement.bindString(2, site.getSiteUserId());
        statement.bindString(3, host);
        statement.bindLong(4, port);

        int num = statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return num;
    }

    public synchronized int updateSiteConnectionStatus(int connStatus, String siteHost, String sitePort) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE " + AKX_SITE_TABLE +
                " SET disconnect_status = ?  " +
                " WHERE site_host  = ? and site_port  = ?  ;";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindLong(1, connStatus);
        statement.bindString(2, siteHost);
        statement.bindString(3, sitePort);
        int num = statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return num;
    }

    public void delSiteInfo(String siteHost, String sitePort) {
        long startTime = System.currentTimeMillis();
        String sql = "DELETE FROM " + AKX_SITE_TABLE +
                " WHERE site_host  = ? and site_port  = ?  ;";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, siteHost);
        statement.bindString(2, sitePort);
        statement.execute();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    /**
     * 插入新增站点信息
     *
     * @return
     */
    public synchronized long insertSite(Site site) {
        long startTime = System.currentTimeMillis();
        long _id;
        String sql = "";
        sql = "INSERT INTO " + AKX_SITE_TABLE +
                "(site_host," +
                "site_port," +
                "site_name," +
                "site_logo," +
                "site_version," +
                "real_name_config," +
                "site_status," +
                "global_user_id," +
                "site_user_id," +
                "site_user_name," +
                "site_user_icon," +
                "latest_time," +
                " is_invite_code, " +
                "site_session_id," +
                "site_scheme) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, site.getSiteHost());
        statement.bindLong(2, site.getSitePort());
        statement.bindString(3, site.getSiteName());
        statement.bindString(4, site.getSiteIcon());//消息帧，消息列表中某一个cells的标识
        statement.bindString(5, site.getSiteVersion());
        statement.bindLong(6, site.getRealNameConfig());
        statement.bindLong(7, site.getSiteStatus());
        statement.bindString(8, site.getGlobalUserId());
        statement.bindString(9, site.getSiteUserId());
        statement.bindString(10, site.getSiteUserName());
        statement.bindString(11, site.getSiteUserImage());
        statement.bindLong(12, site.getLastLoginTime());
        statement.bindLong(13, site.getCodeConfig());
        statement.bindString(14, site.getSiteSessionId());
        statement.bindString(15, "zaly");

        _id = statement.executeInsert();//插入的最后一行id

        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return _id;
    }

    /**
     * 更新某一站点的用户数据，用于在站点管理中显示用户信息
     *
     * @param siteHost
     * @param sitePort
     * @param username
     * @param userimg
     */
    public synchronized void updateSiteUserInfo(String siteHost, String sitePort, String username, String userimg, String siteLoginId) {
        long startTime = System.currentTimeMillis();

        String sql = "UPDATE " + AKX_SITE_TABLE +
                " SET site_user_name = ? , " +
                " site_user_icon = ? , " +
                " site_login_id = ?  " +
                " WHERE site_host = ? " +
                " AND site_port = ?";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, username);
        statement.bindString(2, userimg);
        statement.bindString(3, siteLoginId);
        statement.bindString(4, siteHost);
        statement.bindString(5, sitePort);
        statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    /**
     * 更新某一站点数据
     *
     * @param site
     */
    public synchronized void updateSiteInfo(Site site) {
        long startTime = System.currentTimeMillis();

        String sql = "UPDATE " + AKX_SITE_TABLE +
                " SET site_name = ? , " +
                " site_logo = ? , " +
                " site_version = ? , " +
                " real_name_config = ? , " +
                " is_invite_code = ? " +
                " WHERE site_host = ? " +
                " AND site_port = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, site.getSiteName());
        statement.bindString(2, site.getSiteIcon());
        statement.bindString(3, site.getSiteVersion());
        statement.bindLong(4, site.getRealNameConfig());
        statement.bindLong(5, site.getCodeConfig());
        statement.bindString(6, site.getSiteHost());
        statement.bindLong(7, site.getSitePort());
        statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    public synchronized boolean updadeSiteMute(String siteHost, String sitePort, Boolean isMute) {
        long startTime = System.currentTimeMillis();
        Boolean isMuteFlag;
        try {
            String sql = "UPDATE " + AKX_SITE_TABLE +
                    " SET mute = " + (isMute ? 1 : 0) +
                    " WHERE site_host = '" + siteHost +
                    "' and site_port ='" + sitePort + "' ;";
            database.execSQL(sql);
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

            isMuteFlag = true;
        } catch (Exception e) {
            Logger.e(TAG, e);
            isMuteFlag = false;
        }
        return isMuteFlag;
    }

    public synchronized Site querySiteByHostAndPort(String host, String port) {
        long startTime = System.currentTimeMillis();
        Site site = new Site();
        String sql = "SELECT * FROM " + AKX_SITE_TABLE + " WHERE site_host=? AND site_port=?;";
        Cursor cursor = database.rawQuery(sql, new String[]{host, port});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            site.setGlobalUserId(cursor.getString(cursor.getColumnIndex("global_user_id")));
            site.setSiteUserId(cursor.getString(cursor.getColumnIndex("site_user_id")));
            site.setSiteUserName(cursor.getString(cursor.getColumnIndex("site_user_name")));
            site.setSiteUserImage(cursor.getString(cursor.getColumnIndex("site_user_icon")));
            site.setSiteSessionId(cursor.getString(cursor.getColumnIndex("site_session_id")));
            site.setConnStatus(cursor.getInt(cursor.getColumnIndex("disconnect_status")));
            site.setSiteLoginId(cursor.getString(cursor.getColumnIndex("site_login_id")));
            site.setSiteVersion(cursor.getString(cursor.getColumnIndex("site_version")));
            site.setSiteHost(host);
            site.setSitePort(Integer.valueOf(port));
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return site;
    }

    /**
     * 查询站点表中，站点信息
     *
     * @param siteAddress
     * @return
     */
    public synchronized Site querySiteInfo(SiteAddress siteAddress) {
        long startTime = System.currentTimeMillis();
        Site site = new Site();
        String sql = "SELECT * FROM " + AKX_SITE_TABLE + " WHERE site_host=? AND site_port=?;";
        String host = siteAddress.getHost();
        String port = siteAddress.getPort() + "";

        Cursor cursor = database.rawQuery(sql, new String[]{host, port});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            site.setGlobalUserId(cursor.getString(cursor.getColumnIndex("global_user_id")));
            site.setSiteUserId(cursor.getString(cursor.getColumnIndex("site_user_id")));
            site.setSiteUserName(cursor.getString(cursor.getColumnIndex("site_user_name")));
            site.setSiteUserImage(cursor.getString(cursor.getColumnIndex("site_user_icon")));
            site.setSiteSessionId(cursor.getString(cursor.getColumnIndex("site_session_id")));
            site.setConnStatus(cursor.getInt(cursor.getColumnIndex("disconnect_status")));
            site.setSiteLoginId(cursor.getString(cursor.getColumnIndex("site_login_id")));
            site.setSiteVersion(cursor.getString(cursor.getColumnIndex("site_version")));
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return site;
    }

    /**
     * 查询全部站点
     *
     * @param isNeedUnreadNum 是否需要查询每个站点的未读消息数量（可能会耗时）
     * @return
     */
    public synchronized List<Site> queryAllSite(boolean isNeedUnreadNum) {
        long startTime = System.currentTimeMillis();
        List<Site> sites = new ArrayList<>();
        String sql = "SELECT * FROM " + AKX_SITE_TABLE + ";";
        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Site site = new Site();
                site.setSiteHost(cursor.getString(cursor.getColumnIndex("site_host")));
                site.setSitePort(cursor.getInt(cursor.getColumnIndex("site_port")));
                site.setSiteName(cursor.getString(cursor.getColumnIndex("site_name")));
                site.setSiteUserId(cursor.getString(cursor.getColumnIndex("site_user_id")));
                site.setSiteUserName(cursor.getString(cursor.getColumnIndex("site_user_name")));
                site.setSiteUserImage(cursor.getString(cursor.getColumnIndex("site_user_icon")));
                site.setLastLoginTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("latest_time"))));
                site.setMute(cursor.getInt(cursor.getColumnIndex("mute")) == 1);
                site.setConnStatus(cursor.getInt(cursor.getColumnIndex("disconnect_status")));
                int status = cursor.getInt(cursor.getColumnIndex("site_status"));
                site.setSiteStatus(status);
                site.setSiteSessionId(cursor.getString(cursor.getColumnIndex("site_session_id")));
                site.setCodeConfig(cursor.getInt(cursor.getColumnIndex("is_invite_code")));
                site.setSiteIcon(cursor.getString(cursor.getColumnIndex("site_logo")));
                site.setSiteVersion(cursor.getString(cursor.getColumnIndex("site_version")));
                site.setSiteLoginId(cursor.getString(cursor.getColumnIndex("site_login_id")));

                if (isNeedUnreadNum) {
                    site.setUnreadNum(querySiteTotalUnreadNum(site.getSiteHost() + ":" + site.getSitePort()));
                }
                sites.add(site);
            } while (cursor.moveToNext());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return sites;
    }


    /**
     * 查询站点未读消息总数
     *
     * @return
     */
    public synchronized long querySiteTotalUnreadNum(String siteAddress) {
        SiteAddress siteAddressObj = new SiteAddress(siteAddress);
        return SiteChatSessionDao.getInstance(siteAddressObj).querySiteAllUnreadNum();
    }
}
