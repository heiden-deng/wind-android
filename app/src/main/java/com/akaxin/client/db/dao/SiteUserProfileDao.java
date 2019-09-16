package com.akaxin.client.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.akaxin.client.bean.User;
import com.akaxin.client.db.bean.UserFriendBean;
import com.akaxin.client.db.sql.DBSQL;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.UserProto;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.SiteAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 客户端登陆不同站点，针对站点的相关数据库操作
 * Created by anguoyue on 28/02/2018.
 */

public class SiteUserProfileDao {
    private static final String TAG = SiteUserProfileDao.class.getSimpleName();
    private static final String SITE_USER_PROFILE_TABLE = DBSQL.SITE_USER_PROFILE_TABLE;

    private static HashMap<String, SiteUserProfileDao> daoMap = new HashMap<>();
    protected SQLiteDatabase database;
    private SiteAddress siteAddress;

    private SiteUserProfileDao(SiteAddress address) {
        this.siteAddress = address;
    }

    public static SiteUserProfileDao getInstance(SiteAddress address) {
        if (!daoMap.containsKey(address.getSiteDBAddress())) {
            SiteUserProfileDao dao = new SiteUserProfileDao(address);
            dao.database = ZalyBaseDao.getInstance(address).getSiteDatabase(address);
            daoMap.put(address.getSiteDBAddress(), dao);
        }
        return daoMap.get(address.getSiteDBAddress());
    }

    public  void removeUserProfileDaoMap(SiteAddress address){
        if(daoMap.containsKey(address.getSiteDBAddress())) {
            daoMap.remove(address.getSiteDBAddress());
        }
    }

    /**
     * 群里陌生人的信息
     * 为什么安卓的sqlite 更新会删除掉原来的数据？？？？
     * @param userProfile
     * @return
     */
    public synchronized Long insertStrangerFriend(UserProto.UserProfile userProfile) {
        long startTime = System.currentTimeMillis();
        try {

            long _id;
            String sql = "REPLACE INTO " + SITE_USER_PROFILE_TABLE +
                    " (site_user_id," +
                    " site_user_name," +
                    " site_user_icon, " +
                    " site_nick_name, " +
                    " site_login_id, " +
                    " user_id_pubk, "  +
                    " relation ) " + " values (?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement = database.compileStatement(sql);

            statement.bindString(1, userProfile.getSiteUserId());
            statement.bindString(2, userProfile.getUserName());
            statement.bindString(3, userProfile.getUserPhoto());
            statement.bindString(4, userProfile.getNickName());
            statement.bindString(5, userProfile.getSiteLoginId());
            statement.bindString(6, userProfile.getUserIdPubk());
            statement.bindLong(7, User.RELATION_IS_NOT_FRIEND);

            _id = statement.executeInsert();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;
        } catch (Exception e) {
            Logger.e(TAG, e);
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            return null;
        }
    }

