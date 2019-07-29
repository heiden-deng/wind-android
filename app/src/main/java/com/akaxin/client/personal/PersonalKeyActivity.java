package com.akaxin.client.personal;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.TipMsg;
import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.util.toast.Toaster;

/**
 * Created by yichao on 2018/1/20.
 */

public class PersonalKeyActivity extends BaseActivity implements View.OnClickListener {

    private TextView priKeyTv, pubKeyTv, priKeyTitle, pubKeyTitle;
    private String priKey, pubKey;
    private LinearLayout personal_item_pubkey, personal_item_priKey;
    boolean isFold = true;

    @Override
    public int getResLayout() {
        return R.layout.activity_personal_key;
    }

    @Override
    public void initView() {
        setCenterTitle(R.string.show_self_keys);
        priKeyTv = findViewById(R.id.priKey);
        pubKeyTv = findViewById(R.id.pubKey);
        personal_item_pubkey = findViewById(R.id.personal_item_pubkey);
        personal_item_priKey = findViewById(R.id.personal_item_priKey);
        pubKeyTitle = findViewById(R.id.pubKeyTitle);
        priKeyTitle = findViewById(R.id.priKeyTitle);
        personal_item_pubkey.setOnClickListener(this);
        personal_item_priKey.setOnClickListener(this);
        showFold();
    }

    @Override
    public void initEvent() {
        priKeyTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toaster.show(TipMsg.TIP_MSG_FOR_COPY);
                ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(priKey); //将内容放入粘贴管理器,在别的地方长按选择"粘贴"即可
                return true;
            }
        });

        pubKeyTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toaster.show(TipMsg.TIP_MSG_FOR_COPY);
                ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setText(pubKey); //将内容放入粘贴管理器,在别的地方长按选择"粘贴"即可
                return true;
            }
        });

    }

    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        priKey = ZalyApplication.getCfgSP().getKey(Configs.USER_PRI_KEY);
        priKeyTv.setText("长按复制\n" + priKey);
        pubKey = ZalyApplication.getCfgSP().getKey(Configs.USER_PUB_KEY);
        pubKeyTv.setText("长按复制\n" + pubKey);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.personal_item_priKey:
                isFold = !isFold;
                showFold();
                break;
        }
    }

    private void showFold() {
        if (isFold) {
            priKeyTitle.setText("▲ " + getString(R.string.user_id_prik));
            priKeyTv.setVisibility(View.GONE);
        } else {
            priKeyTitle.setText("▼ " + getString(R.string.user_id_prik));
            priKeyTv.setVisibility(View.VISIBLE);
        }
    }
}
