package com.akaxin.client.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.akaxin.client.bean.ChatSession;
import com.akaxin.client.bean.Message;
import com.akaxin.client.db.sql.DBSQL;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.CoreProto;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.SiteAddress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 客户端接受消息 或者 发送消息相关数据库操作
 * Created by anguoyue on 28/02/2018.
 */
public class SiteMessageDao {
    private static final String TAG = SiteMessageDao.class.getSimpleName();

    private static final String SITE_U2_MSG_TABLE = DBSQL.SITE_U2_MSG_TABLE;
    private static final String SITE_GROUP_MSG_TABLE = DBSQL.SITE_GROUP_MSG_TABLE;
    private static final String SITE_USE_PROFILE_TABLE = DBSQL.SITE_USER_PROFILE_TABLE;

    private static final String SESSION_TYPE_U2 = "u2";
    private static final String SESSION_TYPE_GROUP = "group";

    private static HashMap<String, SiteMessageDao> daoMap = new HashMap<>();
    protected SQLiteDatabase database;
    protected SiteAddress siteAddress;
    protected String siteUserId;

    private SiteMessageDao(SiteAddress siteAddress) {
        this.siteAddress = siteAddress;
    }

    public static SiteMessageDao getInstance(SiteAddress address) {
        if (!daoMap.containsKey(address.getSiteDBAddress())) {
            SiteMessageDao dao = new SiteMessageDao(address);
            dao.database = ZalyBaseDao.getInstance(address).getSiteDatabase(address);
            dao.siteUserId = ZalyBaseDao.siteUserId;
            daoMap.put(address.getSiteDBAddress(), dao);
        }
        return daoMap.get(address.getSiteDBAddress());
    }

    public void removeMessageDaoMap(SiteAddress address) {
        if (daoMap.containsKey(address.getSiteDBAddress())) {
            daoMap.remove(address.getSiteDBAddress());
        }
    }

    /**
     * 插入库中新的聊天消息，同时更新聊天界面的会话
     *
     * @param msg 业务层传递来的消息，二人/群组以及其他
     * @return
     */
    public synchronized Long insertU2Message(Message msg) {
        Long _id = null;
        //插入消息
        _id = insertU2Msg(msg);
        //插入聊天会话
        ChatSession chatSession = buildChatSessionFromMessage(msg, SESSION_TYPE_U2);
        insertChatSession(chatSession, siteAddress);
        return _id;
    }

