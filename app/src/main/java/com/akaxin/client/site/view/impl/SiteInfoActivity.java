package com.akaxin.client.site.view.impl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.bean.Site;
import com.akaxin.client.bean.event.AppEvent;
import com.akaxin.client.bean.event.SiteEvent;
import com.akaxin.client.bridge.WebActivity;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.SiteConfig;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.maintab.WelcomeActivity;
import com.akaxin.client.personal.ChangeUsernameActivity;
import com.akaxin.client.site.presenter.ISiteInfoPresenter;
import com.akaxin.client.site.presenter.impl.SiteInfoPresenter;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.site.view.ISiteInfoView;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.file.ImageUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.iwf.photopicker.PhotoPicker;

public class SiteInfoActivity extends BaseActivity
        implements ISiteInfoView, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    public static final String WHO_IS_PREFIX = "https://whois.aliyun.com/whois/domain/";
    public static final String KEY_SITE = "key_site";

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.user_name)
    TextView userNameTv;
    @BindView(R.id.user_avatar)
    ImageView avatar;
    @BindView(R.id.site_icon)
    ImageView siteImg;
    @BindView(R.id.site_name)
    TextView siteName;
    @BindView(R.id.site_address)
    TextView siteAddress;
    @BindView(R.id.notification_switch)
    SwitchCompat notificationSwitch;
    @BindView(R.id.menu_item_disconnect)
    View menuItemDisconnect;
    @BindView(R.id.menu_item_query_owner)
    View menuItemQueryOwner;
    @BindView(R.id.menu_item_change_username)
    View menuItemChangeUsername;
    @BindView(R.id.menu_item_change_avatar)
    View menuItemChangeAvatar;
    @BindView(R.id.menu_item_disconnect_text)
    TextView menuItemDisconnectText;
    @BindView(R.id.menu_item_logout_site)
    View menuItemLogoutSite;
    @BindView(R.id.menu_item_site_login_id)
    View siteLoginIdView;
    @BindView(R.id.site_login_id)
    TextView siteLoginIdTV;
    Site site;

    private String siteLoginId = "";
    private ISiteInfoPresenter iPresenter;

    @Override
    public int getResLayout() {
        return R.layout.activity_site_info;
    }


    @Override
    public void initView() {
        ButterKnife.bind(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));
        swipeRefreshLayout.setEnabled(false);
    }

    @Override
    public void initEvent() {
        menuItemDisconnect.setOnClickListener(this);
        menuItemQueryOwner.setOnClickListener(this);
        menuItemChangeUsername.setOnClickListener(this);
        menuItemChangeAvatar.setOnClickListener(this);
        notificationSwitch.setOnCheckedChangeListener(this);
        menuItemLogoutSite.setOnClickListener(this);
        siteLoginIdView.setOnClickListener(this);
    }

    @Override
    public void initPresenter() {
        iPresenter = new SiteInfoPresenter(this);
    }

    @Override
    public void onLoadData() {
        setCenterTitle(getString(R.string.site_info));
        site = getIntent().getParcelableExtra(KEY_SITE);

        if (site == null) {
            return;
        }
        Site siteInfo = SitePresenter.getInstance().getSiteUser(site.getHostAndPort());
        if (siteInfo == null) {
            return;
        }
        siteLoginId = siteInfo.getSiteLoginId();
        ZalyLogUtils.getInstance().info(TAG, "site === " + site.getSiteAddress());
        iPresenter.setSite(site);
        setMultTitle(getString(R.string.site_info), StringUtils.getSiteSubTitle(site));
        userNameTv.setText(site.getSiteUserName());
        new ImageUtils(this, site).loadImage(site.getSiteUserImage(), avatar, site);
        new ImageUtils(this, site).loadSiteImage(site.getSiteIcon(), siteImg, site);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(site.getSiteUserImage())
//                .setFileType(FileProto.FileType.USER_PORTRAIT)
//                .setSite(site)
//                .build();
//        Glide.with(this).load(model).
//                apply(new RequestOptions().dontAnimate()).into(avatar);
//
//
//        ZalyGlideModel model1 = new ZalyGlideModel.Builder()
//                .setImageID(site.getSiteIcon())
//                .setFileType(FileProto.FileType.SITE_ICON)
//                .setSite(site)
//                .build();
//        Glide.with(this).load(model1).
//                apply(new RequestOptions().dontAnimate()).into(siteImg);


        siteName.setText(site.getSiteName());
        siteAddress.setText(site.getHostAndPort());
        siteLoginIdTV.setText(site.getSiteLoginId());
        boolean isNet = NetUtils.getNetInfo();
        siteLoginId = site.getSiteLoginId();
        updateNoNetNotificationSwitch(site.isMute());
        if (site.getProtocolVersion() < SiteConfig.site_login_id_minProtocol) {
            siteLoginIdView.setVisibility(View.GONE);
        }
        if (isNet) {
            iPresenter.getPlatformSiteSetting();
        }
        boolean tmpIsConnected = isSiteConnected(site);

        menuItemDisconnectText.setText(tmpIsConnected ?
                R.string.disconnect : R.string.connect);
    }

    @Override
    public void onClick(View v) {
        boolean isNet = NetUtils.getNetInfo();

        switch (v.getId()) {
            case R.id.menu_item_disconnect:
                if (!isNet) {
                    Toaster.showInvalidate("请稍候再试");
                    return;
                }
                boolean tmpIsConnected = isSiteConnected(site);
                if (!tmpIsConnected) {
                    iPresenter.connectSite();
                } else {
                    iPresenter.disconnectSite();
                }
                break;
            case R.id.menu_item_query_owner:
                if (!isNet) {
                    Toaster.showInvalidate("请稍候再试");
                    return;
                }
                startQueryWebActivity();
                break;
            case R.id.menu_item_change_username:
                if (!isNet) {
                    Toaster.showInvalidate("请稍候再试");
                    return;
                }
                Intent intent = new Intent(this, ChangeUsernameActivity.class);
                intent.putExtra(IntentKey.KEY_OLD_USER_NAME, iPresenter.getSite().getSiteUserName());
                startActivityForResult(intent, ChangeUsernameActivity.REQUEST_CODE);
                break;
            case R.id.menu_item_change_avatar:
                if (!isNet) {
                    Toaster.showInvalidate("请稍候再试");
                    return;
                }
                startPhotoPicker();
                break;
            case R.id.menu_item_site_login_id:
                if (siteLoginId != "" && siteLoginId.length() > 2) {
                    Toaster.showInvalidate("站点账户只可以设置一次");
                    return;
                }
                new MaterialDialog.Builder(getContext())
                        .content("站点账户只可以设置一次，确定要设置吗？")
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
                                        Intent siteLoginIdIntent = new Intent(getContext(), ChangeUsernameActivity.class);
                                        siteLoginIdIntent.putExtra(ChangeUsernameActivity.KEY_TYPE, ChangeUsernameActivity.SITE_LOGIN_ID);
                                        startActivityForResult(siteLoginIdIntent, ChangeUsernameActivity.SITE_LOGIN_ID_CODE);
                                        break;
                                }
                            }
                        })
                        .show();

                break;
            case R.id.menu_item_logout_site:

                new MaterialDialog.Builder(getContext())
                        .content("是否要删除该站点？")
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
                                        iPresenter.delSite();
                                        break;
                                }

                            }
                        })
                        .show();
                break;

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        switch (compoundButton.getId()) {
            case R.id.notification_switch:
                boolean isNet = NetUtils.getNetInfo();
                if (!isNet) {
                    Toaster.showInvalidate("请稍候再试");
                    notificationSwitch.setChecked(!checked);
                    return;
                }
                boolean mute = compoundButton.isChecked();
                iPresenter.updateSiteMute(mute);
                break;
        }
    }

    @Override
    public void updateNotificationSwitch(boolean mute) {

        notificationSwitch.setOnCheckedChangeListener(null);
        notificationSwitch.setChecked(mute);
        notificationSwitch.setOnCheckedChangeListener(this);

    }

    public void updateNoNetNotificationSwitch(boolean mute) {
        notificationSwitch.setOnCheckedChangeListener(null);
        notificationSwitch.setChecked(mute);
        notificationSwitch.setOnCheckedChangeListener(this);
    }

    private void startQueryWebActivity() {
        Intent intent = new Intent(SiteInfoActivity.this, WebActivity.class);
        intent.putExtra(IntentKey.KEY_WEB_URL, WHO_IS_PREFIX + iPresenter.getSite().getSiteHost());
        startActivity(intent);
    }

    private void startPhotoPicker() {
        PhotoPicker.PhotoPickerBuilder builder = PhotoPicker.builder()
                .setPhotoCount(1)
                .setShowCamera(true)
                .setShowGif(false)
                .setPreviewEnabled(true);
        startActivityForResult(builder.getIntent(this), PhotoPicker.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
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
                        options.setShowCropGrid(true);
                        UCrop uCrop = UCrop.of(sourceUri, destinationUri)
                                .withAspectRatio(1, 1)
                                .withMaxResultSize(200, 200)
                                .withOptions(options);
                        startActivityForResult(uCrop.getIntent(this), UCrop.REQUEST_CROP);
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    final Uri resultUri = UCrop.getOutput(data);
                    iPresenter.updateUserImage(resultUri);
                    break;
                case ChangeUsernameActivity.REQUEST_CODE:
                    if (!StringUtils.isEmpty(data.getStringExtra(IntentKey.KEY_USER_NAME))) {
                        String username = data.getStringExtra(IntentKey.KEY_USER_NAME);
                        iPresenter.updateUsername(username);
                    }
                    break;
                case ChangeUsernameActivity.SITE_LOGIN_ID_CODE:
                    if (!StringUtils.isEmpty(data.getStringExtra(IntentKey.KEY_SITE_LOGIN_ID))) {
                        siteLoginId = data.getStringExtra(IntentKey.KEY_SITE_LOGIN_ID);
                        iPresenter.updateSiteLoginId(siteLoginId);
                    }
                    break;
            }
        }
    }

    @Override
    public void onUpdateUserProfileStart() {
        showProgress();
    }

    @Override
    public void onUpdateUserProfileSuccess() {
        hideProgress();
        this.siteLoginId = iPresenter.getSite().getSiteLoginId();
        new ImageUtils(this, iPresenter.getSite()).loadImage(iPresenter.getSite().getSiteUserImage(), avatar);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(iPresenter.getAndCheckLegalSite().getSiteUserImage())
//                .setFileType(FileProto.FileType.SITE_ICON)
//                .setSite(iPresenter.getAndCheckLegalSite())
//                .build();
//        Glide.with(this).load(model).
//                apply(new RequestOptions()
//                        .dontAnimate()
//                        .error(R.drawable.ic_default)
//                        .fallback(R.drawable.ic_default))
//                .into(this.avatar);

        userNameTv.setText(iPresenter.getSite().getSiteUserName());
        siteLoginIdTV.setText(iPresenter.getSite().getSiteLoginId());


    }

    @Override
    public void onUpdateUserProfileError() {
        hideProgress();
        Toaster.show("上传失败");
    }

    @Override
    public void onGetPlatformSiteSettingStart() {
        showProgress();
    }

    @Override
    public void onGetPlatformSiteSettingSuccess(boolean mute) {
        hideProgress();
        updateNotificationSwitch(mute);
    }

    @Override
    public void onGetPlatformSiteSettingError() {
        hideProgress();
    }

    @Override
    public void onUpdateSiteSettingStart() {
        showProgress();
    }

    @Override
    public void onUpdateSiteSettingSuccess() {
        hideProgress();
    }

    @Override
    public void onUpdateSiteSettingError(boolean originalMuteStatus) {
        hideProgress();
        updateNotificationSwitch(originalMuteStatus);
    }

    @Override
    public void onDisconnectStart() {
        showProgress();
    }

    @Override
    public void onDisconnectSuccess() {
        hideProgress();
        menuItemDisconnectText.setText(R.string.connect);
        Toaster.show("已断开");
    }

    @Override
    public void onDisconnectError() {
        hideProgress();
    }

    @Override
    public void onConnectStart() {
        showProgress();
    }

    @Override
    public void onConnectSuccess() {
        hideProgress();
        menuItemDisconnectText.setText(R.string.disconnect);
        Toaster.show("已连接");
    }

    @Override
    public void onConnectError() {
        hideProgress();
    }

    @Override
    public void onDelSiteStart() {
        showProgress();
    }

    @Override
    public void onDelSiteSuccessAtCurrentSite(Site deleted, Site toSite) {
        if (toSite == null) {
            Toaster.show("已删除。当前已无其他站点");
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
        } else {
            Toaster.show(String.format("已删除。正切换至%s", toSite.getSiteDisplayAddress()));
            Bundle bundle = new Bundle();
            bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, toSite);
            EventBus.getDefault().postSticky(new SiteEvent(SiteEvent.SWITCH_SITE_KEY, bundle));
            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_SWITCH_SITE, bundle));
            finish();
        }
    }

    @Override
    public void onDelSiteSuccessAtAnotherSite(Site deleted) {
        hideProgress();
        Toaster.show("已删除");
        finish();
    }

    @Override
    public void onDelSiteError() {
        hideProgress();
        Toaster.showInvalidate("删除失败");
    }

    @Override
    public void showProgress() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        swipeRefreshLayout.setRefreshing(false);
    }


}
