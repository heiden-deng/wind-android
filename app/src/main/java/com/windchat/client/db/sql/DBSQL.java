package com.windchat.client.db.sql;

/**
 * Created by anguoyue on 28/02/2018.
 */

public interface DBSQL {

    //公共数据库中表，用户身份表
    String AKX_USER_IDENTITY_TABLE = "akx_user_identity_table";

    //公共数据库中表，访问的站点表
    String AKX_SITE_TABLE = "akx_site_table";

    //站点：聊天会话表
    String SITE_CHAT_SESSION_TABLE = "site_chat_session_table";

    //站点：二人消息表
    String SITE_U2_MSG_TABLE = "site_u2_message_table";

    //站点：群组消息表
    String SITE_GROUP_MSG_TABLE = "site_group_message_table";

    //站点：用户资料表
    String SITE_USER_PROFILE_TABLE = "site_user_profile_table";

    //站点：群组资料表
    String SITE_GROUP_PROFILE_TABLE = "site_group_profile_table";

    String SQL_CREATE_AKX_USER_IDENTITY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + AKX_USER_IDENTITY_TABLE + "(" +
                    "_id INTEGER PRIMARY KEY," +
                    "global_user_id VARCHAR(100) UNIQUE NOT NULL ," + //身份id
                    "user_id_prik TEXT UNIQUE NOT NULL," +           //用户身份私钥
                    "user_id_pubk TEXT UNIQUE NOT NULL," +           //用户身份公钥
                    "device_id_prik TEXT UNIQUE NOT NULL," +         //设备私钥
                    "device_id_pubk TEXT UNIQUE NOT NULL," +         //设备公钥
                    "platform_session_id VARCHAR(50)," +            //平台sessionid
                    "name VARCHAR(50), " +                          //身份名称
                    "source VARCHAR(50)" +                          //身份来源, 手机登录login_with_phone, 输入公私密钥 login_with_input, 授权登录login_with_auth， 生成login_with_generate
                    ");";

    String SQL_CREATE_AKX_SITE_TABLE =

            "CREATE TABLE IF NOT EXISTS " + AKX_SITE_TABLE + "(" +
                    " _id INTEGER PRIMARY KEY," +
                    "site_host VARCHAR(50)," +              //站点host
                    "site_port INTEGER," +                  //站点端口
                    "site_name VARCHAR(50)," +              //站点名称
                    "site_logo VARCHAR(50)," +              //站点logo
                    "site_version VARCHAR(10)," +           //站点版本
                    " real_name_config INTEGER," +         //站点注册方式 0匿名方式;1实名方式,必须用户是实, 2优先实名方式，其次接受非实名用户
                    " is_invite_code INTEGER ," +           ///是否开启邀请码 0 不需要， 1需要
                    "site_status INTEGER," +                //站点状态 关闭还是开启
                    "global_user_id  VARCHAR(100) , " +     //用户globalUserId
                    "site_user_id VARCHAR(50)," +           //用户在该站点ID
                    "site_user_name VARCHAR(50)," +         //用户昵称
                    "site_user_icon VARCHAR(50)," +         //用户头像
                    "site_login_id VARCHAR(50), " +        ////用户唯一登录名
                    "site_session_id VARCHAR(50)," +        //用户session
                    "user_token VARCHAR(50)," +             //用户令牌
                    "disconnect_status INTEGER," +         //断开的状态，默认0:自动从连接 1:手动断开，需要手动连接
                    "site_scheme VARCHAR(10)," +            //站点模式，zaly或者zalys，连接方式
                    "sort_num INTEGER , " +                 //站点排序
                    "latest_time LONG," +                   //上次断开时间
                    "mute BOOLEAN, " +                      //站点消息免打扰 0没开，1开启
                    "UNIQUE(site_host, site_port, global_user_id) ON CONFLICT IGNORE);";


    String SQL_CREATE_SITE_CHAT_SESSION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SITE_CHAT_SESSION_TABLE + "(" +
                    "_id INTEGER PRIMARY KEY," +
                    "chat_session_id VARCHAR(50) UNIQUE NOT NULL, " +   //对方siteUserId或者groupid
                    "latest_msg VARCHAR(100)," +                        //最新的一条消息
                    "type INTEGER," +                                   //群2或者个人1;
                    "unread_num INTEGER," +                             //聊天会话的未读消息数量
                    "session_goto VARCHAR(50)," +                       //会话支持跳转
                    "open_ts_chat BOOLEAN," +                           //是否开启绝密聊天
                    "status INTEGER," +                                 //状态.....
                    "edit_text TEXT, " +                                //发送消息内容，没有发送出去，所有的内容的记录
                    "latest_time LONG);";                               //最后一条消息时间


