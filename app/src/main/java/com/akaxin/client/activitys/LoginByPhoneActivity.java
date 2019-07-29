package com.akaxin.client.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.User;
import com.akaxin.client.bean.event.LoginEvent;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.mvp.BaseMVPActivity;
import com.akaxin.client.mvp.contract.LoginByPhoneContract;
import com.akaxin.client.mvp.presenter.LoginByPhonePresenter;
import com.akaxin.client.register.LoginSiteActivity;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.client.view.TimeButton;
import com.akaxin.proto.core.PhoneProto;
import com.blankj.utilcode.util.KeyboardUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Mr.kk on 2018/5/5.
 * This Project was client-android
 */

public class LoginByPhoneActivity extends BaseMVPActivity<LoginByPhoneContract.View, LoginByPhonePresenter> implements LoginByPhoneContract.View {
    @BindView(R.id.base_login_byphone_et_phone)
    TextInputLayout baseLoginByphoneEtPhone;
    @BindView(R.id.base_login_byphone_et_code)
    TextInputLayout baseLoginByphoneEtCode;
    @BindView(R.id.base_login_byphone_btn_getcode)
    TimeButton baseLoginByphoneBtnGetcode;
    @BindView(R.id.base_login_byphone_btn_commit)
    Button baseLoginByphoneBtnCommit;
    @BindView(R.id.base_login_byphone_btn_anonymous)
    Button baseLoginByphoneBtnAnonymous;
    @BindView(R.id.base_desc)
    TextView baseDesc;
    @BindView(R.id.base_desc_left)
    View baseDescLeft;
    @BindView(R.id.base_desc_right)
    View baseDescRight;

    private String phoneNum;
    private String verifyCode;




