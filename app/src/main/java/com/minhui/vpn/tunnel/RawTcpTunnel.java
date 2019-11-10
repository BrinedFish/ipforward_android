package com.minhui.vpn.tunnel;

import android.net.VpnService;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


public class RawTcpTunnel extends TcpTunnel {

	public RawTcpTunnel(VpnService vpnService, SocketChannel innerChannel, Selector selector) {
		super(vpnService, innerChannel, selector);
	}

	public RawTcpTunnel(VpnService vpnService, InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
		super(vpnService, serverAddress, selector, portKey);

	}

	@Override
	protected void onConnected() throws Exception {
		onTunnelEstablished();
	}

	@Override
	protected boolean isTunnelEstablished() {
		return true;
	}

	@Override
	protected void beforeSend(ByteBuffer buffer) throws Exception {
		if (((RemoteTcpTunnel)mBrotherTunnel).needResetHost) {
			byte[] hb = buffer.array();
			Log.e("RawTcpTunnel",new String(hb,0, buffer.limit()));
		}
	}

	@Override
	protected void afterReceived(ByteBuffer buffer) throws Exception {

	}

	@Override
	protected void onDispose() {

	}
}
