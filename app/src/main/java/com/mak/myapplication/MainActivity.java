package com.mak.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.minhui.vpn.forwardConfig;
import com.minhui.vpn.utils.VpnServiceHelper;
import android.widget.Button;
import android.widget.EditText;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String cfgUrl = "http://127.0.0.1/lst.txt";
    final Handler mHandler = new Handler();
    Button btn_start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_start = findViewById(R.id.btn_1);
        EditText editText_rule = findViewById(R.id.editText_rule);
        editText_rule.setText(downloadCfg(cfgUrl));
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("aaaaaaa", "onClick: ------------------------------");
                //finish();
                if(VpnServiceHelper.vpnRunningStatus()){
                    closeVpn();
                }else {
                    forwardConfig.init(((EditText)findViewById(R.id.editText_rule)).getText().toString());
                    startVPN();
                }
            }
        });
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if(VpnServiceHelper.vpnRunningStatus()) {
                    btn_start.setText("断开");
                } else {
                    btn_start.setText("连接");
                }
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(r, 1000);
    }
    private void closeVpn() {
        VpnServiceHelper.changeVpnRunningStatus(this,false);
    }

    private void startVPN() {
        VpnServiceHelper.changeVpnRunningStatus(this,true);
    }

    public String iS2String(InputStream in) throws IOException {
        StringBuilder out = new StringBuilder();
        byte[] arrayOfByte = new byte[4096];
        while (true) {
            int i = in.read(arrayOfByte);
            if (i == -1) {
                break;
            }
            out.append(new String(arrayOfByte, 0, i));
        }
        return out.toString();
    }

    private String downloadCfg(String urlStr){
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(urlStr)).openConnection();
            httpURLConnection.setConnectTimeout(30000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == 200) {
                String data = iS2String(httpURLConnection.getInputStream());
                //System.out.println(data);
                return data;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return "";
    }

}
