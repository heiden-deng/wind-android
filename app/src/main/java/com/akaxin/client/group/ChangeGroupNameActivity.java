package com.akaxin.client.group;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.akaxin.client.R;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.toast.Toaster;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by yichao on 2017/10/31.
 */

public class ChangeGroupNameActivity extends BaseActivity implements View.OnClickListener {

    public static final int REQUEST_CODE = 1;

    public static final String KEY_OLD_NAME = "key_old_name";
    public static final String KEY_GROUP_NAME = "key_group_name";
    @BindView(R.id.group_name_edit) EditText groupName;
    @BindView(R.id.action_confirm) LinearLayout actionConfirm;

    @Override
    public int getResLayout() {
        return R.layout.activity_change_group_name;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        setCenterTitle(getString(R.string.change_group_name));
    }

    @Override
    public void initEvent() {
        actionConfirm.setOnClickListener(this);
    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        String oldName = getIntent().getStringExtra(KEY_OLD_NAME);
        if (!StringUtils.isEmpty(oldName)) groupName.setText(oldName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_confirm:
                String username = groupName.getText().toString().trim();
                if (StringUtils.isEmpty(username)) {
                    Toaster.showInvalidate("请输入群名");
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(KEY_GROUP_NAME, username);
                setResult(RESULT_OK, intent);
                hideSoftKey();
                finish();
                break;
        }
    }

}