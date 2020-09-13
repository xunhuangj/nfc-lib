package com.wugj.nfc.base;

import android.app.Application;

import com.wugj.nfc.nfc.util.UIRun;

public class BaseNfcApplication extends Application {




    private static BaseNfcApplication application ;


    public static BaseNfcApplication getApplication() {
        return application;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        UIRun.init(application);
    }
}
