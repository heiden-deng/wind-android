package com.akaxin.client.util.file;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.akaxin.client.api.ApiClient;
import com.akaxin.client.api.ZalyAPIException;
import com.akaxin.client.bean.AudioInfo;
import com.akaxin.client.bean.Message;
import com.akaxin.client.bean.Site;
import com.akaxin.client.im.files.IMFileUtils;
import com.akaxin.client.util.data.StringUtils;
import com.akaxin.client.util.log.ZalyLogUtils;
import com.akaxin.client.util.security.AESUtils;
import com.akaxin.client.util.security.RSAUtils;
import com.akaxin.client.util.task.ZalyTaskExecutor;
import com.akaxin.proto.core.FileProto;
import com.akaxin.proto.site.ApiFileDownloadProto;
import com.akaxin.proto.site.ApiFileUploadProto;
import com.orhanobut.logger.Logger;
import com.windchat.im.socket.ConnectionConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Created by yichao on 2017/11/6.
 */

public class UploadFileUtils {

    public static final String TAG = "UploadFileUtils";

    public static int imgMaxSize = 300 * 1000;//500KB

    public interface UploadFileListener {
        void onUploadSuccess(String fileId);

        void onUploadFail(Exception e);

        void onProcessRate(int processNum);
    }

    public interface UploadSecretFileListener extends UploadFileListener {
        void encryptFileSuccess(String tsk64);
    }

    /**
     * 下载文件回调
     * <p>
     * 注意：以InBackground结尾方法运行在后台线程，不可进行UI操作
     */
    public interface DownloadFileListener {

        /**
         * 开始下载 后台线程
         */
        void onDownloadStartInBackground();

        /**
         * 下载完成 后台线程
         */
        void onDownloadCompleteInBackground(String fileId, String filePath);

        /**
         * 下载成功 UI线程
         */
        void onDownloadSuccess(String fileId, String filePath);

        /**
         * 下载失败 UI线程
         */
        void onDownloadFail(Exception e);
    }

    public interface DownloadSecretFileListener extends DownloadFileListener {
        void decryptFileStartInBackground();

        void decryptFileCompleteInBackground(String fileId, String filePath);
    }

    /**
     * 上传文件
     *
     * @param filePath
     * @param fileListener
     */
    public static void uploadFile(String filePath, @NonNull final UploadFileListener fileListener, FileProto.FileType fileType, Site site) {
        if (fileListener == null) {
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new UploadFileTask(filePath, fileListener, fileType, site));
    }

    /**
     * 上传加密文件
     *
     * @param filePath
     * @param fileListener
     */
    public static void uploadSecretFile(String filePath, @NonNull final UploadSecretFileListener fileListener, FileProto.FileType fileType, String pubKey64Str, Site site) {
        if (fileListener == null) {
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new uploadEncryptFileTask(filePath, fileListener, fileType, pubKey64Str, site));
    }


    /**
     * 上传文件
     *
     * @param filePath
     * @param fileListener
     */
    public static void uploadMsgFile(String filePath, @NonNull final UploadFileListener fileListener, FileProto.FileType fileType, Message msg, Site site) {
        if (fileListener == null) {
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new UploadMsgFileTask(filePath, fileListener, fileType, msg, site));
    }

    /**
     * 上传加密文件
     *
     * @param filePath
     * @param fileListener
     */
    public static void uploadMsgSecretFile(String filePath, @NonNull final UploadSecretFileListener fileListener, FileProto.FileType fileType, String pubKey64Str, Message message, Site site) {
        if (fileListener == null) {
            return;
        }
        ZalyTaskExecutor.executeUserTask(TAG, new UploadMsgEncryptFileTask(filePath, fileListener, fileType, pubKey64Str, message, site));
    }

    /**
     * 上传加密文件task
     */
    private static final class uploadEncryptFileTask extends ZalyTaskExecutor.Task<Void, Void, ApiFileUploadProto.ApiFileUploadResponse> {

        private String filePath;
        private UploadSecretFileListener fileListener;
        private FileProto.FileType fileType;
        private String pubKey64Str;
        private int width;
        private int height;
        private int length;
        private Site currentSite;

        public uploadEncryptFileTask(String filePath, UploadSecretFileListener fileListener, FileProto.FileType fileType, String pubKey64Str, Site site) {
            this.filePath = filePath;
            this.fileListener = fileListener;
            this.fileType = fileType;
            this.pubKey64Str = pubKey64Str;
            this.currentSite = site;
        }

