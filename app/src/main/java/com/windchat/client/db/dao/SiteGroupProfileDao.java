package com.windchat.client.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.bean.Group;
import com.windchat.client.db.bean.UserGroupBean;
import com.windchat.client.db.sql.DBSQL;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.UserProto;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.SiteAddress;

import java.util.HashMap;

/**
 * 客户端登陆不同站点，针对站点的相关数据库操作
 * Created by anguoyue on 28/02/2018.
 */

public class SiteGroupProfileDao  {
    private static final String TAG = SiteGroupProfileDao.class.getSimpleName();
    private static final String SITE_GROUP_PROFILE_TABLE = DBSQL.SITE_GROUP_PROFILE_TABLE;


    private static HashMap<String, SiteGroupProfileDao> daoMap = new HashMap<>();
    private SQLiteDatabase database;
    private SiteAddress siteAddress;


    private SiteGroupProfileDao(SiteAddress siteAddress) {
        this.siteAddress = siteAddress;
    }

    public static SiteGroupProfileDao getInstance(SiteAddress address) {
        if (!daoMap.containsKey(address.getSiteDBAddress())) {
            SiteGroupProfileDao dao = new SiteGroupProfileDao(address);
            dao.database = ZalyBaseDao.getInstance(address).getSiteDatabase(address);
            daoMap.put(address.getSiteDBAddress(), dao);
        }
        return daoMap.get(address.getSiteDBAddress());
    }

    public  void removeGroupProfileDaoMap(SiteAddress address){
        if(daoMap.containsKey(address.getSiteDBAddress())) {
            daoMap.remove(address.getSiteDBAddress());
        }
    }


