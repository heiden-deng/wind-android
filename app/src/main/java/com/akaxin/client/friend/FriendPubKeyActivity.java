package com.akaxin.client.friend;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.akaxin.client.R;
import com.akaxin.client.bean.TipMsg;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.toast.Toaster;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhangjun on 2018/2/28.
 */

public class FriendPubKeyActivity extends BaseActivity {

    public static final String KEY_USER_NAME = "key_user_name";
    public static final String KEY_PUB_KEY = "key_pub_key";

    @BindView(R.id.pub_key) TextView pubKeyTV;
    private String pubKey;

    @Override
    public int getResLayout() {
        return R.layout.activity_friend_key;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initEvent() {
        pubKeyTV.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toaster.showInvalidate(TipMsg.TIP_MSG_FOR_COPY);
                ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(pubKey); //将内容放入粘贴管理器,在别的地方长按选择"粘贴"即可
                return true;
            }
        });
    }

    @Override
    public void onLoadData() {
        pubKey = getIntent().getStringExtra(KEY_PUB_KEY);
        String userName = getIntent().getStringExtra(KEY_USER_NAME);
        String title = userName + "的数字证书";
        setCenterTitle(title);
        pubKeyTV.setText(pubKey);
    }

    @Override
    public void initPresenter() {

    }

}
