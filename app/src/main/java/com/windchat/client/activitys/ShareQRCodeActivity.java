package com.windchat.client.activitys;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.windchat.client.R;
import com.windchat.client.bean.Site;
import com.windchat.client.constant.IntentKey;
import com.windchat.client.mvp.BaseMVPActivity;
import com.windchat.client.mvp.contract.ShareQRCodeContract;
import com.windchat.client.mvp.presenter.ShareQRCodePresenter;
import com.windchat.client.util.QRUtils;
import com.windchat.client.util.ClipboardUtils;
import com.windchat.client.util.UrlUtils;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.file.ImageUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.site.ApiGroupApplyTokenProto;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Mr.kk on 2018/6/29.
 * This Project was client-android
 */

public class ShareQRCodeActivity extends BaseMVPActivity<ShareQRCodeContract.View, ShareQRCodePresenter> implements ShareQRCodeContract.View {
    @BindView(R.id.qr_group_image)
    CircleImageView qrGroupImage;
    @BindView(R.id.qr_group_name)
    TextView qrGroupName;
    @BindView(R.id.qr_group_site_address)
    TextView qrGroupSiteAddress;
    @BindView(R.id.qr_group_card)
    CardView qrGroupCard;
    private Site currentSite;
    String groupId, groupName, shareUrl;
    private int qrCodeBlackColor = Color.BLACK;
    private int qrCodeWhiteColor = Color.WHITE;
    @BindView(R.id.group_qr_code_image)
    ImageView loginQRImage;
    @BindView(R.id.group_qr_code_valid_time)
    TextView GroupQrCodeValidTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_qr_code);
        ButterKnife.bind(this);
        initToolBar();
        getData();

    }

    private void getData() {
        switch (getIntent().getIntExtra(IntentKey.KEY_QR_CODE_TYPE, 0)) {
            case IntentKey.KEY_TYPE_USER:
                showUser();
                break;
            case IntentKey.KEY_TYPE_SITE:
                showSite();
                break;
            case IntentKey.KEY_TYPE_GROUP:
                showGroup();
                break;
        }
    }

    private void showGroup() {
        groupId = getIntent().getStringExtra(IntentKey.KEY_GROUP_ID);
        groupName = getIntent().getStringExtra(IntentKey.KEY_GROUP_NAME);
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        qrGroupName.setText(groupName);
        qrGroupSiteAddress.setText("站点地址: " + currentSite.getSiteAddress());
        qrGroupImage.setImageDrawable(getResources().getDrawable(R.drawable.avatar_group_default));
        setTitle("扫描进群", currentSite.getSiteName() + "|" + currentSite.getSiteAddress());
        mPresenter.getGroupToken(currentSite, groupId);
    }

    private void showSite() {
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(currentSite.getSiteIcon())
//                .setFileType(FileProto.FileType.SITE_ICON)
//                .setSite(currentSite)
//                .build();
//        Glide.with(this).load(model).
//                apply(new RequestOptions().dontAnimate().error(R.drawable.site_default_icon).fallback(R.drawable.site_default_icon)).into(qrGroupImage);

        new ImageUtils(this, currentSite).loadImage(currentSite.getSiteIcon(), qrGroupImage, R.drawable.ic_default);
        qrGroupName.setText(currentSite.getSiteName());
        qrGroupSiteAddress.setText("站点地址: " + currentSite.getSiteAddress());
        setTitle("扫描进站", currentSite.getSiteName() + "|" + currentSite.getSiteAddress());
        shareUrl = UrlUtils.buildShareLinkForSite(currentSite.getHostAndPort());
        new QRThread(shareUrl).start();
        ZalyLogUtils.getInstance().info(TAG, shareUrl);
    }

    private void showUser() {
        currentSite = getIntent().getParcelableExtra(IntentKey.KEY_CURRENT_SITE);
        String userName = getIntent().getStringExtra(IntentKey.KEY_USER_NAME);
        String userPhoto = getIntent().getStringExtra(IntentKey.KEY_USER_HEAD);
        String userID = getIntent().getStringExtra(IntentKey.KEY_USER_ID);
        qrGroupName.setText(userName);

//        ZalyGlideModel model = new ZalyGlideModel.Builder()
//                .setImageID(userPhoto)
//                .setFileType(FileProto.FileType.USER_PORTRAIT)
//                .setSite(currentSite)
//                .build();
//        Glide.with(this).load(model).
//                apply(new RequestOptions()
//                        .dontAnimate()
//                        .error(R.drawable.ic_default)
//                        .fallback(R.drawable.ic_default))
//                .into(qrGroupImage);
        new ImageUtils(this, currentSite).loadImage(userPhoto, qrGroupImage, R.drawable.ic_default);
        qrGroupSiteAddress.setText("站点地址: " + currentSite.getSiteAddress());
        setTitle("扫描加我", currentSite.getSiteName() + "|" + currentSite.getSiteAddress());
        shareUrl = UrlUtils.buildShareLinkForUser(currentSite.getHostAndPort(), userID);
        new QRThread(shareUrl).start();
        ZalyLogUtils.getInstance().info(TAG, shareUrl);
    }

    @Override
    public void onTaskStart(String content) {

    }

    @Override
    public void onTaskFinish() {

    }

    @Override
    public void onGetGroupTokenSuccess(ApiGroupApplyTokenProto.ApiGroupApplyTokenResponse apiGroupApplyTokenResponse) {
        shareUrl = UrlUtils.buildShareGroupQR(currentSite.getHostAndPort(), groupId, apiGroupApplyTokenResponse.getToken());
        new QRThread(shareUrl).start();
        ZalyLogUtils.getInstance().info(TAG, shareUrl);
    }

    @Override
    public void onTaskError() {

    }

    @OnClick({R.id.group_qr_code_btn_save_image, R.id.group_qr_code_btn_save_url})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.group_qr_code_btn_save_image:
                viewSaveToImage(qrGroupCard);
                break;
            case R.id.group_qr_code_btn_save_url:
                if (StringUtils.isEmpty(shareUrl))
                    break;
                if (ClipboardUtils.copyToClipboard(UrlUtils.buildShareLinkForSite(shareUrl)))
                    Toaster.show(R.string.share_copied);
                break;
        }
    }

    private Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();

        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);
        /** 如果不设置canvas画布为白色，则生成透明 */

        //    v.layout(0, 0, w, h);
        v.draw(c);

        return bmp;
    }

    private void viewSaveToImage(View view) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);

        // 把一个View转换成图片
        Bitmap cachebmp = loadBitmapFromView(view);

        FileOutputStream fos;
        String imagePath = "";
        try {
            // 判断手机设备是否有SD卡
            boolean isHasSDCard = Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED);
            if (isHasSDCard) {
                // SD卡根目录
                File sdRoot = Environment.getExternalStorageDirectory();
                File file = new File(sdRoot, Calendar.getInstance().getTimeInMillis() + ".png");
                fos = new FileOutputStream(file);
                imagePath = file.getAbsolutePath();
            } else
                throw new Exception("创建文件失败!");

            cachebmp.compress(Bitmap.CompressFormat.PNG, 90, fos);

            fos.flush();
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Toaster.show("保存图片成功");
        ZalyLogUtils.getInstance().info(TAG, "imagePath=" + imagePath);
        view.destroyDrawingCache();
    }


    class QRThread extends Thread {
        String shareUrl;

        public QRThread(String shareUrl) {
            this.shareUrl = shareUrl;
        }

        @Override
        public void run() {
            super.run();
            try {
                final Bitmap bitmap = QRUtils.encodeAsBitmap(shareUrl, (int) getResources().getDimension(R.dimen.size_qrcode), qrCodeBlackColor, qrCodeWhiteColor);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginQRImage.setImageBitmap(QRUtils.addLogo(bitmap, com.blankj.utilcode.util.ImageUtils.getBitmap(R.drawable.ic_default)));
                    }
                });
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }
}
