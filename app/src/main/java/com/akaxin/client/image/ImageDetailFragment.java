package com.akaxin.client.image;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.akaxin.client.Configs;
import com.akaxin.client.R;
import com.akaxin.client.ZalyApplication;
import com.akaxin.client.bean.ImageInfo;
import com.akaxin.client.bean.Message;
import com.akaxin.client.bean.Site;
import com.akaxin.client.constant.IntentKey;
import com.akaxin.client.image.PhotoView.PhotoViewAttacher;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.file.ImageUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.toast.Toaster;
import com.akaxin.proto.core.CoreProto;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mr.kk on 2018/5/23.
 * This Project was client-android
 */

public class ImageDetailFragment extends Fragment implements View.OnClickListener, RequestListener<Drawable> {
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.fb_save_img)
    ImageView fbSaveImg;
    @BindView(R.id.content)
    FrameLayout content;

    private PhotoViewAttacher mAttacher;
    private Message message;
    private Site currentSite;

    public static ImageDetailFragment newInstance(Message message, Site currentSite) {
        final ImageDetailFragment f = new ImageDetailFragment();
        final Bundle args = new Bundle();
        args.putParcelable("message", message);
        args.putParcelable(IntentKey.KEY_CURRENT_SITE, currentSite);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        message = getArguments() != null ? (Message) getArguments().getParcelable("message") : null;
        currentSite = getArguments() != null ? (Site) getArguments().getParcelable(IntentKey.KEY_CURRENT_SITE) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        ButterKnife.bind(this, v);
        mAttacher = new PhotoViewAttacher(image);
        content.setOnClickListener(this);
        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View arg0, float arg1, float arg2) {
                getActivity().finish();
            }
        });
        ZalyLogUtils.getInstance().info("shaoye ", "图片下载界面");
        mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ZalyLogUtils.getInstance().info("shaoye ", "图片开始下载界面");
                ImageUtils.saveImageToAlbum(ZalyApplication.getContext(), ((BitmapDrawable) image.getDrawable()).getBitmap());
                return true;
            }
        });
        fbSaveImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageUtils.saveImageToAlbum(ZalyApplication.getContext(), ((BitmapDrawable) image.getDrawable()).getBitmap());
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageInfo imageInfo = ImageInfo.parseJSON(message.getContent());
//

        if (StringUtils.isEmpty(imageInfo.getFileId()) || message.getMsgType() == CoreProto.MsgType.SECRET_IMAGE_VALUE) {
            File cacheFile = new File(imageInfo.getFilePath());
            if (cacheFile != null && cacheFile.exists()) {
                Glide.with(getActivity())
                        .load(cacheFile)
                        .listener(this).into(image);
            } else {
                Toaster.show("1");
            }
        } else {
            String imageFilePath = Configs.getImgDir().getAbsolutePath() + "/" + imageInfo.getFileId();
            File cacheFile = new File(imageFilePath);
            if (cacheFile != null && cacheFile.exists()) {
                Glide.with(getActivity())
                        .load(cacheFile)
                        .listener(this).into(image);
            } else {
                cacheFile = new File(imageInfo.getFilePath());
                Glide.with(getActivity())
                        .load(cacheFile)
                        .listener(this).into(image);
            }
        }

    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
      //  loading.setVisibility(View.GONE);
        fbSaveImg.setVisibility(View.GONE);
        return false;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
      //  loading.setVisibility(View.GONE);
        mAttacher.update();
        fbSaveImg.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
