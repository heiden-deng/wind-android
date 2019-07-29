package com.akaxin.client.site.view;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.event.LoginEvent;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.site.presenter.impl.SitePresenter;
import com.akaxin.client.util.SPUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.security.RSAUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by yichao on 2018/1/16.
 */

public class LoginWithInputKeyActivity extends BaseActivity implements View.OnClickListener{

    private EditText pubKey, priKey;
    private TextView submit;
    private SitePresenter sitePresenter;
    public static final String LOGIN_WITH_INPUT = "login_with_input";

    @Override
    public int getResLayout() {
        return R.layout.activity_inputkey_login;
    }

    @Override
    public void initView() {
        pubKey = findViewById(R.id.pubKey);
        priKey = findViewById(R.id.priKey);
        submit = findViewById(R.id.submit);
    }

    @Override
    public void initEvent() {
        submit.setOnClickListener(this);
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        setCenterTitle("手动输入公私钥");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                loginWithInputKeyPair();
                break;
        }
    }

    private void loginWithInputKeyPair() {
        String pubKeyStr = pubKey.getText().toString();
        if (StringUtils.isEmpty(pubKeyStr)) {
            Toaster.showInvalidate("请输入公钥");
            return;
        }
        String priKeyStr = priKey.getText().toString();
        if (StringUtils.isEmpty(priKeyStr)) {
            Toaster.showInvalidate("请输入私钥");
            return;
        }
        ZalyApplication.getCfgSP().putKey(Configs.USER_PUB_KEY, pubKeyStr);
        ZalyApplication.getCfgSP().putKey(Configs.USER_PRI_KEY, priKeyStr);
        //生成本机设备公钥
        ZalyTaskExecutor.executeUserTask(TAG, new GenerateNewIdentityTask());
    }

    class GenerateNewIdentityTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress("启动中...");
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            String[] deviceKeyPair = RSAUtils.getInstance().generateNewKeyPairPEMStr();
            if (StringUtils.isEmpty(deviceKeyPair[0]) || StringUtils.isEmpty(deviceKeyPair[1])) {
                Toaster.showInvalidate("登录失败，请稍候再试");
                return false;
            }
            //一部设备对应一用户密钥对，一设备密钥对，所需要存储在上层配置项
            SPUtils spUtils = ZalyApplication.getCfgSP();
            spUtils.putKey(Configs.DEVICE_PRI_KEY, deviceKeyPair[0]);
            spUtils.putKey(Configs.DEVICE_PUB_KEY, deviceKeyPair[1]);

            //需要新建site表 TODO DBChange 检查站点表， 写入身份
//            ZalyDbHelper zalyDbHelper = new ZalyDbHelper(ZalyApplication.getContext());
//            zalyDbHelper.checkBaseTable();
            SitePresenter.getInstance().checkCommonBaseTable();
//            sitePresenter.getInstance().insertUserIdentity("", Configs.getGlobalUserId(), LOGIN_WITH_INPUT);
            return true;
        }

        @Override
        protected void onTaskSuccess(Boolean aBoolean) {
            super.onTaskSuccess(aBoolean);
            if (aBoolean) {
                Intent intent = new Intent(LoginWithInputKeyActivity.this, ZalyMainActivity.class);
                startActivity(intent);
                finish();
                EventBus.getDefault().post(new LoginEvent(-1, null));
            }

        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }
    }
}
