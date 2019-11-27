package com.windchat.client.register;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.windchat.client.Configs;
import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.api.ApiClient;
import com.windchat.client.api.ZalyAPIException;
import com.windchat.client.bean.Site;
import com.windchat.client.bean.User;
import com.windchat.client.bean.event.AppEvent;
import com.windchat.client.bean.event.SiteEvent;
import com.windchat.client.constant.ErrorCode;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.constant.ServerConfig;
import com.windchat.client.im.files.IMFileUtils;
import com.windchat.client.maintab.BaseActivity;
import com.windchat.client.maintab.ZalyMainActivity;
import com.windchat.client.register.presenter.ILoginSitePresenter;
import com.windchat.client.register.presenter.impl.LoginSitePresenter;
import com.windchat.client.register.view.ILoginSiteView;
import com.windchat.client.site.presenter.impl.SitePresenter;
import com.windchat.client.util.NetUtils;
import com.windchat.client.util.SiteUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.security.RSAUtils;
import com.windchat.client.util.task.ZalyTaskExecutor;
import com.windchat.client.util.toast.Toaster;
import com.windchat.client.view.TimeButton;
import com.akaxin.proto.core.ConfigProto;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.core.UserProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.akaxin.proto.site.ApiSiteLoginProto;
import com.akaxin.proto.site.ApiSiteRegisterProto;
import com.akaxin.proto.site.ApiUserUpdateProfileProto;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.windchat.im.IMClient;
import com.windchat.im.socket.ConnectionConfig;
import com.yalantis.ucrop.UCrop;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import me.iwf.photopicker.PhotoPicker;


/**
 * Created by Mr.kk on 2018/5/5.
 * This Project was client-android
 */
public class RegisterActivity extends BaseActivity implements ViewPager.OnPageChangeListener, View.OnClickListener, ILoginSiteView {

    public static final String TAG = "RegisterActivity";
    static RegisterActivity registerActivity;
    @BindView(R.id.register_vp)
    ViewPager registerVp;
    @BindView(R.id.register_progress)
    ProgressBar mProgressBar;
    List<View> views = new ArrayList<>();

    Button inviteViewViewBtnNext, headViewBtnNext, verifyViewLogin, exitPhoneBtnLogin;
    TextInputLayout inviteViewEtSiteInviteNum, headViewNickName, verifyViewPhone, verifyViewPhoneCode;
    TextView exitPhoneTvShowPhone;
    CircleImageView headViewAvtor;
    TimeButton verifyViewGetPhoneCode;

    private Uri userImgUri;
    private String userImgId;
    private String userName;
    private String vcCode;
    private String phoneNum;
    private String verifyPhoneCode;
    private int curPagerIndex;
    private View headView, exitPhoneView, verifyView, inviteView;
    private ILoginSitePresenter presenter;

    Site loginSite;
    RegisterPagerAdapter adapter;


    @Override
    public int getResLayout() {
        return R.layout.activity_register;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        initData();
        adapter = new RegisterPagerAdapter();
        registerVp.setAdapter(adapter);
        registerVp.addOnPageChangeListener(this);
        setMultTitle(loginSite.getSiteHost(), loginSite.getHostAndPort());
        mProgressBar.setProgress((100 / views.size()));
        registerActivity = this;
    }