    public synchronized void updateSiteGroupProfile(String siteGroupId, UserProto.UserProfile userProfile, int countNumber, boolean isInviteClose) {
        long startTime = System.currentTimeMillis();

        String sql = " UPDATE " + SITE_GROUP_PROFILE_TABLE +
                " SET group_owner_id = ?, group_owner_name = ?, group_owner_icon = ?, count_member = ?, is_close_invite = ? " +
                " WHERE site_group_id = ?;";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, userProfile.getSiteUserId());
        statement.bindString(2, userProfile.getUserName());
        statement.bindString(3, userProfile.getUserPhoto());
        statement.bindLong(4,countNumber);
        statement.bindLong(5, isInviteClose ? 1 : 0);
        statement.bindString(6, siteGroupId);
        statement.execute();
        statement.clearBindings();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    public synchronized void updateSiteGroupProfile(String siteGroupId, String siteGroupName) {
        long startTime = System.currentTimeMillis();

        String sql = " UPDATE " + SITE_GROUP_PROFILE_TABLE +
                " SET site_group_name = ? " +
                " WHERE site_group_id = ?;";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, siteGroupName);
        statement.bindString(2, siteGroupId);
        statement.execute();
        statement.clearBindings();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    public synchronized  Long createSiteSimpleGroupProfile(UserGroupBean bean) {
        long startTime = System.currentTimeMillis();
        Long _id = null;

        try{
            String sql = "INSERT INTO  " + SITE_GROUP_PROFILE_TABLE + " (" +
                    "site_group_id," +
                    "site_group_name," +
                    "site_group_icon," +
                    "group_owner_id," +
                    "group_owner_name, " +
                    "group_owner_icon," +
                    "is_group_member," +
                    "count_member," +
                    "is_close_invite,"+
                    "mute," +
                    "latest_time" +
                    ") " + "VALUES (?,?,?,?,?,?,?,?,?, ?, ?)";
                SQLiteStatement statement = database.compileStatement(sql);
                statement.bindString(1, bean.getGroupId());
                statement.bindString(2, bean.getGroupName());
                statement.bindString(3, bean.getGroupImage());
                statement.bindString(4, bean.getGroupOwnerId());
                statement.bindString(5, bean.getGroupOwnerName());
                statement.bindString(6, bean.getGroupOwnerIcon());
                statement.bindLong(7, bean.isAsGroupMember() ? 1 : 0);
                statement.bindLong(8, bean.getGroupCountMember());
                statement.bindLong(9, bean.isCloseInviteGroupChat() ? 1 : 0);
                statement.bindLong(10, bean.isMute() ? 1 : 0);
                statement.bindLong(11, bean.getLatestTime());
                _id = statement.executeInsert();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        }catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }
        return _id;
    }

    public synchronized Long insertSiteGroupProfile(UserGroupBean bean) {
        long startTime = System.currentTimeMillis();
        Long _id = null;

        String sql = "REPLACE INTO  " + SITE_GROUP_PROFILE_TABLE + " (" +
                "site_group_id," +
                "site_group_name," +
                "site_group_icon," +
                "group_owner_id," +
                "group_owner_name, " +
                "group_owner_icon," +
                "is_group_member," +
                "count_member," +
                "is_close_invite,"+
                "mute," +
                "latest_time" +
                ") " + "VALUES (?,?,?,?,?,?,?,?,?, ?, ?)";

        try {
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, bean.getGroupId());
            statement.bindString(2, bean.getGroupName());
            statement.bindString(3, bean.getGroupImage());
            statement.bindString(4, bean.getGroupOwnerId());
            statement.bindString(5, bean.getGroupOwnerName());
            statement.bindString(6, bean.getGroupOwnerIcon());
            statement.bindLong(7, bean.isAsGroupMember() ? 1 : 0);
            statement.bindLong(8, bean.getGroupCountMember());
            statement.bindLong(9, bean.isCloseInviteGroupChat() ? 1 : 0);
            statement.bindLong(10, bean.isMute() ? 1 : 0);
            statement.bindLong(11, bean.getLatestTime());
            _id = statement.executeInsert();
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return _id;
    }

    /**
     * 查询一个群组
     *
     * @return
     */
    public synchronized GroupProto.SimpleGroupProfile queryGroupProfileByGroupId(String siteGroupId) {
        long startTime = System.currentTimeMillis();
        GroupProto.SimpleGroupProfile profile = null;
        String sql = " SELECT * FROM " + SITE_GROUP_PROFILE_TABLE +
                " WHERE site_group_id = '" + siteGroupId +
                "' AND is_group_member = " + (Group.isGroupMember ? 1 : 0);

        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            profile = GroupProto.SimpleGroupProfile.newBuilder()
                    .setGroupIcon(cursor.getString(cursor.getColumnIndex("site_group_icon")))
                    .setGroupId(cursor.getString(cursor.getColumnIndex("site_group_id")))
                    .setGroupName(cursor.getString(cursor.getColumnIndex("site_group_name")))
                    .build();

        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

        return profile;
    }

    /**
     * 更新站点静音
     * @param siteGroupId
     * @param isMute
     * @return
     */
    public synchronized boolean updateGroupMute(String siteGroupId, Boolean isMute) {
        long startTime = System.currentTimeMillis();
        Boolean isMuteFlag;
        try {
            String sql = "UPDATE " + SITE_GROUP_PROFILE_TABLE +
                    " SET mute = " + (isMute ? 1 : 0) +
                    " WHERE site_group_id = '" + siteGroupId + "' ;";
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
     * 查询一个群组
     *
     * @return
     */
    public synchronized UserGroupBean queryGroupBeanByGroupId(String siteGroupId) {
        long startTime = System.currentTimeMillis();
        UserGroupBean bean = null;
        String sql = " SELECT * FROM " + SITE_GROUP_PROFILE_TABLE +
                " WHERE site_group_id = '" + siteGroupId +
                "' AND is_group_member = " + (Group.isGroupMember ? 1 : 0);

        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            bean = new UserGroupBean();
            bean.setGroupName(cursor.getString(cursor.getColumnIndex("site_group_name")));
            bean.setGroupImage(cursor.getString(cursor.getColumnIndex("site_group_icon")));
            bean.setGroupOwnerId(cursor.getString(cursor.getColumnIndex("group_owner_id")));
            bean.setGroupId(cursor.getString(cursor.getColumnIndex("site_group_id")));
            bean.setGroupOwnerName(cursor.getString(cursor.getColumnIndex("group_owner_name")));
            bean.setGroupOwnerIcon(cursor.getString(cursor.getColumnIndex("group_owner_icon")));
            bean.setMute(cursor.getInt(cursor.getColumnIndex("mute")) == 1);
            bean.setCloseInviteGroupChat(cursor.getInt(cursor.getColumnIndex("is_close_invite")) == 1);
            bean.setGroupCountMember(cursor.getInt(cursor.getColumnIndex("count_member")));
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return bean;
    }

}
