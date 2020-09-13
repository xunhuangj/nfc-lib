package com.wugj.nfc.nfc.ndef;

import com.wugj.nfc.nfc.INfc;
import com.wugj.nfc.nfc.NfcCall;

import java.io.IOException;

/**
 * ....，后面再添加,时间有限，后面会更新上去.期望大家多多支持. 点亮你们的小星星啊.
 */
public class NdefImp implements INfc<NdefResponse> {


    @Override
    public void write(NfcCall.Callback<NdefResponse> writeCallback) {

    }

    @Override
    public void read(NfcCall.Callback<NdefResponse> readCallback) {

    }

    @Override
    public void format(NfcCall.Callback<NdefResponse> formatCallback) {

    }

    @Override
    public void close() throws IOException {

    }
}
