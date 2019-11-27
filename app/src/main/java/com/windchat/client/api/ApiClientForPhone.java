package com.windchat.client.api;

import com.windchat.client.Configs;
import com.akaxin.proto.core.PhoneProto;
import com.windchat.im.socket.TransportPackageForResponse;

/**
 * Created by Mr.kk on 2018/6/14.
 * This Project was client-android
 */

public class ApiClientForPhone {
    private static String TAG = ApiClientForPhone.class.getSimpleName();
    private String logTag = "ApiClientForPhone";
    private ApiClient client = null;


    private static final String API_PHONE_VERIFY_CODE = "api.phone.verifyCode";
    private static final String API_PHONE_LOGIN = "api.phone.login";
    private static final String API_PHONE_APPLY_TOKEN = "api.phone.applyToken";

    public ApiClientForPhone(ApiClient client) {
        this.client = client;
        this.logTag = "ApiClient." + this.getClass().getName();
    }
}
