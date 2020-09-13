package com.wugj.nfc.nfc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class NfcDispather {


    private ExecutorService executorService;


    public ExecutorService getExecutorService() {
        return executorService;
    }


    public synchronized ExecutorService executorService(){
        if(executorService == null)
            executorService =
                    new ThreadPoolExecutor(1,1,
                            0l, TimeUnit.SECONDS,new LinkedBlockingDeque<Runnable>());

        return executorService;
    }



    //excute asyncTask
    public synchronized void equeue(Runnable runnable){

        try{
            executorService().execute(runnable);
        }catch (RejectedExecutionException e){
            if(executorService.isShutdown()){
                equeue(runnable);
            }
        }

    }



    public synchronized void close(){
        if(executorService != null && !executorService.isShutdown()){
            executorService.shutdownNow();
        }
    }




}
