package com.windchat.client;

import android.os.Environment;

import com.windchat.client.util.data.StringUtils;
import com.blankj.utilcode.util.Utils;

import java.io.File;

/**
 * Created by yichao on 2017/10/10.
 */

public abstract class Configs {

    public static final String DATABASE_NAME = "AKAXIN.db";
    //网络协议1.0版本
    public static final String PROTOCOL_VERSION = BuildConfig.PROTOCOL_VERSION;
    //客户端APP 版本号
    public static final String APP_VERSION = BuildConfig.VERSION_NAME;

    /**
     * 应用级别存储
     */
    public static final String ZALY_SP = "JTt1lFLz";
    public static final String KEY_CUR_SITE = "key_cur_site";

    public static final String USER_ID_NUM = "IDnzDUSER";//用户身份ID标识
    public static final String USER_PRI_KEY = "FUnzDQC0";//用户私钥
    public static final String USER_PUB_KEY = "sJfSV0sM";//用户公钥
    public static final String USER_SIGN = "l2LpP8qt";//用户身份签名
    public static final String DEVICE_PRI_KEY = "UJWUXdeO";//设备私钥
    public static final String DEVICE_PUB_KEY = "rrJiL3tP";//设备公钥
    public static final String DEVICE_SIGN = "r2LpJ8qf";//设备签名
    public static final String PLATFORM_IP = "qq6QWHiP";//平台ip
    public static final String PHONE_ID = "aa6QWHiP";//实名手机号
    public static final String PHONE_TOKEN = "aa6QWHiPkj";//实名手机号

    public static final String DEVICE_IMEI = "device_imei";

    public static final String SUFFIX_USER_TOKEN = "suffix_push_token";

    public static final String PUSH_TOKEN = "PPAiL3tr";

    public static final String LOCAL_SITE_DEFAULT_MARK = "0.0.0.0";

    /**
     * 用户级别存储
     */
    public static final String KEY_NEW_APPLY_FRIEND = "KEY_NEW_APPLY_FRIEND";

    /**
     * SD卡的路径名
     */
    public static final String PATH_SDCARD = Environment.getDataDirectory().getAbsolutePath();

    /**
     * 应用程序在SD卡的根路径
     */
    public static final String PATH_APP_HOME;

    /**
     * 程序在SD卡的文件夹名称
     */
    public static final String DIR_APPHOME = "akaxin";

    static {
        String homeName = DIR_APPHOME;

        if (!PATH_SDCARD.endsWith("/")) {
            homeName = "/" + DIR_APPHOME;
        }
        PATH_APP_HOME = PATH_SDCARD + homeName;
    }


    private static File imgFile = null;
    private static File audioFile = null;

    public static File getImgDir() {
        if (imgFile == null) {
            imgFile = new File(Utils.getApp().getCacheDir(), "image");
        }

        if (!imgFile.exists()) {
            imgFile.mkdirs();
        }

        return imgFile;
    }

    public static File getAudioDir() {
        if (audioFile == null) {
            audioFile = new File(Utils.getApp().getCacheDir(), "audio");
        }
        if (!audioFile.exists()) {
            audioFile.mkdirs();
        }

        return audioFile;
    }


    public static String getGlobalUserId() {
        String pubKey = ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY);
        String globalUserId = StringUtils.getGlobalUserIdHash(pubKey);
        return globalUserId;
    }

}
