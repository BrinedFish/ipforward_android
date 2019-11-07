package com.mak.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Utility {

    public static String iS2String(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] arrayOfByte = new byte[4096];
        while (true) {
            int i = in.read(arrayOfByte);
            if (i == -1) {
                break;
            }
            out.append(new String(arrayOfByte, 0, i));
        }
        return out.toString();
    }

    public static String getUserId(Context context){
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        String userId = sp.getString("userid","");
        if (TextUtils.isEmpty(userId)) {
            userId = getDeviceId(context);
            if (TextUtils.isEmpty(userId)) {
                userId = UUID.randomUUID().toString();
            }
            sp.edit().putString("userid", userId).apply();
        }
        return userId;
    }

    private static String getDeviceId(Context context) {
        String iemi = "";
        TelephonyManager mTelephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony != null && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            try
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    iemi = mTelephony.getImei();
                }else{
                    iemi = mTelephony.getDeviceId();
                }
                if (iemi == null || iemi.length() == 0)
                {
                    iemi = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            }catch (Exception ignored){
            }
        }
        return iemi;
    }
}
