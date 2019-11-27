package com.windchat.client.util.security;

import android.util.Base64;

import com.orhanobut.logger.Logger;
import com.windchat.client.bean.Message;
import com.windchat.client.chat.presenter.impl.GroupMessagePresenter;
import com.windchat.client.util.data.StringUtils;

import org.bouncycastle.jce.provider.JCERSAPublicKey;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by yichao on 2017/10/15.
 */

public class RSAUtils {

    private static final String TAG = "RSAUtils";
    private static final String ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static RSAUtils rsaUtils;

    private RSAUtils() {
    }

    public static RSAUtils getInstance() {
        if (rsaUtils == null) {
            rsaUtils = new RSAUtils();
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
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
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
    @Deprecated
    public String[] generateNewKeyPairStr() {
        String[] keyParis = new String[2];
        KeyPair keyPair = generateNewKeyPair();
        if (keyPair != null) {
            keyParis[0] = Base64.encodeToString(keyPair.getPrivate().getEncoded(), Base64.NO_WRAP);
            keyParis[1] = Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.NO_WRAP);
        }
        return keyParis;
    }

    /**
     * 生成新的密钥对，目前全部统一为pem格式
     *
     * @return 公私钥，返回格式为以Base64编码的字符串
     */
    public String[] generateNewKeyPairPEMStr() {
        String[] keyParis = new String[2];
        KeyPair keyPair = generateNewKeyPair();
        if (keyPair != null) {
            keyParis[0] = RSAUtils.getPEMStringFromRSAKey(keyPair.getPrivate());
            keyParis[1] = RSAUtils.getPEMStringFromRSAKey(keyPair.getPublic());
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
    public byte[] RSAEncrypt(final byte[] content, Key publicKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(content);
        System.out.println("EEncrypted ---- " + Base64.encodeToString(encryptedBytes, Base64.NO_WRAP));
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
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        String decrypted = new String(decryptedBytes);
        System.out.println("DDecrypted ----- " + decrypted);
        return decryptedBytes;
    }

    /**
     * 把64编码的公钥字符串转换成{@link PublicKey}并返回
     *
     * @param publicKeyStr 目前统一使用pem格式
     * @return
     * @throws Exception
     */
    public PublicKey convertToPubicKey(String publicKeyStr) throws Exception {
        return extractPublicRSAKey(publicKeyStr);
    }

    /**
     * 把64编码的私钥字符串转换成{@link PrivateKey}并返回
     *
     * @param privateKeyStr 目前统一使用pem格式
     * @return
     * @throws Exception
     */
    public PrivateKey convertToPrivateKey(String privateKeyStr) throws Exception {
        return extractPrivateRSAKey(new String(privateKeyStr));
    }

    /**
     *
     * @param privateKeyStr
     * @param targetString
     * @return
     */
    public String signInBase64String(String privateKeyStr, String targetString) {

        //每次临时计算sign即可，不要使用之前初始化的
        try {
            PrivateKey userPrivKey = this.convertToPrivateKey(privateKeyStr);
            Signature signHandler = Signature.getInstance("SHA512withRSA");
            signHandler.initSign(userPrivKey);
            signHandler.update(targetString.getBytes());
            return Base64.encodeToString(signHandler.sign(), Base64.NO_WRAP);
        } catch (Exception e) {
            Logger.e("rsa_sign_error", e.getMessage());
            return "";
        }
    }

    /**
     * 加密消息 仅适用于单人聊天，群组见{@link GroupMessagePresenter}
     *
     * @param message     要加密的消息
     * @param pubKey64Str 64编码的公钥字符串
     * @return 加密完成的消息
     * @throws Exception
     */
    public Message encryptMsg(Message message, String pubKey64Str) throws Exception {
        System.out.println("fuck encryptMsg ==== start =======");

        if (StringUtils.isEmpty(message.getMsgId()) || StringUtils.isEmpty(message.getContent())) {
            throw new Exception("message is dirty");
        }

        Logger.w(TAG, "---- encryptMsg start");
        //根据字符串获取公钥对象
        PublicKey publicKey = RSAUtils.getInstance().convertToPubicKey(pubKey64Str);
        if (publicKey == null) {
            throw new Exception("publicKey is null");
        }
        //随机生成tsk
        byte[] tsk = AESUtils.generateTSKey();
        printTsk(tsk);
        Logger.i(TAG, " tsk ==== " + Base64.encodeToString(tsk, Base64.NO_WRAP));
        Logger.i(TAG, new String(tsk));


        if (tsk == null || tsk.length <= 0) {
            throw new Exception("generateTSKey is error");
        }
        //加密消息体内容
        byte[] encryptedContent = AESUtils.encrypt(tsk, message.getContent().getBytes());

        printContent(encryptedContent);
        Logger.i(TAG, " content ==== " + Base64.encodeToString(encryptedContent, Base64.NO_WRAP));

        //加密tsk
        byte[] encryptedTsk = RSAUtils.getInstance().RSAEncrypt(tsk, publicKey);
        Logger.w(TAG, "---- encryptMsg complite");
        //获取64编码的字符串数据
        message.setSecretData(encryptedContent);
        message.setContent(Base64.encodeToString(encryptedContent, Base64.NO_WRAP));
        message.setMsgTsk(Base64.encodeToString(encryptedTsk, Base64.NO_WRAP));

        System.out.println(" encryptMsg ====  over  =======");

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
        PrivateKey privateKey = RSAUtils.getInstance().convertToPrivateKey(priKey64Str);
        if (privateKey == null) {
            throw new Exception("privateKey is null");
        }
        //解密tsk
        String encryptedTskStr = message.getMsgTsk();
        byte[] tsk = RSAUtils.getInstance().RSADecrypt(Base64.decode(encryptedTskStr, Base64.NO_WRAP), privateKey);

        if (tsk == null || tsk.length == 0) {
            throw new Exception("RSADecrypt tsk is wrong");
        }
        //解密消息体
        Logger.i(TAG, "message content ==== " + message.getContent());
        printContent(Base64.decode(message.getContent(), Base64.NO_WRAP));

        byte[] content = AESUtils.decrypt(tsk, Base64.decode(message.getContent(), Base64.NO_WRAP));
        message.setContent(new String(content));
        return message;
    }

    public static void printTsk(byte[] data) {
        String result = "";
        for (int i = 0; i < data.length; i++) {
            result += ((int) data[i]);
        }
        Logger.w(TAG, "fuck print ==== " + result);
    }


    public static void printContent(byte[] data) {
        String result = "";
        for (int i = 0; i < data.length; i++) {
            result += ((int) data[i]);
        }
        Logger.i(TAG, "fuck content ==== " + result);
    }

    /**
     * 下述方法摘自{https://www.programcreek.com/java-api-examples/index.php?source_dir=netlib-master/src/main/java/org/silvertunnel/netlib/layer/tor/util/Encryption.java}
     */
    /**
     * 获取pem格式的Key的str
     *
     * @param key
     * @return
     */
    public static String getPEMStringFromRSAKey(Key key) {
        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);
        try {
            pemWriter.writeObject(key);
            pemWriter.close();
        } catch (IOException e) {
            Logger.e(e);
            return null;
        }
        return pemStrWriter.toString();
    }


    /**
     * makes RSA public key from PEM string
     *
     * @param s PEM string that contains the key
     * @return
     * @see JCERSAPublicKey
     */
    public static PublicKey extractPublicRSAKey(String s) {
        PublicKey theKey;
        try {
            PEMReader reader = new PEMReader(new StringReader(s));
            theKey = (PublicKey)reader.readObject();
//            if (!(o instanceof PublicKey)) {
//                throw new IOException("Encryption.extractPublicRSAKey: no public key found in string '" + s + "'");
//            }
//            JCERSAPublicKey JCEKey = (JCERSAPublicKey) o;
//            theKey = getRSAPublicKey(JCEKey.getModulus(), JCEKey.getPublicExponent());
        } catch (Exception e) {
            Logger.e(e, "Encryption.extractPublicRSAKey: Caught exception:" + s);
            theKey = null;
        }
        return theKey;
    }

    /**
     * makes RSA private key from PEM string
     *
     * @param s PEM string that contains the key
     * @return
     * @see JCERSAPublicKey
     */
    public static PrivateKey extractPrivateRSAKey(String s) {
        PrivateKey theKey;
        try {
            PEMReader reader = new PEMReader(new StringReader(s));
            theKey = ((KeyPair) reader.readObject()).getPrivate();
//            if (!(o instanceof JCERSAPrivateKey)) {
//                throw new IOException("Encryption.extractPublicRSAKey: no public key found in string '" + s + "'");
//            }
//            JCERSAPrivateKey JCEKey = (JCERSAPrivateKey) o;
//            theKey = getRSAPrivateKey(JCEKey.getModulus(), JCEKey.getPrivateExponent());
        } catch (Exception e) {
            Logger.e(e, "Encryption.extractPublicRSAKey: Caught exception:" + s);
            theKey = null;
        }
        return theKey;
    }

    /**
     * Create a key based on the parameters.
     *
     * @param modulus
     * @param publicExponent
     * @return the key
     */
    public static RSAPublicKey getRSAPublicKey(BigInteger modulus, BigInteger publicExponent) {
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a key based on the parameters.
     *
     * @param modulus
     * @param privateExponent
     * @return the key
     */
    public static RSAPrivateKey getRSAPrivateKey(BigInteger modulus, BigInteger privateExponent) {
        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(modulus, privateExponent));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
