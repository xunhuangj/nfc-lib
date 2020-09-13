package com.wugj.nfc.nfc;

public class NfcResponse<T> {

    T t;
    public NfcResponse(T t){
        this.t = t;
    }

    public T getResponse(){
        return t;
    };
}