    protected int type;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_login_byphone);
        ButterKnife.bind(this);
        initToolBar();
        type = getIntent().getIntExtra(IntentKey.VC_TYPE, 1);
        setCenterTitle(type == PhoneProto.VCType.PHONE_REGISTER_VALUE ? "通过手机号注册" : "通过手机号同步登录");
        if (type == PhoneProto.VCType.PHONE_LOGIN_VALUE) {
            baseLoginByphoneBtnAnonymous.setVisibility(View.GONE);
            baseDesc.setVisibility(View.GONE);
            baseDescLeft.setVisibility(View.GONE);
            baseDescRight.setVisibility(View.GONE);
        }
        baseLoginByphoneBtnCommit.setText(type == PhoneProto.VCType.PHONE_REGISTER_VALUE ? "注册手机号" : "登录");
        baseLoginByphoneBtnGetcode.setTextBefore("获取验证码").setTextAfter("秒后重新获取").setLenght(60 * 1000).onCreate();
    }


    private void getVerifyCode() {
        phoneNum = baseLoginByphoneEtPhone.getEditText().getText().toString().trim();
        ZalyLogUtils.getInstance().info("llll", phoneNum);
        if (StringUtils.isEmpty(phoneNum)) {
            Toaster.showInvalidate(R.string.error_empty_phone_input);
            return;
        }
        if (!StringUtils.isPhone(phoneNum)) {
            Toaster.showInvalidate("请输入正确的手机号");
            return;
        }
        switch (type) {
            case PhoneProto.VCType.PHONE_LOGIN_VALUE:
                //纯登录
                break;
            case PhoneProto.VCType.PHONE_REGISTER_VALUE:
                //纯注册
                break;
            case PhoneProto.VCType.PHONE_REGISTER_FOR_SITE_VALUE:
                //匿名账号登录实名站点
                break;
        }
        mPresenter.getVerifyCode(phoneNum, type);
    }

    private void submitInfo() {
        phoneNum = baseLoginByphoneEtPhone.getEditText().getText().toString().trim();
        if (StringUtils.isEmpty(phoneNum)) {
            Toaster.showInvalidate(R.string.error_empty_phone_input);
            return;
        }
        if (!StringUtils.isPhone(phoneNum)) {
            Toaster.showInvalidate("请输入正确的手机号");
            return;
        }
        verifyCode = baseLoginByphoneEtCode.getEditText().getText().toString().trim();
        if (StringUtils.isEmpty(verifyCode)) {
            Toaster.showInvalidate(R.string.error_empty_validation_code);
            return;
        }
        if (type == PhoneProto.VCType.PHONE_LOGIN_VALUE) {
            ////手机号登录
            mPresenter.loginPlatformByPhone(phoneNum, verifyCode);
        } else {
            ////手机号注册
            mPresenter.registPlatformByPhone(phoneNum, verifyCode);

        }
    }

    public void gotoMainTabView() {
        Intent intent = new Intent(this, LoginSiteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    @OnClick({R.id.base_login_byphone_btn_getcode, R.id.base_login_byphone_btn_commit, R.id.base_login_byphone_btn_anonymous})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.base_login_byphone_btn_getcode:
                getVerifyCode();
                break;
            case R.id.base_login_byphone_btn_commit:
                submitInfo();
                break;
            case R.id.base_login_byphone_btn_anonymous:
                if (type == PhoneProto.VCType.PHONE_REGISTER_VALUE) {
                    mPresenter.generateLocalIdentity();
                }
                break;
        }
    }

    @Override
    public void onGetVerifyCodeSuccess() {
        baseLoginByphoneBtnGetcode.onStart();
        Toaster.show("发送成功，请注意查收");
    }

    @Override
    public void onTaskStart(String content) {
        KeyboardUtils.hideSoftInput(this);
        showProgressDialog(content);

    }

    @Override
    public void onTaskFinish() {
        hideProgressDialog();
    }

    @Override
    public void onLoginPlatformByPhoneError() {
        Toaster.showInvalidate("请稍候再试");
        startActivity(new Intent(this, LoginByPhoneActivity.class));
    }

    @Override
    public void onLoginPlatformByPhoneSuccess(User user) {
        mPresenter.generateNewIdentity(user);
    }

    @Override
    public void onGenerateNewIdentityError() {
        if (StringUtils.isNotEmpty(ZalyApplication.getGotoUrl())) {
            ZalyApplication.setGotoUrl("");
        }
    }

    @Override
    public void onGenerateNewIdentitySuccess() {
        startActivity(new Intent(this, LoginSiteActivity.class));
        EventBus.getDefault().post(new LoginEvent(-1, null));

    }

    @Override
    public void onGenerateLocalIdentitySuccess() {
        Intent intent = new Intent(this, LoginSiteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onGenerateLocalIdentityError() {
        ZalyApplication.setGotoUrl("");
        Toaster.showInvalidate("生成匿名账户失败，请稍候再试");
    }

    @Override
    public void onRegistPlatformSuccess() {
        gotoMainTabView();
    }

    @Override
    public void onRegistPlatformChangeIdentity(final String platformPubk, final String platformPrik) {
        new MaterialDialog.Builder(this)
                .content("手机号已经绑定，需要更换实名账户")
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
                                ZalyApplication.getCfgSP().putKey(Configs.USER_PUB_KEY, platformPubk);
                                ZalyApplication.getCfgSP().putKey(Configs.USER_PRI_KEY, platformPrik);
                                User user = new User();
                                user.setGlobalUserId(StringUtils.getGlobalUserIdHash(platformPubk));
                                user.setIdentityName(ServerConfig.LOGIN_WITH_PHONE_NAME);
                                user.setIdentitySource(ServerConfig.LOGIN_WITH_PHONE);
                                Long flag = SitePresenter.getInstance().insertUserIdentity(user);

                                if (flag != null) {
                                    SitePresenter.getInstance().updateGlobalUserId(user.getGlobalUserId());
                                    gotoMainTabView();
                                    return;
                                }
                                Toaster.showInvalidate("请稍候再试");
                                break;
                        }
                    }
                })
                .show();
    }
}
