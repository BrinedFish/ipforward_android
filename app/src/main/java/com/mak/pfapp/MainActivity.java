package com.mak.pfapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
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
//    private RewardedVideoAd mRewardedVideoAd;
//
//    private void loadRewardedVideoAd() {
//        mRewardedVideoAd.loadAd("ca-app-pub-5930562548810475/2706192227",
//                new AdRequest.Builder().build());
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        MobileAds.initialize(this, "ca-app-pub-5930562548810475~1424900144");
//        // Use an activity context to get the rewarded video instance.
//        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
//        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
//            @Override
//            public void onRewardedVideoAdLoaded() {
//
//            }
//
//            @Override
//            public void onRewardedVideoAdOpened() {
//
//            }
//
//            @Override
//            public void onRewardedVideoStarted() {
//
//            }
//
//            @Override
//            public void onRewardedVideoAdClosed() {
//                loadRewardedVideoAd();
//            }
//
//            @Override
//            public void onRewarded(RewardItem rewardItem) {
//                System.out.println("-----------------------------onRewarded------------------------------------");
//            }
//
//            @Override
//            public void onRewardedVideoAdLeftApplication() {
//
//            }
//
//            @Override
//            public void onRewardedVideoAdFailedToLoad(int i) {
//
//            }
//
//            @Override
//            public void onRewardedVideoCompleted() {
//
//            }
//        });
//
//        loadRewardedVideoAd();
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        gad.reloadAd(this);

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
                    checkAuth(new Runnable() {
                        @Override
                        public void run() {
                            if(!vpnServiceHelper.startVPN()){
                                guiLog("请重新连接...");
                            }
                        }
                    });
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
                                JSONArray data = result.data.getJSONArray("list");
                                guiLog(data.toString());
                                guiLog("下载规则："+data.length());
                                ForwardConfig.getInstance().init(data);
                                guiLog("应用规则："+ForwardConfig.getInstance().length());
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
//            if (mRewardedVideoAd.isLoaded()) {
//                mRewardedVideoAd.show();
//            }
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

}
