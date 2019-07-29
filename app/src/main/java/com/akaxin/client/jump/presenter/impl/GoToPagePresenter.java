package com.akaxin.client.jump.presenter.impl;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.akaxin.client.Configs;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.activitys.SiteConnListActivity;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bridge.PluginWebActivity;
import com.akaxin.client.chat.view.impl.GroupMsgActivity;
import com.akaxin.client.chat.view.impl.MessageActivity;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.db.bean.UserGroupBean;
import com.akaxin.client.friend.ApplyAddFriendActivity;
import com.akaxin.client.friend.FriendApplyListActivity;
import com.akaxin.client.friend.FriendProfileActivity;
import com.akaxin.client.friend.presenter.impl.UserProfilePresenter;
import com.akaxin.client.group.presenter.impl.GroupPresenter;
import com.akaxin.client.group.view.impl.GroupProfileActivity;
import com.akaxin.client.jump.presenter.IGotoPagePresenter;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.util.SiteUtils;
import com.akaxin.client.util.UrlUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.PluginProto;
import com.akaxin.proto.core.UserProto;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.akaxin.client.util.UrlUtils.JOIN_GROUP_BY_TOKEN;

/**
 * Created by zhangjun on 2018/3/20.
 */

/**
 * 这段代码的结构现在很有问题.
 */

public class GoToPagePresenter implements IGotoPagePresenter {

    public static final String TAG = GoToPagePresenter.class.getSimpleName();

    private static final String[] SCHEMES = {"zaly", "zalys"};
    private static final String PATH_GOTO = "/goto";
    private static final String KEY_ACTIVITY = "page";
    private static final String KEY_USER_ID = "site_user_id";
    private static final String KEY_GROUP_ID = "site_group_id";
    private static final String KEY_PLUGIN_ID = "plugin_id";
    private static final String KEY_PAGE_URL = "page_url";
    private static final String KEY_PARAM = "akaxin_param";

    private static final String ACTIVITY_MAIN_TAB_MESSAGE = "message";
    private static final String ACTIVITY_MAIN_TAB_CONTACTS = "contacts";
    private static final String ACTIVITY_MAIN_TAB_PERSONAL = "personal";
    private static final String ACTIVITY_U2_MSG = "u2_msg";
    private static final String ACTIVITY_GROUP_MSG = "group_msg";
    private static final String ACTIVITY_USER_PROFILE = "user_profile";
    private static final String ACTIVITY_GROUP_PROFILE = "group_profile";
    private static final String ACTIVITY_REQUEST_FRIEND = "request_friend";
    private static final String ACTIVITY_FRIEND_APPLY = "friend_apply";
    private static final String ACTIVITY_PLUGIN = "plugin";
    private static final String OS_BROWSER = "os_browser";
    private static final String PLUGIN_FOR_U2_CHAT = "plugin_for_u2_chat";
    private static final String PLUGIN_FOR_GROUP_CHAT = "plugin_for_group_chat";

    private static String host;
    private static int port;
    private static Uri uri;
    private static Site currentSite;

    private Site s;

    public GoToPagePresenter(Site s) {
        this.s = s;
    }

    public String getGroupIdKey() {
        return KEY_GROUP_ID;
    }

    public String getU2IdKey() {
        return KEY_USER_ID;
    }

    /**
     * 处理跳转逻辑
     *
     * @param context
     * @param url
     */
    @Override
    public Intent handleGotoPage(Context context, String url, Boolean isIntent) {
        if (!StringUtils.isEmpty(url)) {
            ZalyApplication.setGotoUrl(url);
            parseUrl(url);
            return handleJumpByType(context, isIntent);
        }
        ZalyApplication.setGotoUrl("");
        return null;
    }

    /**
     * 解析url
     *
     * @param url
     */

    private void parseUrl(String url) {
        uri = Uri.parse(url);
        if (validScheme(uri)) {
            host = uri.getHost();
            port = uri.getPort();
            if (port < 0) {
                port = 2021;
            }
        }
    }

    /**
     * 负责跳转
     *
     * @param context
     */
    public Intent handleJumpByType(Context context, Boolean isIntent) {
        Intent intent = null;

        switch (uri.getPath()) {
            case PATH_GOTO:

                intent = getActivityIntentFromParams(context, uri);
                break;
        }
        if (intent != null && !isIntent) {
            context.startActivity(intent);
            return null;
        } else {
            return intent;
        }
    }

    public Intent getJumpIntent(Context context, String url) {
        Uri uri = Uri.parse(url);
        if (validScheme(uri)) {
            ZalyApplication.setGotoUrl(url);
            switch (uri.getPath()) {
                case PATH_GOTO:
                    return getActivityIntentFromParams(context, uri);
            }
        }
        return goToNull(context);
    }

