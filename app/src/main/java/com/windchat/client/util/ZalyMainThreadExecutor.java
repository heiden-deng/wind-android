package com.windchat.client.util;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yichao on 2018/1/13.
 */

public class ZalyMainThreadExecutor {

    private static Handler handler;
    private static HashMap<Object, List<Runnable>> runnableMap = new HashMap<>();

    static {
        handler = new Handler(Looper.getMainLooper());
    }

    public static void post(Runnable runnable) {
        handler.post(runnable);
    }

    public static void postDelayed(Object tag, Runnable runnable, long delayInMills) {
        handler.postDelayed(runnable, delayInMills);
        List<Runnable> runnables = runnableMap.get(tag);
        if (runnables == null) {
            runnables = new ArrayList<>();
            runnableMap.put(tag, runnables);
        }
        runnables.add(runnable);
    }

    public static void cancelAllRunnables(Object tag) {
        List<Runnable> runnables = runnableMap.get(tag);
        if (runnables == null) {
            return;
        }
        for (Runnable runnable : runnables) {
            handler.removeCallbacks(runnable);
        }
    }

}
