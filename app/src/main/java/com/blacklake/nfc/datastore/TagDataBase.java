package com.blacklake.nfc.datastore;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(version = 1,entities = {MCTag.class})
abstract class TagDataBase  extends RoomDatabase {


    public abstract MCDao getMCDao();



}
