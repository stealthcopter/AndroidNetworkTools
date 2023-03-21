package com.stealthcotper.networktools;

import android.app.Application;

public class ANTApplication extends Application {

    @Override
    public void onCreate() {
        initStrictMode();
        super.onCreate();
    }

    private void initStrictMode() {
        if (BuildConfig.DEBUG) {

            // Let's be super strict so that we can discover bugs during testing

//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectAll()
//                    .penaltyLog()
////                    .penaltyDeath()
//                    .build());
//
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectAll()
//                    .penaltyLog()
////                    .penaltyDeath()
//                    .build());
        }
    }


}
