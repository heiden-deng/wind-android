package com.windchat.client.activitys;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.mvp.BaseMVPActivity;
import com.windchat.client.mvp.contract.ImageShowContract;
import com.windchat.client.mvp.presenter.ImageShowPresenter;
import com.windchat.client.util.file.ImageUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mr.kk on 2018/7/9.
 * This Project was client-android
 */

public class ImageShowActivity extends BaseMVPActivity<ImageShowContract.View, ImageShowPresenter> implements ImageShowContract.View {
    @BindView(R.id.image_show_iv)
    ImageView imageShowIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        ButterKnife.bind(this);
        initToolBar();
        String path = getIntent().getStringExtra(IntentKey.KEY_USER_HEAD);
        Site site = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        new ImageUtils(this, site).loadByLocalAndSiteDownLoad(path, imageShowIv, R.drawable.ic_default, site);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(path)
//                .setFileType(FileProto.FileType.USER_PORTRAIT)
//                .setSite(site)
//                .build();
//        Glide.with(this).load(model).
//                apply(new RequestOptions()
//                        .dontAnimate()
//                        .error(R.drawable.ic_default)
//                        .fallback(R.drawable.ic_default))
//                .into(this.imageShowIv);
        setCenterTitle("查看大图");
    }

    @Override
    public void onTaskStart(String content) {

    }

    @Override
    public void onTaskFinish() {

    }

    @Override
    public void onTaskError() {

    }
}
