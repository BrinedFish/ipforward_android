package com.mak.pfapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocalPfCfgActivity extends AppCompatActivity {
    public final static String SETTING_TYPE = "SETTING_TYPE";
    public final static int SETTING_TYPE_DNS = 1;
    public final static int SETTING_TYPE_IP = 2;

    SharedPreferences sp;
    JSONArray cfg_data;
    ListView listView;
    String CfgtableaName = "localpfcfg";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_pf_cfg);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });
        switch (getIntent().getIntExtra(SETTING_TYPE, SETTING_TYPE_IP))
        {
            case SETTING_TYPE_DNS:
                setTitle("本地DNS设置");
                CfgtableaName = "localdnscfg";
                break;
            case SETTING_TYPE_IP:
            default:
                setTitle("本地IP设置");
                CfgtableaName = "localpfcfg";
                break;
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditor(-1);
            }
        });
        sp = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        try {
            cfg_data = new JSONArray(sp.getString(CfgtableaName,"[]"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listView = findViewById(R.id.lv_lpc);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showEditor(position);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                try {
                    final JSONObject aRow = (JSONObject)cfg_data.get(position);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LocalPfCfgActivity.this);
                    builder.setMessage("确认删除？\r\n"+aRow.optString("data", ""))
                            .setTitle("提示")
                            .setPositiveButton(getText(R.string.Ok), new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dlg, int paramInt) {
                                    //Toast.makeText(LocalPfCfgActivity.this,aRow.optString("data", ""), Toast.LENGTH_SHORT).show();
                                    cfg_data.remove(position);
                                    sp.edit().putString(CfgtableaName, cfg_data.toString()).apply();
                                    refreshLvData();
                                    dlg.dismiss();
                                }
                            })
                            .setNegativeButton(getText(R.string.Cancel), new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dlg, int paramInt) {
                                    dlg.dismiss();
                                }
                            })
                            .show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        refreshLvData();
    }
    private void showEditor(final int objIdx){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.pf_cfg_item_edit, null);
        builder.setView(dialogView) ;
        final AlertDialog authDialog = builder.create();
//        authDialog.setCanceledOnTouchOutside(false);
//        authDialog.setCancelable(false);
        authDialog.show();
        final EditText edt_remark = authDialog.findViewById(R.id.edt_remark);
        final EditText edt_data = authDialog.findViewById(R.id.edt_data);
        if (objIdx > -1){
            try {
                JSONObject obj = (JSONObject)cfg_data.get(objIdx);
                edt_remark.setText(obj.optString("remark"));
                edt_data.setText(obj.optString("data"));
            } catch (JSONException e) { }
        }
        authDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (objIdx > -1) {
                        JSONObject obj = (JSONObject) cfg_data.get(objIdx);
                        obj.put("remark", edt_remark.getText());
                        obj.put("data", edt_data.getText());

                    } else {
                        JSONObject obj = new JSONObject();
                        obj.put("remark", edt_remark.getText());
                        obj.put("data", edt_data.getText());
                        cfg_data.put(obj);
                    }
                    sp.edit().putString(CfgtableaName, cfg_data.toString()).apply();
                } catch (JSONException e) {
                }
                refreshLvData();
                authDialog.dismiss();
            }
        });
    }
    private void refreshLvData(){
        final List<JSONObject> oo = new ArrayList<>();
        for (int i = 0; i < cfg_data.length(); i++) {
            try {
                oo.add((JSONObject)cfg_data.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cfgLvAdapter adapter = new cfgLvAdapter(this,R.layout.pf_cfg_item, oo);
        listView.setAdapter(adapter);
    }

    class cfgLvAdapter extends ArrayAdapter<JSONObject> {
        private int resourceId;

        public cfgLvAdapter(Context context, int textViewResourceId, List objects) {
            super(context, textViewResourceId, objects);
            resourceId = textViewResourceId;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            JSONObject aRow = getItem(position);
            View view;
            ViewHolder viewHolder;
            if (convertView == null) {
                view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.lbl = view.findViewById(R.id.lbl_cfg_item);
                viewHolder.remark = view.findViewById(R.id.lbl_cfg_item_remark);
                view.setTag(viewHolder);
            } else {
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.lbl.setText(aRow.optString("data", ""));
            viewHolder.remark.setText(aRow.optString("remark", ""));
            return view;
        }

        class ViewHolder {
            TextView remark;
            TextView lbl;
        }
    }
}
