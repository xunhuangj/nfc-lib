package com.wugj.nfc.nfc.util;

import android.util.Log;

import com.wugj.nfc.BuildConfig;


public class MLog {

    private static boolean isPrintLog = BuildConfig.DEBUG ? true : false;


    public static void w(String tag,String messge){
        if(isPrintLog)
            Log.w(tag,messge);
    };

    public static void i(String tag,String messge){
        if(isPrintLog)
            Log.i(tag,messge);
    };

    public static void d(String tag,String messge){
        if(isPrintLog)
            Log.d(tag,messge);
    };

    public static void e(String tag,String messge){
        if(isPrintLog)
            Log.e(tag,messge);
    };

}
