package com.blacklake.nfc;

import com.blacklake.nfc.datastore.DBHelp;
import com.wugj.nfc.base.BaseNfcApplication;

public class MyApplication extends BaseNfcApplication {




    @Override
    public void onCreate() {
        super.onCreate();

        DBHelp.initDataBase(this);
    }
}
