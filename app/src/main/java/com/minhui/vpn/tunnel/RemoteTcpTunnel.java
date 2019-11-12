package com.minhui.vpn.tunnel;

import android.net.VpnService;
import android.util.Log;
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
    public int ProxyModePort = 0;
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
        if (ProxyModePort > 0){
            byte[] hb = buffer.array();
            for (int i = 0; i < buffer.limit(); i++) {
                hb[i] ^= 0x01;
            }
            ByteBuffer Tmpbuffer = ByteBuffer.allocate(hb.length);
            Tmpbuffer.putShort((short) 0x0155);
            Tmpbuffer.putShort((short) (buffer.limit() + 6));
            Tmpbuffer.putShort((short) ProxyModePort);
            Tmpbuffer.put(hb, 0, buffer.limit());
            Tmpbuffer.flip();
            buffer.rewind();
            buffer.limit(buffer.capacity());
            buffer.put(Tmpbuffer.array(),0, Tmpbuffer.limit());
            buffer.flip();
        }else if (needResetHost) {
            ResetHttpHost(buffer);
            Log.e("RemoteTcpTunnel",new String(buffer.array(),0, buffer.limit()));
        }
    }
    protected void ResetHttpHost(ByteBuffer buffer) throws Exception {
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
    private void removeTlsServerName(ByteBuffer buffer) {
        buffer.rewind();
        //skip header
        buffer.position(buffer.position() + 0x2b);
        //skip sessionID
        buffer.position(buffer.get() + buffer.position());
        //skip Cipher Suites
        buffer.position(buffer.getShort() + buffer.position());
        //skip Compression Methods
        buffer.position(buffer.get() + buffer.position());
        int ExtensionsLength = buffer.getShort();
        System.out.println("ExtensionsLength:" + ExtensionsLength);
        int ExtensionsEnd = ExtensionsLength + buffer.position();
        int server_name_len = 0;
        int loopCnt = 0;
        while (buffer.position() < ExtensionsEnd && loopCnt++ < 100) {
            int type = buffer.getShort();
            int length = buffer.getShort();
            if (type == 0) {
                //Extension: server_name (len=11)
                server_name_len = length + 4;
                //move Extension
                int NodeStartOffset = buffer.position() - 4;
                buffer.position(NodeStartOffset);
                int cpyCnt = ExtensionsEnd - buffer.position() - server_name_len;
                buffer.put(buffer.array(), NodeStartOffset + server_name_len, cpyCnt);
                buffer.position(NodeStartOffset);
            } else if (type == 21 && server_name_len > 0) {
                buffer.position(buffer.position() - 2);
                buffer.putShort((short) (length + server_name_len));
                break;
            } else {
                buffer.position(buffer.position() + length);
            }
        }
        buffer.rewind();
    }
}
