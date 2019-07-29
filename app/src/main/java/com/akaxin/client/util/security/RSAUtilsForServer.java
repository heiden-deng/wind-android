package com.akaxin.client.util.security;

import com.orhanobut.logger.Logger;
import com.akaxin.client.bean.Message;
import com.akaxin.client.chat.presenter.impl.GroupMsgPresenter;
import com.akaxin.client.util.data.StringUtils;

import org.apache.commons.codec.binary.Base64;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by yichao on 2017/10/15.
 */

public class RSAUtilsForServer {

    private static final String TAG = "RSAUtils";
    private static final String ALGORITHM = "RSA";
    private static RSAUtilsForServer rsaUtils;

    private RSAUtilsForServer() {
    }

    public static RSAUtilsForServer getInstance() {
        if (rsaUtils == null) {
            rsaUtils = new RSAUtilsForServer();
        }
        return rsaUtils;
    }

    /**
     * 生成新的密钥对
     *
     * @return
     */
    public KeyPair generateNewKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(ALGORITHM);
            kpg.initialize(1024);
            return kpg.generateKeyPair();
        } catch (Exception e) {
            Logger.e(e, "generat key pair is error");
            return null;
        }
    }

    /**
     * 生成新的密钥对
     *
     * @return 公私钥，返回格式为以Base64编码的字符串
     */
    public String[] generateNewKeyPairStr() {
        String[] keyParis = new String[2];
        KeyPair keyPair = generateNewKeyPair();
        if (keyPair != null) {
            keyParis[0] = Base64.encodeBase64String(keyPair.getPrivate().getEncoded());
            keyParis[1] = Base64.encodeBase64String(keyPair.getPublic().getEncoded());
        }
        return keyParis;
    }

    /**
     * RSA加密
     *
     * @param content   加密内容 二进制数据，字符串需要64编码成字节数组
     * @param publicKey 加密公钥
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] RSAEncrypt(final byte[] content, PublicKey publicKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(content);
        System.out.println("EEncrypted ---- " + Base64.encodeBase64String(encryptedBytes));
        return encryptedBytes;
    }

    /**
     * 解密字符串
     *
     * @param encryptedBytes
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] RSADecrypt(final byte[] encryptedBytes, PrivateKey privateKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        String decrypted = new String(decryptedBytes);
        System.out.println("DDecrypted ----- " + decrypted);
        return decryptedBytes;
    }

    /**
     * 把64编码的公钥字符串转换成{@link PublicKey}并返回
     *
     * @param publicKey64Str
     * @return
     * @throws Exception
     */
    public PublicKey convertToPubicKey(String publicKey64Str) throws Exception {
        byte[] publicBytes = Base64.decodeBase64(publicKey64Str);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 把64编码的私钥字符串转换成{@link PrivateKey}并返回
     *
     * @param privateKey64Str
     * @return
     * @throws Exception
     */
    public PrivateKey convertToPrivateKey(String privateKey64Str) throws Exception {
        byte[] privateBytes = Base64.decodeBase64(privateKey64Str);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 加密消息 仅适用于单人聊天，群组见{@link GroupMsgPresenter.SendSecretTask}
     *
     * @param message     要加密的消息
     * @param pubKey64Str 64编码的公钥字符串
     * @return 加密完成的消息
     * @throws Exception
     */
    public Message encryptMsg(Message message, String pubKey64Str) throws Exception {
        if (StringUtils.isEmpty(message.getMsgId()) || StringUtils.isEmpty(message.getContent())) {
            throw new Exception("message is dirty");
        }

        Logger.w(TAG, "---- encryptMsg start");
        //根据字符串获取公钥对象
        PublicKey publicKey = RSAUtilsForServer.getInstance().convertToPubicKey(pubKey64Str);
        if (publicKey == null) {
            throw new Exception("publicKey is null");
        }
        //随机生成tsk
        byte[] tsk = AESUtils.generateTSKey();
        printTsk(tsk);
        if (tsk == null || tsk.length <= 0) {
            throw new Exception("generateTSKey is error");
        }
        //加密消息体内容
        byte[] encryptedContent = AESUtils.encrypt(tsk, message.getContent().getBytes());
        //加密tsk
        byte[] encryptedTsk = RSAUtilsForServer.getInstance().RSAEncrypt(tsk, publicKey);
        Logger.w(TAG, "---- encryptMsg complite");
        //获取64编码的字符串数据
        message.setSecretData(encryptedContent);
        message.setContent(Base64.encodeBase64String(encryptedContent));
        message.setMsgTsk(Base64.encodeBase64String(encryptedTsk));
        return message;
    }

    /**
     * 解密消息
     *
     * @param message
     * @param priKey64Str
     * @return
     * @throws Exception
     */
    public Message decryptMsg(Message message, String priKey64Str) throws Exception {
        if (StringUtils.isEmpty(message.getMsgId()) || StringUtils.isEmpty(message.getContent())
                || StringUtils.isEmpty(message.getMsgTsk())) {
            throw new Exception("message is dirty");
        }
        //获取私钥
        PrivateKey privateKey = RSAUtilsForServer.getInstance().convertToPrivateKey(priKey64Str);
        if (privateKey == null) {
            throw new Exception("privateKey is null");
        }
        //解密tsk
        String encryptedTskStr = message.getMsgTsk();
        byte[] tsk = RSAUtilsForServer.getInstance().RSADecrypt(Base64.decodeBase64(encryptedTskStr), privateKey);
        printTsk(tsk);
        if (tsk == null || tsk.length == 0) {
            throw new Exception("RSADecrypt tsk is wrong");
        }
        //解密消息体
        byte[] content = AESUtils.decrypt(tsk, Base64.decodeBase64(message.getContent()));
        message.setContent(new String(content));
        return message;
    }

    public static void printTsk(byte[] data) {
        String result = "";
        for (int i = 0; i < data.length; i++) {
            result += ((int) data[i]);
        }
        Logger.w(TAG, " ==== " + result);
    }

}
