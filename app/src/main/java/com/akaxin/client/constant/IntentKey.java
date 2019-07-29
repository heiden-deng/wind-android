package com.akaxin.client.constant;

/**
 * Created by Mr.kk on 2018/6/29.
 * This Project was client-android
 */

public class IntentKey {
    //传递用户信息的统一如下
    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_USER_HEAD = "key_user_head";
    public static final String KEY_USER_PHONE = "key_user_phone";
    public static final String KEY_USER_ID = "key_user_id";
    public static final String KEY_USER_IMAGE_ID = "key_user_image_id";
    public static final String KEY_OLD_USER_NAME = "key_user_old_name";
    public static final String KEY_SITE_LOGIN_ID = "key_user_site_login_id";
    public static final String KEY_OLD_REMARK_NAME = "key_user_old_remark_name";
    public static final String KEY_REMARK_NAME = "key_user_remark_name";
    public static final String KEY_USER_SITE_ID = "key_user_site_id";


    public static final String KEY_WEB_URL = "key_web_url";
    //传递群组信息的统一如下
    public static final String KEY_GROUP_ID = "key_group_id";
    public static final String KEY_GROUP_NAME = "key_group_name";
    //当前Site
    public static final String KEY_CURRENT_SITE = "key_current_site";
    public static final String KEY_CURRENT_SITE_ADDRESS = "key_current_site_address";
    /**
     * 二维码类型
     * 0:分享用户
     * 1:分享站点
     * 2:分享群组
     */

    public static final String KEY_QR_CODE_TYPE = "key_qr_code_type";
    public static final int KEY_TYPE_USER = 0;
    public static final int KEY_TYPE_SITE = 1;
    public static final int KEY_TYPE_GROUP = 2;




    /**
     * 连接站点的类型
     * 0:常规
     * 1:带result的连接
     * 2:自动连接
     */
    public static final String KEY_MODE = "KEY_MODE";
    public static final int MODE_NORMAL = 0;
    public static final int MODE_FOR_RESULT = 1;
    public static final int AUTO_MODE_NORMAL = 2;

    /**
     * 手机验证码类型
     */
    public static final String VC_TYPE = "vc_type";

    //好友信息
    public static final String KEY_FRIEND_PROFILE = "key_friend_profile";
    public static final String KEY_FRIEND_SITE_USER_ID = "friend_site_user_id";
    public static final String KEY_FRIEND_USER_NAME = "key_friend_user_name";
    public static final String KEY_FRIEND_RELATION = "key_friend_profile_relation";
    public static final String KEY_FRIEND_SITE_ID = "key_friend_site_id";

    public static final String KEY_IS_GROUP_MEMBER = "key_is_group_member";


    public static final String KEY_MSG_UNREAD_NUM = "key_msg_unread_num";

    public static final String KEY_PROFILE_MODE = "key_profile_mode";

    public static final String TOKEN= "token";

}