    private boolean validScheme(Uri uri) {
        String uriScheme = uri.getScheme();
        for (String scheme : SCHEMES) {
            if (scheme.equals(uriScheme)) {
                return true;
            }
        }
        return false;
    }

    private void getCurrentSite(Uri uri) {
        this.currentSite = new Site();
        String host = uri.getHost();
        int port = uri.getPort();
        if (port < 0) port = SiteUtils.DEFAULT_PORT;
        this.currentSite.setSiteHost(host);
        this.currentSite.setSitePort(port + "");
    }


    private HashMap<String, String> getQueryMap(Uri uri) {
        Set<String> paramNames = uri.getQueryParameterNames();
        HashMap<String, String> params = new HashMap<>();
        for (String paramName : paramNames) {
            params.put(paramName, uri.getQueryParameter(paramName));
        }
        return params;
    }

    private Intent getActivityIntentFromParams(Context context, Uri uri) {
        getCurrentSite(uri);
        Map<String, String> params = getQueryMap(uri);
        String value = params.get(KEY_ACTIVITY);
        String currentSiteIndenty = ZalyApplication.getCfgSP().getString(Configs.KEY_CUR_SITE, "");
        try {
            if (currentSiteIndenty != null && !currentSiteIndenty.equals(currentSite.getSiteIdentity())) {
                ////TODO  跳转，非当前站点，弹框提示用户是否切换站点
                Intent intent = new Intent(ZalyApplication.getContext(), SiteConnListActivity.class);
                intent.putExtra(IntentKey.KEY_MODE, IntentKey.AUTO_MODE_NORMAL);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                intent.putExtra(IntentKey.KEY_CURRENT_SITE_ADDRESS, currentSite.getSiteAddress());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                return intent;
            }
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            return goToNull(context);
        }


        Intent intent = null;
        if (value == null) {
            intent = goToNull(context);
            return intent;
        }
        switch (value) {
            case ACTIVITY_MAIN_TAB_MESSAGE:
                intent = goToDefault(context);
                break;
            case ACTIVITY_MAIN_TAB_CONTACTS:
                intent = goToContact(context);
                break;
            case ACTIVITY_MAIN_TAB_PERSONAL:
                intent = goToPersonal(context);
                break;
            case ACTIVITY_U2_MSG:
                String siteUserId = params.get(KEY_USER_ID);
                intent = goToU2Message(context, siteUserId);
                break;
            case ACTIVITY_GROUP_MSG:
                String siteGroupId = params.get(KEY_GROUP_ID);
                intent = goToGroupMessage(context, siteGroupId);
                break;
            case ACTIVITY_USER_PROFILE:
                String siteUserProfileId = params.get(KEY_USER_ID);
                intent = goToFriendProfile(context, siteUserProfileId);
                break;
            case ACTIVITY_GROUP_PROFILE:
                String siteGroupProfileId = params.get(KEY_GROUP_ID);
                intent = goToGroupProfile(context, siteGroupProfileId, true);
                break;
            case ACTIVITY_REQUEST_FRIEND:
                String addSiteUserId = params.get(KEY_USER_ID);
                intent = goToRequestFriend(context, addSiteUserId);
                break;
            case ACTIVITY_FRIEND_APPLY:
                intent = goToFriendApplyList(context);
                break;
            case ACTIVITY_PLUGIN:
                intent = goToPluginWeb(context, params.get(KEY_PLUGIN_ID));
                break;
            case OS_BROWSER:
                String pageUrl = params.get(KEY_PAGE_URL);
                intent = goToOsBrowser(pageUrl);
                break;
            case PLUGIN_FOR_GROUP_CHAT:
                String siteGroupIdByPlugin = params.get(KEY_GROUP_ID);
                String pluginIdGroup = params.get(KEY_PLUGIN_ID);
                String paramGroup = params.get(KEY_PARAM);
                intent = goToGroupMessage(context, siteGroupIdByPlugin, pluginIdGroup, paramGroup);
                break;
            case PLUGIN_FOR_U2_CHAT:
                String siteUserIdByPlugin = params.get(KEY_USER_ID);
                String pluginIdU2 = params.get(KEY_PLUGIN_ID);
                String paramU2 = params.get(KEY_PARAM);
                intent = goToU2Message(context, siteUserIdByPlugin, pluginIdU2, paramU2);
                break;
            case JOIN_GROUP_BY_TOKEN:
                String token = uri.getQueryParameter(UrlUtils.TOKEN);
                String siteGroupID = uri.getQueryParameter(UrlUtils.KEY_GROUP_ID);
                intent = goToIndex(context, token, siteGroupID);
                break;

        }
        return intent;
    }


