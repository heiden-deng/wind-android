package com.akaxin.client.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.activitys.ImageShowActivity;
import com.akaxin.client.activitys.ShareQRCodeActivity;
import com.akaxin.client.activitys.SiteManageActivity;
import com.akaxin.client.adapter.PersonalDialogAdapter;
import com.akaxin.client.bean.PersonalItem;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.AppEvent;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.db.bean.UserFriendBean;
import com.akaxin.client.db.dao.AkxCommonDao;
import com.akaxin.client.friend.presenter.impl.UserProfilePresenter;
import com.akaxin.client.mvp.MVPBaseFragment;
import com.akaxin.client.mvp.contract.PersonalContract;
import com.akaxin.client.mvp.presenter.PersonalPresenter;
import com.akaxin.client.personal.BindPhoneActivity;
import com.akaxin.client.personal.ChangeUsernameActivity;
import com.akaxin.client.personal.DeviceManageActivity;
import com.akaxin.client.personal.PersonalKeyActivity;
import com.akaxin.client.site.SetPlatformIpActivity;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.ClipboardUtils;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.UrlUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.file.ImageUtils;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.client.view.RoundImageView;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.platform.ApiUserPhoneProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.beta.UpgradeInfo;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.iwf.photopicker.PhotoPicker;

/**
 * Created by yichao on 2017/10/9.
 */

public class PersonalFragment extends MVPBaseFragment<PersonalContract.View, PersonalPresenter> implements PersonalContract.View, MaterialDialog.SingleButtonCallback {

    public static final String KEY_SPACE_KEY = "key_space_key";

    @BindView(R.id.item_bind_phone_num)
    View bindPhoneNumItem;
    @BindView(R.id.phone_num)
    TextView phoneNum;
    @BindView(R.id.item_manage_devices)
    View manageDevicesItem;
    @BindView(R.id.item_manage_site)
    View manageSitesItem;
    @BindView(R.id.item_delete_identity)
    View deleteIdentityItem;
    @BindView(R.id.item_set_site_ip)
    View setSiteIpItem;
    @BindView(R.id.item_show_self_qrcode)
    View showSelfQrCodeItem;
    @BindView(R.id.item_show_self_keys)
    View showSelfKeyItem;
    @BindView(R.id.scroll_view)
    ScrollView scrollView;
    @BindView(R.id.layout_me)
    View meLayout;
    @BindView(R.id.avatar)
    ImageView avatar;
    @BindView(R.id.user_name)
    TextView userNameTv;
    @BindView(R.id.site_address)
    TextView siteAddressTv;
    @BindView(R.id.action_share)
    View actionShare;
    @BindView(R.id.site_login_id)
    TextView siteLoginIdTv;
    @BindView(R.id.siteIcon3)
    RoundImageView siteIcon3;
    @BindView(R.id.siteIcon2)
    RoundImageView siteIcon2;
    @BindView(R.id.siteIcon1)
    RoundImageView siteIcon1;
    @BindView(R.id.check_updata_bubble)
    ImageView checkUpdataBubble;
    @BindView(R.id.check_updata_version_name)
    TextView checkUpdataVersionName;
    @BindView(R.id.item_check_updata)
    LinearLayout itemCheckUpdata;
    @BindView(R.id.site_login_id_bubble)
    ImageView siteLoginIdBubble;
    private String username;
    private String userImgId;
    private String siteLoginId = "";

    private ImageView[] siteIcons;

    public Site currentSite;

    private PersonalDialogAdapter personalDialogAdapter;
    List<PersonalItem> items = new ArrayList<>();
    Unbinder unbinder;

