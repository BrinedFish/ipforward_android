package com.mak.pfapp.ad;

import android.app.Activity;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

public class u3d {
    private String unityGameID = "3369613";
    private boolean testMode = true;
    private String placementId= "rewardedVideo";

    private final UnityAdsListener myAdsListener = new UnityAdsListener ();

    private static u3d instance= null;
    private u3d(){}

    public static u3d get() {
        if (instance == null)
            instance = new u3d();
        return instance;
    }

    public void init(Activity activity) {
        // Initialize the SDK:
        UnityAds.initialize(activity, unityGameID, myAdsListener, testMode);
    }
    public boolean isReady(){
        return UnityAds.isReady (placementId);
    }
    public void show(Activity activity){
        if (UnityAds.isReady (placementId)) {
            UnityAds.show (activity, placementId);
        }
    }
    private class UnityAdsListener implements IUnityAdsListener {
        @Override
        public void onUnityAdsReady (String placementId) {
            // Implement functionality for an ad being ready to show.
        }

        @Override
        public void onUnityAdsStart (String placementId) {
            // Implement functionality for a user starting to watch an ad.
        }

        @Override
        public void onUnityAdsFinish (String placementId, UnityAds.FinishState finishState) {
            // Implement functionality for a user finishing an ad.
            if (finishState == UnityAds.FinishState.COMPLETED) {
                System.out.println("==============ok==================");


            } else if (finishState == UnityAds.FinishState.SKIPPED) {
                // Do not reward the user for skipping the ad.
            } else if (finishState == UnityAds.FinishState.ERROR) {
                // Log an error.
            }
        }

        @Override
        public void onUnityAdsError (UnityAds.UnityAdsError error, String message) {
            // Implement functionality for a Unity Ads service error occurring.
        }
    }
}
