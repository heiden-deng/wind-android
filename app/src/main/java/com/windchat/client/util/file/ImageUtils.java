package com.windchat.client.util.file;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.windchat.client.Configs;
import com.windchat.client.R;
import com.windchat.client.ZalyApplication;
import com.windchat.client.bean.ImageInfo;
import com.windchat.client.bean.Site;
import com.windchat.client.util.data.StringUtils;
import com.windchat.client.util.log.ZalyLogUtils;
import com.windchat.client.util.toast.Toaster;
import com.akaxin.proto.core.FileProto;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by yichao on 2018/1/2.
 */

public class ImageUtils {

    private static final String TAG = "ImageUtils";
    private static final int ROUND_RADIUS = 1;
    private static RequestOptions options;
    private Context mContext;
    private Site currentSite;

    public ImageUtils(Context mContext, Site site) {
        if (mContext instanceof FragmentActivity) {

        } else if (mContext instanceof Activity) {
        } else if (mContext instanceof ContextWrapper) {
            Log.i("lipengfeiImage", "this mContext is Application");
        }

        this.mContext = mContext;

        this.currentSite = site;
    }


    private static int getWidthSize() {
        DisplayMetrics dm = ZalyApplication.getContext().getResources().getDisplayMetrics();
        // return dm.widthPixels/3;
        return 400;
    }

    public static String getImagePath(String imageId) {
        return Configs.getImgDir().getAbsolutePath() + "/" + imageId;
    }

    /**
     * 加载图片
     *
     * @param imageId
     * @param imageView
     */
    public void loadImage(final String imageId, final ImageView imageView) {
        loadByLocalAndSiteDownLoad(imageId, imageView, R.drawable.ic_default, currentSite);
    }

    /**
     * 加载图片
     *
     * @param imageId
     * @param imageView
     */
    public void loadSiteIcon(final String imageId, final ImageView imageView) {
        loadByLocalAndSiteDownLoad(imageId, imageView, R.drawable.site_default_icon, currentSite);
    }


    /**
     * 加载图片
     *
     * @param imageId
     * @param imageView
     */
    public void loadByLocalAndDownLoad(final String imageId, final ImageView imageView) {
        loadByLocalAndDownLoad(imageId, imageView, R.drawable.ic_default, currentSite);
    }

    /**
     * 加载图片
     *
     * @param imageId
     * @param imageView
     */
    public void loadImage(final String imageId, final ImageView imageView, int defaultIcon) {
        loadByLocalAndSiteDownLoad(imageId, imageView, defaultIcon, currentSite);
    }


    /**
     * 加载指定站点上的图片
     *
     * @param imageId
     * @param imageView
     */
    public void loadImage(final String imageId, final ImageView imageView, Site site) {
        loadByLocalAndSiteDownLoad(imageId, imageView, R.drawable.ic_default, site);
    }

    /**
     * 加载指定站点上的图片
     *
     * @param imageId
     * @param imageView
     */
    public void loadSiteImage(final String imageId, final ImageView imageView, Site site) {
        loadByLocalAndSiteDownLoad(imageId, imageView, R.drawable.site_default_icon, site);
    }

    public void loadImage(File file, final ImageView imageView) {
        Glide.with(mContext)
                .load(file)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .apply(getOptions())
                .into(imageView);
    }

