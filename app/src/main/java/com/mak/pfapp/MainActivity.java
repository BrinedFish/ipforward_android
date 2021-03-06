package com.mak.pfapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.mak.pfapp.ad.u3d;
import com.minhui.vpn.ForwardConfig;
import com.minhui.vpn.utils.VpnServiceHelper;
import android.widget.Button;
import android.widget.EditText;
import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
    private final Handler mHandler = new Handler();
    private Button btn_start;
    private EditText editText_rule;
    private VpnServiceHelper vpnServiceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        gad.reloadAd(this);

        u3d.get().init(this);

        checkAuth(new Runnable() {
            @Override
            public void run() {
                downloadCfg();
            }
        });
        vpnServiceHelper = new VpnServiceHelper(this);
        btn_start = findViewById(R.id.btn_1);
        editText_rule = findViewById(R.id.editText_rule);
        editText_rule.setInputType(InputType.TYPE_NULL);
        editText_rule.setSingleLine(false);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vpnServiceHelper.vpnRunningStatus()){
                    vpnServiceHelper.stopVpn();
                }else {
                    ForwardConfig fc = ForwardConfig.getInstance();
                    SharedPreferences sp = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                    //初始化转发规则
                    fc.ResetForwardTable();
                    fc.AppendForwardTable(Api.PfData);
                    try {
                        JSONArray cfg_data = new JSONArray(sp.getString("localpfcfg", "[]"));
                        fc.AppendForwardTable(cfg_data);
                    } catch (JSONException ignored) {
                    }
                    //初始化DNS规则
                    fc.ResetDnsTable();
                    fc.AppendDnsTable(Api.dnsData);
                    try {
                        JSONArray cfg_data = new JSONArray(sp.getString("localdnscfg", "[]"));
                        fc.AppendDnsTable(cfg_data);
                    } catch (JSONException ignored) {
                    }

                    guiLog("应用规则：" + fc.lengthOfForwardTable() + "+" + fc.lengthOfDnsTable());
                    if (!vpnServiceHelper.startVPN()) {
                        guiLog("请重新连接...");
                    }
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


    private void guiLog(String msg){
        editText_rule.append(msg+"\r\n");
    }

    private void checkAuth(final Runnable r){
        final AlertDialog loadingDialog = CreateLoadingDialog();
        Api.getSvrVersion(this, new ApiCallBack<ApiResult>() {
            @Override
            public void run(final ApiResult result) {
                loadingDialog.dismiss();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!result.ok){
                            //认证失败
                            Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_SHORT).show();
                            guiLog(result.msg);
                            ShowAuthDlg();
                        }else{
                            Api.Point = 0;
                            String svrVer = result.data.optString("version","");
                            if (!Api.sdkVersion.equals(svrVer)){
                                final String updateUrl = result.data.optString("url","http://www.baidu.com/");
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setCancelable(false)
                                        .setMessage(getString(R.string.text_update))
                                        .setTitle(getString(R.string.text_update_title))
                                        .setPositiveButton("ok", new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dlg, int paramInt) {
                                                dlg.dismiss();
                                                Intent intent = new Intent();
                                                intent.setData(Uri.parse(updateUrl));
                                                intent.setAction(Intent.ACTION_VIEW);
                                                startActivity(intent);
                                                MainActivity.this.finish();
                                            }
                                        }).show();
                            }else{
                                Api.Point = result.data.optInt("point",0);
                                Api.ViewPageUrl = result.data.optString("home",Api.ViewPageUrl);
                                Api.ViewPageUrlAuth = result.data.optString("home_auth",Api.ViewPageUrlAuth);
                                r.run();
                            }
                        }
                    }
                });
            }
        });
    }

    private void ShowAuthDlg(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.activity_auth, null);
        builder.setView(dialogView) ;
        final AlertDialog authDialog = builder.create();
//        authDialog.setCanceledOnTouchOutside(false);
//        authDialog.setCancelable(false);
        authDialog.show();
        final EditText edt_auth_key = authDialog.findViewById(R.id.edt_auth_key);
        authDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog loadingDialog = CreateLoadingDialog();
                Api.getAuth(MainActivity.this, new ApiCallBack<ApiResult>() {
                    @Override
                    public void run(final ApiResult result) {
                        loadingDialog.dismiss();
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result.ok) {
                                    authDialog.dismiss();
                                    checkAuth(new Runnable() {
                                        @Override
                                        public void run() {
                                            downloadCfg();
                                        }
                                    });
                                }else{
                                    Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }, edt_auth_key.getText().toString());
            }
        });

        authDialog.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authDialog.dismiss();
            }
        });

    }
    private AlertDialog CreateLoadingDialog(){
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         View dialogView = View.inflate(this, R.layout.dlg_loading, null);
         builder.setView(dialogView) ;
         AlertDialog loadingDialog = builder.create();
         loadingDialog.setCanceledOnTouchOutside(false);
         loadingDialog.setCancelable(false);
         loadingDialog.show();
         return loadingDialog;
     }

    private void downloadCfg(){
        final AlertDialog loadingDialog = CreateLoadingDialog();
        Api.getForwardTable(this, new ApiCallBack<ApiResult>() {
            @Override
            public void run(final ApiResult result) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msg;
                        if (result.ok){
                            try {
                                Api.PfData = result.data.getJSONArray("list");
                                Api.dnsData = result.data.getJSONArray("dns");
                                guiLog("下载规则：" + Api.PfData.length() + "+" + Api.dnsData.length());
                                msg = "ok";
                            } catch (JSONException e) {
                                msg = e.getMessage();
                            }
                        } else{
                            msg = result.msg;
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
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
            cfg.setClass(MainActivity.this, cfgActivity.class);
            MainActivity.this.startActivity(cfg);
            return true;
        }else if (id == R.id.action_adr) {
            MainActivity.this.checkAuth(new Runnable() {
                @Override
                public void run() {
                    Intent adr = new Intent();
                    adr.setClass(MainActivity.this, AdrActivity.class);
                    MainActivity.this.startActivity(adr);
                }
            });
            return true;
        } else if (id == R.id.action_wbv) {
            Intent cfg = new Intent();
            cfg.setClass(MainActivity.this, WebviewActivity.class);
            MainActivity.this.startActivity(cfg);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        vpnServiceHelper.stopVpn();
    }
}
