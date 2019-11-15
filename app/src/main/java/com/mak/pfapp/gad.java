package com.mak.pfapp;

import android.content.Context;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class gad {
    // used by google ad
    private static RewardedAd rewardedAd;

    public static RewardedAd createAndLoadRewardedAd(Context context) {
        RewardedAd rewardedAd = new RewardedAd(context, "ca-app-pub-5930562548810475/2706192227");
        //RewardedAd rewardedAd = new RewardedAd(context, "ca-app-pub-3940256099942544/5224354917");
        RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
            @Override
            public void onRewardedAdLoaded() {
                // Ad successfully loaded.
            }
            @Override
            public void onRewardedAdFailedToLoad(int errorCode) {
                // Ad failed to load.
            }
        };
        rewardedAd.loadAd(new AdRequest.Builder().addTestDevice("5AF77A538D4239F201B85DE44B2D1217").build(), adLoadCallback);
        return rewardedAd;
    }
    public static void reloadAd(Context context){
        rewardedAd = createAndLoadRewardedAd(context);
    }

    public static RewardedAd getAd() {
        return rewardedAd;
    }
}
