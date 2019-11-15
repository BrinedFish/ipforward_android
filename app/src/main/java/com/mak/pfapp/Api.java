package com.mak.pfapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.*;

public class Api {
    public static final String sdkVersion = "1";
    private static String ApiSvrUrl = "";
    private static String userId = "";
    public static int Point = 0;
    public static String ViewPageUrl = "http://www.baidu.com/";
    public static String ViewPageUrlAuth = "1234567890123";
    public static JSONArray PfData =  new JSONArray();

    private static ExecutorService executor = null;

    public static ExecutorService GetSingleExecutorService(){
        if (executor == null) {
            executor = Executors.newSingleThreadExecutor();
        }
        return executor;
    }

    public static String getApiSvrUrl(Context context) {
        if (TextUtils.isEmpty(ApiSvrUrl))
        {
            SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
            ApiSvrUrl = sp.getString("ApiSvrUrl","");
            if (TextUtils.isEmpty(ApiSvrUrl)) {
                ApiSvrUrl = "http://192.168.4.77:54824/api/x/";
            }
        }
        return ApiSvrUrl;
    }

    public static void setApiSvrUrl(Context context, String apiSvrUrl) {
        ApiSvrUrl = apiSvrUrl;
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        sp.edit().putString("ApiSvrUrl", apiSvrUrl).apply();
    }

    public static String getUserId(Context context) {
        if (TextUtils.isEmpty(userId)) {
            SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
            userId = sp.getString("userid", "");
            if (TextUtils.isEmpty(userId)) {
                userId = Utility.getDeviceId(context);
                if (TextUtils.isEmpty(userId)) {
                    userId = UUID.randomUUID().toString();
                }
                sp.edit().putString("userid", userId).apply();
            }
        }
        return userId;
    }

    public static ApiResult HttpReqs(String url, boolean isPost, String data){
        ApiResult resObj = new ApiResult();
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) (new URL(url)).openConnection(Proxy.NO_PROXY);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
             if (isPost) {
                 httpURLConnection.setRequestMethod("POST");
                 httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                 httpURLConnection.setRequestProperty("Accept", "*/*");
                 httpURLConnection.setDoOutput(true);
                 OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), "utf-8");
                 outputStreamWriter.write(Des.Encrypt(data));
                 outputStreamWriter.flush();
                 outputStreamWriter.close();
             }else{
                 httpURLConnection.setRequestMethod("GET");
                 httpURLConnection.connect();
             }
            if (httpURLConnection.getResponseCode() == 200) {
                String respData = Utility.iS2String(httpURLConnection.getInputStream());
                JSONObject jobj = new JSONObject(Des.Decrypt(respData));
                if (jobj.optBoolean("ok")){
                    resObj.data = jobj.getJSONObject("data");
                    resObj.ok = true;
                }else{
                    resObj.msg = jobj.optString("msg","服务器错误");
                }
            }else{
                resObj.msg = httpURLConnection.getResponseCode()+"";
            }
            httpURLConnection.disconnect();
        }catch (Exception ex){
            resObj.msg = ex.getMessage();
            if (TextUtils.isEmpty(resObj.msg)){
                resObj.msg = ex.toString();
            }
        }
        return resObj;
    }

    private static void ApiHttpReqsSync(final ApiCallBack runnable, final String url,final boolean isPost,final JSONObject jSONObject){
        GetSingleExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                ApiResult data = HttpReqs(url, isPost, jSONObject.toString());
                if(runnable!=null){
                    runnable.run(data);
                }
            }
        });
    }

    private static Future<ApiResult> ApiHttpReqs(final String url,final boolean isPost,final JSONObject jSONObject){
        return GetSingleExecutorService().submit(new Callable<ApiResult>() {
            @Override
            public ApiResult call() {
                return HttpReqs(url, isPost, jSONObject.toString());
            }
        });
    }

    public static void getForwardTable(Context context, ApiCallBack runnable) {
        final String actionUrl = getApiSvrUrl(context) + "forwardtable";
        final JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("userid", getUserId(context));
            jSONObject.put("version", sdkVersion);
        } catch (Exception ignored) {
        }
        ApiHttpReqsSync(runnable, actionUrl, true, jSONObject);
    }

    public static void getAuth(Context context, ApiCallBack runnable, String auth_key) {
        final String actionUrl = getApiSvrUrl(context) + "auth";
        final JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("userid", getUserId(context));
            jSONObject.put("version", sdkVersion);
            jSONObject.put("key", auth_key);
        } catch (Exception ignored) {
        }
        ApiHttpReqsSync(runnable, actionUrl, true, jSONObject);
    }

    public static void getSvrVersion(Context context, ApiCallBack runnable) {
        final String actionUrl = getApiSvrUrl(context) + "checkversion";
        final JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("userid", getUserId(context));
            jSONObject.put("version", sdkVersion);
        } catch (Exception ignored) {
        }
        ApiHttpReqsSync(runnable, actionUrl, true, jSONObject);
    }


}
