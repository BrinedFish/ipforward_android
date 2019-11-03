package com.minhui.vpn;

public class ProxyConfig {
	public static final boolean IS_DEBUG = true;

	public static String SessionName = "single vpn";
	static int mMtu = 20000;

	//本地代理ip池
	public static String LocalIpAddress = "10.8.0.2";
	public static int LocalIpPrefixLength = 32;



	public static int getMTU() {
		if (mMtu > 1400 && mMtu <= 20000) {
			return mMtu;
		} else {
			return 20000;
		}
	}
}
