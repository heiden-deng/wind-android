package com.windchat.client.friend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.activitys.ImageShowActivity;
import com.windchat.client.activitys.ShareQRCodeActivity;
import com.windchat.client.activitys.SiteConnListActivity;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.Site;
import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.chat.view.impl.U2MessageActivity;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.constant.SiteConfig;
import com.windchat.client.db.bean.UserFriendBean;
import com.windchat.client.friend.presenter.impl.UserProfilePresenter;
import com.windchat.client.maintab.BaseActivity;
import com.windchat.client.personal.ChangeUsernameActivity;
import com.windchat.client.util.AnimationUtil;
import com.windchat.client.util.ClipboardUtils;
import com.windchat.client.util.NetUtils;
import com.windchat.client.util.UrlUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.file.ImageUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFriendDeleteProto;
import com.akaxin.proto.site.ApiFriendProfileProto;
import com.akaxin.proto.site.ApiFriendRemarkProto;
import com.akaxin.proto.site.ApiFriendSettingProto;
import com.akaxin.proto.site.ApiFriendUpdateSettingProto;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by yichao on 2017/10/28.
 */


public class FriendProfileActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {


    public static final int MODE_FRIEND_SITE_ID = 1;
    public static final int MODE_USER_PROFILE = 2;

    private String siteUserId;
    private UserProto.UserProfile profile;
    private String siteLoginId;
    private String nickName;
    private String userName;

