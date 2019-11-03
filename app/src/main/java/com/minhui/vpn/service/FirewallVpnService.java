package com.minhui.vpn.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;


import com.minhui.vpn.*;
import com.minhui.vpn.nat.NatSession;
import com.minhui.vpn.nat.NatSessionManager;
import com.minhui.vpn.proxy.TcpProxyServer;
import com.minhui.vpn.tcpip.IPHeader;
import com.minhui.vpn.tcpip.TCPHeader;
import com.minhui.vpn.tcpip.UDPHeader;
import com.minhui.vpn.utils.CommonMethods;
import com.minhui.vpn.utils.DebugLog;
import com.minhui.vpn.utils.TimeFormatUtil;
import com.minhui.vpn.utils.VpnServiceHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FirewallVpnService extends VpnService implements Runnable {
    public static final String ACTION_START_VPN = "com.minhui.START_VPN";
    public static final String ACTION_CLOSE_VPN = "com.minhui.roav.CLOSE_VPN";
    private static final String FACEBOOK_APP = "com.facebook.katana";
    private static final String YOUTUBE_APP = "com.google.android.youtube";
    private static final String GOOGLE_MAP_APP = "com.google.android.apps.maps";

    private static final String VPN_ADDRESS = "10.0.0.2"; // Only IPv4 support for now
    private static final String VPN_ROUTE = "0.0.0.0"; // Intercept everything
    private static final String GOOGLE_DNS_FIRST = "8.8.8.8";
    private static final String GOOGLE_DNS_SECOND = "8.8.4.4";
    private static final String AMERICA = "208.67.222.222";
    private static final String HK_DNS_SECOND = "205.252.144.228";
    private static final String CHINA_DNS_FIRST = "114.114.114.114";
    public static final String BROADCAST_VPN_STATE = "com.minhui.localvpn.VPN_STATE";
    public static final String SELECT_PACKAGE_ID = "select_protect_package_id";
    private static final String TAG = "FirewallVpnService";
    private static int ID;
    private static int LOCAL_IP;
    private final UDPHeader mUDPHeader;
    private final ByteBuffer mDNSBuffer;
    private boolean IsRunning = false;
    private Thread mVPNThread;
    private ParcelFileDescriptor mVPNInterface;
    private TcpProxyServer mTcpProxyServer;
    // private DnsProxy mDnsProxy;
    private FileOutputStream mVPNOutputStream;

    private byte[] mPacket;
    private IPHeader mIPHeader;
    private TCPHeader mTCPHeader;
    private Handler mHandler;
    private ConcurrentLinkedQueue<Packet> udpQueue;
    private UDPServer udpServer;
    private String selectPackage;
    public static final int MUTE_SIZE = 2560;
    private int mReceivedBytes;
    private int mSentBytes;
    public static long vpnStartTime;
    public static String lastVpnStartTimeFormat = null;
    private SharedPreferences sp;

    public FirewallVpnService() {
        ID++;
        mHandler = new Handler();
        mPacket = new byte[MUTE_SIZE];
        mIPHeader = new IPHeader(mPacket, 0);
        //Offset = ip报文头部长度
        mTCPHeader = new TCPHeader(mPacket, 20);
        mUDPHeader = new UDPHeader(mPacket, 20);
        //Offset = ip报文头部长度 + udp报文头部长度 = 28
        mDNSBuffer = ((ByteBuffer) ByteBuffer.wrap(mPacket).position(28)).slice();

        DebugLog.i("New VPNService(%d)\n", ID);
    }

    @Override
    public void onCreate() {
        DebugLog.i("VPNService(%s) created.\n", ID);
        sp = getSharedPreferences(VPNConstants.VPN_SP_NAME, Context.MODE_PRIVATE);
        VpnServiceHelper.onVpnServiceCreated(this);
        mVPNThread = new Thread(this, "VPNServiceThread");
        mVPNThread.start();
        setVpnRunningStatus(true);
        //   notifyStatus(new VPNEvent(VPNEvent.Status.STARTING));
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        DebugLog.i("VPNService(%s) destroyed.\n", ID);
        if (mVPNThread != null) {
            mVPNThread.interrupt();
        }
        VpnServiceHelper.onVpnServiceDestroy();
        super.onDestroy();
    }

    private void runVPN() throws Exception {
        this.mVPNInterface = establishVPN();
        startStream();
    }

    private void startStream() throws Exception {
        int size = 0;
        mVPNOutputStream = new FileOutputStream(mVPNInterface.getFileDescriptor());
        FileInputStream in = new FileInputStream(mVPNInterface.getFileDescriptor());
        try{
            while (size != -1 && IsRunning) {
                boolean hasWrite = false;
                size = in.read(mPacket);
                if (size > 0) {
                    if (mTcpProxyServer.Stopped) {
                        throw new Exception("LocalServer stopped.");
                    }
                    hasWrite = onIPPacketReceived(mIPHeader, size);
                }
                if (!hasWrite) {
                    Packet packet = udpQueue.poll();
                    if (packet != null) {
                        ByteBuffer bufferFromNetwork = packet.backingBuffer;
                        bufferFromNetwork.flip();
                        mVPNOutputStream.write(bufferFromNetwork.array());
                    }
                }
                Thread.sleep(10);
            }
        }finally {
            in.close();
            mVPNOutputStream.close();
            mVPNOutputStream = null;
        }
        disconnectVPN();
    }

    boolean onIPPacketReceived(IPHeader ipHeader, int size) throws IOException {
        boolean hasWrite = false;

        switch (ipHeader.getProtocol()) {
            case IPHeader.TCP:
                hasWrite = onTcpPacketReceived(ipHeader, size);
                break;
            case IPHeader.UDP:
                onUdpPacketReceived(ipHeader, size);
                break;
            default:
                break;
        }
        return hasWrite;
    }

    private void onUdpPacketReceived(IPHeader ipHeader, int size) throws UnknownHostException {
        TCPHeader tcpHeader = mTCPHeader;
        short portKey = tcpHeader.getSourcePort();


        NatSession session = NatSessionManager.getSession(portKey);
        if (session == null || session.remoteIP != ipHeader.getDestinationIP() || session.remotePort
                != tcpHeader.getDestinationPort()) {
            session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader
                    .getDestinationPort(), NatSession.UDP);
            session.vpnStartTime = vpnStartTime;
        }

        session.lastRefreshTime = System.currentTimeMillis();
        session.packetSent++; //注意顺序

        byte[] bytes = Arrays.copyOf(mPacket, mPacket.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, size);
        byteBuffer.limit(size);
        Packet packet = new Packet(byteBuffer);
        udpServer.processUDPPacket(packet, portKey);
    }

    private boolean onTcpPacketReceived(IPHeader ipHeader, int size) throws IOException {
        TCPHeader tcpHeader = mTCPHeader;
        //矫正TCPHeader里的偏移量，使它指向TCP包地址
        tcpHeader.mOffset = ipHeader.getHeaderLength();
        if (tcpHeader.getSourcePort() == mTcpProxyServer.port) {
            DebugLog.dWithTag(TAG, "process  tcp packet from net ");
            NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
            if (session != null) {
                ipHeader.setSourceIP(ipHeader.getDestinationIP());
                tcpHeader.setSourcePort(session.remotePort);
                ipHeader.setDestinationIP(LOCAL_IP);

                CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
                mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
                mReceivedBytes += size;
            } else {
                DebugLog.i("NoSession: %s %s\n", ipHeader.toString(), tcpHeader.toString());
            }

        } else {
            DebugLog.dWithTag(TAG, "process  tcp packet to net ");
            //添加端口映射
            short portKey = tcpHeader.getSourcePort();
            NatSession session = NatSessionManager.getSession(portKey);
            if (session == null
                    || session.remoteIP != ipHeader.getDestinationIP()       //连接不同主机
                    || session.remotePort != tcpHeader.getDestinationPort()  //连接相同主机的不同服务
            ) {
                session = NatSessionManager.createSession(portKey, ipHeader.getDestinationIP(), tcpHeader.getDestinationPort(), NatSession.TCP);
                session.vpnStartTime = vpnStartTime;
            }
            session.lastRefreshTime = System.currentTimeMillis();
            session.packetSent++; //注意顺序
            int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
            //丢弃tcp握手的第二个ACK报文。因为客户端发数据的时候也会带上ACK，这样可以在服务器Accept之前分析出HOST信息。
            if (session.packetSent == 2 && tcpDataSize == 0) {
                return false;
            }

            //转发给本地TCP服务器
            ipHeader.setSourceIP(ipHeader.getDestinationIP());
            ipHeader.setDestinationIP(LOCAL_IP);
            tcpHeader.setDestinationPort(mTcpProxyServer.port);

            CommonMethods.ComputeTCPChecksum(ipHeader, tcpHeader);
            mVPNOutputStream.write(ipHeader.mData, ipHeader.mOffset, size);
            //注意顺序
            session.bytesSent += tcpDataSize;
            mSentBytes += size;
        }
        return true;
    }

    private void waitUntilPrepared() {
        while (prepare(this) != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                DebugLog.e("waitUntilPrepared catch an exception %s\n", e);
            }
        }
    }

    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(MUTE_SIZE);
        DebugLog.i("setMtu: %d\n", ProxyConfig.getMTU());

        LOCAL_IP = CommonMethods.ipStringToInt(ProxyConfig.LocalIpAddress);
        builder.addAddress(ProxyConfig.LocalIpAddress, ProxyConfig.LocalIpPrefixLength);
        DebugLog.i("addAddress: %s/%d\n", ProxyConfig.LocalIpAddress, ProxyConfig.LocalIpPrefixLength);

        builder.addRoute(VPN_ROUTE, 0);

        builder.addDnsServer(GOOGLE_DNS_FIRST);
        builder.addDnsServer(CHINA_DNS_FIRST);
        builder.addDnsServer(GOOGLE_DNS_SECOND);
        builder.addDnsServer(AMERICA);
        vpnStartTime = System.currentTimeMillis();
        lastVpnStartTimeFormat = TimeFormatUtil.formatYYMMDDHHMMSS(vpnStartTime);
        selectPackage = sp.getString(VPNConstants.DEFAULT_PACKAGE_ID, null);
        try {
            if (selectPackage != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.addAllowedApplication(selectPackage);
                    builder.addAllowedApplication(getPackageName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        builder.setSession(ProxyConfig.SessionName);
        ParcelFileDescriptor pfdDescriptor = builder.establish();
        //  notifyStatus(new VPNEvent(VPNEvent.Status.ESTABLISHED));
        return pfdDescriptor;
    }

    @Override
    public void run() {
        try {
            DebugLog.i("VPNService(%s) work thread is Running...\n", ID);

            waitUntilPrepared();
            udpQueue = new ConcurrentLinkedQueue<>();
            //启动TCP代理服务
            mTcpProxyServer = new TcpProxyServer(0);
            mTcpProxyServer.start();
            udpServer = new UDPServer(this, udpQueue);
            udpServer.start();
            NatSessionManager.clearAllSession();
            DebugLog.i("DnsProxy started.\n");

            while (IsRunning) {
                runVPN();
            }


        } catch (InterruptedException e) {
            DebugLog.e("VpnService run catch an exception %s.\n", e);
        } catch (Exception e) {
            DebugLog.e("VpnService run catch an exception %s.\n", e);
        } finally {
            DebugLog.i("VpnService terminated");
            dispose();
        }
    }

    public void disconnectVPN() {
        try {
            if (mVPNInterface != null) {
                mVPNInterface.close();
                mVPNInterface = null;
            }
        } catch (Exception e) {
            //ignore
        }
        // notifyStatus(new VPNEvent(VPNEvent.Status.UNESTABLISHED))
    }

    private synchronized void dispose() {
        try {
            //断开VPN
            disconnectVPN();

            //停止TCP代理服务
            if (mTcpProxyServer != null) {
                mTcpProxyServer.stop();
                mTcpProxyServer = null;
                DebugLog.i("TcpProxyServer stopped.\n");
            }
            if (udpServer != null) {
                udpServer.closeAllUDPConn();
            }

            stopSelf();
            setVpnRunningStatus(false);
        } catch (Exception e) {

        }

    }


    public boolean vpnRunningStatus() {
        return IsRunning;
    }

    public void setVpnRunningStatus(boolean isRunning) {
        IsRunning = isRunning;
    }
}
