package com.movit.platform.common.okhttp.utils;

import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.util.encoders.Base64;


/**
 * AES工具类
 *
 * @author xzmeng
 * @since 20180831
 */
public class AesUtils {

  private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
  public static final String KEY = "t96IBJWOMMI98OIJjQ36pw==";

  private static AesUtils aesUtils;

  private AesUtils() {
  }

  public static final AesUtils getInstance() {
    if (aesUtils == null) {
      synchronized (AesUtils.class) {
        if (aesUtils == null) {
          aesUtils = new AesUtils();
        }
      }
    }
    return aesUtils;
  }

  /**
   * 获取秘钥方法
   */
  private byte[] getKey() {
    KeyGenerator kg;
    try {
      kg = KeyGenerator.getInstance("AES");
      kg.init(192);
      SecretKey sk = kg.generateKey();
      byte[] b = sk.getEncoded();
      System.out.println("KEY---------" + new String(Base64.encode(b)));
      return b;
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * AES加密方法
   */
  public String encrypt(String str) {
    byte[] result = null;
//	      getKey();
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      SecretKeySpec keySpec = new SecretKeySpec(Base64.decode(KEY.getBytes()), "AES"); //生成加密解密需要的Key
      cipher.init(Cipher.ENCRYPT_MODE, keySpec);
      result = cipher.doFinal(str.getBytes("UTF-8"));
      return URLEncoder.encode(new String(Base64.encode(result)), "UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * AES解密方法
   */
  public String decrypt(String str) {
    String result = null;
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      SecretKeySpec keySpec = new SecretKeySpec(Base64.decode(KEY.getBytes()), "AES");
      cipher.init(Cipher.DECRYPT_MODE, keySpec);
      byte[] decoded = cipher.doFinal(Base64.decode(str.getBytes()));
      result = new String(decoded, "UTF-8");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

}
