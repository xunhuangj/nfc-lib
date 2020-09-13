package com.blacklake.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.blacklake.nfc.datastore.DBHelp;
import com.blacklake.nfc.datastore.MCTag;
import com.blacklake.nfc.util.Promise;
import com.wugj.nfc.base.BaseNfcActivity;
import com.wugj.nfc.nfc.NfcCall;
import com.wugj.nfc.nfc.NfcRequest;
import com.wugj.nfc.nfc.NfcResponse;
import com.wugj.nfc.nfc.mifare.MCCommon;
import com.wugj.nfc.nfc.mifare.MCRequest;
import com.wugj.nfc.nfc.mifare.MCResponse;
import com.wugj.nfc.nfc.mifare.MCSimpleRW;
import com.wugj.nfc.nfc.util.NfcUtil;
import com.wugj.nfc.nfc.util.UIRun;
import com.wugj.nfc.nfc.util.Utils;

import java.util.ArrayList;
import java.util.List;


public class MCWriteTextActivity extends BaseNfcActivity {
    private String mText = null;

    private EditText editText;
    private EditText et_keyA;
    private EditText et_keyB;
    private EditText et_keyNewA;
    private EditText et_keyNewB;
    MCSimpleRW mcSimpleRW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_format);
        editText = findViewById(R.id.et);

        et_keyA = findViewById(R.id.et_key1);

        et_keyB = findViewById(R.id.et_key2);

        et_keyNewA = findViewById(R.id.et_key3);
        et_keyNewB = findViewById(R.id.et_key4);


        mcSimpleRW = new MCSimpleRW(this,null,null);

    }

    Intent intent = null;
    @Override
    public void onNewIntent(Intent intent) {


        if(!TextUtils.isEmpty(editText.getText().toString()))
            mText = editText.getText().toString();

        this.intent = intent;
        super.onNewIntent(intent);

        readCurKeys(intent, this, et_keyA, et_keyB, null);
    }



    public static void readCurKeys(Intent intent, Context context, final EditText et_keyA, final EditText et_keyB, final ReadDataBaseCall readDataBaseCall){

        if(intent == null || TextUtils.isEmpty(intent.getAction())) return;

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            final Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (MCCommon.isSupportMifareClassic(detectedTag,context)) {

                if(NfcUtil.isTagIdOk(detectedTag))
                new Promise().then(new Promise.Work<MCTag>() {
                    @Override
                    public MCTag work() {
                        MCTag mcTag = DBHelp.getTargetTag(Utils.byte2HexString(detectedTag.getId()));
                        return mcTag;
                    }
                }).complete(new Promise.Complete<MCTag>() {
                    @Override
                    public void complete(MCTag mcTag) {
                        if(mcTag != null){
                            String keys[] = mcTag.keys.split("\n");
                            if(keys != null && keys.length >= 2) {
                                et_keyA.setText(keys[0]);
                                et_keyB.setText(keys[1]);
                            }
                            if(readDataBaseCall != null)
                                readDataBaseCall.read(true);
                        }else{
                            if(readDataBaseCall != null)
                                readDataBaseCall.read(false);
                        }
                    }
                }).start();
                    else{
                        if(readDataBaseCall != null)
                            readDataBaseCall.read(false);
                }
            }
        }

    }


    public interface ReadDataBaseCall{
        void read(boolean isReadOk);
    }




    public void write(View view){
        String [] curKeys = getCurKeys();
        if(curKeys != null && curKeys.length > 0)
            mcSimpleRW.setCurKeys(curKeys);

        mcSimpleRW.nfcrw.write(intent, mText, new NfcCall.Callback<MCResponse>() {
            @Override
            public void failedCall(NfcRequest request, Exception ex) {
                UIRun.toastLength(ex.getMessage());
            }

            @Override
            public void successCall(NfcRequest request, NfcResponse<MCResponse> nfcResponse) {
                MCResponse response = nfcResponse.getResponse();
                UIRun.toastLength("写入成功");
            }
        });

    }

    public void format(View view){

        final String [] newKeys = getNewKeys();
        if(newKeys != null && newKeys.length > 0)
            mcSimpleRW.setNewKeys(newKeys);

        String [] curKeys = getCurKeys();
        if(curKeys != null && curKeys.length > 0)
            mcSimpleRW.setCurKeys(curKeys);

        mcSimpleRW.nfcrw.format(intent,new NfcCall.Callback<MCResponse>() {
            @Override
            public void failedCall(NfcRequest request, Exception ex) {
                UIRun.toastLength(ex.getMessage());
            }

            @Override
            public void successCall(NfcRequest request, NfcResponse<MCResponse> nfcResponse) {
                MCResponse mcResponse = nfcResponse.getResponse();
                UIRun.toastLength(mcResponse.toString());

                final MCRequest mcRequest = ((MCRequest)request);
                String keys [] = ((MCRequest)request).getSaveKeys();
                if(keys != null || keys.length == 2) {
                    et_keyA.setText(keys[0]);
                    et_keyB.setText(keys[1]);
                }

                if(!NfcUtil.isTagIdOk(request.getTag())){
                    return;
                }
                final String tagId = Utils.byte2HexString(request.getTag().getId());

                new Promise().then(new Promise.Work<Void>() {
                    @Override
                    public Void work() {
                        insertTag(tagId,mcRequest.getSaveKeys());
                        return null;
                    }
                }).start();
            }
        });

    }


    private void insertTag(String tagId, String...keys){

        if(keys == null && keys.length < 2 ) return;

        MCTag tag = new MCTag();
        tag.tagId = tagId;

        StringBuilder stringBuilder = new StringBuilder();
        for(String item : keys){
            stringBuilder.append(item).append("\n");
        }
        if(keys.length == 1)
            stringBuilder.append(keys[0]);
        else
            stringBuilder.delete(stringBuilder.length() - 1,stringBuilder.length());

        tag.keys = stringBuilder.toString();


        DBHelp.insertTag(tag);
    }





    private String [] getCurKeys(){

        List<String> keyArray = new ArrayList<>();
        if(!TextUtils.isEmpty(et_keyA.getText().toString())){
            keyArray.add(et_keyA.getText().toString());
        }

        if(!TextUtils.isEmpty(et_keyB.getText().toString())){
            keyArray.add(et_keyB.getText().toString());
        }
        return keyArray.toArray(new String[keyArray.size()]);
    }

    private String [] getNewKeys(){

        List<String> keyArray = new ArrayList<>();
        if(!TextUtils.isEmpty(et_keyNewA.getText().toString())){
            keyArray.add(et_keyNewA.getText().toString());
        }
        if(!TextUtils.isEmpty(et_keyNewB.getText().toString())){
            keyArray.add(et_keyNewB.getText().toString());
        }

        return keyArray.toArray(new String[keyArray.size()]);
    };



}