    public void loadByLocalAndSiteDownLoad(final String imageId, final ImageView imageView, final int defaultIcon, final Site site) {
        if (StringUtils.isEmpty(imageId) || imageId.equals("null")) {
            loadDefault(imageView, defaultIcon);
            return;
        }
        String imageFilePath = Configs.getImgDir().getAbsolutePath() + "/" + imageId;
        File cacheFile = new File(imageFilePath);
        if (cacheFile != null && cacheFile.exists()) {
            Glide.with(mContext)
                    .load(imageFilePath)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                            downloadImage(imageId, imageView, defaultIcon, site);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                            return false;
                        }
                    })
                    .apply(getOptions())
                    .into(imageView);
        } else {
            try {
                downloadImage(imageId, imageView, defaultIcon, site);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void loadByLocalAndDownLoad(final String imageId, final ImageView imageView, final int defaultIcon, final Site site) {
        if (StringUtils.isEmpty(imageId) || imageId.equals("null")) {
            loadDefault(imageView, defaultIcon);
            return;
        }
        String imageFilePath = Configs.getImgDir().getAbsolutePath() + "/" + imageId;
        File cacheFile = new File(imageFilePath);
        if (cacheFile != null && cacheFile.exists()) {
            Glide.with(mContext)
                    .load(cacheFile)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            //downloadWHImage(imageId, imageView, defaultIcon, site);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (imageView == null) {
                                return false;
                            }

                            ViewGroup.LayoutParams params = imageView.getLayoutParams();
                            params.width = getWidthSize();
                            int vw = imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
                            float scale = (float) vw / (float) resource.getIntrinsicWidth();
                            int vh = Math.round(resource.getIntrinsicHeight() * scale);
                            params.height = vh + imageView.getPaddingTop() + imageView.getPaddingBottom();
                            imageView.setLayoutParams(params);
                            return false;
                        }
                    })
                    .apply(getOptions())
                    .into(imageView);

        } else {
            downloadWHImage(imageId, imageView, defaultIcon, site);
        }

    }


    public void loadByLocalIDAndNetWithoutLocalFile(final ImageInfo imageInfo, final ImageView imageView, final int defaultIcon, final Site site) {
        if (StringUtils.isEmpty(imageInfo.getFileId()) || imageInfo.getFileId().equals("null")) {
            loadDefault(imageView, defaultIcon);
            return;
        }
        String imageFilePath = Configs.getImgDir().getAbsolutePath() + "/" + imageInfo.getFileId();
        File cacheFile = new File(imageFilePath);
        if (cacheFile != null && cacheFile.exists()) {
            Glide.with(mContext)
                    .load(cacheFile)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .apply(getOptions())
                    .into(imageView);
        } else {
            /**
             * 1.本地有 显示本地优先  子线程尽管开线程去下载图片
             * 2.若本地没有 加载default图片 ,然后下载完加载最新图片
             */
            File localFile = new File(imageInfo.getFilePath());
            if (localFile.exists()) {
                loadOnlyByLocalFile(localFile, imageView);
            } else {
                //下载完再加载
                loadDefault(imageView);
                downloadWHImage(imageInfo.getFileId(), imageView, defaultIcon, site);
            }
            //这步应该只下载不加载
        }

    }


    /**
     * 下载图片
     *
     * @param imageId
     * @param imageView
     */
    private void downloadImage(String imageId, final ImageView imageView, final int defaultIcon, Site site) {
        ZalyLogUtils.getInstance().info(TAG, imageId);
        String imageFilePath = Configs.getImgDir().getAbsolutePath() + "/" + imageId;
        if (site == null) {
            loadDefault(imageView);
            return;
        }
        UploadFileUtils.downloadFile(imageId, imageFilePath, FileProto.FileType.MESSAGE_IMAGE, new UploadFileUtils.DownloadFileListener() {
            @Override
            public void onDownloadStartInBackground() {
            }

            @Override
            public void onDownloadCompleteInBackground(String fileId, String filePath) {

            }

            @Override
            public void onDownloadSuccess(String fileId, String filePath) {
                Glide.with(mContext)
                        .load(new File(filePath))
                        .apply(getOptions())
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(imageView);

            }

            @Override
            public void onDownloadFail(Exception e) {
                loadDefault(imageView, defaultIcon);
            }
        }, site);
    }


    private void downloadWHImage(String imageId, final ImageView imageView, final int defaultIcon, Site site) {
        ZalyLogUtils.getInstance().info(TAG, imageId);
        final String imageFilePath = Configs.getImgDir().getAbsolutePath() + "/" + imageId;
        UploadFileUtils.downloadFile(imageId, imageFilePath, FileProto.FileType.MESSAGE_IMAGE, new UploadFileUtils.DownloadFileListener() {
            @Override
            public void onDownloadStartInBackground() {
            }

            @Override
            public void onDownloadCompleteInBackground(String fileId, String filePath) {

            }

            @Override
            public void onDownloadSuccess(String fileId, String filePath) {
                Glide.with(mContext)
                        .load(new File(imageFilePath))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                                loadDefault(imageView);


                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                return false;
                            }
                        })
                        .apply(getOptions())
                        .into(imageView);
            }

            @Override
            public void onDownloadFail(Exception e) {
                loadDefault(imageView, defaultIcon);
            }
        }, site);

        loadOnlyByLocalFile(new File(imageFilePath), imageView);

    }


    public void loadOnlyByLocalFile(final File file, final ImageView imageView) {
        Glide.with(mContext)
                .load(file)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if (imageView == null) {
                            return false;
                        }
                        if (imageView.getScaleType() != ImageView.ScaleType.FIT_XY) {
                            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        }
                        ViewGroup.LayoutParams params = imageView.getLayoutParams();
                        params.width = getWidthSize();
                        int vw = imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
                        float scale = (float) vw / (float) resource.getIntrinsicWidth();
                        int vh = Math.round(resource.getIntrinsicHeight() * scale);
                        params.height = vh + imageView.getPaddingTop() + imageView.getPaddingBottom();
                        imageView.setLayoutParams(params);
                        return false;
                    }
                })
                .apply(getOptions())
                .into(imageView);
    }


    public void loadDefault(ImageView imageView) {
        loadDefault(imageView, R.drawable.ic_default);
    }

    /**
     * 加载默认图片
     *
     * @param imageView
     */
    public void loadDefault(ImageView imageView, int defaultIcon) {

        Glide.with(mContext)
                .load(defaultIcon)
                .apply(getOptions())
                .into(imageView);
    }

    public static RequestOptions getOptions() {
        if (options == null) {
            options = new RequestOptions();
//            options.transforms(new CenterCrop(), new RoundedCorners(ROUND_RADIUS));
            options.placeholder(R.drawable.ic_default);
            options.error(R.drawable.ic_default);
            options.dontAnimate();
            options.diskCacheStrategy(DiskCacheStrategy.ALL);

        }
        return options;
    }


    /**
     * 长按保存在相册
     *
     * @param context
     */
    public static boolean saveImageToAlbum(Context context, Bitmap bitmap) {
        // 首先保存图片
        String externalStoragePath = Environment.getExternalStorageDirectory().getPath();
        File appDir = new File(externalStoragePath, "akaxin");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();

            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);

            Uri contentUri = Uri.fromFile(file);
            // 最后通知图库更新
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri));
            Toaster.showInvalidate("保存成功");
        } catch (Exception e) {
            ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
        }
        return true;
    }


}
