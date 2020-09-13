package com.blacklake.nfc.datastore;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

@Dao
public interface MCDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MCTag ...tags);

    //if we use LiveData that we can not getData.
    @Query("SELECT * FROM MCTag WHERE tagId = :tagId")
    List<MCTag> query(String tagId);

    @Query("SELECT * FROM MCTag")
    List<MCTag> query();

    //if we use liveData we must sepcify entity.
    @RawQuery(observedEntities = MCTag.class)
    List<MCTag> query(SupportSQLiteQuery supportSQLiteQuery);


    @Delete
    void delete(MCTag...mcTags);

}