    public synchronized Long insertSiteUserProfile(UserProto.UserProfile userProfile) {
        try {
            long startTime = System.currentTimeMillis();
            long _id;
            String sql = "REPLACE INTO " + SITE_USER_PROFILE_TABLE +
                    " (site_user_id," +
                    " site_user_name," +
                    " site_user_icon, " +
                    " site_nick_name, " +
                    " site_login_id, " +
                    " user_id_pubk, "  +
                    " relation ) " + " values (?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement = database.compileStatement(sql);
        ZalyLogUtils.getInstance().info(TAG, "user image UpdateProfile ==" + userProfile.getUserPhoto());

            statement.bindString(1, userProfile.getSiteUserId());
            statement.bindString(2, userProfile.getUserName());
            statement.bindString(3, userProfile.getUserPhoto());
            statement.bindString(4, userProfile.getNickName());
            statement.bindString(5, userProfile.getSiteLoginId());
            statement.bindString(6, userProfile.getUserIdPubk());
            statement.bindLong(7, User.RELATION_IS_FRIEND);

            _id = statement.executeInsert();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    /**
     * 更新用户资料
     * @param userBean
     * @return
     */
    public synchronized Long updateSiteUserProfile(UserFriendBean userBean) {
        try {
            long startTime = System.currentTimeMillis();
            long _id;
            String sql = "UPDATE " + SITE_USER_PROFILE_TABLE +
                    " SET site_user_name = ?, " +
                    " site_user_icon = ?, " +
                    " site_login_id = ?, " +
                    " site_nick_name = ?, " +
                    " user_id_pubk = ?, " +
                    " relation = ?" +
                    " WHERE site_user_id =?;";

            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1,  userBean.getUserName());
            statement.bindString(2, userBean.getUserImage());
            statement.bindString(3, userBean.getSiteLoginId());
            statement.bindString(4, userBean.getSiteNickName());
            statement.bindString(5, userBean.getUserIdPubk());
            statement.bindLong(6, userBean.getRelation());
            statement.bindString(7, userBean.getSiteUserId());
            statement.execute();
            _id = statement.executeUpdateDelete();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    /**
     * 设置备注
     * @param remarkName
     * @param siteUserId
     * @return
     */
    public synchronized Long updateRemarkName(String remarkName, String siteUserId) {
        try {
            long startTime = System.currentTimeMillis();
            long _id;
            String sql = "UPDATE " + SITE_USER_PROFILE_TABLE +
                    " SET site_user_name = ?" +
                    " WHERE site_user_id =?;";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, remarkName);
            statement.bindString(2, siteUserId);
            statement.execute();
            _id = statement.executeUpdateDelete();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }


    /**
     * 写入用户资料
     * @param bean
     * @return
     */
    public synchronized Long insertSiteUserProfile(UserFriendBean bean) {
        try {
            long startTime = System.currentTimeMillis();
            long _id;
            String sql = "REPLACE INTO " + SITE_USER_PROFILE_TABLE +
                    " (site_user_id," +
                    " site_user_name," +
                    " site_user_icon, " +
                    " site_login_id, " +
                    " site_nick_name, " +
                    " user_id_pubk," +
                    " mute, " +
                    " relation ) " + " values (?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement = database.compileStatement(sql);

            statement.bindString(1, bean.getSiteUserId());
            statement.bindString(2, bean.getUserName());
            statement.bindString(3, bean.getUserImage());
            statement.bindString(4, bean.getSiteLoginId());
            statement.bindString(5, bean.getSiteNickName());
            statement.bindString(6, bean.getUserIdPubk());
            statement.bindLong(7, bean.isMute() ? 1 : 0);
            statement.bindLong(8, bean.getRelation());

            _id = statement.executeInsert();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql + bean.toString());
            return _id;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    public synchronized boolean updadeSiteUserMute(String siteUserId, Boolean isMute) {
        long startTime = System.currentTimeMillis();
        Boolean isMuteFlag;
        try {
            String sql = "UPDATE " + SITE_USER_PROFILE_TABLE +
                    " SET mute = " + (isMute ? 1 : 0) +
                    " WHERE site_user_id = '" + siteUserId + "' ;";
            database.execSQL(sql);
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

            isMuteFlag = true;
        } catch (Exception e) {
            Logger.e(TAG, e);

            isMuteFlag = false;
        }
        return isMuteFlag;
    }

    /**
     * 删除好友
     *
     * @param siteUserId
     * @return
     */
    public synchronized boolean deleteFriendBySiteUserId(String siteUserId) {
        long startTime = System.currentTimeMillis();
        Boolean delFlag;
        try {
            String sql = " UPDATE " + SITE_USER_PROFILE_TABLE +
                    " SET relation = 0 " +
                    " WHERE site_user_id = ?";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, siteUserId);
            statement.execute();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            delFlag = true;
        } catch (Exception e) {
            Logger.e(TAG, e);
            delFlag = false;
        }
        return delFlag;
    }

    /**
     * 查询用户的profile
     *
     * @param siteUserId
     * @return 没有的时候返回null
     */
    public synchronized UserProto.SimpleUserProfile queryFriend(String siteUserId) {
        long startTime = System.currentTimeMillis();
        UserProto.SimpleUserProfile profile = null;
        String sql = "SELECT * FROM " + SITE_USER_PROFILE_TABLE +
                "  WHERE site_user_id = '" + siteUserId + "'";
        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            try {
                cursor.moveToFirst();
                profile = UserProto.SimpleUserProfile.newBuilder()
                        .setSiteUserId(cursor.getString(cursor.getColumnIndex("site_user_id")))
                        .setUserName(cursor.getString(cursor.getColumnIndex("site_user_name")))
                        .setUserPhoto(cursor.getString(cursor.getColumnIndex("site_user_icon")))
                        .setNickName(cursor.getString(cursor.getColumnIndex("site_nick_name")))
                        .setSiteLoginId(cursor.getString(cursor.getColumnIndex("site_login_id")))
                        .build();
                ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
        return profile;
    }

    public synchronized UserFriendBean queryUserById(String siteUserId) {
        long startTime = System.currentTimeMillis();

        UserFriendBean bean = null;
        //TODO 不要使用select *
        String sql = "SELECT * FROM " + SITE_USER_PROFILE_TABLE + "  WHERE site_user_id =?;";
        Cursor cursor = database.rawQuery(sql, new String[]{siteUserId});
        if (cursor != null && cursor.getCount() > 0) {
            try {
                cursor.moveToFirst();
                bean = new UserFriendBean();
                bean.setSiteUserId(cursor.getString(cursor.getColumnIndex("site_user_id")));
                bean.setUserName(cursor.getString(cursor.getColumnIndex("site_user_name")));
                bean.setUserImage(cursor.getString(cursor.getColumnIndex("site_user_icon")));
                bean.setUserIdPubk(cursor.getString(cursor.getColumnIndex("user_id_pubk")));
                bean.setSiteNickName(cursor.getString(cursor.getColumnIndex("site_nick_name")));
                bean.setSiteLoginId(cursor.getString(cursor.getColumnIndex("site_login_id")));
                bean.setMute(cursor.getInt(cursor.getColumnIndex("mute")) == 1);
                bean.setRelation(cursor.getInt(cursor.getColumnIndex("relation")));
                bean.setSiteNickName(cursor.getString(cursor.getColumnIndex("site_nick_name")));
                ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql + siteUserId);
            } catch (Exception e) {
                Logger.e(TAG, e);
            }
        }
        return bean;
    }


    /**
     * 查询全部好友
     *
     * @return
     */
    public synchronized List<UserProto.SimpleUserProfile> queryAllFriend(String siteUserId) {
        long startTime = System.currentTimeMillis();
        List<UserProto.SimpleUserProfile> profiles = new ArrayList<>();
        String sql = "SELECT * FROM " + SITE_USER_PROFILE_TABLE +
                " WHERE site_user_id != '" + siteUserId +
                "' AND relation != 0 " +
                " ORDER BY _id";
        try {
            Cursor cursor = database.rawQuery(sql, new String[]{});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    UserProto.SimpleUserProfile profile = UserProto.SimpleUserProfile.newBuilder()
                            .setSiteUserId(cursor.getString(cursor.getColumnIndex("site_user_id")))
                            .setUserName(cursor.getString(cursor.getColumnIndex("site_user_name")))
                            .setUserPhoto(cursor.getString(cursor.getColumnIndex("site_user_icon")))
                            .setNickName(cursor.getString(cursor.getColumnIndex("site_nick_name")))
                            .setSiteLoginId(cursor.getString(cursor.getColumnIndex("site_login_id")))
                            .build();
                    profiles.add(profile);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return profiles;
    }
}
