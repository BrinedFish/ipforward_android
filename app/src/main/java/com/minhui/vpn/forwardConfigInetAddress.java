package com.minhui.vpn;

import com.minhui.vpn.utils.CommonMethods;

import java.io.Serializable;
import java.net.InetAddress;

public class forwardConfigInetAddress implements Serializable {
    public InetAddress address;
    public short port;
    public boolean needResetHost;
    public int ProxyModePort;

    forwardConfigInetAddress(int address, short port, boolean nrh, int pm) {
        this.address = CommonMethods.ipIntToInet4Address(address);
        this.port = port;
        this.needResetHost = nrh;
        this.ProxyModePort = pm;
    }
}
