package com.mak.pfapp;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.*;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;

public class WebviewActivity extends AppCompatActivity {

    private WebView mainWbv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });

        mainWbv = findViewById(R.id.mainWbv);
        WebSettings settings = mainWbv.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAppCacheEnabled(true);
        mainWbv.addJavascriptInterface(new WebAppInterface(this), "android");
        mainWbv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {

            }
            @Override
            public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
                sslErrorHandler.proceed();
                super.onReceivedSslError(webView, sslErrorHandler, sslError);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                loadUrl(request.getUrl().toString());
                return true;
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String string) {
                loadUrl(string);
                return true;
            }
        });
        mainWbv.loadUrl(Api.ViewPageUrl);
    }
    private void loadUrl(String url) {
        if (url.startsWith("baidu")){
            return;
        }
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("x-auth", Api.ViewPageUrlAuth);
        //自定义请求头
        mainWbv.loadUrl(url, hashMap);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mainWbv.canGoBack()) {
            mainWbv.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }

}