        @Override
        protected ApiFileUploadProto.ApiFileUploadResponse executeTask(Void... voids) throws Exception {
            File file = new File(filePath);
            byte[] bytesArray = new byte[(int) file.length()];

            Long len;
            int offset;
            Long fileLength = file.length();
            long everyNum = fileLength / 100;
            try {
                FileInputStream fis = new FileInputStream(file);

                for (int countNum = 0; countNum < 101; countNum++) {
                    offset = (int) everyNum * countNum;
                    if (100 == countNum) {
                        everyNum = fileLength - offset;
                    }
                    fis.read(bytesArray, offset, (int) everyNum);
                    fileListener.onProcessRate(countNum);
                }
//                fis.read(bytesArray); //read file into bytes[]
                fis.close();
            } catch (Exception e) {
                Logger.e(e);
                throw e;
            }
            //根据字符串获取公钥对象
            PublicKey publicKey = RSAUtils.getInstance().convertToPubicKey(pubKey64Str);


            if (publicKey == null) {
                throw new Exception("publicKey is null");
            }
            //随机生成tsk
            byte[] tsk = AESUtils.generateTSKey();
            if (tsk == null || tsk.length <= 0) {
                throw new Exception("generateTSKey is error");
            }
            //加密消息体内容
            byte[] encryptedContent = AESUtils.encrypt(tsk, bytesArray);
            //加密tsk
            byte[] encryptedTsk = RSAUtils.getInstance().RSAEncrypt(tsk, publicKey);

            if (fileListener != null) {
                fileListener.encryptFileSuccess(Base64.encodeToString(encryptedTsk, Base64.NO_WRAP));
            }

            if (fileType == FileProto.FileType.MESSAGE_IMAGE) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

                width = bitmap.getWidth();
                height = bitmap.getHeight();
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
                length = 0;
            } else if (fileType == FileProto.FileType.MESSAGE_VOICE) {
                width = 0;
                height = 0;
                length = (int) file.length();
            }
            return IMFileUtils.uploadFile(encryptedContent, fileType, width, height, length, currentSite);
        }

