package com.minhui.vpn.tunnel;

import android.net.VpnService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {

	public static TcpTunnel CreateLocalTunnel(VpnService vpnService, SocketChannel channel, Selector selector) {
		return new RawTcpTunnel(vpnService, channel, selector);
	}

	public static TcpTunnel CreateRemoteTunnel(VpnService vpnService, InetSocketAddress destAddress, Selector selector, short portKey) throws IOException {
		return new RemoteTcpTunnel(vpnService,destAddress, selector, portKey);
	}
}
