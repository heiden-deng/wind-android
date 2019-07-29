package com.akaxin.client.constant;

/**
 * Created by zhangjun on 2018/3/20.
 */

public interface SiteConfig {
    String  SITE_PROT = "2021";//阿卡信站点默认端口

    String PLUGIN_HOME_LIST = "_plugin_home_list";
    String PLUGIN_PAGE_LIST = "_plugin_page_list";
    String PLUGIN_MSG_LIST  = "_plugin_msg_list";
    String DEVICE_LIST      = "_device_list";

    String PLUGIN_HOME_REFERER  = "zaly://siteAddress/goto?page=message";
    String PLUGIN_U2_REFERER    = "zaly://siteAddress/goto?page=u2_msg&site_user_id=chatSessionId";
    String PLUGIN_GROUP_REFERER = "zaly://siteAddress/goto?page=group_msg&site_group_id=chatSessionId";

    String FRIEND_LIST      = "_friend_list";
    String GROUP_LIST       = "_group_list";
    String USER_NAME_CACHE  = "user_name_";
    String USER_ICON_CACHE  = "user_icon_";
    String MSG_IMG_CACHE    = "msg_img_";
    String DB_COMMON_HELPER = "akaxin_dbcommon";

    String FRIEND_APPLY_LIST = "_friend_apply_list_";

     int remark_name_minProtocol   = 5;
     int site_login_id_minProtocol = 5;

     String AKAXIN_SITE_SESSION_ID = "akaxin_site_session_id";

     String AKAXIN_HREFERER_URL = "AKAXIN_HREF_URL";

     String[] SCHEMES = {"zaly", "zalys"};

     String ZALY_SCHEME  = "zaly://";
     String ZALYS_SCHEME = "zalys://";

     String HTTP_SCHEME  = "http://";
     String HTTPS_SCHEME = "https://";

     String SITE_LOGIN_BY_AUTH_FAIL = "_site_login_by_auth_fail";

}