    @BindView(R.id.action_share)
    View actionShare;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.view_group)
    LinearLayout viewGroup;
    @BindView(R.id.item_send_message)
    View sendMessage;
    @BindView(R.id.add_friend_layout)
    View addFriendLayout;
    @BindView(R.id.item_add_friend)
    View addFriendItem;
    @BindView(R.id.item_show_pub_key)
    View showUserPubKey;
    @BindView(R.id.item_show_qrcode)
    View showUserQRCode;
    @BindView(R.id.item_notification_switch)
    View notificationSwitchItem;
    @BindView(R.id.notification_switch)
    SwitchCompat notificationSwitch;
    @BindView(R.id.item_delete_friend)
    View deleteFriend;
    @BindView(R.id.item_site_login_id)
    View siteLoginIdView;
    @BindView(R.id.site_login_id)
    TextView siteLoginIdTV;
    @BindView(R.id.item_site_nick_name)
    View siteNickNameView;
    @BindView(R.id.site_nick_name)
    TextView siteNickNameTv;

    @BindView(R.id.edit_remark_name)
    View remarkNameView;
    @BindView(R.id.remark_name)
    TextView remarkNameTv;

    private boolean messageMute;
    private Site currentSite;


    @Override
    public int getResLayout() {
        return R.layout.activity_friend_profile;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefresh.setEnabled(false);
    }

    @Override
    public void initEvent() {
        notificationSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        int profileMode = getIntent().getIntExtra(IntentKey.KEY_PROFILE_MODE, MODE_FRIEND_SITE_ID);
        ZalyLogUtils.getInstance().info(TAG, "user pub keyprofileMode ==" + profileMode);

        switch (profileMode) {
            case MODE_FRIEND_SITE_ID:
                siteUserId = getIntent().getStringExtra(IntentKey.KEY_FRIEND_SITE_ID);
                if (StringUtils.isEmpty(siteUserId)) {
                    Toaster.showInvalidate(getString(R.string.data_error));
                    finish();
                    return;
                }
                ZalyTaskExecutor.executeUserTask(TAG, new QueryFriendTask());
                break;
            case MODE_USER_PROFILE:
                try {
                    byte[] data = getIntent().getByteArrayExtra(IntentKey.KEY_FRIEND_PROFILE);
                    int relation = getIntent().getIntExtra(IntentKey.KEY_FRIEND_RELATION, 0);
                    profile = UserProto.UserProfile.parseFrom(data);
                    siteUserId = profile.getSiteUserId();
                    siteLoginId = profile.getSiteLoginId();
                    if (relation == 1) {
                        isFriendUI();
                    } else {
                        isNotFriendUI();
                    }
                } catch (Exception e) {
                    finish();
                    return;
                }
                fillData();
                break;
            default:
                Toaster.showInvalidate(getString(R.string.data_error));
                finish();
        }
    }


    private void fillData() {
        userName = profile.getUserName();
        nickName = profile.getNickName();
        siteLoginId = profile.getSiteLoginId();
        new ImageUtils(this, currentSite).loadImage(profile.getUserPhoto(), avatar, R.drawable.ic_default);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(profile.getUserPhoto())
//                .setFileType(FileProto.FileType.USER_PORTRAIT)
//                .setSite(currentSite)
//                .build();
//        Glide.with(this).load(model).
//                apply(new RequestOptions()
//                        .dontAnimate()
//                        .error(R.drawable.ic_default)
//                        .fallback(R.drawable.ic_default))
//                .into(avatar);


        checkoutLoginId(siteLoginId, userName, nickName);
    }

    private void checkoutLoginId(String siteLoginId, String userName, String nickName) {
        if (siteLoginId != null && siteLoginId.length() > 2) {
            siteLoginIdTV.setText(siteLoginId);
            setMultTitle(userName, StringUtils.getSiteSubTitle(currentSite));
            siteLoginIdView.setVisibility(View.VISIBLE);
        } else {
            siteLoginIdView.setVisibility(View.GONE);
            setMultTitle(userName, StringUtils.getSiteSubTitle(currentSite));
        }
        this.nickName = nickName;
        this.userName = userName;

        if (userName.equals(nickName) || nickName == "" || nickName == null) {
            siteNickNameView.setVisibility(View.GONE);
            this.nickName = userName;
        } else {
            siteNickNameTv.setText(nickName);
            siteNickNameView.setVisibility(View.VISIBLE);
            remarkNameTv.setText(userName);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SiteConnListActivity.REQUEST_CODE_SWITCH_SITE:
                if (resultCode == RESULT_CANCELED)
                    finish();
                else if (resultCode == RESULT_OK) {
                    ZalyTaskExecutor.executeUserTask(TAG, new QueryFriendTask());
                }
                break;
            case ChangeUsernameActivity.SITE_REMARK_NAME_CODE:
                if (data != null) {
                    String remarkName = data.getStringExtra(IntentKey.KEY_REMARK_NAME);
                    if (remarkName == null || remarkName.length() < 1) {
                        remarkName = "";
                    }
                    ZalyTaskExecutor.executeUserTask(TAG, new UpdateFriendRemark(remarkName, siteUserId));
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZalyTaskExecutor.cancleAllTasksByTag(TAG);
    }

    @Override
    public void hideProgress() {
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void showProgress() {
        swipeRefresh.setRefreshing(true);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            notificationSwitch.setOnCheckedChangeListener(null);
            notificationSwitch.setChecked(!checked);
            notificationSwitch.setOnCheckedChangeListener(this);
            Toaster.showInvalidate("请稍候重试");
        }
        switch (compoundButton.getId()) {
            case R.id.notification_switch:
                messageMute = compoundButton.isChecked();
                ZalyTaskExecutor.executeUserTask(TAG, new SetFriendSettingTask(messageMute));
                break;
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        AnimationUtil.scaleDown(avatar);
        super.onBackPressed();
    }

    public void updateNotificationSwitch() {
        // 自动更改switch状态。为了防止非人为的更改switch也调用onCheckedChange里面的方法，这里先反注册，更改后再注册回来。
        notificationSwitch.setOnCheckedChangeListener(null);
        notificationSwitch.setChecked(messageMute);
        notificationSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.nav_back_layout, R.id.action_share, R.id.avatar, R.id.item_add_friend, R.id.item_show_qrcode, R.id.item_show_pub_key, R.id.edit_remark_name, R.id.item_delete_friend, R.id.item_send_message})
    public void onViewClicked(View view) {
        boolean isNet = NetUtils.getNetInfo();
        switch (view.getId()) {
            case R.id.nav_back_layout:
                onBackPressed();
                break;
            case R.id.action_share:
                if (ClipboardUtils.copyToClipboard(
                        UrlUtils.buildShareLinkForUser(currentSite.getHostAndPort(), siteUserId)))
                    Toaster.show(R.string.share_copied);
                break;
            case R.id.avatar:
                Intent intentImage = new Intent(FriendProfileActivity.this, ImageShowActivity.class);
                intentImage.putExtra(IntentKey.KEY_USER_HEAD, profile.getUserPhoto());
                intentImage.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intentImage);
                break;
            case R.id.item_add_friend:
                if (!isNet) {
                    Toaster.showInvalidate("请稍候重试");
                    return;
                }
                Intent intentAddFriend = new Intent(this, ApplyAddFriendActivity.class);
                intentAddFriend.putExtra(IntentKey.KEY_USER_SITE_ID, profile.getSiteUserId());
                intentAddFriend.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intentAddFriend);
                break;
            case R.id.item_show_qrcode:
                Intent intentQR = new Intent(this, ShareQRCodeActivity.class);
                intentQR.putExtra(IntentKey.KEY_QR_CODE_TYPE, IntentKey.KEY_TYPE_USER);
                intentQR.putExtra(IntentKey.KEY_USER_NAME, profile.getUserName());
                intentQR.putExtra(IntentKey.KEY_USER_ID, profile.getSiteUserId());
                intentQR.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                intentQR.putExtra(IntentKey.KEY_USER_HEAD, profile.getUserPhoto());
                startActivity(intentQR);
                break;
            case R.id.item_show_pub_key:
                if (profile.getUserIdPubk() == null) {
                    Toaster.showInvalidate(getString(R.string.disabled_function));
                    return;
                }

                Intent intentShowPub = new Intent(this, FriendPubKeyActivity.class);
                intentShowPub.putExtra(FriendPubKeyActivity.KEY_USER_NAME, profile.getUserName());
                intentShowPub.putExtra(FriendPubKeyActivity.KEY_PUB_KEY, profile.getUserIdPubk());
                intentShowPub.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intentShowPub);
                break;
            case R.id.edit_remark_name:
                if (!isNet) {
                    Toaster.showInvalidate("请稍候再试");
                    return;
                }
                Intent remarkNameIntent = new Intent(getContext(), ChangeUsernameActivity.class);
                remarkNameIntent.putExtra(ChangeUsernameActivity.KEY_TYPE, ChangeUsernameActivity.SITE_REMARK_NAME);
                remarkNameIntent.putExtra(IntentKey.KEY_OLD_REMARK_NAME, userName);
                remarkNameIntent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivityForResult(remarkNameIntent, ChangeUsernameActivity.SITE_REMARK_NAME_CODE);
                break;
            case R.id.item_delete_friend:
                if (!isNet) {
                    Toaster.showInvalidate("请稍候重试");
                    return;
                }
                new MaterialDialog.Builder(getContext())
                        .content("确定删除好友？")
                        .positiveText("确定")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case NEUTRAL:
                                        dialog.dismiss();
                                        break;
                                    case NEGATIVE:
                                        dialog.dismiss();
                                        break;
                                    case POSITIVE:
                                        ZalyTaskExecutor.executeUserTask(TAG, new DeleteFriendTask());
                                        break;
                                }

                            }
                        })
                        .show();
                break;
            case R.id.item_send_message:
                Intent intent = new Intent(this, U2MessageActivity.class);
                intent.putExtra(IntentKey.KEY_FRIEND_SITE_USER_ID, profile.getSiteUserId());
                intent.putExtra(IntentKey.KEY_FRIEND_USER_NAME, profile.getUserName());
                intent.putExtra(IntentKey.KEY_FRIEND_PROFILE, profile.toByteArray());
                intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intent);
                break;
        }
    }

    /**
     * 删除好友
     */
    class DeleteFriendTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendDeleteProto.ApiFriendDeleteResponse> {

        @Override
        protected ApiFriendDeleteProto.ApiFriendDeleteResponse executeTask(Void... voids) throws Exception {
            UserProfilePresenter.getInstance(currentSite).deleteFriendBySiteUserId(siteUserId);
            return ApiClient.getInstance(currentSite).getFriendApi().deleteFriend(profile.getSiteUserId());
        }

        @Override
        protected void onTaskSuccess(ApiFriendDeleteProto.ApiFriendDeleteResponse apiFriendDeleteResponse) {
            super.onTaskSuccess(apiFriendDeleteResponse);
            finish();
            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_RELOAD, null));
        }
    }

    /**
     * 查询是否为好友关系
     */
    class QueryFriendTask extends ZalyTaskExecutor.Task<Void, Void, UserFriendBean> {

        public QueryFriendTask() {
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected UserFriendBean executeTask(Void... voids) throws Exception {
            return UserProfilePresenter.getInstance(currentSite).queryFriendBeanBySiteUserId(siteUserId);
        }

        @Override
        protected void onTaskSuccess(UserFriendBean userFriendBean) {
            super.onTaskSuccess(userFriendBean);
            if (userFriendBean == null) {
                ZalyTaskExecutor.executeUserTask(TAG, new FindUserTask(siteUserId));
                return;
            }
            profile = UserProto.UserProfile.newBuilder()
                    .setSiteUserId(userFriendBean.getSiteUserId())
                    .setUserName(userFriendBean.getUserName())
                    .setUserPhoto(userFriendBean.getUserImage())
                    .setSiteLoginId(userFriendBean.getSiteLoginId())
                    .setUserIdPubk(userFriendBean.getUserIdPubk())
                    .setSiteLoginId(userFriendBean.getSiteLoginId())
                    .build();

            siteUserId = profile.getSiteUserId();
            siteLoginId = profile.getSiteLoginId();

            fillData();

            boolean isNet = NetUtils.getNetInfo();

            ////本地数据先展示，然后使用服务端填充, 如果是用户自己，不需要请求设置，
            if (siteUserId.equals(currentSite.getSiteUserId())) {
                //TransitionManager.beginDelayedTransition(viewGroup);
                isUserSelfUI();
            } else {
                //是好友
                if (userFriendBean.getRelation() == 1) {
                    isFriendUI();
                } else {
                    isNotFriendUI();
                }
                messageMute = userFriendBean.isMute();
                updateNotificationSwitch();
                if (isNet) {
                    ZalyTaskExecutor.executeUserTask(TAG, new FindUserTask(siteUserId));
                }
            }
            ///// protocol version < 5 备注，设置siteLoginId不可见
            judgeShowLoginInName();
            judgeShowRemarkName();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

    public void isUserSelfUI() {
        addFriendLayout.setVisibility(View.GONE);
        notificationSwitchItem.setVisibility(View.GONE);
        deleteFriend.setVisibility(View.GONE);
        sendMessage.setVisibility(View.GONE);
        remarkNameView.setVisibility(View.GONE);
    }

    public void isNotFriendUI() {
        notificationSwitchItem.setVisibility(View.GONE);
        if (siteUserId.equals(currentSite.getSiteUserId())) {
            addFriendLayout.setVisibility(View.GONE);
        } else {
            addFriendLayout.setVisibility(View.VISIBLE);
        }
        deleteFriend.setVisibility(View.GONE);
        sendMessage.setVisibility(View.GONE);
        remarkNameView.setVisibility(View.GONE);
    }

    public void isFriendUI() {
        addFriendLayout.setVisibility(View.GONE);
        notificationSwitchItem.setVisibility(View.VISIBLE);
        deleteFriend.setVisibility(View.VISIBLE);
        sendMessage.setVisibility(View.VISIBLE);
        remarkNameView.setVisibility(View.VISIBLE);
    }

    public void judgeShowRemarkName() {
        if (currentSite.getProtocolVersion() < SiteConfig.remark_name_minProtocol) {
            remarkNameView.setVisibility(View.GONE);
        }
    }

    public void judgeShowLoginInName() {
        if (currentSite.getProtocolVersion() < SiteConfig.site_login_id_minProtocol) {
            siteLoginIdView.setVisibility(View.GONE);
        }
    }

    /**
     * 非好友，查询好友信息
     */
    class FindUserTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendProfileProto.ApiFriendProfileResponse> {

        private String siteUserId;
        private final String TAG = FindUserTask.class.getSimpleName();

        public FindUserTask(String siteUserId) {
            this.siteUserId = siteUserId;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected ApiFriendProfileProto.ApiFriendProfileResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().findUser(siteUserId);
        }

        @Override
        protected void onTaskSuccess(ApiFriendProfileProto.ApiFriendProfileResponse response) {
            super.onTaskSuccess(response);
            profile = response.getProfile();
            if (profile.getUserIdPubk() == null || profile.getUserIdPubk().length() < 1) {
                String userPubKey = response.getUserIdPubk();
                profile = UserProto.UserProfile.newBuilder()
                        .setUserName(profile.getUserName())
                        .setSiteUserId(profile.getSiteUserId())
                        .setNickName(profile.getNickName())
                        .setSiteLoginId(profile.getSiteLoginId())
                        .setUserPhoto(profile.getUserPhoto())
                        .setUserIdPubk(userPubKey)
                        .build();
            }
            fillData();
            checkoutLoginId(profile.getSiteLoginId(), profile.getUserName(), profile.getNickName());

            Integer userRelation = response.getRelationValue();
            ZalyApplication.setUserInfo(siteUserId, profile.getUserPhoto(), profile.getUserName());
            if (userRelation == 1) {
                ////表示是好友
                UserFriendBean userFriendBean = new UserFriendBean();
                userFriendBean.setSiteUserId(siteUserId);
                userFriendBean.setUserName(profile.getUserName());
                userFriendBean.setUserImage(profile.getUserPhoto());
                userFriendBean.setRelation(response.getRelationValue());
                userFriendBean.setUserIdPubk(profile.getUserIdPubk());
                UserProfilePresenter.getInstance(currentSite).updateSiteUserProfile(userFriendBean);
                ZalyTaskExecutor.executeUserTask(TAG, new GetFriendSettingTask());
                isFriendUI();
            } else {
                ////表示是不是好友
                UserProfilePresenter.getInstance(currentSite).insertStrangerFriend(profile);
                isNotFriendUI();
            }

            judgeShowLoginInName();
            judgeShowRemarkName();

        }

        @Override
        protected void onTaskError(Exception e) {
            Intent intent = new Intent(FriendProfileActivity.this, SiteConnListActivity.class);
            intent.putExtra(IntentKey.KEY_MODE, IntentKey.MODE_FOR_RESULT);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
            startActivityForResult(intent, SiteConnListActivity.REQUEST_CODE_SWITCH_SITE);
        }

        @Override
        protected void onAPIError(ZalyAPIException apiException) {
            Intent intent = new Intent(FriendProfileActivity.this, SiteConnListActivity.class);
            intent.putExtra(IntentKey.KEY_MODE, IntentKey.MODE_FOR_RESULT);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
            startActivityForResult(intent, SiteConnListActivity.REQUEST_CODE_SWITCH_SITE);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

    class GetFriendSettingTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendSettingProto.ApiFriendSettingResponse> {
        private final String TAG = GetFriendSettingTask.class.getSimpleName();

        @Override
        protected ApiFriendSettingProto.ApiFriendSettingResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().getFriendSetting(siteUserId);
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }

        @Override
        protected void onTaskSuccess(ApiFriendSettingProto.ApiFriendSettingResponse apiFriendSettingResponse) {
            super.onTaskSuccess(apiFriendSettingResponse);
            messageMute = apiFriendSettingResponse.getMessageMute();
            /////设置静音，更新profile,
            UserProfilePresenter.getInstance(currentSite).setFriendIsMuteBySiteUserId(siteUserId, messageMute);
            updateNotificationSwitch();
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
        }
    }

    ////设置
    class SetFriendSettingTask extends ZalyTaskExecutor.Task<Void, Void, ApiFriendUpdateSettingProto.ApiFriendUpdateSettingResponse> {

        boolean mute;
        private final String TAG = SetFriendSettingTask.class.getSimpleName();

        public SetFriendSettingTask(boolean mute) {
            this.mute = mute;
        }

        @Override
        protected ApiFriendUpdateSettingProto.ApiFriendUpdateSettingResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().updateFriendSetting(profile.getSiteUserId(), mute);
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }

        @Override
        protected void onTaskSuccess(ApiFriendUpdateSettingProto.ApiFriendUpdateSettingResponse response) {
            super.onTaskSuccess(response);
            UserProfilePresenter.getInstance(currentSite).setFriendIsMuteBySiteUserId(siteUserId, mute);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            messageMute = !mute;
            updateNotificationSwitch();
        }
    }

    /**
     * 上传用户资料
     */
    class UpdateFriendRemark extends ZalyTaskExecutor.Task<Void, Void, ApiFriendRemarkProto.ApiFriendRemarkResponse> {

        private String remarkName;
        private String siteFriendId;


        public UpdateFriendRemark(String remarkName, String siteFriendId) {
            this.remarkName = remarkName;
            this.siteFriendId = siteFriendId;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        @Override
        protected ApiFriendRemarkProto.ApiFriendRemarkResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(currentSite).getFriendApi().updateRemarkName(siteFriendId, remarkName);
        }

        @Override
        protected void onTaskSuccess(ApiFriendRemarkProto.ApiFriendRemarkResponse remarkResponse) {
            super.onTaskSuccess(remarkResponse);
            UserProfilePresenter.getInstance(currentSite).updateRemarkName(remarkName, siteFriendId);
            checkoutLoginId(siteLoginId, remarkName, nickName);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            ZalyLogUtils.getInstance().apiError(TAG, zalyAPIException);
        }
    }
}