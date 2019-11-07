package com.minhui.vpn.utils;

import android.app.Activity;
import android.content.*;


import android.os.IBinder;
import com.minhui.vpn.service.FirewallVpnService;

import static android.content.Context.BIND_AUTO_CREATE;

public class VpnServiceHelper {
    private static final int START_VPN_SERVICE_REQUEST_CODE = 2015;

    private Activity activity;
    private FirewallVpnService sVpnService;
    private ServiceConnection mServiceConnection;

    public VpnServiceHelper(Activity activity) {
        this.activity = activity;
        mServiceConnection=new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                sVpnService = null;
            }
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                sVpnService = ((FirewallVpnService.ServiceBinder) service).getInstance();//程序走到这里之后，就得到了Service对象
            }
        };
    }

    public boolean vpnRunningStatus() {
        if (sVpnService != null) {
            return sVpnService.vpnRunningStatus();
        }
        return false;
    }

    public void stopVpn() {
        sVpnService.setVpnRunningStatus(false);
        activity.unbindService(mServiceConnection);
        //context.stopService(new Intent(context, FirewallVpnService.class));
    }

    public void startVPN() {
        Intent intent = FirewallVpnService.prepare(activity);
        if (intent == null) {
            Intent bindIntent = new Intent(activity, FirewallVpnService.class);
            activity.bindService(bindIntent, mServiceConnection, BIND_AUTO_CREATE);
            //context.startService(bindIntent);
        } else {
            activity.startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
        }
    }


}