    String SQL_CREATE_SITE_U2_MSG_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SITE_U2_MSG_TABLE + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "msg_id VARCHAR(100) UNIQUE NOT NULL," +
                    "from_site_user_id VARCHAR(100) NOT NULL," +
                    "to_site_user_id VARCHAR(100) NOT NULL," +
                    "chat_session_id VARCHAR(100) NOT NULL," +
                    "content TEXT," +
                    "msg_pointer INTEGER," +
                    "msg_type INTEGER," +
                    "msg_secret INTEGER," +
                    "msg_base64_tsk TEXT," +
                    "to_device_id VARCHAR(100)," +
                    "msg_status INTEGER, " +
                    "msg_ext TEXT, " +
                    "send_msg_time   LONG, " +
                    "server_msg_time  LONG," +
                    "receive_msg_time LONG," +
                    "read_msg_time    LONG, " +
                    "send_msg_error_code VARCHAR(50), " +
                    "to_base64_device_pubk TEXT" +
                    ");";

    String SQL_CREATE_SITE_U2_MSG_INDEX = "CREATE INDEX IF NOT EXISTS index_u2_chat_session_id ON " + SITE_U2_MSG_TABLE + "(from_site_user_id, to_site_user_id);";


    String SQL_CREATE_SITE_GROUP_MSG_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SITE_GROUP_MSG_TABLE + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "msg_id VARCHAR(100) UNIQUE NOT NULL," +
                    "from_site_user_id VARCHAR(100) NOT NULL," +
                    "site_group_id VARCHAR(100) NOT NULL," +
                    "chat_session_id VARCHAR(100) NOT NULL," +
                    "content TEXT," +
                    "msg_pointer INTEGER," +
                    "msg_type INTEGER," +
                    "msg_base64_tsk TEXT," +
                    "to_device_id VARCHAR(100)," +
                    "msg_status INTEGER," +
                    "msg_ext TEXT, " +
                    "send_msg_time   LONG, " +
                    "server_msg_time  LONG," +
                    "receive_msg_time LONG," +
                    "read_msg_time    LONG, " +
                    "send_msg_error_code VARCHAR(50)" +
                    ");";

    String SQL_CREATE_SITE_GROUP_MSG_INDEX = "CREATE INDEX IF NOT EXISTS index_group_chat_session_id ON " + SITE_GROUP_MSG_TABLE + "(from_site_user_id, site_group_id);";

    String SQL_CREATE_SITE_USER_PROFILE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SITE_USER_PROFILE_TABLE + "(" +
                    "_id INTEGER PRIMARY KEY," +                    //主键【自增，唯一】
                    "site_user_id VARCHAR(50) UNIQUE NOT NULL," +   //用户ID
                    "site_user_name VARCHAR(50)," +                 //备注
                    "site_user_icon VARCHAR(50)," +                 //头像
                    "site_nick_name VARCHAR(50), " +               // 用户自己的昵称
                    "site_login_id VARCHAR(50), " +                ///用户设置的用户名
                    "user_id_pubk TEXT," +                          //用户的个人公钥
                    "mute BOOLEAN," +                               //消息免打扰，静音，不接受对用push，消息提示无声音
                    "relation INTEGER," +                           //0:非好友  1:互为好友【互相为好友】
                    "latest_time LONG);";                           //最新同步时间

    String SQL_CREATE_SITE_USER_PROFILE_INDEX = "CREATE INDEX IF NOT EXISTS index_site_user_id ON " + SITE_USER_PROFILE_TABLE + "(site_user_id);";


    String SQL_CREATE_SITE_GROUP_PROFILE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + SITE_GROUP_PROFILE_TABLE + "(" +
                    "_id INTEGER PRIMARY KEY," +
                    "site_group_id VARCHAR(50) UNIQUE NOT NULL," +  //站点群组ID
                    "site_group_name VARCHAR(50)," +                //群昵称
                    "site_group_icon VARCHAR(50)," +                //群头像
                    "group_owner_id VARCHAR(50)," +                 //群管理员ID
                    "group_owner_name VARCHAR(50)," +               //群管理员昵称
                    "group_owner_icon VARCHAR(50)," +               //群管理员头像
                    "is_group_member BOOLEAN," +                    //是否为群成员
                    "mute BOOLEAN," +                               //消息免打扰
                    "count_member INTEGER, " +                      // 群成员个数
                    "is_close_invite BOOLEAN ," +                   // 关闭邀请新用户群聊 true：打开  false：关闭
                    "latest_time LONG);";                           //上次更新时间


