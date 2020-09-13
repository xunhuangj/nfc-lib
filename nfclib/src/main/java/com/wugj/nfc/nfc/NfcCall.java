package com.wugj.nfc.nfc;

public interface NfcCall<T> {

     void enqueue(Callback<T> callback);


     interface Callback<T>{

        void failedCall(NfcRequest request, Exception ex);

        void successCall(NfcRequest request , NfcResponse<T> nfcResponse);
    }
}
