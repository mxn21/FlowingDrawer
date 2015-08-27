package com.mxn.soul.fluiddrawer;

import android.app.Application;

import timber.log.Timber;


public class DrawerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}
