/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.akaxin.client.util.file;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.akaxin.client.ZalyApplication;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;

import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple subclass of {@link } that resizes images from resources given a target width
 * and height. Useful for when the input images might be too large to simply load directly into
 * memory.
 */
public class ImageResizer {
    private static final String TAG = "ImageResizer";

    protected static int maxWidth = 400;
    protected static int maxHeight = 400;
    protected static int maxWindowHeight = 1000;

    public static final String IMG_WIDTH = "width";
    public static final String IMG_HEIGHT = "height";

    protected int mImageWidth;
    protected int mImageHeight;

    protected static int width;
    protected static int height;


    public static Map<String, Integer> getImageRatioInfo(String fileId) {
        Map<String, Integer> imgInfo = new HashMap<>();
        int originWidth = StringUtils.getInfoForFileId(fileId, 1);
        int originHeight = StringUtils.getInfoForFileId(fileId, 2);
        if (originWidth==0){
            imgInfo.put(ImageResizer.IMG_WIDTH, 400);
            imgInfo.put(ImageResizer.IMG_HEIGHT, 400);
            return imgInfo;
        }
        if (originWidth > maxWidth) {
            imgInfo.put(ImageResizer.IMG_WIDTH, maxWidth);
            float temp = (float) originWidth / (float) maxWidth;
            float finishHeight = originHeight/temp;
            imgInfo.put(ImageResizer.IMG_HEIGHT, (int) finishHeight);
        } else {
            imgInfo.put(ImageResizer.IMG_WIDTH, originWidth);
            imgInfo.put(ImageResizer.IMG_HEIGHT, originHeight);
        }


        //  Map<String, Integer> imgInfo = ImageResizer.getImageRatioInfo(originWidth, originHeight);

        return imgInfo;
    }


    public static Map<String, Integer> getImageRatioInfoByPath(String filePath) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        if (bitmap == null)
            return null;
        int originWidth = bitmap.getWidth();
        int originHeight = bitmap.getHeight();
        Map<String, Integer> imgInfo = ImageResizer.getImageRatioInfo(originWidth, originHeight);
        if(bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return imgInfo;
    }

    public static Map<String, Integer> getImageRatioInfo(int originWidth, int originHeight) {
        ZalyLogUtils.getInstance().info(TAG, " image info originWidth  is " + originWidth + "  originHeight is  " + originHeight);

        if (originWidth > maxWidth || originHeight > maxHeight) {
            int newWidth = originWidth / maxWidth;
            int newHeight = originHeight / maxHeight;

            if (newWidth > newHeight) {
                width = maxWidth;
                height = originHeight / newWidth;
            } else if (newWidth < newHeight) {
                height = maxHeight;
                width = originWidth / newHeight;
            }
            if (height > originHeight) {
                height = originHeight;
            }

            Map<String, Integer> imgInfo = new HashMap<>();
            imgInfo.put(IMG_WIDTH, width);
            imgInfo.put(IMG_HEIGHT, height);
            ZalyLogUtils.getInstance().info(TAG, " image info is " + imgInfo);

            return imgInfo;
        }
        Map<String, Integer> imgInfo = new HashMap<>();
        imgInfo.put(IMG_WIDTH, maxWidth);
        imgInfo.put(IMG_HEIGHT, maxHeight);
        ZalyLogUtils.getInstance().info(TAG, " image info is " + imgInfo);

        return imgInfo;
    }

    public static Map<String, Integer> getImageRatioInfoById(String fileId, int windowWidth, int windowHeight) {
        int originWidth = StringUtils.getInfoForFileId(fileId, 1);
        int originHeight = StringUtils.getInfoForFileId(fileId, 2);

        Map<String, Integer> imgInfo = ImageResizer.getImageRatioInfo(originWidth, originHeight, windowWidth, windowHeight);

        return imgInfo;
    }

    public static Map<String, Integer> getImageRatioInfoByPath(String filePath, int windowWidth, int windowHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap    = BitmapFactory.decodeFile(filePath, options);
        int originWidth  = bitmap.getWidth();
        int originHeight = bitmap.getHeight();
        Map<String, Integer> imgInfo = ImageResizer.getImageRatioInfo(originWidth, originHeight, windowWidth, windowHeight);
        if(bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return imgInfo;
    }

    public static Map<String, Integer> getImageRatioInfo(int originWidth, int originHeight, int windowWidth, int windowHeight) {

        width = windowWidth;
        height = windowHeight;
        ZalyLogUtils.getInstance().info(TAG, "window  img info is origin width " + originWidth + " origin height is " + originHeight);

        if (originWidth > windowWidth || originHeight > windowHeight) {
            int newWidth = originWidth / windowWidth;
            int newHeight = originHeight / windowHeight;

            if (newWidth > newHeight) {
                width = windowWidth;
                height = originHeight / newWidth;
            } else if (newWidth < newHeight) {
                height = windowHeight;
                width = originWidth / newHeight;
            } else {
                width = windowWidth;
                height = windowHeight;
            }
        }
        Map<String, Integer> imgInfo = new HashMap<>();
        imgInfo.put(IMG_WIDTH, width);
        imgInfo.put(IMG_HEIGHT, height);
        ZalyLogUtils.getInstance().info(TAG, "window  img info is " + imgInfo);
        return imgInfo;

    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImageResizer(Context context, int imageWidth, int imageHeight) {
        setImageSize(imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     *
     * @param context
     * @param imageSize
     */
    public ImageResizer(Context context, int imageSize) {
        setImageSize(imageSize);
    }

    /**
     * Set the target image width and height.
     *
     * @param width
     * @param height
     */
    public void setImageSize(int width, int height) {
        mImageWidth = width;
        mImageHeight = height;
    }

    /**
     * Set the target image size (width and height will be the same).
     *
     * @param size
     */
    public void setImageSize(int size) {
        setImageSize(size, size);
    }

    /**
     * The main processing method. This happens in a background task. In this case we are just
     * sampling down the bitmap and returning it from a resource.
     *
     * @param resId
     * @return
     */
    private Bitmap processBitmap(int resId) {
        return null;
//        return decodeSampledBitmapFromResource(mResources, resId, mImageWidth,
//                mImageHeight, getImageCache());
    }


    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res       The resources object containing the image data
     * @param resId     The resource id of the image data
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     * that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // BEGIN_INCLUDE (read_bitmap_dimensions)
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // END_INCLUDE (read_bitmap_dimensions)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename  The full path of the file to decode
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     * that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth       The requested width of the resulting bitmap
     * @param reqHeight      The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     * that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }


    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap
     * having a width and height equal to or larger than the requested width and height.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // BEGIN_INCLUDE (calculate_sample_size)
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
        // END_INCLUDE (calculate_sample_size)
    }
}
