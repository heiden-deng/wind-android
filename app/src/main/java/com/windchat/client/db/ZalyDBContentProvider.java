package com.windchat.client.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.windchat.client.BuildConfig;
import com.windchat.client.Configs;
import com.windchat.client.ZalyApplication;
import com.windchat.client.bean.Message;
import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.bean.event.MessageEvent;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

/**
 * Created by yichao on 2017/10/18.
 * <p>
 * 数据库ContentProvider用于与IMService跨进程通讯
 */

public class ZalyDBContentProvider extends ContentProvider {

    public static final String TAG = ZalyDBContentProvider.class.getSimpleName();
    public static final String CONTENT_AUTHORITY = "content://" + BuildConfig.APPLICATION_ID + ".ZalyDBContentProvider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * bundle需要添加该classloader 因为此处会跨进程传递Parcelable
     *
     * @param method
     * @param arg
     * @param bundle
     * @return
     */
    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle bundle) {
        ZalyApplication.logProcessInfo(TAG);
        if (bundle != null) {
            bundle.setClassLoader(this.getClass().getClassLoader());
        }
        String currentSiteIndenty = ZalyApplication.getCfgSP().getString(Configs.KEY_CUR_SITE, "");
        if(currentSiteIndenty == null) {
            return bundle;
        }
        //获取站点信息
        if (bundle != null) {
            String siteIdentity = bundle.getString(ZalyDbContentHelper.KEY_SITE_IDENTITY);
            String siteUserId = bundle.getString(ZalyDbContentHelper.KEY_CUR_SITE_USER_ID);
            boolean isCurSite = currentSiteIndenty.equals(siteIdentity);
            //post 事件
            switch (method) {
                case ZalyDbContentHelper.Action.MSG_STATUS:
                    String msgId = bundle.getString(ZalyDbContentHelper.KEY_MSG_ID);
                    if (StringUtils.isEmpty(msgId)) {
                        return bundle;
                    }
                    if (isCurSite) {
                        EventBus.getDefault().post(new MessageEvent(Message.Action.UPDATE_MSG_STATUS, bundle));
                    }
                    break;
                case ZalyDbContentHelper.Action.MSG_RECEIVE:
//                    Vibrator v = (Vibrator) ZalyApplication.getContext().getSystemService(Context.VIBRATOR_SERVICE);
//                    v.vibrate(400);
                    try {
                        ArrayList<Message> messages = bundle.getParcelableArrayList(ZalyDbContentHelper.KEY_MSG_RECEIVE_LIST);
                        if (messages == null || messages.size() == 0) {
                            return bundle;
                        }
                        if (isCurSite) {
                            EventBus.getDefault().post(new MessageEvent(Message.Action.MSG_RECEIVE, bundle));
                            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_UPDATE_SESSION_LIST, bundle));
                        } else {
                            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_UPDATE_MAIN_SESSION_TAB_BUBBLE, null));
                        }
                    } catch (Exception e) {
                        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                    }
                    break;
                case ZalyDbContentHelper.Action.MSG_IMG_PROCESS:
                    try{
                        EventBus.getDefault().post(new MessageEvent(Message.Action.IMG_PROGRESS,bundle));
                    }catch (Exception e) {
                        ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                    }
                    break;
            }
        }
        return super.call(method, arg, bundle);
    }
}
