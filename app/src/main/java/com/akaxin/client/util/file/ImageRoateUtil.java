package com.akaxin.client.util.file;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.akaxin.client.util.log.ZalyLogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import me.iwf.photopicker.utils.FileUtils;

/**
 * Created by zhangjun on 01/06/2018.
 */

public class ImageRoateUtil {

    public static final String TAG = ImageRoateUtil.class.getSimpleName();
    private static int outWidth = 0;//输出bitmap的宽
    private static int outHeight = 0;//输出bitmap的高

    //通过img得到旋转rotate角度后的bitmap
    public static Bitmap rotateImage(Bitmap img, int rotate){
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate); ///pre相当于向队首增加一个操作，post相当于向队尾增加一个操作，set相当于清空当前队列重新设置。


        int width = img.getWidth();
        int height =img.getHeight();
        img = Bitmap.createBitmap(img, 0, 0, width, height, matrix, false);
        return img;
    }

    /**
     * 读取照片exif信息中的旋转角度
     * @param path 照片路径
     * @return角度
     */
    public static int readPictureDegree(String path) {
        int degree  = 0;
        ExifInterface exifInterface = null;
        try {
            ////ExifInterface 支持3种类型，Exif信息在文件头中是以二进制的形式存储的，存储的字段名称和字段值格式都是固定的。
            ////旋转角度， 文件宽高，拍摄时间等
            exifInterface = new ExifInterface(path);
        } catch (Exception ex) {
            ZalyLogUtils.getInstance().exceptionError(ex);
        }
        if(exifInterface == null)
            return degree;
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }
        return degree;
    }


    //计算sampleSize
    private static int caculateSampleSize(String imgFilePath,int rotate){
        outWidth = 0;
        outHeight = 0;
        int imgWidth = 0;//原始图片的宽
        int imgHeight = 0;//原始图片的高
        int sampleSize = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(imgFilePath);
            BitmapFactory.decodeStream(inputStream,null,options);//由于options.inJustDecodeBounds位true，所以这里并没有在内存中解码图片，只是为了得到原始图片的大小
            imgWidth = options.outWidth;
            imgHeight = options.outHeight;
            //初始化
            outWidth = imgWidth;
            outHeight = imgHeight;
            //如果旋转的角度是90的奇数倍,则输出的宽和高和原始宽高调换
            if((rotate / 90) % 2 != 0){
                outWidth = imgHeight;
                outHeight = imgWidth;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        //计算输出bitmap的sampleSize
        while (imgWidth / sampleSize > outWidth || imgHeight / sampleSize > outHeight) {
            sampleSize = sampleSize << 1;
        }
        return sampleSize;
    }

    public  static void doRotateImageAndSave(String filePath){
        ZalyLogUtils.getInstance().info(TAG, " doRotateImageAndSave start  === ");

        int rotate = readPictureDegree(filePath);

        ZalyLogUtils.getInstance().info(TAG, " doRotateImageAndSave start  === rotate =" + rotate);

        if(rotate == 0)
            return;
        //得到sampleSize
        int sampleSize = caculateSampleSize(filePath, rotate);
        if (outWidth == 0 || outHeight == 0)
            return;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        //适当调整颜色深度
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        FileInputStream inputStream = null;
        boolean result = false;
        try {
            inputStream = new FileInputStream(filePath);
            Bitmap srcBitmap = BitmapFactory.decodeStream(inputStream, null, options);//加载原图
            // ///int destMem = srcBitmap.getRowBytes() * srcBitmap.getHeight();//计算bitmap占用的内存大小

            Bitmap destBitmap = rotateImage(srcBitmap, rotate);
            srcBitmap.recycle();
            File file = new File(filePath);
            OutputStream outputStream = null;

            //保存bitmap到文件（覆盖原始图片）

            outputStream = new FileOutputStream(file);
            result = destBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                    outputStream);
            ZalyLogUtils.getInstance().info(TAG, " doRotateImageAndSave  result ==" +result);
            destBitmap.recycle();
        } catch (Exception error) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
