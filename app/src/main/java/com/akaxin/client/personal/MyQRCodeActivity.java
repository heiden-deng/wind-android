package com.akaxin.client.personal;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.akaxin.client.maintab.BaseActivity;
import com.akaxin.client.fragments.PersonalFragment;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.R;
import com.akaxin.client.util.UIUtils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by yichao on 2017/11/3.
 */

public class MyQRCodeActivity extends BaseActivity {

    private ImageView qrImg;

    private int qrCodeBlackColor;
    private int qrCodeWhiteColor;

    @Override
    public int getResLayout() {
        return R.layout.activity_my_qrcode;
    }

    @Override
    public void initView() {
        qrImg = findViewById(R.id.qr_image);
        qrCodeBlackColor = getResources().getColor(R.color.black);
        qrCodeWhiteColor = getResources().getColor(R.color.white);
    }

    @Override
    public void initEvent() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void initPresenter() {

    }

    @Override
    public void onLoadData() {
        String spaceKey = getIntent().getStringExtra(PersonalFragment.KEY_SPACE_KEY);
        HashMap<String, String> data = new HashMap<>();
        data.put("action", "my_global_user_id");
        data.put("data", spaceKey);
        String json = new JSONObject(data).toString();
        if (StringUtils.isEmpty(json)) {
            finish();
            return;
        }
        try {
            Bitmap bitmap = encodeAsBitmap(json, UIUtils.getPixels(267), qrCodeBlackColor, qrCodeWhiteColor);
            qrImg.setImageBitmap(bitmap);
        } catch (Exception e) {
            ZalyLogUtils.getInstance().exceptionError(e);
        }
    }
}