    private void initData() {
        loginSite = getIntent().getParcelableExtra(LoginSiteActivity.LOGIN_SITE);

        inviteView = getLayoutInflater().inflate(R.layout.viewpager_item_invitation, null);

        inviteViewViewBtnNext = inviteView.findViewById(R.id.vp_invitation_btn_next);
        inviteViewEtSiteInviteNum = inviteView.findViewById(R.id.vp_invitation_et_invita);

        inviteViewViewBtnNext.setOnClickListener(this);
        if (loginSite.getCodeConfig() == ConfigProto.InviteCodeConfig.UIC_YES_VALUE) {
            views.add(inviteView);
        }

        headView = getLayoutInflater().inflate(R.layout.viewpager_item_headportrait, null);
        headViewBtnNext = headView.findViewById(R.id.vp_headportrait_btn_next);
        headViewAvtor = headView.findViewById(R.id.vp_headportrait_avtor);
        headViewNickName = headView.findViewById(R.id.vp_headportrait_et_nick);
        views.add(headView);

        exitPhoneView = getLayoutInflater().inflate(R.layout.viewpager_item_exist_phone, null);
        exitPhoneBtnLogin = exitPhoneView.findViewById(R.id.vp_exit_phone_btn_login);
        exitPhoneTvShowPhone = exitPhoneView.findViewById(R.id.vp_exit_phone_tv_show_phone);

        String phoneNum = ZalyApplication.getCfgSP().getKey(Configs.PHONE_ID);

        exitPhoneTvShowPhone.setText(phoneNum);
        exitPhoneBtnLogin.setOnClickListener(this);

        verifyView = getLayoutInflater().inflate(R.layout.viewpager_item_verification, null);
        verifyViewLogin = verifyView.findViewById(R.id.vp_verification_btn_login);
        verifyViewGetPhoneCode = verifyView.findViewById(R.id.vp_verification_btn_getcode);
        verifyViewPhone = verifyView.findViewById(R.id.vp_verification_et_phone);
        verifyViewPhoneCode = verifyView.findViewById(R.id.vp_verification_et_code);
        verifyViewLogin.setOnClickListener(this);
        verifyViewGetPhoneCode.setOnClickListener(this);
        verifyViewGetPhoneCode.onCreate();
        verifyViewGetPhoneCode.setTextBefore("获取验证码")
                .setTextAfter("秒后重新获取").setLenght(60 * 1000).onCreate();

        if (loginSite.getRealNameConfig() == ConfigProto.RealNameConfig.REALNAME_YES_VALUE) {
            if (StringUtils.isNotEmpty(phoneNum)) {
                views.add(exitPhoneView);
            } else {
                views.add(verifyView);
            }
        } else {
            headViewBtnNext.setText("登录");
        }
    }


    @Override
    public void initEvent() {
        headViewBtnNext.setOnClickListener(this);
        headViewAvtor.setOnClickListener(this);
    }

    @Override
    public void initPresenter() {
        presenter = new LoginSitePresenter(this);
    }

    @Override
    public void onLoadData() {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        //stepsView.setCompletedPosition(position);
        mProgressBar.setProgress((100 / views.size()) * (position + 1));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void showProgressDialog() {
        this.showProgress();
    }

    public void hideProgressDialog() {
        this.hideProgress();
    }

    @Override
    public Context getAppContext() {
        return this;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vp_invitation_btn_next:
                hideSoftKey();
                registerVp.setCurrentItem(1);
                break;
            case R.id.vp_headportrait_btn_next:
                hideSoftKey();

                if (loginSite.getRealNameConfig() == ConfigProto.RealNameConfig.REALNAME_YES_VALUE) {
                    registerVp.setCurrentItem(2);
                } else {
                    registerSiteAndLogin();
                }
                break;
            case R.id.vp_verification_btn_login:
                registerOnPlatform();
                break;
            case R.id.vp_verification_btn_getcode:
                phoneNum = verifyViewPhone.getEditText().getText().toString().trim();
                if (StringUtils.isEmpty(phoneNum)) {
                    Toaster.showInvalidate("请输入手机号");
                    return;
                }
                if (!StringUtils.isPhone(phoneNum)) {
                    Toaster.showInvalidate("请输入正确的手机号");
                    return;
                }
                if (!NetUtils.getNetInfo()) {
                    Toaster.show(getString(R.string.without_network_hint));
                    return;
                }
                break;
            case R.id.vp_headportrait_avtor:
                PhotoPicker.PhotoPickerBuilder builder = PhotoPicker.builder()
                        .setPhotoCount(1)
                        .setShowCamera(true)
                        .setShowGif(false)
                        .setPreviewEnabled(true);
                startActivityForResult(builder.getIntent(this), PhotoPicker.REQUEST_CODE);
                break;
            case R.id.vp_exit_phone_btn_login:
                registerSiteAndLogin();
                break;

        }
    }

