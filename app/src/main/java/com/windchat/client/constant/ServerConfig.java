package com.windchat.client.constant;

/**
 * platform address address && port
 * Created by anguoyue on 27/01/2018.
 */

public interface ServerConfig {
    String PLATFORM_ADDRESS = "platform.akaxin.com";//阿卡信平台地址
    int PLATFORM_PROT = 8000;//阿卡信平台端口

    String PLATFORM_INDENTIY = "platform_akaxin_com_8000";

    String LOGIN_WITH_PHONE = "login_with_phone";
    String LOGIN_WITH_PHONE_NAME = "手机号登陆的身份";

    String LOGIN_WITH_AUTH = "login_with_auth";
    String LOGIN_WITH_AUTH_NAME = "授权登录的身份";

    String LOGIN_WITH_GENERATE = "login_with_generate";
    String LOGIN_WITH_GENERATE_NAME = "创建身份登录";

    String CHINA_COUNTRY_CODE = "+86";

    String DEMO_SIRE_ADDRESS = "demo.akaxin.com";
}
