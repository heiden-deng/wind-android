package com.akaxin.client.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.akaxin.client.bean.Site;
import com.akaxin.client.chat.view.impl.GroupMsgActivity;
import com.akaxin.client.chat.view.impl.MessageActivity;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.db.bean.UserGroupBean;
import com.akaxin.client.friend.ApplyAddFriendActivity;
import com.akaxin.client.friend.FriendApplyListActivity;
import com.akaxin.client.friend.FriendProfileActivity;
import com.akaxin.client.friend.presenter.impl.UserProfilePresenter;
import com.akaxin.client.group.presenter.impl.GroupPresenter;
import com.akaxin.client.group.view.impl.GroupProfileActivity;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.UserProto;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

/**
 * Created by alexfan on 2018/4/12.
 * 这个抽象类负责把跳转的 url 转换成对应的 intent 和生成供分享使用的 url.
 */

public abstract class UrlUtils {

    public static final String TAG = "UrlUtils";

    private static final String[] SCHEMES = {"zaly", "zalys"};
    public static final String PATH_GOTO = "goto";
    public static final String KEY_ACTIVITY = "page";
    public static final String KEY_USER_ID = "site_user_id";
    public static final String KEY_GROUP_ID = "site_group_id";
    private static final String KEY_PAGE_URL = "page_url";

    public static final String ACTIVITY_MAIN_TAB_MESSAGE = "message";
    private static final String ACTIVITY_MAIN_TAB_CONTACTS = "contacts";
    private static final String ACTIVITY_MAIN_TAB_PERSONAL = "personal";
    private static final String ACTIVITY_U2_MSG = "u2_msg";
    private static final String ACTIVITY_GROUP_MSG = "group_msg";
    public static final String ACTIVITY_USER_PROFILE = "user_profile";
    private static final String ACTIVITY_GROUP_PROFILE = "group_profile";
    private static final String ACTIVITY_REQUEST_FRIEND = "request_friend";
    private static final String ACTIVITY_FRIEND_APPLY = "friend_apply";
    private static final String OS_BROWSER = "os_browser";
    public static final String TOKEN = "token";
    public static final String JOIN_GROUP_BY_TOKEN = "join_group_by_token";
    private static Site currentSite;

