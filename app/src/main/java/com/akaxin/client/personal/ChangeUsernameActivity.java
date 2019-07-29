package com.akaxin.client.personal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yichao on 2017/10/31.
 */

public class ChangeUsernameActivity extends BaseActivity {
    public static final String TAG = ChangeUsernameActivity.class.getSimpleName();

    public static final int REQUEST_CODE = 123;
    public static final int SITE_LOGIN_ID_CODE = 124;
    public static final int SITE_REMARK_NAME_CODE = 124;
    public static final String KEY_TYPE = "key_TYPE";
    public static final int SITE_LOGIN_ID = 1;
    public static final int CHANG_USER_NAME = 2;
    public static final int SITE_REMARK_NAME = 3;


    @BindView(R.id.nickname_edit)
    EditText userName;
    @BindView(R.id.action_confirm)
    LinearLayout actionConfirm;
    @BindView(R.id.input_tip_text)
    TextView inputTipText;
    private String userImageId;

    @Override
    public int getResLayout() {
        return R.layout.activity_change_username;
    }

    public int keyType;

    @Override
    public void initView() {
        ButterKnife.bind(this);
        keyType = getIntent().getIntExtra(KEY_TYPE, 2);
        switch (keyType) {
            case ChangeUsernameActivity.CHANG_USER_NAME:
                setCenterTitle(getString(R.string.change_username));
                userName.setHint("昵称");
                userImageId = getIntent().getStringExtra(IntentKey.KEY_USER_IMAGE_ID);
                String oldName = getIntent().getStringExtra(IntentKey.KEY_OLD_USER_NAME);
                ZalyLogUtils.getInstance().errorToInfo(TAG, " user name is " + oldName);
                if (!StringUtils.isEmpty(oldName))
                    userName.setText(oldName);
                break;
            case ChangeUsernameActivity.SITE_LOGIN_ID:
                setCenterTitle(getString(R.string.set_site_login_id));
                userName.setHint("设置站点账户");
                userName.setMaxLines(20);
                inputTipText.setVisibility(View.VISIBLE);
                inputTipText.setText(getString(R.string.input_site_name_tip));
                userImageId = getIntent().getStringExtra(IntentKey.KEY_USER_IMAGE_ID);
                ZalyLogUtils.getInstance().info(TAG, "  user image id ==" + userImageId);
                break;
            case ChangeUsernameActivity.SITE_REMARK_NAME:
                setCenterTitle(getString(R.string.edit_remark_name));
                userName.setHint("备注");
                String oldRemarkName = getIntent().getStringExtra(IntentKey.KEY_OLD_REMARK_NAME);
                if (!StringUtils.isEmpty(oldRemarkName))
                    userName.setText(oldRemarkName);
                userImageId = getIntent().getStringExtra(IntentKey.KEY_USER_IMAGE_ID);

                break;
        }
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

    @OnClick({R.id.action_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.action_confirm:
                switch (keyType) {
                    case ChangeUsernameActivity.CHANG_USER_NAME:
                        String username = userName.getText().toString().trim();
                        if (StringUtils.isEmpty(username)) {
                            Toaster.showInvalidate("请输入昵称");
                            return;
                        }
                        Intent intent = new Intent();
                        intent.putExtra(IntentKey.KEY_USER_NAME, username);
                        intent.putExtra(IntentKey.KEY_USER_IMAGE_ID, userImageId);
                        setResult(RESULT_OK, intent);
                        hideSoftKey();
                        finish();
                        break;
                    case ChangeUsernameActivity.SITE_LOGIN_ID:
                        String siteLoginId = userName.getText().toString().trim();
                        if (StringUtils.isEmpty(siteLoginId)) {
                            Toaster.showInvalidate("请输入用户名");
                            return;
                        }
                        if (siteLoginId.length() > 16 || siteLoginId.length() < 3) {
                            Toaster.showInvalidate("用户名长度为3到16位");
                            return;
                        }
                        if (!StringUtils.isWordOrNumOnly(siteLoginId)) {
                            Toaster.showInvalidate("用户名只能包含英文，数字");
                            return;
                        }
                        Intent intentSiteLoginId = new Intent();
                        intentSiteLoginId.putExtra(IntentKey.KEY_SITE_LOGIN_ID, siteLoginId);
                        intentSiteLoginId.putExtra(IntentKey.KEY_USER_IMAGE_ID, userImageId);
                        setResult(RESULT_OK, intentSiteLoginId);
                        hideSoftKey();
                        finish();
                        break;
                    case ChangeUsernameActivity.SITE_REMARK_NAME:
                        String remarkName = userName.getText().toString().trim();
                        Intent remarkNameIntent = new Intent();
                        remarkNameIntent.putExtra(IntentKey.KEY_REMARK_NAME, remarkName);
                        setResult(RESULT_OK, remarkNameIntent);
                        hideSoftKey();
                        finish();
                        break;
                }

                break;

        }
    }


}