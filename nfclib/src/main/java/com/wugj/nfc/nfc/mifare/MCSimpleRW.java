package com.wugj.nfc.nfc.mifare;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.text.TextUtils;

import com.wugj.nfc.nfc.NfcCall;
import com.wugj.nfc.nfc.NfcClient;
import com.wugj.nfc.nfc.Nfcrw;

import java.util.Iterator;
import java.util.List;

final public class MCSimpleRW {

    Context context;
    NfcClient nfcClient;
    NfcAdapter nfcAdapter;
    String curKeys[];
    String newKeys[];

    public MCSimpleRW(Context context, String curKeys[], String newKeys[]) {
        this.context = context;
        this.curKeys = curKeys;
        this.newKeys = newKeys;
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(context);

        nfcClient = new NfcClient.Builder()
                .setNfcAdapter(nfcAdapter)
                .build();

    }

    public MCSimpleRW(Context context){
        this(context,null,null);
    }


    public void setCurKeys(String[] curKeys) {
        this.curKeys = curKeys;
    }

    public void setNewKeys(String[] newKeys) {
        this.newKeys = newKeys;
    }

    public final Nfcrw<MCResponse> nfcrw = new Nfcrw<MCResponse>() {
        @Override
        public void read(Intent intent, NfcCall.Callback<MCResponse> callback) {
            operate(intent,null, MCCommon.Operations.Read, callback);
        }

        @Override
        public <R> void  write(Intent intent, R content, NfcCall.Callback<MCResponse> callback) {
            operate(intent,content, MCCommon.Operations.Write, callback);
        }

        @Override
        public void format(Intent intent, NfcCall.Callback<MCResponse> callback) {
            operate(intent,null, MCCommon.Operations.Format, callback);
        }
    };

    /**
     * read,write ,format
     * @param intent
     * @param content
     * @param operation
     * @param callback
     */
    private <R> void operate(Intent intent, R content, MCCommon.Operations operation, NfcCall.Callback callback) {

        try {
            checkNfc(intent);

            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
                Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                checkTagSupport(detectedTag);
                MCRequest.Builder mcRequestBuilder = new MCRequest.Builder()
                        .setOperation(operation)
                        .setKeys(curKeys)
                        .setFrom(1)
                        .setTag(detectedTag);

                if (operation == MCCommon.Operations.Write) {
                    mcRequestBuilder.setWriteText(checkRType(content));
                } else if (operation == MCCommon.Operations.Format) {
                    mcRequestBuilder.setFrom(0);
                    mcRequestBuilder.setSaveKeys(newKeys);
                }

                NfcCall<MCResponse> nfcCall = nfcClient.newCall(mcRequestBuilder.build());
                nfcCall.enqueue(callback);
            }
        } catch (Exception e) {
            callback.failedCall(null, e);
        }

    }

    private <R> String checkRType(R r){

        if(r == null) throw new NullPointerException("r == null 写入内容不能为空");

        if(r instanceof String)
            return (String)r;

        else if(r instanceof List){

            List list = (List)r;
            if(list.isEmpty())
                throw new NullPointerException("list is empty,写入内容不能为空");
            Iterator iterator = list.iterator();
            StringBuilder stringBuilder = new StringBuilder();
            while (iterator.hasNext()){
                try{
                    String  string = (String) iterator.next();
                    stringBuilder.append(string).append("\n");

                }catch (ClassCastException e){
                    throw e;
                }
            }
           ;
            return  stringBuilder.delete(stringBuilder.length() - 1,stringBuilder.length()).toString();
        }else if(r instanceof String[]){
            StringBuilder stringBuilder = new StringBuilder();
            for(String item:(String[])r){
                if(TextUtils.isEmpty(item))
                    throw new NullPointerException("写入内容中存在空串");
                stringBuilder.append(item).append("\n");
            }
            stringBuilder.delete(stringBuilder.length() - 1,stringBuilder.length());
           return stringBuilder.toString();
        }
        else{
            throw new IllegalArgumentException("请写入合适的参数");
        }
    }



    private void checkNfc(Intent intent) {
        if (nfcAdapter == null) {
            throw new NullPointerException("该设备不支持Nfc");
        }
        if (intent == null)
            throw new NullPointerException("intent is null");

        if (intent.getAction() == null) {
            throw new NullPointerException("intent action is  null");
        }
    }


    private void checkTagSupport(Tag tag) {
        int status = MCCommon.checkMifareClassicSupport(tag, context);

        if (status != 0)
            throw new IllegalStateException("当前标签不是MifareClassic标签");
    }


}
