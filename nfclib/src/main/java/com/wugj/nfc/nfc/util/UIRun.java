package com.wugj.nfc.nfc.util;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;


public class UIRun {

    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void runOnUI(Runnable runnable){
        handler.post(runnable);
    }

    public static void toastShort(final String message){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application,message,Toast.LENGTH_SHORT).show();
            }
        });

    }

    private static Application application;

    public static void init(Application application1){
        application = application1;
    }

    public static String getString(int strId){
        if(application == null) throw new NullPointerException("UIRun application is null,you must init");

        return application.getString(strId);
    }

    public static void toastShort(final int messageId){



        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application,messageId,Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static void toastLength(final String message){

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application,message,Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void toastLength(final int messageId){

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(application,messageId,Toast.LENGTH_LONG).show();
            }
        });

    }
}
