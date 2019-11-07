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
    private AlertDialog authDialog = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Thread ddd = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Api.HttpReqs("http://192.168.4.77:54824/api/x/gettoken",true, "{\"data\":\"111111111111111111111111\"}");
//            }
//        });
//        ddd.start();
        checkAuth();
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

    private void checkAuth(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.activity_auth, null);
        builder.setView(dialogView) ;
        authDialog = builder.create();
        authDialog.setCanceledOnTouchOutside(false);
        authDialog.setCancelable(false);
        authDialog.show();
        final EditText edt_auth_key = authDialog.findViewById(R.id.edt_auth_key);
        authDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), edt_auth_key.getText(), Toast.LENGTH_SHORT).show();

                final AlertDialog loadingDialog = CreateLoadingDialog();
                try {
                    Api.getAuth(MainActivity.this, new ApiCallBack<ApiResult>() {
                        @Override
                        public void run(final ApiResult result) {
                            if (result.ok) {
                                Api.setApiToken(MainActivity.this, result.data.optString("token"));
                                authDialog.dismiss();
                            }else{
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), result.msg, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            loadingDialog.dismiss();
                        }
                    }, edt_auth_key.getText().toString());
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
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
        try {
            Api.getForwardTable(this, new ApiCallBack<ApiResult>() {
                @Override
                public void run(final ApiResult result) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String msg;
                            if (result.ok){
                                try {
                                    editText_rule.setText("");
                                    JSONArray data = result.data.getJSONArray("list");
                                    for (int i = 0; i < data.length(); i++) {
                                        editText_rule.append(data.get(i)+"\r\n");
                                    }
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
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
        }
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
        }

        return super.onOptionsItemSelected(item);
    }


}
