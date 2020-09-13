package com.wugj.nfc.nfc;

import android.nfc.NfcAdapter;

public class NfcClient {


    private NfcAdapter nfcAdapter;
    private NfcDispather dispatcher;



    public NfcClient(Builder builder){
        this.nfcAdapter = builder.nfcAdapter;
        this.dispatcher = builder.nfcDispather;
    }

    public NfcAdapter getNfcAdapter() {
        return nfcAdapter;
    }


    public NfcCall newCall(NfcRequest mcRequest){

        if(mcRequest == null) throw new  NullPointerException("NfcRequest is null");

        if(mcRequest.getTag() == null) throw  new NullPointerException("tag is null");

        if(mcRequest.getOperation() == null) throw new  NullPointerException("operation is null");


        return NfcRealCall.newRealCall(mcRequest,this);
    }

    public NfcDispather getDispatcher() {
        return dispatcher;
    }


    public static class Builder{

        NfcAdapter nfcAdapter;
        NfcDispather nfcDispather;

        //visible
        public Builder(){
            nfcDispather = new NfcDispather();

        }

        public Builder(NfcClient mcClient){
            this.nfcAdapter = mcClient.nfcAdapter;
            this.nfcDispather = mcClient.dispatcher;
        }

        public Builder setNfcAdapter(NfcAdapter nfcAdapter) {
            this.nfcAdapter = nfcAdapter;
            return this;
        }

        public Builder setNfcDispather(NfcDispather nfcDispather) {
            this.nfcDispather = nfcDispather;
            return this;
        }

        public NfcClient build(){
            return new NfcClient(this);
        }

    }

}
