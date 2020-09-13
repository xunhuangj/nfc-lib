package com.blacklake.nfc.datastore;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.wugj.nfc.nfc.util.Constant;

//m1卡的tag.
@Entity(tableName = "MCTag")
public class MCTag {



    public MCTag(){

    }

    @NonNull
    @PrimaryKey
    public String tagId;

    //我们保存keyA和KeyB
    @ColumnInfo(defaultValue = Constant.STAND_KEYS)
    public String keys;

    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    public long lastModify;


}

