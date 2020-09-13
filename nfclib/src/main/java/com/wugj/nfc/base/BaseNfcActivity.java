package com.wugj.nfc.base;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;

import androidx.appcompat.app.AppCompatActivity;


public class BaseNfcActivity extends AppCompatActivity {
    protected NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;

    @Override
    protected void onStart() {
        super.onStart();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                       , 0);
    }


    @Override
    public void onResume() {
        super.onResume();

        IntentFilter techIntentFilter = new IntentFilter();
        techIntentFilter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);

        IntentFilter ndefIntentFilter = new IntentFilter();
        ndefIntentFilter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);

        //new IntentFilter[]{techIntentFilter,ndefIntentFilter}
        // new String[][]{{NfcA.class.getName(),},{MifareClassic.class.getName()},{MifareUltralight.class.getName()}}

        //设置处理优于所有其他NFC的处理
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent,new IntentFilter[]{techIntentFilter,ndefIntentFilter},
                    new String[][]{{NfcA.class.getName(),},{MifareClassic.class.getName()},});
    }


    @Override
    public void onPause() {
        super.onPause();
        //恢复默认状态
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}