    /**
     * 传入需要的参数，设置给arguments
     *
     * @param site
     * @return
     */
    public static PersonalFragment getObject(Site site) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, site);
        PersonalFragment personalFragment = new PersonalFragment();
        personalFragment.setArguments(bundle);
        return personalFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment_personal, container, false);
        unbinder = ButterKnife.bind(this, view);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentSite = bundle.getParcelable(IntentKey.KEY_CURRENT_SITE);
        }
        siteIcons = new ImageView[3];
        siteIcons[0] = (ImageView) view.findViewById(R.id.siteIcon3);
        siteIcons[1] = (ImageView) view.findViewById(R.id.siteIcon2);
        siteIcons[2] = (ImageView) view.findViewById(R.id.siteIcon1);
        loadVersion();

        return view;
    }


    private void loadVersion() {
        UpgradeInfo upgradeInfo = Beta.getUpgradeInfo();
        if (upgradeInfo == null) {
            checkUpdataBubble.setVisibility(View.GONE);
            checkUpdataVersionName.setText(AppUtils.getAppVersionName());
            return;
        }

        if (upgradeInfo.versionCode > AppUtils.getAppVersionCode()) {
            checkUpdataVersionName.setText("新版本:" + upgradeInfo.versionName);
            checkUpdataBubble.setVisibility(View.VISIBLE);
        } else {
            checkUpdataBubble.setVisibility(View.GONE);
            checkUpdataVersionName.setText(AppUtils.getAppVersionName());
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        Bundle bundle = getArguments();
        if (bundle != null) {
            currentSite = bundle.getParcelable(IntentKey.KEY_CURRENT_SITE);
        }

        if (scrollView != null) {
            scrollView.scrollTo(0, 0);
        }
        String phoneId = StringUtils.hidePhoneNumber(ZalyApplication.getCfgSP().getKey(Configs.PHONE_ID));
        if (!StringUtils.isEmpty(phoneId)) {
            phoneNum.setText(phoneId);
        } else {
            mPresenter.getUserPhone();
        }
        if (currentSite == null) {
            meLayout.setVisibility(View.GONE);
            manageSitesItem.setVisibility(View.GONE);
            manageDevicesItem.setVisibility(View.GONE);
            return;
        }

        meLayout.setVisibility(View.VISIBLE);
        registerForContextMenu(meLayout);
        fillUserData();
        fillSiteIcon();
    }

    private void fillUserData() {
        username = currentSite.getSiteUserName();
        userImgId = currentSite.getSiteUserImage();
        siteLoginId = currentSite.getSiteLoginId();

        avatar.postDelayed(new Runnable() {
            @Override
            public void run() {
                selectUserInfoFromDB();
            }
        }, 5);

    }

    public void selectUserInfoFromDB() {
        Site userInfo = SitePresenter.getInstance().getSiteUser(currentSite.getSiteAddress());
        if (userInfo != null) {
            if (!StringUtils.isEmpty(userInfo.getSiteUserImage())) {
                userImgId = userInfo.getSiteUserImage();
            }
            username = userInfo.getSiteUserName();
            siteLoginId = userInfo.getSiteLoginId();
            currentSite.setSiteUserName(username);
            currentSite.setSiteUserImage(userImgId);
            currentSite.setSiteLoginId(siteLoginId);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setUserInfo();
                }
            });

        }
    }

    private void setUserInfo() {
        userNameTv.setText(username);
        if (siteLoginId != null && siteLoginId.length() > 0 && currentSite.getProtocolVersion() >= SiteConfig.site_login_id_minProtocol) {
            siteLoginIdTv.setText("站点账号:" + siteLoginId);
            siteLoginIdTv.setVisibility(View.VISIBLE);
            siteLoginIdBubble.setVisibility(View.GONE);
        } else {
            siteLoginIdTv.setText("");
            siteLoginIdTv.setVisibility(View.GONE);
            siteLoginIdBubble.setVisibility(View.VISIBLE);
        }
        Log.i("lipengfei", userImgId + "  user");
        siteAddressTv.setText(currentSite.getSiteDisplayAddress());
        new ImageUtils(getActivity(), currentSite).loadImage(userImgId, avatar);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(userImgId)
//                .setFileType(FileProto.FileType.USER_PORTRAIT)
//                .setSite(currentSite)
//                .build();
//        GlideApp.with(getActivity())
//                .load(model)
//                .placeholder(R.color.placeholder).circleCrop()
//                .into(new SimpleTarget<Drawable>() {
//                    @Override
//                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
//                        avatar.setImageDrawable(resource);
//                    }
//                });
    }

    private void fillSiteIcon() {
        for (ImageView iv : siteIcons) {
            iv.setVisibility(View.GONE);
        }
        List<Site> sites = AkxCommonDao.getInstance().queryAllSite(false);
        for (int i = 0; i < sites.size(); i++) {
            if (i > 2)
                break;
            Site site = sites.get(i);
            siteIcons[i].setVisibility(View.VISIBLE);
            new ImageUtils(getActivity(), currentSite).loadSiteImage(ZalyApplication.siteList.get(i).getSiteIcon(), siteIcons[i], ZalyApplication.siteList.get(i));

//            ZalyGlideModel model = new ZalyGlideModel.Builder()
//                    .setImageID(site.getSiteIcon())
//                    .setFileType(FileProto.FileType.SITE_ICON)
//                    .setSite(site)
//                    .build();
//            GlideApp.with(getActivity()).load(model).
//                    into(siteIcons[i]);
        }

    }


    @OnClick({R.id.action_share, R.id.layout_me, R.id.item_show_self_qrcode, R.id.item_show_self_keys, R.id.item_bind_phone_num, R.id.item_manage_site, R.id.item_check_updata, R.id.item_manage_devices, R.id.item_delete_identity, R.id.item_set_site_ip})
    public void onViewClicked(View view) {
        boolean isNet = NetUtils.getNetInfo();
        switch (view.getId()) {
            case R.id.action_share:
                if (ClipboardUtils.copyToClipboard(
                        UrlUtils.buildShareLinkForUser(
                                currentSite.getSiteAddress(), currentSite.getSiteUserId())))
                    Toaster.show(R.string.share_copied);
                break;
            case R.id.layout_me:
                initListDialogData();
                showListDialog();
                break;
            case R.id.item_show_self_qrcode:

                Intent intentQR = new Intent(getActivity(), ShareQRCodeActivity.class);
                intentQR.putExtra(IntentKey.KEY_QR_CODE_TYPE, IntentKey.KEY_TYPE_USER);
                intentQR.putExtra(IntentKey.KEY_USER_NAME, username);
                intentQR.putExtra(IntentKey.KEY_USER_ID, currentSite.getSiteUserId());
                intentQR.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                intentQR.putExtra(IntentKey.KEY_USER_HEAD, userImgId);
                getActivity().startActivity(intentQR);
                break;
            case R.id.item_show_self_keys:
                startActivity(new Intent(getActivity(), PersonalKeyActivity.class));
                break;
            case R.id.item_bind_phone_num:
                Intent bindPhoneIntent = new Intent(getActivity(), BindPhoneActivity.class);
                bindPhoneIntent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(bindPhoneIntent);
                break;
            case R.id.item_manage_site:
                startActivity(new Intent(getActivity(), SiteManageActivity.class));
                break;
            case R.id.item_check_updata:
                Beta.checkUpgrade();
                break;
            case R.id.item_manage_devices:
                Intent deviceIntent = new Intent(getActivity(), DeviceManageActivity.class);
                deviceIntent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                startActivity(deviceIntent);
                break;
            case R.id.item_delete_identity:
                if (!isNet) {
                    Toaster.showInvalidate("请稍候再试");
                } else {
                    showDialog("此操作将删除本机的账户凭证、消息等数据", getString(R.string.yes), getString(R.string.no), new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.POSITIVE)
                                mPresenter.logoutPlatform();
                            else
                                dialog.dismiss();
                        }
                    });
                }
                break;
            case R.id.item_set_site_ip:
                startActivity(new Intent(getActivity(), SetPlatformIpActivity.class));
                break;
        }
    }

    private void initListDialogData() {
        items.clear();
        items.add(new PersonalItem(getString(R.string.show_large_avatar), false, PersonalItem.ITEM_SHOW_IMAGE_LARGE));
        items.add(new PersonalItem(getString(R.string.set_site_login_id), false, PersonalItem.ITEM_SET_SITE_ACCOUNT));
        items.add(new PersonalItem(getString(R.string.change_avatar), false, PersonalItem.ITEM_CHANGE_USER_HEAD));
        items.add(new PersonalItem(getString(R.string.change_username), false, PersonalItem.ITEM_CHANGE_USER_NICK_NAME));
        Site site = SitePresenter.getInstance().getSiteUser(currentSite.getSiteAddress());
        siteLoginId = site.getSiteLoginId();
        if (siteLoginId != null && siteLoginId.length() > 0 && currentSite.getProtocolVersion() >= SiteConfig.site_login_id_minProtocol) {
            items.get(1).setShowTip(false);
        } else {
            items.get(1).setShowTip(true);
        }
        if (siteLoginId != null && siteLoginId.length() > 0) {
            items.remove(1);
        }
        if (site.getProtocolVersion() < SiteConfig.site_login_id_minProtocol) {
            items.remove(1);
        }
    }

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
                Intent siteLoginIdIntent = new Intent(getContext(), ChangeUsernameActivity.class);
                siteLoginIdIntent.putExtra(ChangeUsernameActivity.KEY_TYPE, ChangeUsernameActivity.SITE_LOGIN_ID);
                if (userImgId == null) {
                    selectUserInfoFromDB();
                }
                siteLoginIdIntent.putExtra(IntentKey.KEY_USER_IMAGE_ID, userImgId);
                startActivityForResult(siteLoginIdIntent, ChangeUsernameActivity.SITE_LOGIN_ID_CODE);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContactEvent(AppEvent event) {
        switch (event.getAction()) {
            case AppEvent.ACTION_SWITCH_SITE:
                currentSite = event.getData().getParcelable(IntentKey.KEY_CURRENT_SITE);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    private void startPhotoPicker() {
        PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE).rationale(new PermissionUtils.OnRationaleListener() {
            @Override
            public void rationale(final ShouldRequest shouldRequest) {
                new MaterialDialog.Builder(getActivity())
                        .content("使用该功能将获取读取文件的权限")
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    shouldRequest.again(true);
                                } else {
//                                    shouldRequest.again(false);
                                }
                                dialog.dismiss();
                            }
                        }).cancelable(false)
                        .show();
            }
        }).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
                PhotoPicker.PhotoPickerBuilder builder = PhotoPicker.builder()
                        .setPhotoCount(1)
                        .setShowCamera(false)
                        .setShowGif(false)
                        .setPreviewEnabled(true);
                startActivityForResult(builder.getIntent(getActivity()), PhotoPicker.REQUEST_CODE);
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                new MaterialDialog.Builder(getActivity())
                        .content(R.string.reject_permiss_dialog_notice)
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    PermissionUtils.launchAppDetailsSettings();
                                } else {
                                    dialog.dismiss();
                                }
                            }
                        }).cancelable(false)
                        .show();
            }
        }).request();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PhotoPicker.REQUEST_CODE:
                    if (data != null) {
                        ArrayList<String> photos = data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                        if (photos == null || photos.size() == 0) {
                            Toaster.showInvalidate("请稍候再试");
                            return;
                        }
                        Uri sourceUri = Uri.fromFile(new File(photos.get(0)));
                        Uri destinationUri = Uri.fromFile(new File(Configs.getImgDir(), "user_" + new Date().getTime() + "_"));
                        UCrop.Options options = new UCrop.Options();
                        options.setHideBottomControls(true);
                        options.setShowCropGrid(false);
                        options.setShowCropFrame(false);
                        options.setCircleDimmedLayer(true);

                        UCrop uCrop = UCrop.of(sourceUri, destinationUri)
                                .withAspectRatio(1, 1)
                                .withMaxResultSize(200, 200)
                                .withOptions(options);
                        startActivityForResult(uCrop.getIntent(getContext()), UCrop.REQUEST_CROP);
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    Uri resultUri = UCrop.getOutput(data);
                    mPresenter.uploadImage(resultUri, currentSite);
                    break;
                case ChangeUsernameActivity.REQUEST_CODE:
                    if (!StringUtils.isEmpty(data.getStringExtra(IntentKey.KEY_USER_NAME))) {
                        username = data.getStringExtra(IntentKey.KEY_USER_NAME);
                        this.userNameTv.setText(username);
                        String userImgId = data.getStringExtra(IntentKey.KEY_USER_IMAGE_ID);
                        mPresenter.updateUserProfile(currentSite, userImgId, username, siteLoginId);
                    }
                    break;
                case ChangeUsernameActivity.SITE_LOGIN_ID_CODE:
                    if (!StringUtils.isEmpty(data.getStringExtra(IntentKey.KEY_SITE_LOGIN_ID))) {
                        siteLoginId = data.getStringExtra(IntentKey.KEY_SITE_LOGIN_ID);
                        String userImgId = data.getStringExtra(IntentKey.KEY_USER_IMAGE_ID);
                        mPresenter.updateUserProfile(currentSite, userImgId, username, siteLoginId);
                        currentSite.setSiteLoginId(siteLoginId);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, currentSite);
                        EventBus.getDefault().postSticky(new AppEvent(AppEvent.SET_SITE_LOGIN_ID, bundle));
                    }
                    break;
            }
        }
    }

    private void showListDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.dialog_recycler_view, false)
                .build();
        View customeView = dialog.getCustomView();
        RecyclerView mRecyclerView = customeView.findViewById(R.id.view_dialog_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        personalDialogAdapter = new PersonalDialogAdapter(items);
        mRecyclerView.setAdapter(personalDialogAdapter);
        dialog.show();
        personalDialogAdapter.setOnItemClickListener(new PersonalDialogAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                boolean isNet = NetUtils.getNetInfo();
                switch (items.get(position).getItemType()) {
                    case PersonalItem.ITEM_SHOW_IMAGE_LARGE:
                        Intent intent = new Intent(getActivity(), ImageShowActivity.class);
                        intent.putExtra(IntentKey.KEY_USER_HEAD, currentSite.getSiteUserImage());
                        intent.putExtra(IntentKey.KEY_CURRENT_SITE, currentSite);
                        startActivity(intent);
//                        PhotoPicker.PhotoPickerBuilder builder = PhotoPicker.builder()
//                                .setPreviewOneAvtor(currentSite.getSiteUserImage());
//                        startActivity(builder.getIntent(getContext()));
                        break;
                    case PersonalItem.ITEM_SET_SITE_ACCOUNT:
                        if (!isNet) {
                            Toaster.showInvalidate("请稍候再试");
                            break;
                        }
                        new MaterialDialog.Builder(getContext())
                                .content("站点账户只可以设置一次，确定要设置吗？")
                                .positiveText("确定")
                                .negativeText("取消")
                                .onAny(PersonalFragment.this)
                                .show();
                        break;
                    case PersonalItem.ITEM_CHANGE_USER_HEAD:
                        if (!isNet) {
                            Toaster.showInvalidate("请稍候再试");
                            break;
                        }
                        startPhotoPicker();
                        break;
                    case PersonalItem.ITEM_CHANGE_USER_NICK_NAME:
                        if (!isNet) {
                            Toaster.showInvalidate("请稍候再试");
                            break;
                        }
                        if (userImgId == null) {
                            selectUserInfoFromDB();
                        }
                        Intent changeIntent = new Intent(getContext(), ChangeUsernameActivity.class);
                        changeIntent.putExtra(IntentKey.KEY_OLD_USER_NAME, username);
                        changeIntent.putExtra(IntentKey.KEY_USER_IMAGE_ID, userImgId);
                        changeIntent.putExtra(ChangeUsernameActivity.KEY_TYPE, ChangeUsernameActivity.CHANG_USER_NAME);
                        startActivityForResult(changeIntent, ChangeUsernameActivity.REQUEST_CODE);
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onTaskStart(String content) {

    }

    @Override
    public void onTaskFinish() {

    }

    @Override
    public void onGetUserPhoneSuccess(final ApiUserPhoneProto.ApiUserPhoneResponse apiUserPhoneResponse) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                phoneNum.setText(StringUtils.hidePhoneNumber(apiUserPhoneResponse.getPhoneId()));
            }
        });
    }

    @Override
    public void onLogoutPlatformSuccess() {
        mPresenter.cleanData(getActivity());
    }

    @Override
    public void onUpdateUserProfileSuccess(Site site, UserProto.UserProfile userProfileDetails) {
        currentSite = site;
        UserFriendBean userFriendBean = new UserFriendBean();
        userFriendBean.setSiteUserId(currentSite.getSiteUserId());
        userFriendBean.setUserName(username);
        userFriendBean.setUserImage(userImgId);
        userFriendBean.setSiteLoginId(siteLoginId);
        userFriendBean.setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY));
        UserProfilePresenter.getInstance(currentSite).updateSiteUserProfile(userFriendBean);

        currentSite.setSiteLoginId(userProfileDetails.getSiteLoginId());
        currentSite.setSiteUserImage(userProfileDetails.getUserPhoto());
        currentSite.setSiteUserName(userProfileDetails.getUserName());

        ////头像上传成功，之后更新用户表中的数据
        SitePresenter.getInstance().updateSiteUserInfo(currentSite);
        fillUserData();
        ZalyApplication.setUserInfo(currentSite.getSiteUserId(), userImgId, username);
        ZalyApplication.setCurProfile(userProfileDetails);

    }

    @Override
    public void onUploadImageSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
        mPresenter.updateUserProfile(currentSite, apiFileUploadResponse.getFileId(), username, siteLoginId);
    }

    @Override
    public void onCleanDataSuccess() {
        //  getActivity().startActivity(new Intent(getActivity(), WelcomeActivity.class));
        System.exit(0);
    }


}
