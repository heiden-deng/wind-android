package com.windchat.client.util.log;

import com.windchat.client.BuildConfig;
import com.windchat.client.api.ZalyAPIException;
import com.orhanobut.logger.Logger;

/**
 * Created by zhangjun on 2018/3/16.
 */

public class ZalyLogUtils {

    public static ZalyLogUtils getInstance() {
        return ZalyLogUtils.SingletonHolder.instance;
    }

    static class SingletonHolder {
        private static ZalyLogUtils instance = new ZalyLogUtils();
    }

    public String makeLogMsg(String tag, String param, Object context) {
        return String.format(
                "thread:%-4d Context:%s:%d %s",
                Thread.currentThread().getId(),
                context.getClass().getName(),
                context.hashCode(),
                param
        );
    }

    public String makeLogMsg(String tag, Throwable e, Object context) {
            return String.format(
                    "thread:%-4d Context:%s:%d",
                    Thread.currentThread().getId(),
                    context.getClass().getName(),
                    context.hashCode()
            );
    }

    public void info(String tag, String param) {
        try {
            if (BuildConfig.DEBUG) {
                Logger.i(tag, String.format("thread:%-4d %s ", Thread.currentThread().getId(), param));
            }
        }catch (Exception ex) {

        }
    }

    public void debug(String tag, String param) {
        try {
            if (BuildConfig.DEBUG) {
                Logger.d(tag + " " + String.format("thread:%-4d %s ", Thread.currentThread().getId(), param));
            }
        }catch (Exception ex) {

        }
    }

    public void debug(String tag, String param, Object context) {
        try {
            if (BuildConfig.DEBUG) {
                Logger.d(tag + " " + this.makeLogMsg(tag, param, context));
            }
        }catch (Exception ex) {

        }
    }

    public void debug(String tag, Throwable e, Object context) {
        try {
            if (BuildConfig.DEBUG) {
                Logger.d(tag, String.format(
                        "thread:%-4d Context:%s:%d",
                        Thread.currentThread().getId(),
                        context.getClass().getName(),
                        context.hashCode()
                ));
                Logger.d(e);
            }
        }catch (Exception ex) {

        }
    }

    public void info(String tag, String param, Object context) {
        try {
            Logger.i(tag, this.makeLogMsg(tag, param, context));
        }catch (Exception e) {

        }
    }

    public void warn(String tag, String param, Object context) {
        try {
            Logger.w(tag, this.makeLogMsg(tag, param, context));
        }catch (Exception ex) {

        }
    }

    public void warn(String tag, Throwable e, Object context) {
        try{
            Logger.w(tag, this.makeLogMsg(tag, e, context));
            Logger.e(e);
        }catch (Exception ex) {

        }
    }

    public void warn(String tag, String message, Throwable e, Object context) {
        try{
            Logger.w(tag, this.makeLogMsg(tag, message, context));
            Logger.e(e);
        }catch (Exception ex) {

        }
    }

    public void dbLog(String tag, long startTime, String sql) {
        try{
            if (BuildConfig.DEBUG) {
                Logger.i(tag, String.format("thread:%-4d %-5s %-2dms %s", Thread.currentThread().getId(), "", System.currentTimeMillis() - startTime, sql));
            }
        }catch (Exception e) {

        }
    }

    public void errorToInfo(String tag, String desc) {
        try{
            Logger.i(tag, String.format("thread:%-4d %s", Thread.currentThread().getId(), " exception change to info：" + desc));
        }catch (Exception e) {

        }
    }

    public void apiError(String tag, ZalyAPIException exception) {
        try{
            if (BuildConfig.DEBUG) {
                errorToInfo(tag, String.format("API Error: errorCode: %s, errorInfo: %s",
                        exception.getErrorInfoCode(),
                        exception.getErrorInfoStr()));
            }
        }catch (Exception e) {

        }

    }

    public void exceptionError(Exception e){
        if (BuildConfig.DEBUG) {
            Logger.e(e);
        }else {
            Logger.i("error", String.format("thread:%-4d %s", Thread.currentThread().getId(), " exception change to info：" + e.getMessage()));
        }

    }

    public void platformLoginIn(String tag, String desc){
        if (BuildConfig.DEBUG) {
            Logger.i(tag, String.format("thread:%-4d %s", Thread.currentThread().getId(), " shaoye -- exception change to info：" + desc));
        }
    }

    public void logJSInterface(String tag, String method, String params) {
        try{
            if (BuildConfig.DEBUG) {
                Logger.i(tag, "method:" + method + ", arguments:" + params);
            }
        }catch (Exception e) {

        }
    }

}
