package com.windchat.client.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.bean.ChatSession;
import com.windchat.client.bean.Session;
import com.windchat.client.db.sql.DBSQL;
import com.windchat.client.util.data.StringUtils;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.SiteAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by anguoyue on 28/02/2018.
 */

public class SiteChatSessionDao {
    private static final String TAG = SiteChatSessionDao.class.getSimpleName();

    private static final String U2_MSG_TABLE    = DBSQL.SITE_U2_MSG_TABLE;
    private static final String GROUP_MSG_TABLE = DBSQL.SITE_GROUP_MSG_TABLE;
    private static final String SITE_CHAT_SESSION_TABLE  = DBSQL.SITE_CHAT_SESSION_TABLE;
    private static final String SITE_USE_PROFILE_TABLE   = DBSQL.SITE_USER_PROFILE_TABLE;
    private static final String SITE_GROUP_PROFILE_TABLE = DBSQL.SITE_GROUP_PROFILE_TABLE;

    private static HashMap<String, SiteChatSessionDao> daoMap = new HashMap<>();
    private SQLiteDatabase database;
    private SiteAddress siteAddress;

    private SiteChatSessionDao(SiteAddress siteAddress) {
        this.siteAddress = siteAddress;
    }

    public static SiteChatSessionDao getInstance(SiteAddress address) {
        if (!daoMap.containsKey(address.getSiteDBAddress())) {
            SiteChatSessionDao dao = new SiteChatSessionDao(address);
            dao.database = ZalyBaseDao.getInstance(address).getSiteDatabase(address);
            daoMap.put(address.getSiteDBAddress(), dao);
        }
        return daoMap.get(address.getSiteDBAddress());
    }

    public static void destroyAllInstances() {
        daoMap.clear();
    }

    public  void removeChatSessionDaoMap(SiteAddress address){
        if(daoMap.containsKey(address.getSiteDBAddress())) {
            daoMap.remove(address.getSiteDBAddress());
        }
    }

    /**
     * 新增/替代聊天会话
     * 这里需要改成，先update，如果失败在insert
     *
     * @param chatSession
     */
    public synchronized Long replaceChatSession(ChatSession chatSession) {
        long startTime = System.currentTimeMillis();
        Long _id;
        try {
            String sql = "REPLACE INTO " + DBSQL.SITE_CHAT_SESSION_TABLE + " (" +
                    "chat_session_id, " +
                    "latest_msg," +
                    "type, " +
                    "unread_num," +
                    "session_goto, " +
                    "status, " +
                    "edit_text," +
                    "open_ts_chat, " +
                    "latest_time)" + " VALUES (?,?,?,?,?,?,?,?,?)";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, chatSession.getChatSessionId());
            statement.bindString(2, chatSession.getLatestMsg());
            statement.bindLong(3, chatSession.getType());
            ////自己消息入库的时候，未读数设置为0；
            statement.bindLong(4, 0);
            if(chatSession.getUnreadNum() == 1) {
                statement.bindLong(4, getUnreadNum(chatSession.getChatSessionId(), chatSession.getUnreadNum()));
            }
            statement.bindString(5, chatSession.getSessionGoto());
            statement.bindLong(6, ChatSession.STATUS_NEW_SESSION);
            statement.bindString(7, chatSession.getEditText());
            statement.bindLong(8, getTSByChatSessionId(chatSession.getChatSessionId()));
            statement.bindLong(9, chatSession.getLatestTime());
            _id = statement.executeInsert();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    public synchronized int updateTSByChatSessionId(boolean isOpenTsChat, String chatSessionId){
        long startTime = System.currentTimeMillis();
        Long _id;
        try{
            String sql = " update " +  DBSQL.SITE_CHAT_SESSION_TABLE + " set open_ts_chat = ? where chat_session_id = ?";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindLong(1, isOpenTsChat ? 1: 0);
            statement.bindString(2, chatSessionId);
            return statement.executeUpdateDelete();
        }catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        return 0;
    }
    public synchronized Long getTSByChatSessionId(String chatSessionId){
        long startTime = System.currentTimeMillis();
        Long _id;
        Long isOpenTsChat = Long.valueOf(0);
        try{
            String sql = " select open_ts_chat from  " +  DBSQL.SITE_CHAT_SESSION_TABLE + "  where chat_session_id = ?";
            Cursor cursor = database.rawQuery(sql, new String[]{chatSessionId});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                isOpenTsChat = cursor.getLong(cursor.getColumnIndex("open_ts_chat"));
            }
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        }catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        return isOpenTsChat;
    }


