package com.minhui.vpn.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


import com.minhui.vpn.VPNConstants;
import com.minhui.vpn.service.FirewallVpnService;

import java.net.Socket;

public class VpnServiceHelper {
    private static Context context;
    public static final int START_VPN_SERVICE_REQUEST_CODE = 2015;
    private static FirewallVpnService sVpnService;
    private static SharedPreferences sp;

    public static void onVpnServiceCreated(FirewallVpnService vpnService) {
        sVpnService = vpnService;
        if (context == null) {
            context = vpnService.getApplicationContext();
        }
    }

    public static void onVpnServiceDestroy() {
        sVpnService = null;
    }

    public static Context getContext() {
        return context;
    }

    public static boolean isUDPDataNeedSave() {
        sp = context.getSharedPreferences(VPNConstants.VPN_SP_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(VPNConstants.IS_UDP_NEED_SAVE, false);
    }

    public static boolean protect(Socket socket) {
        if (sVpnService != null) {
            return sVpnService.protect(socket);
        }
        return false;
    }

    public static boolean vpnRunningStatus() {
        if (sVpnService != null) {
            return sVpnService.vpnRunningStatus();
        }
        return false;
    }

    public static void changeVpnRunningStatus(Context context, boolean isStart) {
        if (context == null) {
            return;
        }
        if (isStart) {
            Intent intent = FirewallVpnService.prepare(context);
            if (intent == null) {
                context.startService(new Intent(context, FirewallVpnService.class));
            } else {
                if (context instanceof Activity) {
                    ((Activity) context).startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
                }
            }
        } else if (sVpnService != null) {
            sVpnService.setVpnRunningStatus(false);
        }
    }


}
