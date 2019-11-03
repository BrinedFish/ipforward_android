package com.minhui.vpn;

import com.minhui.vpn.utils.CommonMethods;

import java.io.Serializable;
import java.net.InetAddress;

public class forwardConfigInetAddress implements Serializable {
    public InetAddress address;
    public short port;

    forwardConfigInetAddress(int address, short port) {
        this.address = CommonMethods.ipIntToInet4Address(address);
        this.port = port;
    }
}
