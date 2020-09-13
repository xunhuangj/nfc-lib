package com.blacklake.nfc.datastore;

import android.content.Context;
import android.text.TextUtils;

import androidx.room.Room;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.wugj.nfc.nfc.util.MLog;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DBHelp {

    private final static String TAG = DBHelp.class.getSimpleName();

    private static ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private static ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
    private static ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();

    private static final int MCTAG_SIZE = 1000;

    private static TagDataBase tagDataBase;


    //通常在application中初始化一次.
    public static void initDataBase(Context context) {
        if (tagDataBase != null) return;

        synchronized (DBHelp.class) {
            if (tagDataBase == null)
                tagDataBase = Room.databaseBuilder(context, TagDataBase.class, "TagDataBase").build();
        }
    }


    private static void checkTagDataBase() {

        if (tagDataBase == null)
            throw new NullPointerException("you must call initDataBase first");
    }

    /**
     * insert  MCtag
     *
     * @param mcTag
     */
    public static void insertTag(MCTag mcTag) {
        checkTagDataBase();
        writeLock.lock();
        try {
            tagDataBase.getMCDao().insert(mcTag);
        } catch (Exception e) {
            MLog.e(TAG, e.getMessage());
        } finally {
            writeLock.unlock();
        }
    }


    public static MCTag getTargetTag(String tagId) {

        if (TextUtils.isEmpty(tagId)) return null;

        checkTagDataBase();
        List<MCTag> tags = null;
        readLock.lock();
        try {
            tags = tagDataBase.getMCDao().query(tagId);
        } catch (Exception e) {
            MLog.e(TAG, e.getMessage());
        } finally {
            readLock.unlock();
        }
        if (tags == null || tags.isEmpty()) return null;

        return tags.get(0);
    }


    private static Runnable cleanDataBase = new Runnable() {
        @Override
        public void run() {

            //大于1000条时清理下数据库，清理lastmodify 最小的数据
            String sql = "select * from MCTag order by lastModify AES";

            List<MCTag> tags;
            try {
                readLock.lock();
                try {
                    tags = tagDataBase.getMCDao().query(new SimpleSQLiteQuery(sql));
                } finally {
                    MLog.i(TAG, "cleanData base start");
                    readLock.unlock();
                }

                if (tags == null || tags.isEmpty() || tags.size() < MCTAG_SIZE)
                    return;

                //清理数据库时候，是不允许再做其它操作的.
                writeLock.lock();
                try {
                    tagDataBase.getMCDao().delete(tags.subList(0, tags.size() - MCTAG_SIZE)
                            .toArray(new MCTag[tags.size() - MCTAG_SIZE]));

                } finally {
                    writeLock.unlock();
                }
            } finally {
                isCleaning = true;
            }
        }
    };

    private static ExecutorService executorService = Executors.newSingleThreadExecutor();


    private static void submitTask() {
        try {
            executorService.submit(cleanDataBase);
        } catch (RejectedExecutionException e) {
            if (executorService.isShutdown()) {
                executorService = Executors.newSingleThreadExecutor();
                executorService.submit(cleanDataBase);
            }
        }

    }


    private static boolean isCleaning = false;

    public static void cleanDataBase() {

        checkTagDataBase();

        //无需重复提交cleanDataBase
        synchronized (DBHelp.class) {
            if (isCleaning) return;
            isCleaning = true;
        }

        submitTask();

    }


}
