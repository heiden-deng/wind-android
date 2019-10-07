package com.akaxin.client;

import android.app.Application;
import android.content.Context;
import android.support.text.emoji.EmojiCompat;
import android.support.text.emoji.bundled.BundledEmojiCompatConfig;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.push.MiPushUtils;
import com.akaxin.client.push.UmengPushUtils;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.ClientTypeHepler;
import com.akaxin.client.util.NotificationUtils;
import com.akaxin.client.util.SPUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.WuerLogAdapter;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.ClientProto;
import com.akaxin.proto.core.UserProto;
import com.blankj.utilcode.util.Utils;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.Bugly;
import com.windchat.im.IMClient;
import com.windchat.im.socket.SiteAddress;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZalyApplication extends Application {
    public static final String TAG = ZalyApplication.class.getSimpleName();
    
    public static Map<String, Long> map;
    public static boolean active = false;
    private static Context mContext;
    private volatile static String CurSessionId;
    private static String CurSPName;
    public volatile static List<Site> siteList;
    private static UserProto.UserProfile curProfile;
    public volatile static String gotoUrl;


    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        mContext = this;
        initPush();
        EmojiCompat.Config config = new BundledEmojiCompatConfig(this);
        EmojiCompat.init(config);
        Bugly.init(getApplicationContext(), "cdd1cf248e", BuildConfig.DEBUG);
        //全局toast设置
        Toaster.doEnable(this);

        //Logger设置
        Logger.addLogAdapter(new WuerLogAdapter());
        Logger.addLogAdapter(new DiskLogAdapter());
        //当前站点无身份，忽略IM连接
        if (StringUtils.isEmpty(ZalyApplication.getGlobalUserId())) {
            return;
        }

        /**
         * connect site address
         */
        List<Site> sites = SitePresenter.getInstance().getAllSiteLists();
        IMClient.connect(sites);

        NotificationUtils.initChannels(getApplicationContext());
        getAndroiodScreenProperty();
    }

    public void getAndroiodScreenProperty() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        int densityDpi = dm.densityDpi;     // 屏幕密度dpi（120 / 160 / 240）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        int screenHeight = (int) (height / density);// 屏幕高度(dp)


        ZalyLogUtils.getInstance().info("lipengfei", "屏幕宽度（像素）：" + width);
        ZalyLogUtils.getInstance().info("lipengfei", "屏幕高度（像素）：" + height);
        ZalyLogUtils.getInstance().info("lipengfei", "屏幕密度（0.75 / 1.0 / 1.5）：" + density);
        ZalyLogUtils.getInstance().info("lipengfei", "屏幕密度dpi（120 / 160 / 240）：" + densityDpi);
        ZalyLogUtils.getInstance().info("lipengfei", "屏幕宽度（dp）：" + screenWidth);
        ZalyLogUtils.getInstance().info("lipengfei", "屏幕高度（dp）：" + screenHeight);
    }

    public void initPush() {
        ClientProto.ClientType client = ClientTypeHepler.getClientType();
        switch (client) {
            case ANDROID_XIAOMI:
                //初始化push推送服务
                MiPushUtils.initMiPush();
                //打开Log
                LoggerInterface newLogger = new LoggerInterface() {

                    @Override
                    public void setTag(String tag) {
                        // ignore
                    }

                    @Override
                    public void log(String content, Throwable t) {
                        Log.d(TAG, content, t);
                    }

                    @Override
                    public void log(String content) {
                        Log.d(TAG, content);
                    }
                };
                com.xiaomi.mipush.sdk.Logger.setLogger(mContext, newLogger);
                break;
            case ANDROID_HUAWEI:

            case ANDROID_OPPO:

            default:
                UmengPushUtils.getInstance().init(mContext);
                break;
        }
    }


    /**
     * 获取有效的在线站点
     *
     * @return
     */
    public static List<Site> getOnLineSites() {
        List<Site> onLineSites = new ArrayList<>();
        if (ZalyApplication.siteList == null) {
            return null;
        }
        String currentSiteIndenty = ZalyApplication.getCfgSP().getString(Configs.KEY_CUR_SITE, "");

        for (Site site : ZalyApplication.siteList) {
            try {
                if (IMClient.getInstance(site).isConnected() ||
                        site.getSiteIdentity().equals(currentSiteIndenty)) {
                    onLineSites.add(site);
                }
            } catch (Exception e) {
                ZalyLogUtils.getInstance().exceptionError(e);
                if (site.getSiteIdentity().equals(currentSiteIndenty)) {
                    onLineSites.add(site);
                }
            }
        }
        return onLineSites;
    }

    public static UserProto.UserProfile getCurProfile(Site site) {
        if (curProfile == null) {
            curProfile = UserProto.UserProfile.newBuilder()
                    .setUserPhoto(site.getSiteUserImage())
                    .setSiteUserId(site.getSiteUserId())
                    .setUserName(site.getSiteUserName())
                    .build();
        }
        return curProfile;
    }

    public static void setCurProfile(UserProto.UserProfile curProfile) {
        ZalyApplication.curProfile = curProfile;
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 获取当前用户私人存储空间
     *
     * @return
     */
    public static SPUtils getCurSP() {
        return SPUtils.getInstance(CurSPName);
    }

    /**
     * 获取应用存储空间
     *
     * @return
     */
    public static SPUtils getCfgSP() {
        return SPUtils.getInstance(Configs.ZALY_SP);
    }

    public static SiteAddress getSiteAddressObj(String siteAddress) {
        SiteAddress siteAddressObj = new SiteAddress(siteAddress);
        return siteAddressObj;
    }

    public static String getUserIdNum() {
        return ZalyApplication.getCfgSP().getKey(Configs.USER_ID_NUM);
    }

    public static void setUserIdNum(int un) {
        ZalyApplication.getCfgSP().put(Configs.USER_ID_NUM, un);
    }

    public static void setGotoUrl(String url) {
        gotoUrl = url;
    }

    public static String getGotoUrl() {
        return gotoUrl;
    }

    /**
     * 通过用户身份公钥，生成globalUserId
     *
     * @return
     */
    public static String getGlobalUserId() {
        String pubKey = getCfgSP().getKey(Configs.USER_PUB_KEY);
        if (StringUtils.isNotEmpty(pubKey)) {
            String globalUserId = StringUtils.getGlobalUserIdHash(pubKey);
            return globalUserId;
        }
        return null;
    }

    public static void setUserInfo(String siteUserId, String userImgId, String username) {
        ZalyApplication.getCurSP().putKey(SiteConfig.USER_ICON_CACHE + siteUserId, userImgId);
        ZalyApplication.getCurSP().putKey(SiteConfig.USER_NAME_CACHE + siteUserId, username);
    }

    public static void logProcessInfo(String TAG) {
        Logger.i(TAG + " ProcessInfo", " process: " + android.os.Process.myPid() + ", thread: " + Thread.currentThread().getName());
    }

}
