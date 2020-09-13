package com.blacklake.nfc.util;

import com.wugj.nfc.nfc.util.UIRun;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 这个类线程切换.
 */
public class Promise {


    public enum ThreadType {
        UIThread,
        IO,
    }

    private ThreadType threadType = ThreadType.UIThread;
    private WorkRunable workRunable;
    private static ExecutorService executorService ;


    private void initExcutorService(){

        synchronized (this){
            if(executorService == null || executorService.isShutdown())
                executorService = new ThreadPoolExecutor(0,
                    Integer.MAX_VALUE, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        }
    }


    public <T> Promise then(Work<T> work) {
        initExcutorService();
        workRunable = new WorkRunable(work);
        return this;
    }

    public <T> Promise complete(Complete<T> complete) {

        workRunable.setComplete(complete);
        return this;
    }

    public Promise schedule(ThreadType threadType) {
        this.threadType = threadType;
        return this;
    }


    public void start() {

        try{
            if(workRunable == null)
                throw new IllegalArgumentException("you must first call promise.then() method");

            executorService.submit(workRunable);
        }catch (RejectedExecutionException e){
            if(!executorService.isShutdown()) return;

            initExcutorService();
            start();
        }

    }


    public interface Work<T> {
        T work();
    }

    public interface Complete<T> {
        void complete(T t);
    }


    private final class WorkRunable<T> implements Runnable {

        Work<T> work;
        Complete<T> complete;

        WorkRunable(Work<T> runnable) {
            this.work = runnable;
        }


        void setComplete(Complete<T> complete) {
            this.complete = complete;
        }

        @Override
        public void run() {
            if (work != null) {
                final T t = work.work();
                if (threadType == ThreadType.UIThread) {
                    UIRun.runOnUI(new Runnable() {
                        @Override
                        public void run() {
                            finish(t);
                        }
                    });
                } else {
                    assert ThreadType.IO == threadType;
                    finish(t);
                }
            }
        }

        private void finish(T t) {
            if (this.complete != null) {
                this.complete.complete(t);
            }
        }
    }

}