    /**
     * 注册平台
     */
    public void registerOnPlatform() {
        phoneNum = verifyViewPhone.getEditText().getText().toString().trim();
        if (StringUtils.isEmpty(phoneNum)) {
            Toaster.showInvalidate("请输入手机号");
            return;
        }
        verifyPhoneCode = verifyViewPhoneCode.getEditText().getText().toString().trim();
        if (StringUtils.isEmpty(verifyPhoneCode)) {
            Toaster.showInvalidate("请输入验证码");
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PhotoPicker.REQUEST_CODE:
                    if (data != null) {
                        ArrayList<String> photos =
                                data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                        if (photos == null || photos.size() == 0) {
                            Toaster.showInvalidate("请稍候再试");
                            return;
                        }
                        Uri sourceUri = Uri.fromFile(new File(photos.get(0)));

                        Uri destinationUri = Uri.fromFile(new File(Configs.getImgDir(), "user_" + new Date().getTime() + ".png"));

                        UCrop.Options options = new UCrop.Options();
                        options.setHideBottomControls(true);
                        options.setShowCropGrid(false);
                        options.setShowCropFrame(false);
                        options.setCircleDimmedLayer(true);

                        UCrop uCrop = UCrop.of(sourceUri, destinationUri)
                                .withAspectRatio(1, 1)
                                .withMaxResultSize(200, 200)
                                .withOptions(options);
                        startActivityForResult(uCrop.getIntent(this), UCrop.REQUEST_CROP);
                    }
                    break;
                case UCrop.REQUEST_CROP:
                    try {
                        userImgUri = UCrop.getOutput(data);
                    } catch (Exception e) {
                        Toaster.show("此图片格式不支持");
                        e.printStackTrace();
                    }

                    Glide.with(this).load(userImgUri).
                            apply(new RequestOptions().placeholder(R.drawable.ic_default)
                                    .error(R.drawable.ic_default)
                                    .dontAnimate()
                            )
                            .into(headViewAvtor);
                    break;

            }
        }
    }

    public class RegisterPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            registerVp.removeView(view);
            //container.removeView(view);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            views.get(position).setTag(position);
            container.addView(views.get(position));
            return views.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public void updataViewPagerItem(View view, int index) {
            curPagerIndex = index;
            views.remove(index);
            views.add(view);
        }

    }


    protected void registerSiteAndLogin() {
        ApiSiteRegisterProto.ApiSiteRegisterRequest.Builder builder = ApiSiteRegisterProto.ApiSiteRegisterRequest.newBuilder();
        String phoneToken = ZalyApplication.getCfgSP().getKey(Configs.PHONE_TOKEN + "_" + loginSite.getSiteAddress());
        if (loginSite.getCodeConfig() == ConfigProto.InviteCodeConfig.UIC_YES_VALUE) {
            vcCode = inviteViewEtSiteInviteNum.getEditText().getText().toString().trim();
            if (StringUtils.isEmpty(vcCode)) {
                Toaster.showInvalidate("请输入邀请码");
                return;
            }
            builder.setUserUic(vcCode);
        }

        if (loginSite.getRealNameConfig() == ConfigProto.RealNameConfig.REALNAME_YES_VALUE) {
            builder.setPhoneToken(phoneToken);
        }
        userName = headViewNickName.getEditText().getText().toString().trim();
        if (StringUtils.isEmpty(userName)) {
            Toaster.showInvalidate("请输入昵称");
            return;
        }
        loginSite.setSiteUserName(userName);
        builder.setUserName(userName);
        builder.setUserIdPubk(ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY));

        if (userImgUri == null) {
            Toaster.showInvalidate("请选择用户头像");
            return;
        }
        if (StringUtils.isNotEmpty(userImgId)) {
            builder.setUserPhoto(userImgId);
            loginSite.setSiteUserImage(userImgId);
        }

        ZalyTaskExecutor.executeUserTask(TAG, new RegisterTask(builder.build(), loginSite));
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
        phoneNum = ZalyApplication.getCfgSP().getKey(Configs.PHONE_ID);
        exitPhoneTvShowPhone.setText(phoneNum);
    }

    /**
     * 注册
     */
    class RegisterTask extends ZalyTaskExecutor.Task<Void, Void, ApiSiteRegisterProto.ApiSiteRegisterResponse> {

        ApiSiteRegisterProto.ApiSiteRegisterRequest registerRequest;
        Site loginSite;

        public RegisterTask(ApiSiteRegisterProto.ApiSiteRegisterRequest registerRequest, Site site) {
            this.registerRequest = registerRequest;
            this.loginSite = site;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress("正在注册...");
        }

        @Override
        protected ApiSiteRegisterProto.ApiSiteRegisterResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(loginSite)).getSiteApi().registerSiteByConfig(registerRequest);
        }

        @Override
        protected void onTaskSuccess(ApiSiteRegisterProto.ApiSiteRegisterResponse apiRegisterResponse) {
            super.onTaskSuccess(apiRegisterResponse);
            if (StringUtils.isEmpty(apiRegisterResponse.getSiteUserId())) {
                Toaster.showInvalidate("注册失败，请稍候再试");
                return;
            }
            ////generate userToken
            String userToken = UUID.randomUUID().toString();
            ZalyApplication.getCfgSP().put(loginSite.getSiteIdentity() + Configs.SUFFIX_USER_TOKEN, userToken);

            String phoneToken = ZalyApplication.getCfgSP().getKey(Configs.PHONE_TOKEN + "_" + loginSite.getSiteAddress());
            String userPrivKeyPem = ZalyApplication.getCfgSP().getKey(Configs.USER_PRI_KEY);
            String userPubKeyPem = ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY);
            String devicePubKeyPem = ZalyApplication.getCfgSP().getKey(Configs.DEVICE_PUB_KEY);

            String userSignBase64 = RSAUtils.getInstance().signInBase64String(userPrivKeyPem, userPubKeyPem);
            String deviceSignBase64 = RSAUtils.getInstance().signInBase64String(userPrivKeyPem, devicePubKeyPem);

            ZalyTaskExecutor.executeUserTask(TAG, new LoginTask(userSignBase64, deviceSignBase64, userToken, phoneToken, loginSite));
            hideProgress();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            String errorCode = zalyAPIException.getErrorInfoCode();
            if (errorCode.equals(ErrorCode.USER_UNIQUE_SUI)) {
                presenter.loginOrRegisterSite(loginSite);
            } else {
                super.onAPIError(zalyAPIException);
            }
            ZalyLogUtils.getInstance().errorToInfo(TAG, zalyAPIException.getMessage());
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().exceptionError(e);

            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
            Toaster.showInvalidate("请求失败，请稍候再试");
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

    /**
     * 登录
     */
    class LoginTask extends ZalyTaskExecutor.Task<Void, Void, ApiSiteLoginProto.ApiSiteLoginResponse> {

        Site site;
        String userSignBase64;
        String deviceSignBase64;
        String userToken;
        String phoneToken;

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress("正在登录...");
        }

        public LoginTask(String userSignBase64, String deviceSignBase64, String userToken, String phoneToken, Site site) {
            this.userSignBase64 = userSignBase64;
            this.deviceSignBase64 = deviceSignBase64;
            this.userToken = userToken;
            this.phoneToken = phoneToken;
            this.site = site;
        }

        @Override
        protected ApiSiteLoginProto.ApiSiteLoginResponse executeTask(Void... voids) throws Exception {
            if (userToken == null) {
                userToken = "";
            }
            return ApiClient.getInstance(ConnectionConfig.getConnectionCfg(site)).getSiteApi().loginSite(userSignBase64, deviceSignBase64, userToken, phoneToken);
        }

        @Override
        protected void onTaskSuccess(ApiSiteLoginProto.ApiSiteLoginResponse apiLoginResponse) {
            super.onTaskSuccess(apiLoginResponse);
            if (StringUtils.isEmpty(apiLoginResponse.getSiteUserId()) || StringUtils.isEmpty(apiLoginResponse.getUserSessionId())) {
                Toaster.showInvalidate("登录失败，请稍候再试");
                return;
            }
            //////切换表 存入站点信息
            SitePresenter.getInstance().insertSite(site, apiLoginResponse.getSiteUserId(), apiLoginResponse.getUserSessionId());

            if (ZalyApplication.siteList == null) {
                ZalyApplication.siteList = new ArrayList<>();
            }
            ZalyApplication.siteList.add(site);
            //2. 切换至该站点
            ZalyApplication.getCfgSP().put(Configs.KEY_CUR_SITE, site.getSiteIdentity());
            new SiteUtils().prepareDo(new SiteUtils.SiteUtilsListener() {
                @Override
                public void onPrepareSiteMsg(String msg) {
                    showProgress("正在登录该站点...");
                }

                @Override
                public void onPrepareSiteSuccess(Site currentSite) {
                    hideProgress();
                    try {
                        IMClient.getInstance(currentSite).checkConnection();
                    } catch (Exception e) {
                        ZalyLogUtils.getInstance().info(TAG, e.getMessage());
                    }
                    EventBus.getDefault().post(new SiteEvent(-1, null));

                    //上传个人资料
                    if (userImgUri != null) {
                        ZalyTaskExecutor.executeUserTask(TAG, new UploadUserImageTask(userImgUri));
                    }
                }
            });
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().exceptionError(e);
            Toaster.showInvalidate("请求失败，请稍候再试");

        }

        @Override
        protected void onAPIError(ZalyAPIException e) {
            ZalyLogUtils.getInstance().exceptionError(e);
            Toaster.showInvalidate("请求失败，请稍候再试");
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }

    /**
     * 上传用户头像
     */
    class UploadUserImageTask extends ZalyTaskExecutor.Task<Void, Void, ApiFileUploadProto.ApiFileUploadResponse> {

        private Uri imageUri;

        public UploadUserImageTask(Uri imageUri) {
            this.imageUri = imageUri;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress("处理中...");
        }

        @Override
        protected ApiFileUploadProto.ApiFileUploadResponse executeTask(Void... voids) throws Exception {
            File file = new File(imageUri.getPath());
            byte[] bytesArray = new byte[(int) file.length()];
            try {
                FileInputStream fis = new FileInputStream(file);
                fis.read(bytesArray); //read file into bytes[]
                fis.close();
            } catch (Exception e) {
                throw e;
            }
            byte[] resizedImage = IMFileUtils.resizeImageByWidth(bytesArray, 256);

            return IMFileUtils.uploadFile(resizedImage, FileProto.FileType.USER_PORTRAIT, loginSite);

        }

        @Override
        protected void onTaskSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
            super.onTaskSuccess(apiFileUploadResponse);
            if (apiFileUploadResponse == null) {
                Toaster.showInvalidate("上传失败，请稍候再试");
                return;
            }
            userImgId = apiFileUploadResponse.getFileId();
            ZalyTaskExecutor.executeUserTask(TAG, new UpdateProfile());
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            ZalyLogUtils.getInstance().exceptionError(e);
        }

        @Override
        protected void onAPIError(ZalyAPIException e) {
            ZalyLogUtils.getInstance().apiError(TAG, e);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();

        }
    }

    /**
     * 上传用户资料
     */
    class UpdateProfile extends ZalyTaskExecutor.Task<Void, Void, ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse> {

        User user;

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress("上传资料中...");
        }

        @Override
        protected ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse executeTask(Void... voids) throws Exception {
            Site site = SitePresenter.getInstance().getSiteUser(loginSite.getHostAndPort());

            UserProto.UserProfile userProfileDetails = UserProto.UserProfile.newBuilder()
                    .setSiteUserId(loginSite.getSiteUserId())
                    .setUserPhoto(userImgId)
                    .setUserName(userName)
                    .build();
            return ApiClient.getInstance(loginSite).getUserApi().updateProfile(userProfileDetails);
        }

        @Override
        protected void onTaskSuccess(ApiUserUpdateProfileProto.ApiUserUpdateProfileResponse userProfileUpdateResponse) {
            super.onTaskSuccess(userProfileUpdateResponse);

            UserProto.UserProfile userProfileDetails = UserProto.UserProfile.newBuilder()
                    .setSiteUserId(loginSite.getSiteUserId())
                    .setUserPhoto(userImgId)
                    .setUserName(userName)
                    .build();

            ZalyApplication.setCurProfile(userProfileDetails);

            //上传完用户资料成功后， 用户身份记录入库, 更新用户头像 site_user_icon
            //将 存入用户身份， 个人信息存入 user_profile， 为了消息的读取。
            loginSite.setSiteUserImage(userImgId);
            loginSite.setSiteUserName(userName);

            SitePresenter.getInstance().insertSiteAndUserIdentity(loginSite, Configs.getGlobalUserId(), ServerConfig.LOGIN_WITH_GENERATE);

            Intent intent = new Intent(RegisterActivity.this, ZalyMainActivity.class);
            intent.putExtra(IntentKey.KEY_CURRENT_SITE, loginSite);
            Bundle bundle = new Bundle();
            bundle.putParcelable(IntentKey.KEY_CURRENT_SITE, loginSite);
            EventBus.getDefault().post(new AppEvent(AppEvent.ACTION_SWITCH_SITE, bundle));
            startActivity(intent);
            finish();
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyApiException) {
            ZalyLogUtils.getInstance().apiError(TAG, zalyApiException);
        }

        @Override
        protected void onTaskError(Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }

    public static void actionStart(Activity activity, Site site) {
        Intent intent = new Intent(activity, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(LoginSiteActivity.LOGIN_SITE, site);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (verifyViewGetPhoneCode != null)
            verifyViewGetPhoneCode.onDestroy();
    }

}