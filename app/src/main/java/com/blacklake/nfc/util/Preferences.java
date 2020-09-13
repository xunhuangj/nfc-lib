package com.blacklake.nfc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;

public class Preferences {



    public static String[] getCurKey(Context context) {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        String string = sp.getString("curKey","");

        if(string.equals("")) return new String[]{"FFFFFFFFFFFF", "A0A1A2A3A4A5","D3F7D3F7D3F7"};

        return string.split("\n");

    }

    /**
     * 当前秘钥.
     * @param context
     * @param strings
     */
    public static void putCurKey(Context context,String...strings) {
        putkey(context,"curKey",strings);

    }



    public static String[] getNewKey(Context context) {
        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        String string = sp.getString("newKey","");

        if(string.equals("")) return new String[]{"123456","654321"};

        return string.split("\n");

    }

    /**
     * 新秘钥
     * @param context
     * @param strings
     */
    public static void putNewKey(Context context,String...strings) {

        putkey(context,"newKey",strings);
    }

    private static void putkey(Context context,String key,String...strings) {
        if(strings == null || strings.length <= 0) return;

        SharedPreferences sp = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        StringBuilder stringBuilder = new StringBuilder();
        for(String item : strings){
            stringBuilder.append(item).append("\n");
        }
        if(strings.length == 1)
            stringBuilder.append(strings[0]);
        else
            stringBuilder.delete(stringBuilder.length() - 1,stringBuilder.length());

        SharedPreferences.Editor editor = sp.edit();

        editor.putString(key,stringBuilder.toString());

        editor.apply();

    }

}
