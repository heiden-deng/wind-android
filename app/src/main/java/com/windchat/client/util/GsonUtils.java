package com.windchat.client.util;

/**
 * Created by anguoyue on 30/01/2018.
 */

import com.google.gson.Gson;

public class GsonUtils {
    public static String toJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static <T> T fromJson(String str, Class<T> clazz) {
        return new Gson().fromJson(str, clazz);
    }
}
