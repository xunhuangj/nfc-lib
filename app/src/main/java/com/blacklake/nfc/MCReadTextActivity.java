package com.blacklake.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.wugj.nfc.base.BaseNfcActivity;
import com.wugj.nfc.nfc.NfcCall;
import com.wugj.nfc.nfc.NfcRequest;
import com.wugj.nfc.nfc.NfcResponse;
import com.wugj.nfc.nfc.mifare.MCResponse;
import com.wugj.nfc.nfc.mifare.MCSimpleRW;

import java.util.ArrayList;
import java.util.List;


public class MCReadTextActivity extends BaseNfcActivity {
    private TextView mNfcText;

    private EditText et_keyA;
    private EditText et_keyB;

    MCSimpleRW mcSimpleRW;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_text);
        mNfcText = findViewById(R.id.tv_nfctext);

        et_keyA = findViewById(R.id.et_key1);
        et_keyB = findViewById(R.id.et_key2);

        mcSimpleRW = new MCSimpleRW(this);

        read(getIntent());

    }





    @Override
    public void onNewIntent(final Intent intent) {

        super.onNewIntent(intent);


        read(intent);

    }

    private void read(final Intent intent){

        MCWriteTextActivity.readCurKeys(intent, this, et_keyA, et_keyB, new MCWriteTextActivity.ReadDataBaseCall() {

            //isReadOk 表示是否从数据库中读到了数据.
            @Override
            public void read(boolean isReadOk) {
                readText(intent);
            }
        });
    }

    public void readText(Intent intent){
        String [] curKeys = getKeys();
        if(curKeys != null && curKeys.length > 0)
            mcSimpleRW.setCurKeys(curKeys);

        mcSimpleRW.nfcrw.read(intent, new NfcCall.Callback<MCResponse>() {
            @Override
            public void failedCall(NfcRequest request, Exception ex) {
//                    UIRun.toastLength(ex.getMessage());
                mNfcText.setText(ex.getMessage());
            }

            @Override
            public void successCall(NfcRequest request, NfcResponse<MCResponse> nfcResponse) {
                MCResponse mcResponse = nfcResponse.getResponse();
                mNfcText.setText(mcResponse.toString() + "\n" + mcResponse.getHexString());
            }
        });
    }






    private String [] getKeys(){

        List<String> keyArray = new ArrayList<>();
        if(!TextUtils.isEmpty(et_keyA.getText().toString())){
            keyArray.add(et_keyA.getText().toString());
        }

        if(!TextUtils.isEmpty(et_keyB.getText().toString())){
            keyArray.add(et_keyB.getText().toString());
        }

        return keyArray.toArray(new String[keyArray.size()]);
    }




    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
        this.finish();
    }

}
