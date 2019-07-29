package com.akaxin.client.personal;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ApiClientForPlatform;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.constant.ServerConfig;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.maintab.ZalyMainActivity;
import com.akaxin.client.platform.task.GetPhoneCode;
import com.akaxin.client.util.NetUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.PhoneProto;
import com.akaxin.proto.platform.ApiPhoneVerifyCodeProto;
import com.akaxin.proto.platform.ApiUserRealNameProto;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/11/3.
 */

public class BindPhoneActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = BindPhoneActivity.class.getSimpleName();

    @BindView(R.id.phone_edit_row)
    View phoneEditRow;
    @BindView(R.id.phone_edit)
    EditText phoneEdit;
    @BindView(R.id.hidden_layout)
    View hiddenLayout;
    @BindView(R.id.code_edit)
    EditText codeEdit;
    @BindView(R.id.get_code)
    Button getCodeButton;
    @BindView(R.id.submit)
    Button submitButton;
    private String phoneId;
    private CountDownTimer countDownTimer;
    private static final int millisInFuture = 60 * 1000;
    private static final int countDownInterval = 1000;
    private static final String PHONE_ALREADY_BIND = "error.phone.same";
    private Site currentSite;

    @Override
    public int getResLayout() {
        return R.layout.activity_bind_phone;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initEvent() {
        getCodeButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        codeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) submitButton.setEnabled(true);
                else submitButton.setEnabled(false);
            }
        });
        countDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                getCodeButton.setEnabled(false);
                getCodeButton.setText(String.format(getString(R.string.time_left), millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                getCodeButton.setText(getString(R.string.send_validation_code));
                getCodeButton.setEnabled(true);
            }
        };

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        setCenterTitle(R.string.bind_phone_num);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        ZalyLogUtils.getInstance().info(TAG, "current site ==" + currentSite.toString());
        phoneId = ZalyApplication.getCfgSP().getKey(Configs.PHONE_ID);
        if (!StringUtils.isEmpty(phoneId)) {
            phoneEdit.setText(phoneId);
            showUneditable();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_code:
                getCode();
                break;
            case R.id.submit:
                submitInfo();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void showUneditable() {
        phoneEdit.setFocusable(false);
        phoneEdit.setEnabled(false);
        phoneEditRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toaster.show("已绑定的手机号暂时不可更改");
            }
        });
        getCodeButton.setVisibility(View.GONE);
        hiddenLayout.setVisibility(View.GONE);
    }

    private void getCode() {
        String phoneNum = phoneEdit.getText().toString().trim();
        if (StringUtils.isEmpty(phoneNum)) {
            Toaster.showInvalidate("请输入手机号码");
            return;
        }
        boolean isNet = NetUtils.getNetInfo();
        if (!isNet) {
            Toaster.show(getString(R.string.without_network_hint));
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new GetCodeTask(phoneNum));
    }

    private void submitInfo() {
        String phoneNum = phoneEdit.getText().toString().trim();
        if (StringUtils.isEmpty(phoneNum)) {
            Toaster.showInvalidate("请输入手机号码");
            return;
        }
        String code = codeEdit.getText().toString().trim();
        if (StringUtils.isEmpty(code)) {
            Toaster.showInvalidate("请输入验证码");
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new SubmitInfoTask(phoneNum, code));
    }


    class GetCodeTask extends GetPhoneCode {

        private String phoneNum;

        public GetCodeTask(String phoneNum) {
            super(phoneNum, PhoneProto.VCType.PHONE_REALNAME_VALUE, ServerConfig.CHINA_COUNTRY_CODE);
            this.phoneNum = phoneNum;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            hideSoftKey();
            showProgress("正在获取验证码...");
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }

        @Override
        protected void onTaskSuccess(ApiPhoneVerifyCodeProto.ApiPhoneVerifyCodeResponse phoneVerifyCodeResponse) {
            super.onTaskSuccess(phoneVerifyCodeResponse);
            if (countDownTimer != null) {
                countDownTimer.start();
            }
        }
    }

    class SubmitInfoTask extends ZalyTaskExecutor.Task<Void, Void, ApiUserRealNameProto.ApiUserRealNameResponse> {

        private String phoneNum;
        private String code;

        public SubmitInfoTask(String phoneNum, String code) {
            this.phoneNum = phoneNum;
            this.code = code;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
            showProgress("正在提交...");
        }

        @Override
        protected ApiUserRealNameProto.ApiUserRealNameResponse executeTask(Void... voids) throws Exception {
            return ApiClient.getInstance(ApiClientForPlatform.getPlatformSite()).getPlatformApi().verifyIdentity(phoneNum,code);

        }

        @Override
        protected void onTaskSuccess(ApiUserRealNameProto.ApiUserRealNameResponse realNameVerifyResponse) {
            super.onTaskSuccess(realNameVerifyResponse);
            //写入phoneId
            ZalyApplication.getCfgSP().putKey(Configs.PHONE_ID, phoneNum);
            phoneEdit.setText(phoneId);
            showUneditable();
            finish();
        }

        @Override
        protected void onTaskError(Exception e) {
            super.onTaskError(e);
            if (e instanceof ZalyAPIException) {
                String errorCode = ((ZalyAPIException) e).getErrorInfoCode();
                checkErrorCode(errorCode);
            }
            Toaster.showInvalidate("绑定失败, 请稍候再试");
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            super.onAPIError(zalyAPIException);
            String errorCode = zalyAPIException.getErrorInfoCode();
            checkErrorCode(errorCode);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
            hideProgress();
        }

        public void checkErrorCode(String errorCode) {
            if (errorCode.equals(PHONE_ALREADY_BIND)) {
                ZalyApplication.getCfgSP().putKey(Configs.PHONE_ID, phoneNum);
                phoneEdit.setText(phoneId);
                showUneditable();
                Toaster.showInvalidate("手机号已经绑定");
            }
        }
    }

}