    public static Intent getJumpIntent(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            if (validScheme(uri)) {
                return getActivityIntentFromParams(context, uri);
            }
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, "Error parsing jump url");
        }
        return goToDefault(context);
    }

    private static boolean validScheme(Uri uri) {
        String uriScheme = uri.getScheme();
        for (String scheme : SCHEMES) {
            if (scheme.equals(uriScheme)) {
                return true;
            }
        }
        return false;
    }

    private static HashMap<String, String> getQueryMap(Uri uri) {
        Set<String> paramNames = uri.getQueryParameterNames();
        HashMap<String, String> params = new HashMap<>();
        for (String paramName : paramNames) {
            params.put(paramName, uri.getQueryParameter(paramName));
        }
        return params;
    }

    private static String getSiteAddress(Uri uri) {
        String host = uri.getHost();
        int port = uri.getPort();
        if (port < 0) port = SiteUtils.DEFAULT_PORT;
        currentSite = new Site();
        currentSite.setSitePort(port);
        currentSite.setSiteHost(host);
        return String.format(Locale.getDefault(), "%s:%d", host, port);
    }

    private static Intent getActivityIntentFromParams(Context context, Uri uri) {
        HashMap<String, String> params = getQueryMap(uri);
        String siteAddress = getSiteAddress(uri);

        String value = params.get(KEY_ACTIVITY);
        Intent intent = null;
        ZalyLogUtils.getInstance().info(TAG, "key_activity: " + value);

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
                intent = goToGroupMessage(context, currentSite, siteGroupId);
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
                intent = goToRequestFriend(context, addSiteUserId, currentSite);
                break;
            case ACTIVITY_FRIEND_APPLY:
                intent = goToFriendApplyList(context);
                break;
            case OS_BROWSER:
                String pageUrl = params.get(KEY_PAGE_URL);
                intent = goToOsBrowser(pageUrl);
                break;
        }
        return intent;
    }

    private static Intent goToOsBrowser(String url) {
        Uri uri = Uri.parse(url);
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    /**
     * 跳转到消息帧
     *
     * @param context
     * @return
     */
    private static Intent goToDefault(Context context) {
        Intent intent = new Intent(context, ZalyMainActivity.class);
        intent.putExtra(ZalyMainActivity.KEY_TAB_INDEX, ZalyMainActivity.SESSION_TAB_INDEX);
        return intent;
    }

    /**
     * 跳转到通讯录帧
     *
     * @param context
     * @return
     */
    private static Intent goToContact(Context context) {
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
    private static Intent goToPersonal(Context context) {
        Intent intent = new Intent(context, ZalyMainActivity.class);
        intent.putExtra(ZalyMainActivity.KEY_TAB_INDEX, ZalyMainActivity.PERSONAL_TAB_INDEX);
        return intent;
    }

    /**
     * 存在好友，跳到二人聊天界面；不存在好友，跳到个人资料页面
     *
     * @param context
     * @param siteUserId
     * @return
     */
    private static Intent goToU2Message(Context context, String siteUserId) {
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
    private static Intent goToGroupMessage(Context context, Site site, String siteGroupId) {
        if (siteGroupId == null) {
            return goToDefault(context);
        }
        ////TODO 依赖本地数据库，可能会导致，刚加的群，还不存在群库里面
        UserGroupBean profile = GroupPresenter.getInstance(site).getGroupBeanByGroupId(siteGroupId, site);
        if (profile == null) {
            return goToGroupProfile(context, siteGroupId, false);
        } else {
            ZalyLogUtils.getInstance().info(TAG, "groupId is " + siteGroupId);
            Intent intent = new Intent(context, GroupMsgActivity.class);
            intent.putExtra(GroupMsgActivity.KEY_GROUP_ID, siteGroupId);
            intent.putExtra(GroupMsgActivity.KEY_GROUP_NAME, profile.getGroupName());
            intent.putExtra(GroupMsgActivity.KEY_GROUP_PROFILE, profile.toString());
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
    private static Intent goToFriendProfile(Context context, String siteUserId) {
        if (siteUserId == null) {
            return goToDefault(context);
        }
        Intent intent = new Intent(context, FriendProfileActivity.class);
        intent.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_FRIEND_SITE_ID);
        intent.putExtra(IntentKey.KEY_FRIEND_SITE_ID, siteUserId);
        return intent;
    }

    /**
     * 群组的资料页面
     *
     * @param context
     * @param siteGroupId
     * @return
     */
    private static Intent goToGroupProfile(Context context, String siteGroupId, Boolean isGroupMember) {
        Intent intent = new Intent(context, GroupProfileActivity.class);
        intent.putExtra(IntentKey.KEY_GROUP_ID, siteGroupId);
        intent.putExtra(IntentKey.KEY_IS_GROUP_MEMBER, isGroupMember);
        return intent;
    }


    /**
     * 好友申请列表
     *
     * @param mContext
     * @return
     */
    private static Intent goToFriendApplyList(Context mContext) {
        Intent intent = new Intent(mContext, FriendApplyListActivity.class);
        return intent;
    }

    /**
     * 添加好友
     *
     * @param context
     * @param siteUserId
     * @return
     */
    private static Intent goToRequestFriend(Context context, String siteUserId, Site site) {
        if (siteUserId == null) {
            return goToDefault(context);
        }
        ////先判断是不是好友，不是好友跳转添加好友，是好友，跳转到个人profile
        UserProto.SimpleUserProfile profile = UserProfilePresenter.getInstance(site).queryFriendBySiteUserId(siteUserId);
        if (profile == null) {
            Intent intent = new Intent(context, ApplyAddFriendActivity.class);
            intent.putExtra(IntentKey.KEY_USER_SITE_ID, siteUserId);
            return intent;
        } else {
            Intent intent = goToFriendProfile(context, siteUserId);
            return intent;
        }
    }

    private static String getShareLink(String url) {
        Uri link = new Uri.Builder().scheme("http")
                .encodedAuthority("url.akaxin.com/")
                .appendQueryParameter("u", url)
                .build();
        ZalyLogUtils.getInstance().info(TAG, link.toString());
        return link.toString();
    }

    public static String buildShareLinkForSite(String siteAddress) {
        Uri uri = new Uri.Builder().scheme(SCHEMES[0])
                .encodedAuthority(siteAddress)
                .appendPath(PATH_GOTO)
                .appendQueryParameter(KEY_ACTIVITY, ACTIVITY_MAIN_TAB_MESSAGE)
                .build();
        ZalyLogUtils.getInstance().info(TAG, uri.toString());
        return getShareLink(uri.toString());
    }

    public static String buildShareLinkForUser(String siteAddress, String userId) {
        Uri uri = new Uri.Builder().scheme(SCHEMES[0])
                .encodedAuthority(siteAddress)
                .appendPath(PATH_GOTO)
                .appendQueryParameter(KEY_ACTIVITY, ACTIVITY_USER_PROFILE)
                .appendQueryParameter(KEY_USER_ID, userId)
                .build();
        ZalyLogUtils.getInstance().info(TAG, uri.toString());
        return getShareLink(uri.toString());
    }

    public static String buildShareLinkForGroup(String siteAddress, String groupId) {
        Uri uri = new Uri.Builder().scheme(SCHEMES[0])
                .encodedAuthority(siteAddress)
                .appendPath(PATH_GOTO)
                .appendQueryParameter(KEY_ACTIVITY, ACTIVITY_GROUP_PROFILE)
                .appendQueryParameter(KEY_GROUP_ID, groupId)
                .build();
        ZalyLogUtils.getInstance().info(TAG, uri.toString());
        return getShareLink(uri.toString());
    }


    public static String buildShareGroupQR(String siteAddress, String siteGroupId, String token) {
        Uri uri = new Uri.Builder().scheme(SCHEMES[0])
                .encodedAuthority(siteAddress)
                .appendPath(PATH_GOTO)
                .appendQueryParameter(KEY_ACTIVITY, JOIN_GROUP_BY_TOKEN)
                .appendQueryParameter(KEY_GROUP_ID, siteGroupId)
                .appendQueryParameter(TOKEN, token)
                .build();
        return getShareLink(uri.toString());
    }
}
