package com.mak.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.minhui.vpn.ForwardConfig;
import com.minhui.vpn.utils.VpnServiceHelper;
import android.widget.Button;
import android.widget.EditText;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String DefcfgUrl = "http://192.168.4.77/lst.txt";
    private final Handler mHandler = new Handler();
    private Button btn_start;
    private EditText editText_rule;
    private VpnServiceHelper vpnServiceHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        vpnServiceHelper = new VpnServiceHelper(this);

        btn_start = findViewById(R.id.btn_1);
        editText_rule = findViewById(R.id.editText_rule);
        downloadCfg();
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vpnServiceHelper.vpnRunningStatus()){
                    vpnServiceHelper.stopVpn();
                }else {
                    ForwardConfig.getInstance().init(((EditText)findViewById(R.id.editText_rule)).getText().toString());
                    vpnServiceHelper.startVPN();
                }
            }
        });
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if(vpnServiceHelper.vpnRunningStatus()) {
                    btn_start.setText("断开");
                } else {
                    btn_start.setText("连接");
                }
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(r, 1000);
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

    private void downloadCfg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.dlg_loading, null);
        builder.setView(dialogView) ;
        final AlertDialog loadingalertDialog = builder.create();
        loadingalertDialog.setCanceledOnTouchOutside(false);
        loadingalertDialog.setCancelable(false);
        loadingalertDialog.show();

        Executor g = Executors.newSingleThreadExecutor();
        g.execute(new Runnable() {
            @Override
            public void run() {
                String msg = "";
                try {
                    SharedPreferences sp = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                    String urlStr = sp.getString("api_addr","");
                    if (TextUtils.isEmpty(urlStr)){
                        urlStr = DefcfgUrl;
                    }
                    HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(urlStr)).openConnection(Proxy.NO_PROXY);
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();
                    if (httpURLConnection.getResponseCode() == 200) {
                        final String data = iS2String(httpURLConnection.getInputStream());
                        //System.out.println(data);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editText_rule.setText(data);
                            }
                        });
                        msg = "ok";
                    }else{
                        msg = httpURLConnection.getResponseCode()+"";
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                    msg = exception.getMessage();
                }
                loadingalertDialog.dismiss();
                Looper.prepare();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.down_cfg) {
            downloadCfg();
            return true;
        } else if (id == R.id.action_settings) {
            Intent cfg = new Intent();
            cfg.setClass(MainActivity.this, cfg.class);
            MainActivity.this.startActivity(cfg);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
