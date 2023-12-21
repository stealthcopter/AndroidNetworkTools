package com.stealthcotper.networktools

import android.app.Application

class ANTApplication : Application() {
    override fun onCreate() {
        initStrictMode()
        super.onCreate()
    }

    private fun initStrictMode() {
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