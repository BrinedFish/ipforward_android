package com.minhui.vpn;

import com.minhui.vpn.utils.CommonMethods;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

public class ForwardConfig {
    private static ForwardConfig instance;
    private final ConcurrentHashMap<Long, forwardConfigInetAddress> forwardTable = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> dnsTable = new ConcurrentHashMap<>();

    private ForwardConfig(){ }

    public static ForwardConfig getInstance() {
        if(instance == null){
            instance = new ForwardConfig();
        }
        return instance;
    }

    public forwardConfigInetAddress getAddress(Long portKey) {
        return forwardTable.get(portKey);
    }
    public forwardConfigInetAddress getAddress(int ip, short port) {
        long key = ((long)ip << 24) | port;
        return getAddress(key);
    }
    public void ResetForwardTable(){
        forwardTable.clear();
    }
    public void AppendForwardTable(JSONArray cfgData)
    {
        for (int i = 0; i < cfgData.length(); i++) {
            try {
                JSONObject aObj = (JSONObject)cfgData.get(i);
                String[] aForwardData = aObj.optString("data","").replace(" ","").trim().split("=");
                if (aForwardData.length == 2) {
                    String[] fromData = aForwardData[0].split(":");
                    if (fromData.length != 2) continue;
                    int fromIp = CommonMethods.ipStringToInt(fromData[0]);
                    int fromPort = Integer.parseInt(fromData[1]);

                    String[] toData = aForwardData[1].split(":");
                    if (toData.length != 2) continue;
                    int toIp = CommonMethods.ipStringToInt(toData[0]);
                    int toPort = Integer.parseInt(toData[1]);

                    long key = ((long)fromIp << 24) | (short)(fromPort & 0xffff);
                    forwardTable.put(key, new forwardConfigInetAddress(toIp, (short)(toPort & 0xffff), aObj.optBoolean("nrh"), aObj.optInt("pm",0)));
                }
            } catch (Exception ignored) { }
        }
    }
    public int lengthOfForwardTable(){
        return forwardTable.size();
    }

    public void AppendDnsTable(JSONArray cfgData){
        for (int i = 0; i < cfgData.length(); i++) {
            try {
                JSONObject aObj = (JSONObject) cfgData.get(i);
                String[] aForwardData = aObj.optString("data","").replace(" ","").trim().split("=");
                if (aForwardData.length == 2) {
                    int Ip = CommonMethods.ipStringToInt(aForwardData[1]);
                    dnsTable.put(aForwardData[0], Ip);
                }
            } catch (Exception ignored) { }
        }
    }
    public void ResetDnsTable(){
        dnsTable.clear();
    }
    public int lengthOfDnsTable(){
        return dnsTable.size();
    }

    public int getDnsTable(String domain) {
        Integer xx = dnsTable.get(domain);
        if (xx != null) {
            return xx;
        }
        return 0;
    }
}
