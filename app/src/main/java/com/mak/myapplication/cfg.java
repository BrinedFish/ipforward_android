package com.mak.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class cfg extends AppCompatActivity {

    EditText edit_api_addr;

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
        })
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("api_addr", edit_api_addr.getText().toString());
                ed.apply();
                Snackbar.make(view, "ok", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        edit_api_addr = findViewById(R.id.edit_api_addr);
    }

}
