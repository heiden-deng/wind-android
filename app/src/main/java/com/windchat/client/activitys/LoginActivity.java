package com.windchat.client.activitys;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.windchat.client.R;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.maintab.BaseActivity;
import com.windchat.client.util.log.ZalyLogUtils;
import com.akaxin.proto.core.PhoneProto;
import com.blankj.utilcode.util.PermissionUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mr.kk on 2018/5/5.
 * This Project was client-android
 */

public class LoginActivity extends BaseActivity implements PermissionUtils.OnRationaleListener {

    public static final String TAG = "LoginActivity";

    public static final int KILL_ACTIVITY = 1;
    public static LoginActivity loginActivity;
    @BindView(R.id.base_login_btn_login)
    Button baseLoginBtnLogin;
    @BindView(R.id.base_login_btn_regist)
    Button baseLoginBtnRegist;
    @BindView(R.id.tv_url)
    TextView tvUrl;

    @Override
    public int getResLayout() {
        return R.layout.activity_base_login;
    }

    @Override
    public void initView() {
        ZalyLogUtils.getInstance().info(TAG, "LoginActivity onCreate");
        ButterKnife.bind(this);
        loginActivity = this;
        addActivity(this);
        PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE).rationale(this).callback(new PermissionUtils.FullCallback() {
            @Override
            public void onGranted(List<String> permissionsGranted) {
            }

            @Override
            public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                new MaterialDialog.Builder(LoginActivity.this)
                        .content(R.string.reject_permiss_dialog_notice)
                        .positiveText("好的")
                        .negativeText("取消")
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                if (which == DialogAction.POSITIVE) {
                                    PermissionUtils.launchAppDetailsSettings();
                                } else {
                                    finish();
                                }
                            }
                        }).cancelable(false)
                        .show();
            }
        }).request();

    }

    @Override
    public void initEvent() {

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ZalyLogUtils.getInstance().info(TAG, "activity for result is finish");
        if (resultCode == KILL_ACTIVITY) {
            finish();
        }
    }

    @OnClick({R.id.base_login_btn_login, R.id.base_login_btn_regist, R.id.tv_url})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.base_login_btn_login:
                startActivity(new Intent(LoginActivity.this, LoginByQRActivity.class));
                break;
            case R.id.base_login_btn_regist:
                Intent intent = new Intent(LoginActivity.this, LoginByPhoneActivity.class);
                intent.putExtra(IntentKey.VC_TYPE, PhoneProto.VCType.PHONE_REGISTER_VALUE);
                startActivity(intent);
                break;
            case R.id.tv_url:
                Intent webIntent = new Intent();
                webIntent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(getString(R.string.md_url));
                webIntent.setData(content_url);
                startActivity(webIntent);
                break;
        }
    }

    @Override
    public void rationale(final ShouldRequest shouldRequest) {
        new MaterialDialog.Builder(LoginActivity.this)
                .content("使用风信必须拥有文件读写的权限,请赋予我权限")
                .positiveText("好的")
                .negativeText("取消")
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (which == DialogAction.POSITIVE) {
                            shouldRequest.again(true);
                        } else {
                            shouldRequest.again(true);
                        }
                        dialog.dismiss();
                    }
                }).cancelable(false)
                .show();
    }
}
