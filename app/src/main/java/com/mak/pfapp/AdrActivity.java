package com.mak.pfapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

public class AdrActivity extends AppCompatActivity {
    TextView label_point;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adr);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                finish();
            }
        });
        findViewById(R.id.btn_getpoint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gad.getAd().isLoaded()) {
                    RewardedAdCallback adCallback = new RewardedAdCallback() {
                        @Override
                        public void onRewardedAdOpened() {
                            // Ad opened.
                            Toast.makeText(getApplicationContext(), "Ad opened.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            // Ad closed.
                            Toast.makeText(getApplicationContext(), " Ad closed.", Toast.LENGTH_SHORT).show();
                            gad.reloadAd(AdrActivity.this);
                        }

                        @Override
                        public void onUserEarnedReward(RewardItem reward) {
                            // User earned reward.
                            Toast.makeText(getApplicationContext(), "User earned reward.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRewardedAdFailedToShow(int errorCode) {
                            // Ad failed to display
                            Toast.makeText(getApplicationContext(), "Ad failed to display", Toast.LENGTH_SHORT).show();
                        }
                    };
                    gad.getAd().show(AdrActivity.this, adCallback);
                } else {
                    Toast.makeText(getApplicationContext(), " Ad not Loaded", Toast.LENGTH_SHORT).show();
                }
            }
        });
        label_point = findViewById(R.id.lbl_point);
        label_point.setText(""+Api.Point);
        label_point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), " Point :" +label_point.getText(), Toast.LENGTH_SHORT).show();
            }
        });

    }

}
