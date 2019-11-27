package com.windchat.client.personal;

import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.windchat.client.Configs;
import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.bean.Site;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.maintab.BaseActivity;
import com.windchat.client.util.NetUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.toast.Toaster;

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
    }

}