    /**
     * 新增/替代聊天会话
     * 通讯录点击的时候，第一次可以insert, 如果session中存在会话，则更新
     *
     * @param chatSession
     */
    public synchronized Long insertChatSession(ChatSession chatSession) {
        long startTime = System.currentTimeMillis();
        Long _id;
        try {
            String sql = "INSERT INTO " + DBSQL.SITE_CHAT_SESSION_TABLE + " (" +
                    "chat_session_id, " +
                    "latest_msg," +
                    "type, " +
                    "unread_num," +
                    "session_goto, " +
                    "status, " +
                    "edit_text," +
                    "open_ts_chat, " +
                    "latest_time)" + " VALUES (?,?,?,?,?,?,?,?,?)";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, chatSession.getChatSessionId());
            statement.bindString(2, chatSession.getLatestMsg());
            statement.bindLong(3, chatSession.getType());
            statement.bindLong(4, getUnreadNum(chatSession.getChatSessionId(), chatSession.getUnreadNum()));
            statement.bindString(5, chatSession.getSessionGoto());
            statement.bindLong(6, ChatSession.STATUS_NEW_SESSION);
            statement.bindString(7, chatSession.getEditText());
            statement.bindLong(8, getTSByChatSessionId(chatSession.getChatSessionId()));
            statement.bindLong(9, chatSession.getLatestTime());
            _id = statement.executeInsert();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, "session 已经存在，不在做插入操作");
            return null;
        }
    }


    /**
     * 批量新增会话窗口
     *
     * @param chatSessions
     */
    public synchronized void batchInsertChatSession(List<ChatSession> chatSessions) {
        if (chatSessions == null) {
            return;
        }
        long startTime = System.currentTimeMillis();

        String sql = "REPLACE INTO " + DBSQL.SITE_CHAT_SESSION_TABLE + " (" +
                "chat_session_id, " +
                "latest_msg," +
                "type, " +
                "unread_num," +
                "session_goto, " +
                "status, " +
                "edit_text, " +
                "latest_time)" + " VALUES (?,?,?,?,?,?,?,?)";

        database.beginTransaction();
        SQLiteStatement statement = database.compileStatement(sql);
        for (ChatSession chatSession : chatSessions) {
            try {
                statement.bindString(1, chatSession.getChatSessionId());
                statement.bindString(2, chatSession.getLatestMsg());
                statement.bindLong(3, chatSession.getType());
                statement.bindLong(4, getUnreadNum(chatSession.getChatSessionId(), chatSession.getUnreadNum()));
                ZalyLogUtils.getInstance().info(TAG, "chatSession.getUnreadNum(): " + chatSession.getUnreadNum());
                statement.bindString(5, chatSession.getSessionGoto());
                statement.bindLong(6, ChatSession.STATUS_NEW_SESSION);
                statement.bindString(7, chatSession.getEditText());
                statement.bindLong(8, chatSession.getLatestTime());
                statement.executeInsert();
            } catch (Exception e) {
                ZalyLogUtils.getInstance().exceptionError(e);
            }
            statement.clearBindings();
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }


    public synchronized long getUnreadNum(String chatSessionId, long num) {
        long currNum = queryUnreadNum(chatSessionId);
        return currNum + num;
    }

    private synchronized long queryUnreadNum(String chatSessionId) {
        long startTime = System.currentTimeMillis();
        long unreadNum = 0;
        String sql = "SELECT unread_num FROM " + SITE_CHAT_SESSION_TABLE + " WHERE chat_session_id=?;";
        Cursor cursor = database.rawQuery(sql, new String[]{chatSessionId});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            unreadNum = cursor.getLong(cursor.getColumnIndex("unread_num"));
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return unreadNum;
    }


    public synchronized long querySiteAllUnreadNum() {
        long startTime = System.currentTimeMillis();
        long unreadNum = 0;
        String sql = "SELECT SUM(unread_num) AS all_unread_num " +
                " FROM " + SITE_CHAT_SESSION_TABLE + ";";
        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            unreadNum = cursor.getLong(cursor.getColumnIndex("all_unread_num"));
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

        return unreadNum;
    }

    public synchronized long queryChatSessionByChatSessionId(String chatSessionId) {
        long startTime = System.currentTimeMillis();
        long _id = 0;
        try{
            String sql = "SELECT _id  " +
                    " FROM " + SITE_CHAT_SESSION_TABLE + " where chat_session_id = ?;";
            Cursor cursor = database.rawQuery(sql, new String[]{chatSessionId});
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                _id = cursor.getLong(cursor.getColumnIndex("id"));
            }
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

        }catch ( Exception e) {
            ZalyLogUtils.getInstance().info(TAG, e.getMessage());
        }
        return _id;
    }

    /**
     * 删除会话
     *
     * @param chat_session_id
     * @return
     */
    public synchronized int deleteSessionById(String chat_session_id) {
        long startTime = System.currentTimeMillis();
        String sql = "DELETE FROM " + SITE_CHAT_SESSION_TABLE +
                " WHERE chat_session_id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, chat_session_id);
        int num = statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

        return num;
    }

    /**
     * 清理会话的气泡
     *
     * @param chatSessionId
     * @return
     */
    public synchronized long cleanUnreadNum(String chatSessionId) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE " + SITE_CHAT_SESSION_TABLE +
                " SET unread_num=? " +
                " WHERE chat_session_id=?;";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindLong(1, 0);
        statement.bindString(2, chatSessionId);
        int num = statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return num;
    }

    /**
     * 查询所有聊天会话，此处应该分页展示
     *
     * @return
     */
    //#TODO
    public synchronized List<ChatSession> queryChatSessions() {
        long startTime = System.currentTimeMillis();
        List<ChatSession> sessions = new ArrayList<>();

        String sql = "SELECT " +
                "chat_session_id," +
                "cs.latest_time as latest_time," +
                "type," +
                "status," +
                "unread_num," +
                "latest_msg," +
                "site_user_name," +
                "site_user_icon," +
                "site_group_name," +
                "site_group_icon," +
                "u.mute as u_mute," +
                "g.mute as g_mute" +
                " FROM " + SITE_CHAT_SESSION_TABLE + " AS cs " +
                "LEFT JOIN " + SITE_USE_PROFILE_TABLE + " AS u ON cs.chat_session_id = u.site_user_id " +
                "LEFT JOIN " + SITE_GROUP_PROFILE_TABLE + " AS g ON cs.chat_session_id = g.site_group_id " +
                "ORDER BY latest_time DESC;";

        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                ChatSession session = new ChatSession();
                session.setChatSessionId(cursor.getString(cursor.getColumnIndex("chat_session_id")));
                session.setLatestTime(cursor.getLong(cursor.getColumnIndex("latest_time")));
                session.setType(cursor.getInt(cursor.getColumnIndex("type")));
                session.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                session.setUnreadNum(cursor.getInt(cursor.getColumnIndex("unread_num")));
                session.setLatestMsg(cursor.getString(cursor.getColumnIndex("latest_msg")));

                if (Session.TYPE_FRIEND_SESSION == session.getType()) {
                    session.setTitle(cursor.getString(cursor.getColumnIndex("site_user_name")));
                    session.setIcon(cursor.getString(cursor.getColumnIndex("site_user_icon")));
                    session.setMute(cursor.getInt(cursor.getColumnIndex("u_mute")) == 1);
                } else if (Session.TYPE_GROUP_SESSION == session.getType()) {
                    session.setTitle(cursor.getString(cursor.getColumnIndex("site_group_name")));
                    session.setIcon(cursor.getString(cursor.getColumnIndex("site_group_icon")));
                    session.setMute(cursor.getInt(cursor.getColumnIndex("g_mute")) == 1);
                }

                if (StringUtils.isEmpty(session.getChatSessionId())) {
                    continue;
                }

                sessions.add(session);
            } while (cursor.moveToNext());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return sessions;
    }
}