    private Intent goToIndex(Context mContext, String token, String siteGroupID) {
        Intent intent = new Intent(mContext, ZalyMainActivity.class);
        intent.putExtra(IntentKey.KEY_QR_CODE_TYPE, IntentKey.KEY_TYPE_GROUP);
        intent.putExtra(IntentKey.KEY_GROUP_ID, siteGroupID);
        intent.putExtra(IntentKey.TOKEN, token);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, s);
        return intent;
    }


    private Intent goToOsBrowser(String url) {
        Uri uri = Uri.parse(url);
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    /**
     * 好友申请列表
     *
     * @param mContext
     * @return
     */
    private Intent goToFriendApplyList(Context mContext) {
        Intent intent = new Intent(mContext, FriendApplyListActivity.class);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
        return intent;
    }

    /**
     * 添加好友
     *
     * @param context
     * @param siteUserId
     * @return
     */
    private Intent goToRequestFriend(Context context, String siteUserId) {
        if (siteUserId == null) {
            return goToDefault(context);
        }
        ////先判断是不是好友，不是好友跳转添加好友，是好友，跳转到个人profile
        UserProto.SimpleUserProfile profile = UserProfilePresenter.getInstance(currentSite).queryFriendBySiteUserId(siteUserId);
        if (profile == null) {
            Intent intent = new Intent(context, ApplyAddFriendActivity.class);
            intent.putExtra(IntentKey.KEY_USER_SITE_ID, siteUserId);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
            return intent;
        } else {
            Intent intent = goToFriendProfile(context, siteUserId);
            return intent;
        }
    }

    /**
     * 跳转到消息帧
     *
     * @param context
     * @return
     */
    private Intent goToDefault(Context context) {
        Intent intent = new Intent(context, ZalyMainActivity.class);
        intent.putExtra(ZalyMainActivity.KEY_TAB_INDEX, ZalyMainActivity.SESSION_TAB_INDEX);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);

        return intent;
    }

    private Intent goToNull(Context context) {
        Intent intent = new Intent(context, ZalyMainActivity.class);
        intent.putExtra(ZalyMainActivity.KEY_TAB_INDEX, ZalyMainActivity.SESSION_TAB_INDEX);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, s);

        return intent;
    }

    /**
     * 跳转到通讯录帧
     *
     * @param context
     * @return
     */
    private Intent goToContact(Context context) {
        Intent intent = new Intent(context, ZalyMainActivity.class);
        intent.putExtra(ZalyMainActivity.KEY_TAB_INDEX, ZalyMainActivity.CONTACT_TAB_INDEX);
        return intent;
    }

    /**
     * 跳转到个人帧
     *
     * @param context
     * @return
     */
    private Intent goToPersonal(Context context) {
        Intent intent = new Intent(context, ZalyMainActivity.class);
        intent.putExtra(ZalyMainActivity.KEY_TAB_INDEX, ZalyMainActivity.PERSONAL_TAB_INDEX);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);

        return intent;
    }

    /**
     * 存在好友，跳到二人聊天界面；不存在好友，跳到个人资料页面
     *
     * @param context
     * @param siteUserId
     * @return
     */
    private Intent goToU2Message(Context context, String siteUserId) {
        if (siteUserId == null) {
            return goToDefault(context);
        }
        ////TODO 依赖本地数据库，可能会导致，刚加的好友，还不存在好友库里面

        UserProto.SimpleUserProfile profile = UserProfilePresenter.getInstance(currentSite).queryFriendBySiteUserId(siteUserId);
        if (profile == null) {
            Intent intent = goToFriendProfile(context, siteUserId);
            return intent;
        } else {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra(IntentKey.KEY_FRIEND_SITE_USER_ID, siteUserId);
            intent.putExtra(IntentKey.KEY_FRIEND_USER_NAME, profile.getUserName());
            intent.putExtra(IntentKey.KEY_FRIEND_PROFILE, profile.toByteArray());
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);

            return intent;
        }
    }

    /**
     * 存在好友，跳到二人聊天界面；不存在好友，跳到个人资料页面
     *
     * @param context
     * @param siteUserId
     * @return
     */
    private Intent goToU2Message(Context context, String siteUserId, String pluginId, String param) {
        if (siteUserId == null) {
            return goToDefault(context);
        }
        ////TODO 依赖本地数据库，可能会导致，刚加的好友，还不存在好友库里面

        UserProto.SimpleUserProfile profile = UserProfilePresenter.getInstance(currentSite).queryFriendBySiteUserId(siteUserId);
        if (profile == null) {
            Intent intent = goToFriendProfile(context, siteUserId);
            return intent;
        } else {
            Intent intent = new Intent(context, MessageActivity.class);
            intent.putExtra(IntentKey.KEY_FRIEND_SITE_USER_ID, siteUserId);
            intent.putExtra(IntentKey.KEY_FRIEND_USER_NAME, profile.getUserName());
            intent.putExtra(IntentKey.KEY_FRIEND_PROFILE, profile.toByteArray());
            intent.putExtra(MessageActivity.KEY_PLUGIN_ID_FOR_JUMP, pluginId);
            intent.putExtra(MessageActivity.KEY_PLUGIN_PARAM, param);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);

            return intent;
        }
    }

    /**
     * 跳转到群消息
     *
     * @param context
     * @param siteGroupId
     * @return
     */
    private Intent goToGroupMessage(Context context, String siteGroupId) {
        if (siteGroupId == null) {
            return goToDefault(context);
        }
        ////TODO 依赖本地数据库，可能会导致，刚加的群，还不存在群库里面
        UserGroupBean profile = GroupPresenter.getInstance(currentSite).getGroupBeanByGroupId(siteGroupId, currentSite);
        if (profile == null) {
            return goToGroupProfile(context, siteGroupId, false);
        } else {
            Intent intent = new Intent(context, GroupMsgActivity.class);
            intent.putExtra(GroupMsgActivity.KEY_GROUP_ID, siteGroupId);
            intent.putExtra(GroupMsgActivity.KEY_GROUP_NAME, profile.getGroupName());
            intent.putExtra(GroupMsgActivity.KEY_GROUP_PROFILE, profile.toString());
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);

            return intent;
        }
    }

    /**
     * 跳转到群消息
     *
     * @param context
     * @param siteGroupId
     * @return
     */
    private Intent goToGroupMessage(Context context, String siteGroupId, String pluginId, String param) {
        if (siteGroupId == null) {
            return goToDefault(context);
        }
        ////TODO 依赖本地数据库，可能会导致，刚加的群，还不存在群库里面
        UserGroupBean profile = GroupPresenter.getInstance(currentSite).getGroupBeanByGroupId(siteGroupId, currentSite);
        if (profile == null) {
            return goToGroupProfile(context, siteGroupId, false);
        } else {
            Intent intent = new Intent(context, GroupMsgActivity.class);
            intent.putExtra(GroupMsgActivity.KEY_GROUP_ID, siteGroupId);
            intent.putExtra(GroupMsgActivity.KEY_GROUP_NAME, profile.getGroupName());
            intent.putExtra(GroupMsgActivity.KEY_GROUP_PROFILE, profile.toString());
            intent.putExtra(GroupMsgActivity.KEY_PLUGIN_ID_FOR_JUMP, pluginId);
            intent.putExtra(GroupMsgActivity.KEY_PLUGIN_PARAM, param);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
            return intent;
        }
    }

    /**
     * 好友的个人资料页面
     *
     * @param context
     * @param siteUserId
     * @return
     */
    private Intent goToFriendProfile(Context context, String siteUserId) {
        if (siteUserId == null) {
            return goToDefault(context);
        }
        Intent intent = new Intent(context, FriendProfileActivity.class);
        intent.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_FRIEND_SITE_ID);
        intent.putExtra(IntentKey.KEY_FRIEND_SITE_ID, siteUserId);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
        return intent;
    }

    /**
     * 群组的资料页面
     *
     * @param context
     * @param siteGroupId
     * @return
     */
    private Intent goToGroupProfile(Context context, String siteGroupId, Boolean isGroupMember) {
        Intent intent = new Intent(context, GroupProfileActivity.class);
        intent.putExtra(IntentKey.KEY_GROUP_ID, siteGroupId);
        intent.putExtra(IntentKey.KEY_IS_GROUP_MEMBER, isGroupMember);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);

        return intent;
    }

    private Intent goToPluginWeb(Context context, String pluginId) {
        Intent intent = new Intent(context, PluginWebActivity.class);
        PluginProto.Plugin plugin = PluginProto.Plugin.newBuilder().setId(pluginId).build();
        intent.putExtra(PluginWebActivity.KEY_WEB_VIEW_DATA, plugin.toByteArray());
        String referrer = SiteConfig.PLUGIN_HOME_REFERER.replace("siteAddress", currentSite.getSiteAddress());
        intent.putExtra(PluginWebActivity.REFERER, referrer);
        intent.putExtra(PluginWebActivity.IS_ADD_COOKIE, false);
        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
        return intent;
    }
}
