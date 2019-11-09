package com.minhui.vpn.tunnel;

import android.net.VpnService;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.nat.NatSessionManager;
import com.minhui.vpn.utils.CommonMethods;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

public class RemoteTcpTunnel extends RawTcpTunnel {
    public boolean needResetHost = false;
    private String RemoteHost;
    private byte[] RemoteHostBytes;
    public RemoteTcpTunnel(VpnService vpnService, InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
        super(vpnService, serverAddress, selector, portKey);
        RemoteHost = serverAddress.getAddress().getHostAddress()+":"+serverAddress.getPort();
        RemoteHostBytes = RemoteHost.getBytes();
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
        super.afterReceived(buffer);
    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {
        if (needResetHost) {
            byte[] hb = buffer.array();
            String s = new String(hb,0,3);
            if ("GET".equals(s) || "POS".equals(s)) {
                for (int i = 0; i < hb.length; i++) {
                    if (hb[i]==0x0a){
                        if(hb[i+1]==0x48 &&
                           hb[i+2]==0x6f &&
                           hb[i+3]==0x73 &&
                           hb[i+4]==0x74) //Host
                        {
                            ByteBuffer Tmpbuffer = ByteBuffer.allocate(hb.length);
                            Tmpbuffer.put(hb,0,i + 6);
                            Tmpbuffer.put(RemoteHostBytes);
                            for (int j = i+5; j < hb.length; j++) {
                                if (hb[j]==0x0d) {
                                    Tmpbuffer.put(hb,j,buffer.limit()-j);
                                    break;
                                }
                            }
                            Tmpbuffer.flip();
                            buffer.rewind();
                            buffer.limit(buffer.capacity());
                            buffer.put(Tmpbuffer.array(),0, Tmpbuffer.limit());
                            buffer.flip();
                            break;
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void onDispose() {
        super.onDispose();
    }
}
