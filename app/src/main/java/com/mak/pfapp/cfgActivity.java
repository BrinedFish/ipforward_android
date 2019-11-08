package com.mak.pfapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

public class cfgActivity extends AppCompatActivity {

    EditText edit_api_addr;
    Switch switch_ishentai;

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
    }

}