    /**
     * 删除二人聊天消息的一条记录
     *
     * @param msgId
     */
    public synchronized void deleteU2MsgById(String msgId) {
        long startTime = System.currentTimeMillis();
        String sql = "DELETE FROM " + SITE_U2_MSG_TABLE + " WHERE msg_id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, msgId);
        statement.execute();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    /**
     * 删除二人聊天消息
     *
     * @param chatSessionId
     */
    public synchronized void deleteU2MsgByChatSessionId(String chatSessionId) {
        if (chatSessionId.length() < 1) {
            return;
        }
        long startTime = System.currentTimeMillis();
        String sql = "DELETE FROM " + SITE_U2_MSG_TABLE + " WHERE chat_session_id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, chatSessionId);
        statement.execute();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }


    /**
     * 删除群组聊天消息的一条记录
     *
     * @param msgId
     */
    public synchronized void deleteGroupMsgById(String msgId) {
        long startTime = System.currentTimeMillis();
        String sql = "DELETE FROM " + SITE_GROUP_MSG_TABLE + " WHERE msg_id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, msgId);
        statement.execute();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    /**
     * 删除群组聊天消息的一条记录
     *
     * @param chatSessionId
     */
    public synchronized void deleteGroupMsgByChatSessionId(String chatSessionId) {
        long startTime = System.currentTimeMillis();
        String sql = "DELETE FROM " + SITE_GROUP_MSG_TABLE + " WHERE chat_session_id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, chatSessionId);
        statement.execute();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    /**
     * 二人消息批量入库
     *
     * @param messages
     */
    public synchronized void batchInsertU2Messages(List<? extends com.windchat.im.bean.Message> messages) {
        if (messages != null && messages.size() > 0) {
            //插入消息
            List<ChatSession> chatSessions = insertU2Messages(messages);

            //插入或者更新会话
            batchInsertChatSessions(chatSessions, siteAddress);
        }

    }

    /**
     * 二人消息入库
     *
     * @param msg
     * @return
     */
    public synchronized Long insertU2Msg(Message msg) {
        long startTime = System.currentTimeMillis();
        long _id;
        try {
            String sql = "INSERT INTO " + SITE_U2_MSG_TABLE +
                    "(msg_id, " +
                    "from_site_user_id, " +
                    "to_site_user_id, " +
                    "chat_session_id, " +
                    "content, " +
                    "msg_pointer, " +
                    "msg_type, " +
                    "msg_secret, " +
                    "msg_base64_tsk, " +
                    "to_device_id," +
                    "msg_status, " +
                    "send_msg_time, " +
                    "server_msg_time, " +
                    "receive_msg_time, " +
                    " to_base64_device_pubk) " + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?, ?)";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, msg.getMsgId());
            statement.bindString(2, msg.getSiteUserId());
            statement.bindString(3, msg.getSiteFriendId());
            statement.bindString(4, msg.getChatSessionId());//消息帧，消息列表中某一个cells的标识
            statement.bindString(5, msg.getContent());
            statement.bindLong(6, msg.getMsgPointer());
            statement.bindLong(7, msg.getMsgType());
            statement.bindLong(8, msg.isSecret() ? 1 : 0);
            statement.bindString(9, msg.getMsgTsk());
            statement.bindString(10, msg.getToDeviceId());
            statement.bindLong(11, msg.getMsgStatus());
            statement.bindLong(12, msg.getSendMsgTime());//消息从客户端发送的时间
            statement.bindLong(13, msg.getMsgTime());//消息到服务端时间
            statement.bindLong(14, System.currentTimeMillis());//消息到达本地时间
            statement.bindString(15, msg.getToDevicePubk());//发送的devicePubk

            _id = statement.executeInsert();//插入的最后一行id

            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;
        } catch (Exception e) {
            Logger.e(e);
            return null;
        }

    }

    public synchronized List<ChatSession> insertU2Messages(List<? extends com.windchat.im.bean.Message> messages) {
        long startTime = System.currentTimeMillis();
        List<ChatSession> chatSessionList = new ArrayList<>();
        database.beginTransaction();
        String sql = "INSERT  INTO " + SITE_U2_MSG_TABLE +
                "(msg_id, " +
                "from_site_user_id, " +
                "to_site_user_id, " +
                "chat_session_id, " +
                "content, " +
                "msg_secret, " +
                "msg_pointer, " +
                "msg_type, " +
                "msg_base64_tsk, " +
                "to_device_id," +
                "msg_status, " +
                "send_msg_time, " +
                "server_msg_time, " +
                "receive_msg_time, " +
                "msg_width," +
                " msg_height, " +
                " href_url) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        for (com.windchat.im.bean.Message msg : messages) {
            try {
                if (StringUtils.isEmpty(msg.getMsgId())) {
                    continue;
                }
                statement.bindString(1, msg.getMsgId());
                statement.bindString(2, msg.getSiteUserId());
                statement.bindString(3, msg.getSiteFriendId());
                statement.bindString(4, msg.getChatSessionId());//消息帧，消息列表中某一个cells的标识
                statement.bindString(5, msg.getContent());
                statement.bindLong(6, msg.isSecret() ? 1 : 0);
                statement.bindLong(7, msg.getMsgPointer());
                statement.bindLong(8, msg.getMsgType());
                statement.bindString(9, msg.getMsgTsk());
                statement.bindString(10, msg.getToDeviceId());
                statement.bindLong(11, msg.getMsgStatus());
                statement.bindLong(12, msg.getSendMsgTime());//消息从客户端发送的时间
                statement.bindLong(13, msg.getMsgTime());//消息到服务端时间
                statement.bindLong(14, System.currentTimeMillis());//消息到达本地时间
                statement.bindLong(15, msg.getMsgWidth());
                statement.bindLong(16, msg.getMsgHeight());
                statement.bindString(17, msg.getHrefUrl());

                long result = statement.executeInsert();

                if (result > 0) {
                    ChatSession chatSession = buildChatSessionFromMessage(msg, SESSION_TYPE_U2);
                    chatSessionList.add(chatSession);
                }

            } catch (Exception e) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                continue;
            }
            statement.clearBindings();
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return chatSessionList;
    }


    public synchronized Long insertGroupMsg(Message msg) {
        long _id;
        // 插入群消息
        _id = insertGroupMessage(msg);
        //插入群消息的会话
        ChatSession chatSession = buildChatSessionFromMessage(msg, SESSION_TYPE_GROUP);
        insertChatSession(chatSession, siteAddress);
        return _id;
    }

    public synchronized void batchInsertGroupMessages(List<? extends com.windchat.im.bean.Message> messages) {
        //批量插入群消息
        List<ChatSession> chatSessionList = insertGroupMessages(messages);
        //批量插入会话
        batchInsertChatSessions(chatSessionList, siteAddress);
    }

    /**
     * 群组消息入库
     *
     * @param msg
     */
    public synchronized Long insertGroupMessage(Message msg) {
        long startTime = System.currentTimeMillis();
        long _id;

        try {
            String sql = "INSERT INTO " + SITE_GROUP_MSG_TABLE +
                    " (msg_id," +
                    " from_site_user_id," +
                    " site_group_id," +
                    " chat_session_id, " +
                    " content," +
                    " msg_pointer," +
                    " msg_type," +
                    " msg_base64_tsk," +
                    " to_device_id, " +
                    " msg_status," +
                    " send_msg_time," +
                    " server_msg_time, " +
                    " msg_width," +
                    " msg_height, " +
                    " href_url)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, msg.getMsgId());
            statement.bindString(2, msg.getSiteUserId());
            statement.bindString(3, msg.getGroupId());
            statement.bindString(4, msg.getChatSessionId());
            statement.bindString(5, msg.getContent());
            statement.bindLong(6, msg.getMsgPointer());
            statement.bindLong(7, msg.getMsgType());
            statement.bindString(8, msg.getMsgTsk());
            statement.bindString(9, msg.getToDeviceId());
            statement.bindLong(10, msg.getMsgStatus());
            statement.bindLong(11, msg.getSendMsgTime());
            statement.bindLong(12, msg.getMsgTime());
            statement.bindLong(13, msg.getMsgWidth());
            statement.bindLong(14, msg.getMsgHeight());
            statement.bindString(15, msg.getHrefUrl());

            _id = statement.executeInsert();
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return _id;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return null;
        }
    }

    /**
     * 群组消息批量入库
     *
     * @param messages
     */
    public synchronized List<ChatSession> insertGroupMessages(List<? extends com.windchat.im.bean.Message> messages) {
        long startTime = System.currentTimeMillis();
        List<ChatSession> chatSessionList = new ArrayList<>();
        String sql = "INSERT INTO " + SITE_GROUP_MSG_TABLE +
                " (msg_id," +
                " from_site_user_id, " +
                " content," +
                " server_msg_time," +
                " chat_session_id," +
                " msg_status," +
                " msg_type," +
                " site_group_id, " +
                " msg_width, " +
                " msg_height, " +
                " href_url)" + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        database.beginTransaction();
        SQLiteStatement statement = database.compileStatement(sql);
        for (com.windchat.im.bean.Message msg : messages) {
            if (StringUtils.isEmpty(msg.getMsgId())) {
                continue;
            }
            try {
                statement.bindString(1, msg.getMsgId());
                statement.bindString(2, msg.getSiteUserId() == null ? "" : msg.getSiteUserId());
                statement.bindString(3, msg.getContent());
                statement.bindString(4, msg.getMsgTime() + "");
                statement.bindString(5, msg.getChatSessionId());
                statement.bindLong(6, msg.getMsgStatus());
                statement.bindLong(7, msg.getMsgType());
                statement.bindString(8, msg.getGroupId());
                statement.bindLong(9, msg.getMsgWidth());
                statement.bindLong(10, msg.getMsgHeight());
                statement.bindString(11, msg.getHrefUrl());

                long result = statement.executeInsert();
                if (result > 0) {
                    ChatSession chatSession = buildChatSessionFromMessage(msg, SESSION_TYPE_GROUP);
                    chatSessionList.add(chatSession);
                }
            } catch (Exception ex) {
                ZalyLogUtils.getInstance().errorToInfo(TAG, " insert is failed, id is  " + msg.getMsgId() + " error msg is " + ex.getMessage());
                continue;
            }
            statement.clearBindings();
        }

        database.setTransactionSuccessful();
        database.endTransaction();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return chatSessionList;
    }


    public synchronized void insertChatSession(ChatSession chatSession, SiteAddress siteAddress) {
        SiteChatSessionDao.getInstance(siteAddress).replaceChatSession(chatSession);
    }

    public synchronized void batchInsertChatSessions(List<ChatSession> chatSessionList, SiteAddress siteAddress) {
        SiteChatSessionDao.getInstance(siteAddress).batchInsertChatSession(chatSessionList);
    }


    private ChatSession buildChatSessionFromMessage(com.windchat.im.bean.Message message, String type) {
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMsgId(message.getMsgId());
        chatSession.setChatSessionId(message.getChatSessionId());
        chatSession.setLatestTime(message.getMsgTime());
        chatSession.setLatestMsg(this.getSessionDesc(message));
        if (SESSION_TYPE_GROUP.equals(type)) {
            chatSession.setType(ChatSession.TYPE_GROUP_SESSION);
        } else {
            chatSession.setType(ChatSession.TYPE_FRIEND_SESSION);
        }
        //判断是发送还是接受消息
        if (message != null && StringUtils.isNotBlank(this.siteUserId)) {
            if (!this.siteUserId.equals(message.getSiteUserId())) {
                chatSession.setUnreadNum(1);
            }
        }
        return chatSession;
    }

    /**
     * 获取session描述
     *
     * @param message
     * @return
     */
    public String getSessionDesc(com.windchat.im.bean.Message message) {
        long startTime = System.currentTimeMillis();
        String sessionDesc;
        switch (message.getMsgType()) {
            case CoreProto.MsgType.VOICE_VALUE:
            case CoreProto.MsgType.GROUP_VOICE_VALUE:
                sessionDesc = "[语音消息]";
                break;
            case CoreProto.MsgType.IMAGE_VALUE:
            case CoreProto.MsgType.GROUP_IMAGE_VALUE:
                sessionDesc = "[图片消息]";
                break;
            case CoreProto.MsgType.SECRET_TEXT_VALUE:
                sessionDesc = "[绝密消息]";
                break;
            case CoreProto.MsgType.SECRET_IMAGE_VALUE:
                sessionDesc = "[绝密图片]";
                break;
            case CoreProto.MsgType.SECRET_VOICE_VALUE:
                sessionDesc = "[绝密语音]";
                break;
            case CoreProto.MsgType.GROUP_WEB_NOTICE_VALUE:
            case CoreProto.MsgType.U2_WEB_NOTICE_VALUE:
                sessionDesc = "[小程序]:" + getTitle(message.getContent());
                break;

            case CoreProto.MsgType.GROUP_WEB_VALUE:
            case CoreProto.MsgType.U2_WEB_VALUE:
                sessionDesc = "[小程序]:" + getTitle(message.getContent());
                break;

            default:
                sessionDesc = message.getContent();
        }
        return sessionDesc;
    }


    public String getTitle(String htmlSource) {
        List<String> list = new ArrayList<String>();
        String title = "";

        //Pattern pa = Pattern.compile("<title>.*?</title>", Pattern.CANON_EQ);也可以
        Pattern pa = Pattern.compile("<title>.*?</title>");//源码中标题正则表达式
        Matcher ma = pa.matcher(htmlSource);
        while (ma.find())//寻找符合el的字串
        {
            list.add(ma.group());//将符合el的字串加入到list中
        }
        for (int i = 0; i < list.size(); i++) {
            title = title + list.get(i);
        }
        return outTag(title);
    }

    /**
     * 去掉html源码中的标签
     *
     * @param s
     * @return
     */
    public String outTag(String s) {
        return s.replaceAll("<.*?>", "");
    }

    /**
     * 更新消息状态
     *
     * @param msgId
     */
    public synchronized int updateU2MsgStatusForSend(String msgId, Long serverMsgTime, int msgStatus) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE  " + SITE_U2_MSG_TABLE +
                " SET msg_status = ? ," +
                " server_msg_time = ? " +
                " WHERE msg_id = ? and msg_status = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindLong(1, msgStatus);
        statement.bindLong(2, serverMsgTime);
        statement.bindString(3, msgId);
        statement.bindLong(4, Message.STATUS_SENDING);

        int num = statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);

