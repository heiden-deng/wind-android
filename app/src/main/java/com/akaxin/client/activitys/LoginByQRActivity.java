package com.akaxin.client.activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.widget.ImageView;

import com.akaxin.client.R;
import com.akaxin.client.bean.User;
import com.akaxin.client.bean.event.LoginEvent;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.mvp.BaseMVPActivity;
import com.akaxin.client.mvp.contract.LoginByQRContract;
import com.akaxin.client.mvp.presenter.LoginByQRPresenter;
import com.akaxin.client.register.LoginSiteActivity;
import com.akaxin.client.util.QRUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.security.AESUtils;
import com.akaxin.proto.core.PhoneProto;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Mr.kk on 2018/5/5.
 * This Project was client-android
 */

public class LoginByQRActivity extends BaseMVPActivity<LoginByQRContract.View, LoginByQRPresenter> implements LoginByQRContract.View {
    @BindView(R.id.base_login_qr_image)
    ImageView baseLoginQrImage;
    private int qrCodeBlackColor;
    private int qrCodeWhiteColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_login_qr);
        ButterKnife.bind(this);
        initToolBar();
        qrCodeBlackColor = Color.BLACK;
        qrCodeWhiteColor = Color.WHITE;
        //setCenterTitle("授权协议密钥");
        setCenterTitle(getString(R.string.login_with_other_device));

        new Thread(new Runnable() {
            @Override
            public void run() {
                initQRData();
            }
        }).start();
    }


    private void initQRData() {
        String filePath = UUID.randomUUID().toString();
        byte[] tsk = AESUtils.generateTSKey();
        String tskStr = Base64.encodeToString(tsk, Base64.NO_WRAP);
        HashMap<String, String> data = new HashMap<>();
        data.put("action", "wait_auth");
        data.put("address", filePath);
        data.put("tsKey", tskStr);
        String qrStr = new JSONObject(data).toString();
        ZalyLogUtils.getInstance().info(TAG, "qrStr:" + qrStr);
        try {
            final Bitmap bitmap = QRUtils.encodeAsBitmap(qrStr, (int) getResources().getDimension(R.dimen.size_qrcode), qrCodeBlackColor, qrCodeWhiteColor);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    baseLoginQrImage.setImageBitmap(bitmap);
                }
            });
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, "error msg is " + e.getMessage());
        }
        mPresenter.getTempSpaceContent(filePath, tsk);
    }

    @OnClick(R.id.vp_invitation_btn_next)
    public void onViewClicked() {
        Intent intent = new Intent(this, LoginByPhoneActivity.class);
        intent.putExtra(IntentKey.VC_TYPE, PhoneProto.VCType.PHONE_LOGIN_VALUE);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void getTempSpaceContentSuccess(User user) {
        mPresenter.generateNewIdentityTask(user);
    }

    @Override
    public void getTempSpaceContentError(Exception e, final String spaceKey,
                                         final byte[] tsk) {
        baseLoginQrImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPresenter.getTempSpaceContent(spaceKey, tsk);
            }
        }, 2000);
    }

    @Override
    public void generateNewIdentityTaskSuccess() {
        Intent intent = new Intent(this, LoginSiteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        EventBus.getDefault().post(new LoginEvent(-1, null));
    }

    @Override
    public void onTaskStart(String content) {
        showProgressDialog(content);
    }

    @Override
    public void onTaskFinish() {
        hideProgressDialog();
    }

}


