package com.mak.pfapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.io.InputStream;

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

    public static String getDeviceId(Context context) {
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

    public static void setHentaiMode(Context context, boolean isHentai) {
        ComponentName cp1 = new ComponentName(context, "com.mak.pfapp.MainActivity");
        ComponentName cp2 = new ComponentName(context, "com.mak.pfapp.MainActivity_hentai");

        PackageManager packageManager = context.getPackageManager();
        if (isHentai){
            packageManager.setComponentEnabledSetting(cp2, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(cp1, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        }else{
            packageManager.setComponentEnabledSetting(cp1, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
            packageManager.setComponentEnabledSetting(cp2, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        }
    }

}