        @Override
        protected void onTaskSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
            super.onTaskSuccess(apiFileUploadResponse);
            if (apiFileUploadResponse == null) {
                fileListener.onUploadFail(new Exception("Response is error"));
                return;
            }
            fileListener.onUploadSuccess(apiFileUploadResponse.getFileId());
        }

    }

    /**
     * 上传加密文件task
     */
    private static final class UploadMsgEncryptFileTask extends ZalyTaskExecutor.Task<Void, Void, ApiFileUploadProto.ApiFileUploadResponse> {

        private String filePath;
        private UploadSecretFileListener fileListener;
        private FileProto.FileType fileType;
        private String pubKey64Str;
        private int width;
        private int height;
        private int length;
        private Message message;
        private Site currentSite;

        public UploadMsgEncryptFileTask(String filePath, UploadSecretFileListener fileListener, FileProto.FileType fileType, String pubKey64Str, Message msg, Site site) {
            this.filePath = filePath;
            this.fileListener = fileListener;
            this.fileType = fileType;
            this.pubKey64Str = pubKey64Str;
            this.message = msg;
            this.currentSite = site;
        }

        @Override
        protected ApiFileUploadProto.ApiFileUploadResponse executeTask(Void... voids) throws Exception {
            File file = new File(filePath);
            byte[] bytesArray = new byte[(int) file.length()];

            Long len;
            int offset;
            Long fileLength = file.length();
            long everyNum = fileLength / 100;
            try {
                FileInputStream fis = new FileInputStream(file);

                for (int countNum = 0; countNum < 101; countNum++) {
                    offset = (int) everyNum * countNum;
                    if (100 == countNum) {
                        everyNum = fileLength - offset;
                    }
                    fis.read(bytesArray, offset, (int) everyNum);
                    fileListener.onProcessRate(countNum);
                }
//                fis.read(bytesArray); //read file into bytes[]
                fis.close();
            } catch (Exception e) {
                Logger.e(e);
                throw e;
            }
            ZalyLogUtils.getInstance().info(TAG, " device pubKey64Str is " + pubKey64Str);

            //根据字符串获取公钥对象
            PublicKey publicKey = RSAUtils.getInstance().convertToPubicKey(pubKey64Str);

            ZalyLogUtils.getInstance().info(TAG, " device pubk is " + publicKey);

            if (publicKey == null) {
                throw new Exception("publicKey is null");
            }
            //随机生成tsk
            byte[] tsk = AESUtils.generateTSKey();
            if (tsk == null || tsk.length <= 0) {
                throw new Exception("generateTSKey is error");
            }
            //加密消息体内容
            byte[] encryptedContent = AESUtils.encrypt(tsk, bytesArray);
            //加密tsk
            byte[] encryptedTsk = RSAUtils.getInstance().RSAEncrypt(tsk, publicKey);

            if (fileListener != null) {
                fileListener.encryptFileSuccess(Base64.encodeToString(encryptedTsk, Base64.NO_WRAP));
            }

            if (fileType == FileProto.FileType.MESSAGE_IMAGE) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                length = 0;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                bytesArray = IMFileUtils.resizeImageByWidth(stream.toByteArray(), 1024);
                encryptedContent = AESUtils.encrypt(tsk, bytesArray);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }

            } else if (fileType == FileProto.FileType.MESSAGE_VOICE) {
                width = 0;
                height = 0;
                AudioInfo audioInfo = AudioInfo.parseJSON(message.getContent());
                length = (int) (audioInfo.getAudioTime() / 1000 + 1);
            }
            return IMFileUtils.uploadFile(encryptedContent, fileType, width, height, length, currentSite);
        }

        @Override
        protected void onTaskSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
            super.onTaskSuccess(apiFileUploadResponse);
            if (apiFileUploadResponse == null) {
                fileListener.onUploadFail(new Exception("Response is error"));
                return;
            }
            fileListener.onUploadSuccess(apiFileUploadResponse.getFileId());
        }

    }


    /**
     * 上传文件任务
     */
    private static final class UploadFileTask extends ZalyTaskExecutor.Task<Void, Void, ApiFileUploadProto.ApiFileUploadResponse> {

        private String filePath;
        private UploadFileListener fileListener;
        private FileProto.FileType fileType;
        Long len;
        int offset;
        private int width;
        private int height;
        private int length;
        private Site currentSite;

        public UploadFileTask(String filePath, UploadFileListener fileListener, FileProto.FileType fileType, Site site) {
            this.filePath = filePath;
            this.fileListener = fileListener;
            this.currentSite = site;
            this.fileType = fileType;
            this.currentSite = site;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        @Override
        protected ApiFileUploadProto.ApiFileUploadResponse executeTask(Void... voids) throws Exception {
            if (StringUtils.isEmpty(filePath)) {
                fileListener.onUploadFail(new Exception("FilePath is empty!"));
            }

            byte[] bytesArray;
            if (fileType == FileProto.FileType.MESSAGE_IMAGE) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                int fileLength = stream.toByteArray().length;
                int everyNum = fileLength / 100;

                bytesArray = new byte[fileLength];
                for (int countNum = 0; countNum < 101; countNum++) {
                    offset = everyNum * countNum;
                    if (100 == countNum) {
                        everyNum = fileLength - offset;
                    }
                    stream.write(bytesArray, offset, everyNum);
                    fileListener.onProcessRate(countNum);
                }
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                length = 0;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                bytesArray = IMFileUtils.resizeImageByWidth(stream.toByteArray(), 1024);
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
            } else {
                File file = new File(filePath);
                bytesArray = new byte[(int) file.length()];
                try {
                    FileInputStream fis = new FileInputStream(file);
                    fis.read(bytesArray);
                    fis.close();

                    width = 0;
                    height = 0;
                } catch (Exception e) {
                    Logger.e(e);
                    ZalyLogUtils.getInstance().errorToInfo(TAG, e.getMessage());
                    throw e;
                }
            }
            return IMFileUtils.uploadFile(bytesArray, fileType, width, height, length, currentSite);
        }

        @Override
        protected void onTaskSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
            super.onTaskSuccess(apiFileUploadResponse);
            if (apiFileUploadResponse == null) {
                fileListener.onUploadFail(new Exception("Response is error"));
                return;
            }
            fileListener.onUploadSuccess(apiFileUploadResponse.getFileId());
        }

        @Override
        protected void onTaskError(Exception e) {
            fileListener.onUploadFail(e);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
        }
    }

    /**
     * 上传文件任务
     */
    private static final class UploadMsgFileTask extends ZalyTaskExecutor.Task<Void, Void, ApiFileUploadProto.ApiFileUploadResponse> {

        private String filePath;
        private UploadFileListener fileListener;
        private FileProto.FileType fileType;
        Long len;
        int offset;
        private int width;
        private int height;
        private int length;
        private Message message;
        private Site currentSite;

        public UploadMsgFileTask(String filePath, UploadFileListener fileListener, FileProto.FileType fileType, Message msg, Site site) {
            this.filePath = filePath;
            this.fileListener = fileListener;
            this.fileType = fileType;
            this.message = msg;
            this.currentSite = site;
        }

        @Override
        protected void onPreTask() {
            super.onPreTask();
        }

        @Override
        protected ApiFileUploadProto.ApiFileUploadResponse executeTask(Void... voids) throws Exception {
            if (StringUtils.isEmpty(filePath)) {
                fileListener.onUploadFail(new Exception("FilePath is empty!"));
            }

            byte[] bytesArray;
            if (fileType == FileProto.FileType.MESSAGE_IMAGE) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                int fileLength = stream.toByteArray().length;
                int everyNum = fileLength / 100;

                bytesArray = new byte[fileLength];
                for (int countNum = 0; countNum < 101; countNum++) {
                    offset = everyNum * countNum;
                    if (100 == countNum) {
                        everyNum = fileLength - offset;
                    }
                    stream.write(bytesArray, offset, everyNum);
                    fileListener.onProcessRate(countNum);
                }
                width = bitmap.getWidth();
                height = bitmap.getHeight();
                length = 0;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                bytesArray = IMFileUtils.resizeImageByWidth(stream.toByteArray(), 1024);
            } else {

                File file = new File(filePath);
                bytesArray = new byte[(int) file.length()];

                try {
                    FileInputStream fis = new FileInputStream(file);
                    fis.read(bytesArray);
                    fis.close();

                    AudioInfo audioInfo = AudioInfo.parseJSON(message.getContent());

                    // 这个地方不能用 (int) audioInfo.getAudioTime()/1000;
                    // 导致接收端得到的消息长度永远少1秒。
                    length = (int) (audioInfo.getAudioTime() / 1000 + 1);
                    width = 0;
                    height = 0;
                } catch (Exception e) {
                    ZalyLogUtils.getInstance().debug("recordaudio", e.getMessage(), this);
                    throw e;
                }
            }
            return IMFileUtils.uploadFile(bytesArray, fileType, width, height, length, currentSite);
        }

        @Override
        protected void onTaskSuccess(ApiFileUploadProto.ApiFileUploadResponse apiFileUploadResponse) {
            super.onTaskSuccess(apiFileUploadResponse);
            if (apiFileUploadResponse == null) {
                fileListener.onUploadFail(new Exception("Response is error"));
                return;
            }
            fileListener.onUploadSuccess(apiFileUploadResponse.getFileId());
        }

        @Override
        protected void onTaskError(Exception e) {
            fileListener.onUploadFail(e);
            Logger.e(e);
        }

        @Override
        protected void onTaskFinish() {
            super.onTaskFinish();
        }
    }


    /**
     * 下载文件
     *
     * @param fileId
     * @param filePath
     * @param fileType
     * @param fileListener
     */
    public static void downloadFile(String fileId, String filePath, FileProto.FileType fileType, @NonNull DownloadFileListener fileListener, Site site) {
        ConnectionConfig config = ConnectionConfig.getConnectionCfg(site);
        ZalyTaskExecutor.executeUserTask(TAG, new DownloadFileTask(fileId, filePath, fileType, fileListener, config));
    }


    /**
     * 下载加密文件
     *
     * @param fileId
     * @param filePath
     * @param fileType
     * @param fileListener
     */
    public static void downloadSecretFile(String fileId, String filePath, FileProto.FileType fileType,
                                          @NonNull DownloadSecretFileListener fileListener, String priKey64Str, String encryptedTskStr, Site site) {
        ConnectionConfig config = ConnectionConfig.getConnectionCfg(site);
        ZalyTaskExecutor.executeUserTask(TAG, new DownloadEncryptFile(fileId, filePath, fileType, fileListener, priKey64Str, encryptedTskStr, config));
    }

    private static final class DownloadFileTask extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        private String fileId;
        private String filePath;
        private FileProto.FileType fileType;
        private DownloadFileListener fileListener;
        private ConnectionConfig connectionConfig;

        public DownloadFileTask(String fileId, String filePath, FileProto.FileType fileType, DownloadFileListener fileListener, ConnectionConfig connectionConfig) {
            this.fileId = fileId;
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileListener = fileListener;
            this.connectionConfig = connectionConfig;
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            if (StringUtils.isEmpty(filePath)) {
                fileListener.onDownloadFail(new Exception("FilePath is empty!"));
            }
            ApiFileDownloadProto.ApiFileDownloadResponse response = downloadFile(fileId, fileType, connectionConfig);
            FileOutputStream os = new FileOutputStream(new File(filePath));
            byte[] data = response.getFile().getFileContent().toByteArray();
            os.write(data);
            os.flush();
            os.close();
            fileListener.onDownloadCompleteInBackground(fileId, filePath);
            return true;
        }

        @Override
        protected void onTaskError(Exception e) {
            e.printStackTrace();
            fileListener.onDownloadFail(e);
            File file = new File(filePath);
            if (file != null && file.exists()) {
                file.delete();
            }
        }

        @Override
        protected void onAPIError(ZalyAPIException zalyAPIException) {
            zalyAPIException.printStackTrace();
            fileListener.onDownloadFail(zalyAPIException);
        }

        @Override
        protected void onTaskSuccess(Boolean b) {
            super.onTaskSuccess(b);
            fileListener.onDownloadSuccess(fileId, filePath);
        }

    }

    /**
     * 下载加密文件并解密后输出至存储
     */
    private static final class DownloadEncryptFile extends ZalyTaskExecutor.Task<Void, Void, Boolean> {

        private String fileId;
        private String filePath;
        private FileProto.FileType fileType;
        private DownloadSecretFileListener fileListener;
        private String priKey64Str;
        private String encryptedTskStr;
        private ConnectionConfig connectionConfig;

        public DownloadEncryptFile(String fileId, String filePath, FileProto.FileType fileType, DownloadSecretFileListener fileListener, String priKey64Str, String encryptedTskStr, ConnectionConfig connectionConfig) {
            this.fileId = fileId;
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileListener = fileListener;
            this.priKey64Str = priKey64Str;
            this.encryptedTskStr = encryptedTskStr;
            this.connectionConfig = connectionConfig;
        }

        @Override
        protected Boolean executeTask(Void... voids) throws Exception {
            if (StringUtils.isEmpty(filePath)) {
                fileListener.onDownloadFail(new Exception("FilePath is empty!"));
            }

            /**
             * 下载
             */
            fileListener.onDownloadStartInBackground();
            ApiFileDownloadProto.ApiFileDownloadResponse response = downloadFile(fileId, fileType, connectionConfig);
            FileOutputStream os = new FileOutputStream(new File(filePath));
            byte[] encryptData = response.getFile().getFileContent().toByteArray();
            fileListener.onDownloadCompleteInBackground(fileId, filePath);

            /**
             * 解密
             */
            fileListener.decryptFileStartInBackground();
            //获取私钥
            PrivateKey privateKey = RSAUtils.getInstance().convertToPrivateKey(priKey64Str);
            if (privateKey == null) {
                throw new Exception("privateKey is null");
            }
            //解密tsk
            byte[] tsk = RSAUtils.getInstance().RSADecrypt(Base64.decode(encryptedTskStr, Base64.NO_WRAP), privateKey);
            if (tsk == null || tsk.length == 0) {
                throw new Exception("RSADecrypt tsk is wrong");
            }
            //解密消息体
            byte[] content = AESUtils.decrypt(tsk, encryptData);

            //写文件至存储
            os.write(content);
            os.flush();
            os.close();
            fileListener.decryptFileCompleteInBackground(fileId, filePath);

            return true;
        }

        @Override
        protected void onTaskSuccess(Boolean b) {
            super.onTaskSuccess(b);
            fileListener.onDownloadSuccess(fileId, filePath);
        }

        @Override
        protected void onTaskError(Exception e) {
            fileListener.onDownloadFail(e);
        }

        protected void onTaskError(ZalyAPIException e) {
            fileListener.onDownloadFail(e);
        }

    }

    private static final ApiFileDownloadProto.ApiFileDownloadResponse downloadFile(String fileId, FileProto.FileType fileType, ConnectionConfig connectionConfig) {
        return ApiClient.getInstance(connectionConfig).getFileApi().downloadFile(fileId, fileType);
    }
}
