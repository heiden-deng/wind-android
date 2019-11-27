package com.windchat.client.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.windchat.client.Configs;

/**
 * Created by yichao on 2017/10/14.
 */

public class ZalyDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;


    public ZalyDbHelper(Context context) {
        super(context, Configs.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public synchronized void checkBaseTable() {
        SQLiteDatabase db = this.getWritableDatabase();

        String sql_create_task_table = "create table if not exists " + ZalyDbHelper.getZalyTaskTable() + "(" +
                "_id integer primary key autoincrement," +
                "site_identity text," +
                "task_identity text," +
                "task_type integer," +
                "task_content text," +
                "task_time text" +
                ")";
        db.execSQL(sql_create_task_table);

        db.close();
    }

    public synchronized void delBaseTable(Context context) {
        context.deleteDatabase(Configs.DATABASE_NAME);
    }



    /**
     * 获取任务表
     *
     * @return
     */
    public static String getZalyTaskTable() {
        return "task_table";
    }

}