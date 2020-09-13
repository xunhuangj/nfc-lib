package com.wugj.nfc.nfc;

import android.content.Intent;

/**
 * used for  read ,write, format
 * @param <T> return response

 */
public interface Nfcrw<T> {

    void read(Intent intent, NfcCall.Callback<T> callback);

    /**
     * @param intent
     * @param content
     * @param callback
     * @param <R>  some text that should write tag ;support String, String[],List<String>
     *               other format will throw exception
     */
    <R> void write(Intent intent,R content,NfcCall.Callback<T> callback);

    void format(Intent intent,NfcCall.Callback<T> callback);

}
