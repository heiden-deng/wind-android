package com.windchat.client.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.windchat.client.db.sql.DBSQL;

/**
 *
 */
public class AkxSiteDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 8;
    private static final String TAG = "AkxSiteDBHelper";

    public AkxSiteDBHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据库
        db.execSQL(DBSQL.SQL_CREATE_SITE_CHAT_SESSION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 5) {
            upgradeTo6(db);
            upgradeTo7(db);
            upgradeTo8(db);
        }
        if(oldVersion == 6) {
            upgradeTo7(db);
            upgradeTo8(db);
        }
        if(oldVersion == 7) {
            upgradeTo8(db);
        }
    }

    /**
     * 升级到6
     * @param db
     */
    protected void upgradeTo6(SQLiteDatabase db){
        db.execSQL(DBSQL.SQL_U2_MSG_ADD_DEVICE_PUBK_6);
    }

    /**
     * 升级到7
     * @param db
     */
    protected void upgradeTo7(SQLiteDatabase db){
        db.execSQL(DBSQL.SQL_SITE_GROUP_PROFILE_ADD_COUNT_MEMBER_7);
        db.execSQL(DBSQL.SQL_SITE_GROUP_PROFILE_ADD_IS_CLOSE_INVITE_7);
    }

    /**
     * 升级到8
     * @param db
     */
    protected void upgradeTo8(SQLiteDatabase db){
        db.execSQL(DBSQL.SQL_U2_MSG_ADD_MSG_WIDTH_8);
        db.execSQL(DBSQL.SQL_U2_MSG_ADD_MSG_HEIGHT_8);
        db.execSQL(DBSQL.SQL_U2_MSG_ADD_HREF_URL_8);
        db.execSQL(DBSQL.SQL_GROUP_MSG_ADD_MSG_WIDTH_8);
        db.execSQL(DBSQL.SQL_GROUP_MSG_ADD_MSG_HEIGHT_8);
        db.execSQL(DBSQL.SQL_GROUP_MSG_ADD_HREF_URL_8);
        db.execSQL(DBSQL.SQL_SITE_USER_PROFILE_ADD_NICK_NAME_8);
        db.execSQL(DBSQL.SQL_SITE_USER_PROFILE_ADD_SITE_LOGIN_ID_8);
    }


    /**
     * 检测站点的数据库表
     */
    public synchronized void checkSiteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        //检测站点消息数据库表
        db.execSQL(DBSQL.SQL_CREATE_SITE_CHAT_SESSION_TABLE);
        db.execSQL(DBSQL.SQL_CREATE_SITE_U2_MSG_TABLE);
        db.execSQL(DBSQL.SQL_CREATE_SITE_U2_MSG_INDEX);
        db.execSQL(DBSQL.SQL_CREATE_SITE_GROUP_MSG_TABLE);
        db.execSQL(DBSQL.SQL_CREATE_SITE_GROUP_MSG_INDEX);
        db.execSQL(DBSQL.SQL_CREATE_SITE_USER_PROFILE_TABLE);
        db.execSQL(DBSQL.SQL_CREATE_SITE_USER_PROFILE_INDEX);
        db.execSQL(DBSQL.SQL_CREATE_SITE_GROUP_PROFILE_TABLE);
        db.execSQL(DBSQL.SQL_CREATE_SITE_GROUP_PROFILE_INDEX);

        db.close();
    }

    public synchronized void dropSiteTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(DBSQL.SQL_DROP_SITE_GROUP_MSG_TABLE);
        db.execSQL(DBSQL.SQL_DROP_SITE_GROUP_PROFILE_TABLE);
        db.execSQL(DBSQL.SQL_DROP_SITE_U2_MSG_TABLE);
        db.execSQL(DBSQL.SQL_DROP_SITE_USER_PROFILE_TABLE);
        db.execSQL(DBSQL.SQL_DROP_SITE_CHAT_SESSION_TABLE);
    }

}