package com.minhui.vpn.nat;


import android.os.Build;

import java.io.Serializable;
import java.util.Locale;

public class NatSession implements Serializable {
    public static final String TCP = "TCP";
    public static final String UDP = "UPD";
    public String type;

    public short localPort;
    public int remoteIP;
    public short remotePort;
    public String remoteIpStr;

    public int bytesSent;
    public int packetSent;

    public long receiveByteNum;
    public long receivePacketNum;

    public long lastRefreshTime;
    public final long connectionStartTime = System.currentTimeMillis();

    @Deprecated
    public long vpnStartTime;


    @Override
    public String toString() {
//        return String.format("%s/%s:%d packet: %d", remoteIpStr, CommonMethods.ipIntToString(remoteIP),
//                remotePort & 0xFFFF, packetSent);
        return String.format(Locale.ENGLISH,"%s:  %d <=> %s:%d packet: %d", type, localPort & 0xFFFF, remoteIpStr,
                remotePort & 0xFFFF, packetSent);

    }

    public String getUniqueName() {
        String uinID = connectionStartTime+"";
        return String.valueOf(uinID.hashCode());
    }


    public static class NatSesionComparator implements java.util.Comparator<NatSession> {
        @Override
        public int compare(NatSession o1, NatSession o2) {
            if (o1 == o2) {
                return 0;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return Long.compare(o2.lastRefreshTime, o1.lastRefreshTime);
            }
            return 0;
        }
    }
}
