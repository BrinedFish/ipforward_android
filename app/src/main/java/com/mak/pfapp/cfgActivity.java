package com.mak.pfapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class cfgActivity extends AppCompatActivity {

    EditText edit_api_addr;
    Switch switch_ishentai;
    TextView lbl_local_pf_cnt;
    TextView lbl_local_dns_cnt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cfg);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setNavigationIcon( mDrawable);//或者在布局中 app:navigationIcon="?attr/homeAsUpIndicator"
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });
        final SharedPreferences sp = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("ApiSvrUrl", edit_api_addr.getText().toString());
                ed.putBoolean("ishentai", switch_ishentai.isChecked());
                ed.apply();

                Snackbar.make(view, "ok", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        edit_api_addr = findViewById(R.id.edit_api_addr);
        edit_api_addr.setText(sp.getString("ApiSvrUrl",""));
        switch_ishentai = findViewById(R.id.switch_ishentai);
        switch_ishentai.setChecked(sp.getBoolean("ishentai",false));
        switch_ishentai.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,final boolean isChecked) {
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("ishentai", isChecked);
                ed.apply();
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Utility.setHentaiMode(cfgActivity.this.getApplicationContext(), isChecked);
                    }
                },300);
            }
        });

        lbl_local_pf_cnt = findViewById(R.id.lbl_local_pf_cnt);
        try {
            JSONArray jo = new JSONArray(sp.getString("localpfcfg","[]"));
            lbl_local_pf_cnt.setText(jo.length()+"  >");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((View)lbl_local_pf_cnt.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adr = new Intent();
                adr.setClass(cfgActivity.this, LocalPfCfgActivity.class);
                adr.putExtra(LocalPfCfgActivity.SETTING_TYPE, LocalPfCfgActivity.SETTING_TYPE_IP);
                cfgActivity.this.startActivity(adr);
            }
        });

        lbl_local_dns_cnt = findViewById(R.id.lbl_local_dns_cnt);
        try {
            JSONArray jo = new JSONArray(sp.getString("localdnscfg","[]"));
            lbl_local_dns_cnt.setText(jo.length()+"  >");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ((View)lbl_local_dns_cnt.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent adr = new Intent();
                adr.setClass(cfgActivity.this, LocalPfCfgActivity.class);
                adr.putExtra(LocalPfCfgActivity.SETTING_TYPE, LocalPfCfgActivity.SETTING_TYPE_DNS);
                cfgActivity.this.startActivity(adr);
            }
        });
    }

}
