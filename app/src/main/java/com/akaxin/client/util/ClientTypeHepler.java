package com.akaxin.client.util;

import android.os.Build;
import android.os.Environment;

import com.akaxin.client.BuildConfig;
import com.akaxin.client.push.MiPushUtils;
import com.akaxin.client.push.UmengPushUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.ClientProto;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 判断是何种客户端类型：
 * Androdi小米：
 * Android华为：
 * Android魅族：
 */
public class ClientTypeHepler {
    private static final String TAG = ClientTypeHepler.class.getSimpleName();

    //判断是小米手机
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    //判断是华为手机
    private static final String KEY_EMUI_API_LEVEL = "ro.build.hw_emui_api_level";
    private static final String KEY_EMUI_VERSION = "ro.build.version.emui";
    private static final String KEY_EMUI_CONFIG_HW_SYS_VERSION = "ro.confg.hw_systemversion";

    ///判断是oppo手机



    public static ClientProto.ClientType getClientType() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equals("Xiaomi"))
            return ClientProto.ClientType.ANDROID_XIAOMI;
        if (manufacturer.equals("HUAWEI"))
            return ClientProto.ClientType.ANDROID_HUAWEI;
        return ClientProto.ClientType.ANDROID;
    }

    public static String getClientTypeString() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer.equals("Xiaomi"))
            return String.valueOf(ClientProto.ClientType.ANDROID_XIAOMI_VALUE);
        if (manufacturer.equals("HUAWEI"))
            return String.valueOf(ClientProto.ClientType.ANDROID_HUAWEI_VALUE);
        return String.valueOf(ClientProto.ClientType.ANDROID_VALUE);
    }

    /**
     * 获取系统文件的属性
     */
    public static class BuildProperties {

        private final Properties properties;

        private BuildProperties() throws IOException {
            properties = new Properties();
            properties.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        }

        public boolean containsKey(final Object key) {
            return properties.containsKey(key);
        }

        public boolean containsValue(final Object value) {
            return properties.containsValue(value);
        }

        public Set<Map.Entry<Object, Object>> entrySet() {
            return properties.entrySet();
        }

        public String getProperty(final String name) {
            return properties.getProperty(name);
        }

        public String getProperty(final String name, final String defaultValue) {
            return properties.getProperty(name, defaultValue);
        }

        public boolean isEmpty() {
            return properties.isEmpty();
        }

        public Enumeration<Object> keys() {
            return properties.keys();
        }

        public Set<Object> keySet() {
            return properties.keySet();
        }

        public int size() {
            return properties.size();
        }

        public Collection<Object> values() {
            return properties.values();
        }

        public static BuildProperties newInstance() throws IOException {
            return new BuildProperties();
        }
    }

    public static String getPushToken(){
        ClientProto.ClientType clientType = ClientTypeHepler.getClientType();
        String token = "default_token";
        switch (clientType) {
            case ANDROID_XIAOMI:
                token = MiPushUtils.getRegId();
                if (BuildConfig.DEBUG) {
                    token = "dev_" + token;
                }
                break;
            case ANDROID_HUAWEI:
            default:
                token = UmengPushUtils.getPushToken();
                if (BuildConfig.DEBUG) {
                    token = "dev_" + token;
                }
                break;
        }
        return token;
    }
}
