package com.windchat.client.db.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.windchat.client.Configs;
import com.windchat.client.db.sql.DBSQL;
import com.orhanobut.logger.Logger;

/**
 *
 */
public class AkxCommonDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 8;
    public static final String TAG = "AkxCommonDBHelper";


    public AkxCommonDBHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //1.创建身份table
        db.execSQL(DBSQL.SQL_CREATE_AKX_USER_IDENTITY_TABLE);
        //2.创建站点table
        db.execSQL(DBSQL.SQL_CREATE_AKX_SITE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //升级数据库
        if (oldVersion == 5) {
            db.execSQL(DBSQL.SQL_AKX_SITE_ADD_IS_INVITE_CODE_6);
            db.execSQL(DBSQL.SQL_AKX_SITE_CHANGE_REGISTERWAY_TO_REALNAME_6);
            db.execSQL(DBSQL.SQL_AKX_SITE_ADD_SITE_LOGIN_ID_8);
        } else if(oldVersion == 6 || oldVersion == 7) {
            ////因为跨过了7
            db.execSQL(DBSQL.SQL_AKX_SITE_ADD_SITE_LOGIN_ID_8);
        }
    }

    public synchronized void checkBaseTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DBSQL.SQL_CREATE_AKX_USER_IDENTITY_TABLE);
        db.execSQL(DBSQL.SQL_CREATE_AKX_SITE_TABLE);
        db.close();
    }


    public synchronized void checkUserIdentityTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DBSQL.SQL_CREATE_AKX_USER_IDENTITY_TABLE);
        db.close();
    }

    public synchronized void checkSiteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DBSQL.SQL_CREATE_AKX_SITE_TABLE);
        db.close();
    }


    public synchronized void dropBaseTable() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(DBSQL.SQL_DROP_AKX_USER_IDENTITY_TABLE);
            db.execSQL(DBSQL.SQL_DROP_AKX_SITE_TABLE);
            db.close();
        } catch (Exception e) {
            Logger.w(TAG, e);
        }
    }

}