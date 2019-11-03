package com.minhui.vpn.tunnel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {

	public static TcpTunnel wrap(SocketChannel channel, Selector selector) {
		return new RawTcpTunnel(channel, selector);
	}

	public static TcpTunnel createTunnelByConfig(InetSocketAddress destAddress, Selector selector, short portKey) throws IOException {
		return new RemoteTcpTunnel(destAddress, selector, portKey);
	}
}
