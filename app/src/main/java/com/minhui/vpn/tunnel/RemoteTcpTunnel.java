package com.minhui.vpn.tunnel;

import android.net.VpnService;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.nat.NatSessionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

public class RemoteTcpTunnel extends RawTcpTunnel {
    public RemoteTcpTunnel(VpnService vpnService, InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
        super(vpnService, serverAddress, selector, portKey);
    }


    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
        super.afterReceived(buffer);
    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {
        super.beforeSend(buffer);
    }


    @Override
    protected void onDispose() {
        super.onDispose();
    }
}
