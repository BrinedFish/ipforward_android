package com.mak.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.minhui.vpn.forwardConfig;
import com.minhui.vpn.utils.VpnServiceHelper;
import android.widget.Button;
import android.widget.EditText;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String cfgUrl = "http://192.168.4.77/lst.txt";
    final Handler mHandler = new Handler();
    Button btn_start;
    EditText editText_rule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        btn_start = findViewById(R.id.btn_1);
        editText_rule = findViewById(R.id.editText_rule);
        downloadCfg(cfgUrl);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void downloadCfg(final String urlStr){
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
                    HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(urlStr)).openConnection();
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();
                    if (httpURLConnection.getResponseCode() == 200) {
                        String data = iS2String(httpURLConnection.getInputStream());
                        //System.out.println(data);
                        editText_rule.setText(data);
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
            downloadCfg(cfgUrl);
            return true;
        } else if (id==R.id.action_settings){
            Intent cfg = new Intent();
            cfg.setClass(MainActivity.this, cfg.class);
            MainActivity.this.startActivity(cfg);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
