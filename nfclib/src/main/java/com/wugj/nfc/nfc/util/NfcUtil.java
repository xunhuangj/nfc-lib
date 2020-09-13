package com.wugj.nfc.nfc.util;

import android.nfc.Tag;

public class NfcUtil {

    public static boolean isTagIdOk(Tag tag){

        if(tag == null) return false;

        if(tag.getId().length == 0) return false;

        return true;
    }

}
