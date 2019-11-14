package com.mak.pfapp;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class Des {
    private static final String mIV = "o0l00min";
    private static final String mKey = "6f6o6o0l";

    public static String Decrypt(String paramString) throws Exception {
        if (paramString == null || paramString.length() == 0)
            return null;
        byte[] arrayOfByte = Base64.decode(paramString, 0);
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        DESKeySpec dESKeySpec = new DESKeySpec(mKey.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, SecretKeyFactory.getInstance("DES").generateSecret(dESKeySpec), new IvParameterSpec(mIV.getBytes()));
        return new String(cipher.doFinal(arrayOfByte));
    }
    public static String Encrypt(String paramString) throws Exception {
        if (paramString == null || paramString.length() == 0)
            return null;
        byte[] arrayOfByte = paramString.getBytes();
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        DESKeySpec dESKeySpec = new DESKeySpec(mKey.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeyFactory.getInstance("DES").generateSecret(dESKeySpec), new IvParameterSpec(mIV.getBytes()));
        return Base64.encodeToString(cipher.doFinal(arrayOfByte),0);
    }
}
