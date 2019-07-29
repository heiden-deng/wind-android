package com.akaxin.client.group.view.impl;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.activitys.ShareQRCodeActivity;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.GroupEvent;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.friend.FriendProfileActivity;
import com.akaxin.client.group.ChangeGroupNameActivity;
import com.akaxin.client.group.GroupAddMemberActivity;
import com.akaxin.client.group.GroupDeleteMemberActivity;
import com.akaxin.client.group.GroupMemberActivity;
import com.akaxin.client.group.presenter.IGroupProfilePresenter;
import com.akaxin.client.group.presenter.impl.GroupProfilePresenter;
import com.akaxin.client.group.view.IGroupProfileView;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.AnimationUtil;
import com.akaxin.client.util.ClipboardUtils;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.UrlUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.GroupProto;
import com.akaxin.proto.core.UserProto;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yichao on 2017/10/25.
 */

public class GroupProfileActivity extends BaseActivity
        implements IGroupProfileView, CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.empty_view)
    View emptyView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.item_group_name)
    View groupNameItem;
    @BindView(R.id.group_name)
    TextView groupNameTv;
    @BindView(R.id.item_group_owner)
    View groupOwnerItem;
    @BindView(R.id.group_owner_name)
    TextView groupOwnerNameTv;
    @BindView(R.id.item_group_members)
    View groupMembersItem;
    @BindView(R.id.num_group_members)
    TextView numGroupMembersTv;
    @BindView(R.id.item_add_member)
    View addMemberItem;
    @BindView(R.id.layout_manage_group)
    View groupManageLayout;
    @BindView(R.id.layout_quit_group)
    View quitGroupLayout;
    @BindView(R.id.item_quit_group)
    View quitGroupItem;
    @BindView(R.id.quit_group_text)
    TextView quitGroupTv;
    @BindView(R.id.item_remove_member)
    View removeMemberItem;
    @BindView(R.id.layout_notification_switch)
    View notificationSwitchLayout;
    @BindView(R.id.item_notification_switch)
    View notificationSwitchItem;
    @BindView(R.id.notification_switch)
    SwitchCompat notificationSwitch;
    @BindView(R.id.only_owner_invite_switch)
    SwitchCompat onlyOwnerInviteSwitch;

    @BindView(R.id.item_share_qr_code)
    LinearLayout share_qr_code;

    private IGroupProfilePresenter iPresenter;

    private AlertDialog dialog;
    private Site currentSite;
    String groupId, groupName;

    @Override
    public int getResLayout() {
        return R.layout.activity_group_profile;
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void initView() {
        ButterKnife.bind(this);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        swipeRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefresh.setEnabled(false);
        avatar.setVisibility(View.VISIBLE);
    }

    public void initEvent() {
        notificationSwitch.setOnCheckedChangeListener(this);
        onlyOwnerInviteSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void initPresenter() {
        iPresenter = new GroupProfilePresenter(this, currentSite);
    }

    public void onLoadData() {

        setMultTitle("", StringUtils.getSiteSubTitle(currentSite));

        groupId = getIntent().getStringExtra(IntentKey.KEY_GROUP_ID);
        groupName = getIntent().getStringExtra(IntentKey.KEY_GROUP_NAME);
        iPresenter.setGroupId(groupId);
        // TODO: isGroupMember 这个值不应该靠传递过来, 应该在此处(通过本地库/网络)判断我是不是群成员.，这个值就是当时从本地取出来的
        boolean isGroupMember = getIntent().getBooleanExtra(IntentKey.KEY_IS_GROUP_MEMBER, false);
        iPresenter.setMyPowerOnStart(isGroupMember);
        avatar.setImageDrawable(getResources().getDrawable(R.drawable.avatar_group_default));
        groupNameTv.setText(groupName);
        initLayout();
        iPresenter.getGroupProfile();
        iPresenter.getGroupSetting();
    }

    @Override
    public void onGroupIdError() {
        Toaster.showInvalidate(getResources().getString(R.string.data_error));
        finish();
    }

    @Override
    public void onGroupNotExist() {
        swipeRefresh.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStartDeleteGroup() {
        showProgress();
    }

    @Override
    public void onDeleteGroupSucceed() {
        hideProgress();
        Toaster.show("解散群成功");
        Bundle bundle = new Bundle();
        bundle.putString(GroupEvent.KEY_GROUP_ID, iPresenter.getGroupId());
        EventBus.getDefault().post(new GroupEvent(GroupEvent.ACTION_DEL_GROUP, bundle));
        finish();
    }

    @Override
    public void onDeleteGroupFail() {
        Toaster.showInvalidate("解散群失败");
        hideProgress();
    }

    @Override
    public void onStartQuitGroup() {
        showProgress();
    }

    @Override
    public void onQuitGroupSucceed() {
        hideProgress();
        Toaster.showInvalidate("退出成功");
        Bundle bundle = new Bundle();
        bundle.putString(GroupEvent.KEY_GROUP_ID, iPresenter.getGroupId());
        EventBus.getDefault().post(new GroupEvent(GroupEvent.ACTION_QUIT_GROUP, bundle));
        finish();
    }

    @Override
    public void onQuitGroupFail() {
        Toaster.showInvalidate("退出失败");
        hideProgress();
    }

    @Override
    public void onStartGetGroupProfile() {
        showProgress();
    }

    @Override
    public void onGetGroupProfileSucceed(GroupProto.GroupProfile groupProfile, UserProto.UserProfile ownerProfile, int myPower, boolean inviteGroupChatBanned, int numGroupMembers) {
        hideProgress();
        setMultTitle(groupProfile.getName(), StringUtils.getSiteSubTitle(currentSite));
        groupNameTv.setText(groupProfile.getName());
        groupName = groupProfile.getName();
        numGroupMembersTv.setText(String.format(getString(R.string.num_group_members), numGroupMembers));

        groupOwnerNameTv.setText(ownerProfile.getUserName());
        initLayout();
        if (myPower == GroupProfilePresenter.POWER_OWNER) {
            updateInviteGroupChatSwitch(inviteGroupChatBanned);
            showAddMemberItem(true);
        } else {
            showAddMemberItem(!inviteGroupChatBanned);
        }

    }

    @Override
    public void onGetGroupProfileFail() {
        hideProgress();
        Toaster.show("获取群资料失败");
    }

    @Override
    public void onStartGetGroupSetting() {
        showProgress();
    }

    @Override
    public void onGetGroupSettingSucceed(boolean messageMute) {
        hideProgress();
        updateNotificationSwitch(messageMute);
    }

    @Override
    public void onGetGroupSettingFail() {
        hideProgress();
    }

    @Override
    public void onStartCloseInvite() {
        showProgress();
    }

    @Override
    public void onCloseInviteSucceed() {
        hideProgress();
    }

    @Override
    public void onCloseInviteFail() {
        hideProgress();
    }

    @Override
    public void onStartSetGroupSetting() {
        showProgress();
    }

    @Override
    public void onSetGroupSettingSucceed() {
        hideProgress();
    }

    @Override
    public void onSetGroupSettingFail(boolean mute) {
        hideProgress();
        updateNotificationSwitch(mute);
    }

    @Override
    public void onStartChangeGroupName() {
        showProgress();
    }

    @Override
    public void onChangeGroupNameSucceed(String name) {
        hideProgress();
        groupNameTv.setText(name);
        groupName = name;
        setMultTitle(name, StringUtils.getSiteSubTitle(currentSite));
    }

    @Override
    public void onChangeGroupNameFail() {
        hideProgress();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGroupEvent(GroupEvent event) {
        switch (event.getAction()) {
            case GroupEvent.ACTION_DEL_GROUP:
                Bundle bundle = event.getData();
                if (bundle != null) {
                    String delGroupId = bundle.getString(GroupEvent.KEY_GROUP_ID);
                    if (!StringUtils.isEmpty(delGroupId) && !StringUtils.isEmpty(iPresenter.getGroupId()) &&
                            delGroupId.equals(iPresenter.getGroupId())) {
                        finish();
                    }
                }
                break;
        }
    }

    public void updateNotificationSwitch(boolean messageMute) {
        // 自动更改switch状态。为了防止非人为的更改switch也调用onCheckedChange里面的方法，这里先反注册，更改后再注册回来。
        notificationSwitch.setOnCheckedChangeListener(null);
        notificationSwitch.setChecked(messageMute);
        notificationSwitch.setOnCheckedChangeListener(this);
    }

    public void updateInviteGroupChatSwitch(boolean inviteGroupChatBanned) {
        // 只在群主的页面才有用
        onlyOwnerInviteSwitch.setOnCheckedChangeListener(null);
        onlyOwnerInviteSwitch.setChecked(inviteGroupChatBanned);
        onlyOwnerInviteSwitch.setOnCheckedChangeListener(this);
    }

    public void showAddMemberItem(boolean show) {
        // TransitionManager.beginDelayedTransition(viewGroup);
        addMemberItem.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    /**
     * 根据当前用户的身份 myPower (管理员, 普通成员, 非成员)来展示界面.
     */
    public void initLayout() {
        int myPower = iPresenter.getMyPower();
        switch (myPower) {
            case GroupProfilePresenter.POWER_NON_MEMBER:
                //  TransitionManager.beginDelayedTransition(viewGroup);
                groupNameItem.setVisibility(View.GONE);
                addMemberItem.setVisibility(View.GONE);
                groupManageLayout.setVisibility(View.GONE);
                notificationSwitchLayout.setVisibility(View.GONE);
                quitGroupLayout.setVisibility(View.GONE);
                break;
            case GroupProfilePresenter.POWER_ORDINARY_MEMBER:
                //TransitionManager.beginDelayedTransition(viewGroup);
                groupNameItem.setVisibility(View.GONE);
                addMemberItem.setVisibility(View.GONE);
                groupManageLayout.setVisibility(View.GONE);
                notificationSwitchLayout.setVisibility(View.VISIBLE);
                quitGroupLayout.setVisibility(View.VISIBLE);
                quitGroupTv.setText(R.string.quit_group);
                break;
            case GroupProfilePresenter.POWER_OWNER:
                //  TransitionManager.beginDelayedTransition(viewGroup);
                groupNameItem.setVisibility(View.VISIBLE);
                addMemberItem.setVisibility(View.VISIBLE);
                groupManageLayout.setVisibility(View.VISIBLE);
                notificationSwitchLayout.setVisibility(View.VISIBLE);
                quitGroupLayout.setVisibility(View.VISIBLE);
                quitGroupTv.setText(R.string.dissolve_group);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ChangeGroupNameActivity.REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    if (!StringUtils.isEmpty(data.getStringExtra(ChangeGroupNameActivity.KEY_GROUP_NAME))) {
                        String newGroupName = data.getStringExtra(ChangeGroupNameActivity.KEY_GROUP_NAME);
                        iPresenter.setGroupName(newGroupName);
                    }
                }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        boolean isNet = NetUtils.getNetInfo();

        switch (compoundButton.getId()) {
            case R.id.notification_switch:
                if (!isNet) {
                    notificationSwitch.setOnCheckedChangeListener(null);
                    notificationSwitch.setChecked(!isChecked);
                    notificationSwitch.setOnCheckedChangeListener(this);
                    Toaster.showInvalidate("请稍候重试");
                    return;
                }
                boolean messageMute = compoundButton.isChecked();
                iPresenter.setGroupMessageMute(messageMute);
                break;
            case R.id.only_owner_invite_switch:
                if (!isNet) {
                    onlyOwnerInviteSwitch.setOnCheckedChangeListener(null);
                    onlyOwnerInviteSwitch.setChecked(!isChecked);
                    onlyOwnerInviteSwitch.setOnCheckedChangeListener(this);
                    Toaster.showInvalidate("请稍候重试");
                    return;
                }
                if (iPresenter.isOwner()) {
                    boolean inviteGroupChatBanned = compoundButton.isChecked();
                    iPresenter.setCloseInviteGroupChat(inviteGroupChatBanned);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZalyTaskExecutor.cancleAllTasksByTag(TAG);
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

    @Override
    public void showProgress() {
        swipeRefresh.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        swipeRefresh.setRefreshing(false);
    }


    @OnClick({R.id.nav_back_layout, R.id.action_share, R.id.item_group_name, R.id.item_group_owner,
            R.id.item_group_members, R.id.item_add_member, R.id.item_remove_member,
            R.id.item_quit_group, R.id.item_share_qr_code})
    public void onViewClicked(View view) {
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet && view.getId() != R.id.nav_back_layout) {
            Toaster.showInvalidate("请稍候再试");
            return;
        }
        switch (view.getId()) {
            case R.id.nav_back_layout:
                onBackPressed();
                break;
            case R.id.action_share:
                if (ClipboardUtils.copyToClipboard(
                        UrlUtils.buildShareLinkForGroup(
                                currentSite.getSiteAddress(), iPresenter.getGroupId())))
                    Toaster.show(R.string.share_copied);
                break;
            case R.id.item_group_name:
                Intent intent = new Intent(this, ChangeGroupNameActivity.class);
                intent.putExtra(ChangeGroupNameActivity.KEY_OLD_NAME, groupNameTv.getText().toString());
                startActivityForResult(intent, ChangeGroupNameActivity.REQUEST_CODE);
                break;
            case R.id.item_group_owner:
                Intent intentGroup = new Intent(this, FriendProfileActivity.class);
                intentGroup.putExtra(IntentKey.KEY_PROFILE_MODE, FriendProfileActivity.MODE_FRIEND_SITE_ID);
                intentGroup.putExtra(IntentKey.KEY_FRIEND_SITE_ID, iPresenter.getOwnerId());
                intentGroup.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intentGroup);
                break;
            case R.id.item_group_members:
                Intent intentGroupMembers = new Intent(this, GroupMemberActivity.class);
                intentGroupMembers.putExtra(IntentKey.KEY_GROUP_ID, iPresenter.getGroupId());
                intentGroupMembers.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intentGroupMembers);
                break;
            case R.id.item_add_member:
                Intent intentAddNumber = new Intent(this, GroupAddMemberActivity.class);
                intentAddNumber.putExtra(IntentKey.KEY_GROUP_ID, iPresenter.getGroupId());
                intentAddNumber.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(intentAddNumber);
                break;
            case R.id.item_remove_member:
                Intent groupDeletMemberIntent = new Intent(this, GroupDeleteMemberActivity.class);
                groupDeletMemberIntent.putExtra(IntentKey.KEY_GROUP_ID, iPresenter.getGroupId());
                groupDeletMemberIntent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(groupDeletMemberIntent);
                break;
            case R.id.item_quit_group:
                if (iPresenter.getMyPower() == GroupProfilePresenter.POWER_ORDINARY_MEMBER) {
                    if (dialog == null) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                        alertDialogBuilder.setMessage(getResources().getString(R.string.quit_group_confirmation));
                        alertDialogBuilder.setPositiveButton(getResources().getText(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        iPresenter.quitGroup();
                                    }
                                });
                        alertDialogBuilder.setNegativeButton(getResources().getText(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (GroupProfileActivity.this.dialog != null) {
                                    GroupProfileActivity.this.dialog.dismiss();
                                }
                            }
                        });
                        dialog = alertDialogBuilder.create();
                    }
                    dialog.show();
                } else if (iPresenter.getMyPower() == GroupProfilePresenter.POWER_OWNER) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setMessage(getResources().getString(R.string.dissolve_group_confirmation));
                    alertDialogBuilder.setPositiveButton(getResources().getText(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    iPresenter.deleteGroup();
                                }
                            });
                    alertDialogBuilder.setNegativeButton(getResources().getText(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (GroupProfileActivity.this.dialog != null) {
                                GroupProfileActivity.this.dialog.dismiss();
                            }
                        }
                    });
                    dialog = alertDialogBuilder.create();
                    dialog.show();
                }
                break;
            case R.id.item_share_qr_code:
                Intent shareQrCode = new Intent(this, ShareQRCodeActivity.class);
                shareQrCode.putExtra(IntentKey.KEY_QR_CODE_TYPE,IntentKey.KEY_TYPE_GROUP);
                shareQrCode.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                shareQrCode.putExtra(IntentKey.KEY_GROUP_ID, groupId);
                shareQrCode.putExtra(IntentKey.KEY_GROUP_NAME,groupName);
                startActivity(shareQrCode);
                break;
        }
    }
}
