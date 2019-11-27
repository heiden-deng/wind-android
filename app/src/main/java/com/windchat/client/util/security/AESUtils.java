package com.windchat.client.util.security;

import com.orhanobut.logger.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by yichao on 2017/10/20.
 */

public class AESUtils {

    private static final String TAG = "AESUtils";

    public static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    public static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    /**
     * 生成Ts-key
     *
     * @return 返回256位的Ts-key
     */
    public static byte[] generateTSKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey skey = keyGenerator.generateKey();
            return skey.getEncoded();
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return null;
    }

//    /**
//     * 生成Ts-key
//     *
//     * @return 返回256位的Ts-key
//     */
//    public static String generateTSKey() {
//        return "fhggjg9494hgkgggl";
//        try {
//            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
////        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
//            keyGenerator.init(256);
//            SecretKey skey = keyGenerator.generateKey();
//            return skey.getEncoded();
//        } catch (Exception e) {
//            Logger.e(TAG, e);
//        }
//        return null;
//    }


}