        if (num > 0) {
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return num;
        }
        String sqlUpdateByResend = "UPDATE  " + SITE_U2_MSG_TABLE +
                " SET msg_status = ?  " +
                " WHERE msg_id = ? ";
        SQLiteStatement statementByResend = database.compileStatement(sqlUpdateByResend);
        statementByResend.bindLong(1, msgStatus);
        statementByResend.bindString(2, msgId);
        int numUpdateByResend = statementByResend.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sqlUpdateByResend);

        return numUpdateByResend;
    }


    /**
     * 更新消息发送失败的原因
     *
     * @param msgId
     */
    public synchronized int updateU2MsgErrorCodeForSend(String msgId, String errorCode) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE  " + SITE_U2_MSG_TABLE +
                " SET send_msg_error_code = ? " +
                " WHERE msg_id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, errorCode);
        statement.bindString(2, msgId);
        int num = statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return num;
    }

    /**
     * 更新消息状态
     *
     * @param msgId
     */
    public synchronized int updateGroupMsgStatusForSend(String msgId, Long serverTime, int msgStatus) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE  " + SITE_GROUP_MSG_TABLE +
                " SET msg_status = ? , server_msg_time = ? " +
                " WHERE msg_id = ? and msg_status = ?";
        SQLiteStatement statement = database.compileStatement(sql);

        statement.bindLong(1, msgStatus);
        statement.bindLong(2, serverTime);
        statement.bindString(3, msgId);
        statement.bindLong(4, Message.STATUS_SENDING);

        int num = statement.executeUpdateDelete();
        if (num > 0) {
            ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
            return num;
        }
        String sqlUpdateByResend = "UPDATE  " + SITE_GROUP_MSG_TABLE +
                " SET msg_status = ?  " +
                " WHERE msg_id = ? ";
        SQLiteStatement statementByResend = database.compileStatement(sqlUpdateByResend);
        statementByResend.bindLong(1, msgStatus);
        statementByResend.bindString(2, msgId);
        int numUpdateByResend = statementByResend.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sqlUpdateByResend);

        return numUpdateByResend;
    }

    /**
     * 更新消息发送失败的原因
     *
     * @param msgId
     */
    public synchronized int updateGroupMsgErrorCodeForSend(String msgId, String errorCode) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE  " + SITE_GROUP_MSG_TABLE +
                " SET send_msg_error_code = ? " +
                " WHERE msg_id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, errorCode);
        statement.bindString(2, msgId);
        int num = statement.executeUpdateDelete();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return num;
    }

    /**
     * 更新消息内容，为图片以及语音更新info
     *
     * @param _id
     * @param content
     */
    public synchronized void updateU2MsgContent(long _id, String content, String tsKey) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE " + SITE_U2_MSG_TABLE +
                " SET content = ?," +
                " msg_base64_tsk = ?" +
                " WHERE _id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, content);
        statement.bindString(2, tsKey);
        statement.bindLong(3, _id);
        statement.execute();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    /**
     * 查询历史消息
     *
     * @param _id
     * @param count
     * @return
     */
    public synchronized List<Message> queryU2HistoryMsg(long _id, int count, String chatSessionId) {
        long startTime = System.currentTimeMillis();
        List<Message> messages = new ArrayList<>();

        String sql = "SELECT *, m._id AS mid" + " FROM " + SITE_U2_MSG_TABLE + " AS m " +
                " LEFT JOIN " + SITE_USE_PROFILE_TABLE + " AS f " +
                " ON m.from_site_user_id = f.site_user_id" +
                "  WHERE m._id < " + _id +
                " AND m.chat_session_id = '" + chatSessionId +
                "' ORDER BY m.server_msg_time  DESC, mid DESC " +
                " LIMIT " + count + ";";

        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Message message = new Message();
                String content = cursor.getString(cursor.getColumnIndex(Message.MessageEntry.COLUMN_NAME_CONTENT));
                message.set_id(cursor.getLong(cursor.getColumnIndex("mid")));
                message.setMsgId(cursor.getString(cursor.getColumnIndex("msg_id")));
                message.setSecret(cursor.getInt(cursor.getColumnIndex("msg_secret")) == 1);
                message.setMsgType(cursor.getInt(cursor.getColumnIndex("msg_type")));
                message.setMsgStatus(cursor.getInt(cursor.getColumnIndex("msg_status")));
                message.setMsgTsk(cursor.getString(cursor.getColumnIndex("msg_base64_tsk")));
                message.setToDeviceId(cursor.getString(cursor.getColumnIndex("to_device_id")));
                message.setContent(content);
                message.setMsgPointer(Long.parseLong(cursor.getString(cursor.getColumnIndex("msg_pointer"))));
                message.setImg(cursor.getString(cursor.getColumnIndex("site_user_icon")));
                message.setMsgTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("server_msg_time"))));
                message.setSiteUserId(cursor.getString(cursor.getColumnIndex("from_site_user_id")));
                message.setSiteFriendId(cursor.getString(cursor.getColumnIndex("to_site_user_id")));
                message.setChatSessionId(cursor.getString(cursor.getColumnIndex("chat_session_id")));
                message.setToDevicePubk(cursor.getString(cursor.getColumnIndex("to_base64_device_pubk")));
                message.setMsgWidth(cursor.getInt(cursor.getColumnIndex("msg_width")));
                message.setMsgHeight(cursor.getInt(cursor.getColumnIndex("msg_height")));
                message.setHrefUrl(cursor.getString(cursor.getColumnIndex("href_url")));

                messages.add(message);
            } while (cursor.moveToNext());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql + " queryU2HistoryMsg ");
        return messages;
    }

    /**
     * 查询历史消息
     *
     * @return
     */
    public synchronized Message queryU2LastedMsg(String chatSessionId) {
        long startTime = System.currentTimeMillis();

        String sql = "SELECT *, m._id AS mid" + " FROM " + SITE_U2_MSG_TABLE + " AS m " +
                " LEFT JOIN " + SITE_USE_PROFILE_TABLE + " AS f " +
                " ON m.from_site_user_id = f.site_user_id" +
                "  WHERE  m.chat_session_id = '" + chatSessionId +
                "' ORDER BY m.server_msg_time  DESC, mid DESC " +
                " LIMIT 1;";
        Message message = null;
        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            message = new Message();
            String content = cursor.getString(cursor.getColumnIndex(Message.MessageEntry.COLUMN_NAME_CONTENT));
            message.set_id(cursor.getLong(cursor.getColumnIndex("mid")));
            message.setMsgId(cursor.getString(cursor.getColumnIndex("msg_id")));
            message.setSecret(cursor.getInt(cursor.getColumnIndex("msg_secret")) == 1);
            message.setMsgType(cursor.getInt(cursor.getColumnIndex("msg_type")));
            message.setMsgStatus(cursor.getInt(cursor.getColumnIndex("msg_status")));
            message.setMsgTsk(cursor.getString(cursor.getColumnIndex("msg_base64_tsk")));
            message.setToDeviceId(cursor.getString(cursor.getColumnIndex("to_device_id")));
            message.setContent(content);
            message.setMsgPointer(Long.parseLong(cursor.getString(cursor.getColumnIndex("msg_pointer"))));
            message.setImg(cursor.getString(cursor.getColumnIndex("site_user_icon")));
            message.setMsgTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("server_msg_time"))));
            message.setSiteUserId(cursor.getString(cursor.getColumnIndex("from_site_user_id")));
            message.setSiteFriendId(cursor.getString(cursor.getColumnIndex("to_site_user_id")));
            message.setChatSessionId(cursor.getString(cursor.getColumnIndex("chat_session_id")));
            message.setToDevicePubk(cursor.getString(cursor.getColumnIndex("to_base64_device_pubk")));
            message.setHrefUrl(cursor.getString(cursor.getColumnIndex("href_url")));

        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql + " queryU2HistoryMsg ");
        return message;
    }


    /**
     * 查询图片消息
     * queryGroupImageMsg
     *
     * @param chatSessionId message.getMsgType() == CoreProto.MsgType.IMAGE_VALUE ||
     *                      message.getMsgType() == CoreProto.MsgType.SECRET_IMAGE_VALUE ||
     *                      message.getMsgType() == CoreProto.MsgType.GROUP_IMAGE_VALUE)
     * @return
     */
    public synchronized List<Message> queryU2ImageMsg(String chatSessionId) {
        long startTime = System.currentTimeMillis();
        List<Message> messages = new ArrayList<>();

        String sql = "SELECT *, m._id AS mid" + " FROM " + SITE_U2_MSG_TABLE + " AS m " +
                " LEFT JOIN " + SITE_USE_PROFILE_TABLE + " AS f " +
                " ON m.from_site_user_id = f.site_user_id" +
                "  WHERE m.chat_session_id = '" + chatSessionId +
                "' AND msg_type in (" + CoreProto.MsgType.IMAGE_VALUE + "," + CoreProto.MsgType.SECRET_IMAGE_VALUE + ")" +
                " ORDER BY m.server_msg_time  DESC, mid DESC ;";

        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Message message = new Message();
                String content = cursor.getString(cursor.getColumnIndex(Message.MessageEntry.COLUMN_NAME_CONTENT));
                message.set_id(cursor.getLong(cursor.getColumnIndex("mid")));
                message.setMsgId(cursor.getString(cursor.getColumnIndex("msg_id")));
                message.setSecret(cursor.getInt(cursor.getColumnIndex("msg_secret")) == 1);
                message.setMsgType(cursor.getInt(cursor.getColumnIndex("msg_type")));
                message.setMsgStatus(cursor.getInt(cursor.getColumnIndex("msg_status")));
                message.setMsgTsk(cursor.getString(cursor.getColumnIndex("msg_base64_tsk")));
                message.setToDeviceId(cursor.getString(cursor.getColumnIndex("to_device_id")));
                message.setContent(content);
                message.setMsgPointer(Long.parseLong(cursor.getString(cursor.getColumnIndex("msg_pointer"))));
                message.setImg(cursor.getString(cursor.getColumnIndex("site_user_icon")));
                message.setMsgTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("server_msg_time"))));
                message.setSiteUserId(cursor.getString(cursor.getColumnIndex("from_site_user_id")));
                message.setSiteFriendId(cursor.getString(cursor.getColumnIndex("to_site_user_id")));
                message.setChatSessionId(cursor.getString(cursor.getColumnIndex("chat_session_id")));
                message.setToDevicePubk(cursor.getString(cursor.getColumnIndex("to_base64_device_pubk")));
                message.setHrefUrl(cursor.getString(cursor.getColumnIndex("href_url")));

                messages.add(message);
            } while (cursor.moveToNext());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql + " queryU2HistoryMsg ");
        return messages;
    }


    /**
     * 查询图片消息
     *
     * @param chatSessionId message.getMsgType() == CoreProto.MsgType.IMAGE_VALUE ||
     *                      message.getMsgType() == CoreProto.MsgType.SECRET_IMAGE_VALUE ||
     *                      message.getMsgType() == CoreProto.MsgType.GROUP_IMAGE_VALUE)
     * @return
     */
    public synchronized List<Message> queryGroupImageMsg(String chatSessionId) {
        long startTime = System.currentTimeMillis();
        List<Message> messages = new ArrayList<>();

        String sql = "SELECT *, m._id AS mid " +
                " FROM " + SITE_GROUP_MSG_TABLE + " AS m " +
                " WHERE site_group_id = '" + chatSessionId +
                "' AND msg_type = " + CoreProto.MsgType.GROUP_IMAGE_VALUE +
                " ORDER BY  m.server_msg_time DESC , mid DESC;";

        Logger.i(TAG, sql);
        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Message message = new Message();
                String content = cursor.getString(cursor.getColumnIndex(Message.MessageEntry.COLUMN_NAME_CONTENT));
                message.set_id(cursor.getLong(cursor.getColumnIndex("mid")));
                message.setMsgId(cursor.getString(cursor.getColumnIndex("msg_id")));
                message.setMsgType(cursor.getInt(cursor.getColumnIndex("msg_type")));
                message.setMsgStatus(cursor.getInt(cursor.getColumnIndex("msg_status")));
                message.setSiteUserId(cursor.getString(cursor.getColumnIndex("from_site_user_id")));
                message.setGroupId(cursor.getString(cursor.getColumnIndex("site_group_id")));
                message.setMsgTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("server_msg_time"))));
                message.setContent(content);
                message.setMsgPointer(cursor.getInt(cursor.getColumnIndex("msg_pointer")));
                message.setChatSessionId(cursor.getString(cursor.getColumnIndex("chat_session_id")));
                message.setHrefUrl(cursor.getString(cursor.getColumnIndex("href_url")));

                messages.add(message);
            } while (cursor.moveToNext());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return messages;
    }


    /**
     * 查询最新消息
     *
     * @param _id
     * @param count
     * @return
     */
    public synchronized List<Message> queryU2NewMsg(long _id, int count, String chatSessionId) {
        long startTime = System.currentTimeMillis();
        List<Message> messages = new ArrayList<>();
        String sql;
        ZalyLogUtils.getInstance().info(TAG, " query u2 new msg _id" + _id);
        if (_id == 0) {
            return messages;
        }
        if (_id < 0) {
            sql = "SELECT *, m._id as mid " +
                    " FROM " + SITE_U2_MSG_TABLE + " as m " +
                    " LEFT JOIN  " + SITE_USE_PROFILE_TABLE + " As f" +
                    " ON m.from_site_user_id = f.site_user_id " +
                    " WHERE chat_session_id = '" + chatSessionId +
                    "' ORDER BY m.server_msg_time  DESC, mid DESC" +
                    " LIMIT " + count + ";";
        } else {
            sql = " SELECT *, m._id as mid " +
                    " FROM " + SITE_U2_MSG_TABLE + " AS m " +
                    " LEFT JOIN " + SITE_USE_PROFILE_TABLE + " AS f " +
                    " ON m.from_site_user_id = f.site_user_id " +
                    " WHERE m._id > " + _id +
                    " AND chat_session_id = '" + chatSessionId +
                    "' ORDER BY  m.server_msg_time  DESC, mid DESC;";
        }


        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Message message = new Message();
                String content = cursor.getString(cursor.getColumnIndex(Message.MessageEntry.COLUMN_NAME_CONTENT));
                message.set_id(cursor.getLong(cursor.getColumnIndex("mid")));
                message.setMsgId(cursor.getString(cursor.getColumnIndex("msg_id")));
                message.setSecret(cursor.getInt(cursor.getColumnIndex("msg_secret")) == 1);
                message.setMsgType(cursor.getInt(cursor.getColumnIndex("msg_type")));
                message.setMsgStatus(cursor.getInt(cursor.getColumnIndex("msg_status")));
                message.setMsgTsk(cursor.getString(cursor.getColumnIndex("msg_base64_tsk")));
                message.setToDeviceId(cursor.getString(cursor.getColumnIndex("to_device_id")));
                message.setContent(content);
                message.setMsgPointer(Long.parseLong(cursor.getString(cursor.getColumnIndex("msg_pointer"))));
                message.setImg(cursor.getString(cursor.getColumnIndex("site_user_icon")));
                message.setMsgTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("server_msg_time"))));
                message.setSiteUserId(cursor.getString(cursor.getColumnIndex("from_site_user_id")));
                message.setSiteFriendId(cursor.getString(cursor.getColumnIndex("to_site_user_id")));
                message.setChatSessionId(cursor.getString(cursor.getColumnIndex("chat_session_id")));
                message.setToDevicePubk(cursor.getString(cursor.getColumnIndex("to_base64_device_pubk")));
                message.setMsgWidth(cursor.getInt(cursor.getColumnIndex("msg_width")));
                message.setMsgHeight(cursor.getInt(cursor.getColumnIndex("msg_height")));
                message.setHrefUrl(cursor.getString(cursor.getColumnIndex("href_url")));

                messages.add(message);
            } while (cursor.moveToNext());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql + "queryU2NewMsg ");
        return messages;
    }

    /**
     * 查询群组历史消息
     *
     * @param _id
     * @param count
     * @return
     */
    public synchronized List<Message> queryGroupHistoryMsg(long _id, String groupId, int count) {
        long startTime = System.currentTimeMillis();
        List<Message> messages = new ArrayList<>();

        String sql = "SELECT *, m._id AS mid " +
                " FROM " + SITE_GROUP_MSG_TABLE + " AS m " +
                " WHERE m._id < " + _id +
                " AND site_group_id = '" + groupId +
                "' ORDER BY  m.server_msg_time DESC , mid DESC" +
                " LIMIT " + count + ";";

        Logger.i(TAG, sql);
        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                Message message = new Message();
                String content = cursor.getString(cursor.getColumnIndex(Message.MessageEntry.COLUMN_NAME_CONTENT));
                message.set_id(cursor.getLong(cursor.getColumnIndex("mid")));
                message.setMsgId(cursor.getString(cursor.getColumnIndex("msg_id")));
                message.setMsgType(cursor.getInt(cursor.getColumnIndex("msg_type")));
                message.setMsgStatus(cursor.getInt(cursor.getColumnIndex("msg_status")));
                message.setSiteUserId(cursor.getString(cursor.getColumnIndex("from_site_user_id")));
                message.setGroupId(cursor.getString(cursor.getColumnIndex("site_group_id")));
                message.setMsgTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("server_msg_time"))));
                message.setContent(content);
                message.setChatSessionId(cursor.getString(cursor.getColumnIndex("chat_session_id")));
                message.setMsgPointer(cursor.getInt(cursor.getColumnIndex("msg_pointer")));
                message.setMsgWidth(cursor.getInt(cursor.getColumnIndex("msg_width")));
                message.setMsgHeight(cursor.getInt(cursor.getColumnIndex("msg_height")));
                message.setHrefUrl(cursor.getString(cursor.getColumnIndex("href_url")));

                messages.add(message);
            } while (cursor.moveToNext());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return messages;
    }

    /**
     * 群 更新消息内容，为图片以及语音更新info
     *
     * @param _id
     * @param content
     */
    public synchronized void updateGroupMsgContent(long _id, String content) {
        long startTime = System.currentTimeMillis();
        String sql = "UPDATE " + SITE_GROUP_MSG_TABLE +
                " SET content = ? " +
                " WHERE _id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindString(1, content);
        statement.bindLong(2, _id);
        statement.execute();
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }


    /**
     * 加密的消息图片本机解析不了，修改数据类型为notice
     *
     * @param _id
     * @param content
     * @param msgType
     */
    public synchronized void updateSecretU2MsgContent(long _id, String content, long msgType) {
        long startTime = System.currentTimeMillis();
        String sql = " UPDATE " + SITE_U2_MSG_TABLE + " SET content=?,msg_type=?,msg_base64_tsk=? WHERE _id=?;";
        try {
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, content);
            statement.bindLong(2, msgType);
            statement.bindString(3, "");
            statement.bindLong(4, _id);
            statement.execute();
        } catch (Exception e) {
            ZalyLogUtils.getInstance().info(TAG, "update msg is failed, _id is " + _id + " error msg is " + e.getMessage());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }

    public synchronized void updateSecretU2MsgContent(long _id, String content, String base64Tsk) {
        long startTime = System.currentTimeMillis();
        String sql = " UPDATE " + SITE_U2_MSG_TABLE + " SET content=?,msg_base64_tsk=? WHERE _id=?;";
        try {
            SQLiteStatement statement = database.compileStatement(sql);
            statement.bindString(1, content);
            statement.bindString(2, base64Tsk);
            statement.bindLong(3, _id);
            statement.execute();
        } catch (Exception e) {
            ZalyLogUtils.getInstance().info(TAG, "update msg is failed, _id is " + _id + " error msg is " + e.getMessage());
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
    }


    /**
     * 查询群组最新消息
     *
     * @param _id
     * @param count
     * @return
     */
    public synchronized List<Message> queryGroupNewMsg(long _id, String groupId, int count) {
        long startTime = System.currentTimeMillis();
        List<Message> messages = new ArrayList<>();
        String sql;
        if (_id < 0) {
            sql = " SELECT *, m._id AS mid " +
                    " FROM " + SITE_GROUP_MSG_TABLE + " AS m " +
                    " WHERE site_group_id = '" + groupId +
                    "' ORDER BY m.server_msg_time DESC, mid DESC" +
                    " LIMIT " + count + ";";
        } else {
            sql = "SELECT *, m._id AS mid " +
                    " FROM " + SITE_GROUP_MSG_TABLE + " AS m " +
                    " WHERE m._id > " + _id + " " +
                    " AND site_group_id = '" + groupId +
                    "' ORDER BY m.server_msg_time DESC, mid DESC;";
        }

        Cursor cursor = database.rawQuery(sql, new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            try {
                do {
                    Message message = new Message();

                    String content = cursor.getString(cursor.getColumnIndex(Message.MessageEntry.COLUMN_NAME_CONTENT));

                    message.set_id(cursor.getLong(cursor.getColumnIndex("mid")));
                    message.setMsgId(cursor.getString(cursor.getColumnIndex("msg_id")));
                    message.setMsgType(cursor.getInt(cursor.getColumnIndex("msg_type")));
                    message.setMsgStatus(cursor.getInt(cursor.getColumnIndex("msg_status")));
                    message.setSiteUserId(cursor.getString(cursor.getColumnIndex("from_site_user_id")));
                    message.setGroupId(cursor.getString(cursor.getColumnIndex("site_group_id")));
                    message.setMsgTime(Long.parseLong(cursor.getString(cursor.getColumnIndex("server_msg_time"))));
                    message.setContent(content);
                    message.setMsgPointer(cursor.getInt(cursor.getColumnIndex("msg_pointer")));
                    message.setChatSessionId(cursor.getString(cursor.getColumnIndex("chat_session_id")));
                    message.setMsgWidth(cursor.getInt(cursor.getColumnIndex("msg_width")));
                    message.setMsgHeight(cursor.getInt(cursor.getColumnIndex("msg_height")));
                    message.setHrefUrl(cursor.getString(cursor.getColumnIndex("href_url")));
                    messages.add(message);
                } while (cursor.moveToNext());
            } catch (Exception e) {
                ZalyLogUtils.getInstance().info(TAG, "query new msg is failed,  error msg is " + e.getMessage());
            }
        }
        ZalyLogUtils.getInstance().dbLog(TAG, startTime, sql);
        return messages;
    }

}
