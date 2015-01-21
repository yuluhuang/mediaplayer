package com.share.ylh.mediaplayer.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;


public class BaseApp extends Application {
    public  static Context AppContext;

    @Override
    public void onCreate() {
        super.onCreate();

        AppContext=getApplicationContext();

    }
}