    String SQL_CREATE_SITE_GROUP_PROFILE_INDEX = "CREATE INDEX IF NOT EXISTS index_site_group_id ON " + SITE_GROUP_PROFILE_TABLE + "(site_group_id);";

    String SQL_DROP_AKX_SITE_TABLE = " DROP TABLE IF EXISTS " + AKX_USER_IDENTITY_TABLE;
    String SQL_DROP_SITE_U2_MSG_TABLE = " DROP TABLE IF EXISTS " + SITE_U2_MSG_TABLE;
    String SQL_DROP_SITE_CHAT_SESSION_TABLE = " DROP TABLE IF EXISTS " + SITE_CHAT_SESSION_TABLE;
    String SQL_DROP_SITE_GROUP_MSG_TABLE = " DROP TABLE IF EXISTS " + SITE_GROUP_MSG_TABLE;
    String SQL_DROP_SITE_USER_PROFILE_TABLE = " DROP TABLE IF EXISTS " + SITE_USER_PROFILE_TABLE;
    String SQL_DROP_SITE_GROUP_PROFILE_TABLE = " DROP TABLE IF EXISTS " + SITE_GROUP_PROFILE_TABLE;
    String SQL_DROP_AKX_USER_IDENTITY_TABLE = " DROP TABLE IF EXISTS " + AKX_SITE_TABLE;


    String SQL_U2_MSG_ADD_DEVICE_PUBK_6 = " ALTER  TABLE " + SITE_U2_MSG_TABLE + " ADD  to_base64_device_pubk TEXT ;";
    String SQL_AKX_SITE_ADD_IS_INVITE_CODE_6 = " ALTER  TABLE " + AKX_SITE_TABLE + " ADD  is_invite_code INTEGER ;";
    String SQL_AKX_SITE_CHANGE_REGISTERWAY_TO_REALNAME_6 = " ALTER  TABLE " + AKX_SITE_TABLE + " ADD real_name_config INTEGER; ";


    String SQL_SITE_GROUP_PROFILE_ADD_COUNT_MEMBER_7 = " ALTER  TABLE " + SITE_GROUP_PROFILE_TABLE + " ADD count_member INTEGER;";
    String SQL_SITE_GROUP_PROFILE_ADD_IS_CLOSE_INVITE_7 = " ALTER  TABLE " + SITE_GROUP_PROFILE_TABLE + " ADD is_close_invite BOOLEAN;";

    String SQL_U2_MSG_ADD_MSG_WIDTH_8 = " ALTER  TABLE " + SITE_U2_MSG_TABLE + " ADD msg_width INTEGER;";
    String SQL_U2_MSG_ADD_MSG_HEIGHT_8 = " ALTER  TABLE " + SITE_U2_MSG_TABLE + " ADD msg_height INTEGER ;";
    String SQL_U2_MSG_ADD_HREF_URL_8 = " ALTER  TABLE " + SITE_U2_MSG_TABLE + " ADD href_url VARCHAR(100); ";
    String SQL_GROUP_MSG_ADD_MSG_WIDTH_8 = " ALTER  TABLE " + SITE_GROUP_MSG_TABLE + " ADD msg_width INTEGER;";
    String SQL_GROUP_MSG_ADD_MSG_HEIGHT_8 = " ALTER  TABLE " + SITE_GROUP_MSG_TABLE + " ADD msg_height INTEGER ;";
    String SQL_GROUP_MSG_ADD_HREF_URL_8 = " ALTER  TABLE " + SITE_GROUP_MSG_TABLE + " ADD href_url VARCHAR(100); ";

    String SQL_AKX_SITE_ADD_SITE_LOGIN_ID_8 = " ALTER TABLE " + AKX_SITE_TABLE + " ADD site_login_id VARCHAR(50);";
    String SQL_SITE_USER_PROFILE_ADD_NICK_NAME_8 = " ALTER TABLE " + SITE_USER_PROFILE_TABLE + " add site_nick_name VARCHAR(50);";
    String SQL_SITE_USER_PROFILE_ADD_SITE_LOGIN_ID_8 = " ALTER TABLE " + SITE_USER_PROFILE_TABLE + " ADD site_login_id VARCHAR(50);";

}
