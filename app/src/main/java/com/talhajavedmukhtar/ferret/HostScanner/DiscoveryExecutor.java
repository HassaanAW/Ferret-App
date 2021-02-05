package com.talhajavedmukhtar.ferret.HostScanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Util.Tags;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Talha on 11/20/18.
 */

public class DiscoveryExecutor extends AsyncTask {
    private String TAG = Tags.makeTag("DiscoveryExecutor");

    private ArrayList<String> addressRange;
    private int noOfThreads;

    private ExecutionInterface executionInterface;

    private ExecutorService executorService;

    private final Object lock;

    private int totalTasks;
    private int tasksInitiated;
    private int tasksCompleted;

    DiscoveryExecutor(ArrayList<String> addRange, int nThreads) {
        addressRange = addRange;
        noOfThreads = nThreads;
        executionInterface = null;

        lock = new Object();
    }

    public void setExecutorInterface(ExecutionInterface eInterface) {
        executionInterface = eInterface;
    }

    /*
    private Runnable discoverWrapperFunc(final String ipAd){
        return new Runnable() {
            @Override
            public void run() {
                Boolean hostUp = executionInterface.discover(ipAd);

                if(hostUp){
                    executionInterface.onHostUp(ipAd);
                }

                synchronized (lock){
                    tasksCompleted++;

                    Log.d(TAG,"Total: "+Integer.toString(totalTasks)+" , Completed: "+Integer.toString(tasksCompleted)+" , ip: "+ipAd);

                    if(tasksCompleted == totalTasks){
                        executorService.shutdown();
                        executionInterface.onExecutionCompletion();
                    }else{
                        if(!addressRange.isEmpty()){
                            String nextIp = addressRange.remove(0);
                            executorService.execute(discoverWrapperFunc(nextIp));
                            tasksInitiated++;
                            //Log.d(TAG,"Tasks Initiated: "+tasksInitiated);
                        }
                    }
                }
            }
        };
    }*/

    /*public void execute(){
        executorService = Executors.newFixedThreadPool(noOfThreads);

        totalTasks = addressRange.size();
        tasksInitiated = 0;
        tasksCompleted = 0;


        //keep adding new jobs until <atleast one thread is left free> and <not all addresses are used up>
        while ((tasksInitiated != (noOfThreads-1)) && !addressRange.isEmpty()){
            synchronized (lock){
                if(!addressRange.isEmpty()){
                    String ip = addressRange.remove(0);
                    executorService.execute(discoverWrapperFunc(ip));
                    tasksInitiated++;
                    //Log.d(TAG,"Tasks Initiated: "+tasksInitiated);
                }
            }
        }


        //wait until all tasks are executed

        while(!executorService.isShutdown()){

        }
    }*/

    @Override
    protected Object doInBackground(Object[] objects) {
        executorService = Executors.newFixedThreadPool(noOfThreads);

        totalTasks = addressRange.size();
        tasksInitiated = 0;
        tasksCompleted = 0;

        for (final String ipAd : addressRange) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    Boolean hostUp = executionInterface.discover(ipAd);

                    if (hostUp) {
                        publishProgress(ipAd);
                        //executionInterface.onHostUp(ipAd);
                    }
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(100, TimeUnit.SECONDS);
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        //keep adding new jobs until <atleast one thread is left free> and <not all addresses are used up>
        /*
        while ((tasksInitiated != (noOfThreads-1)) && !addressRange.isEmpty()){
            synchronized (lock){
                if(!addressRange.isEmpty()){
                    String ip = addressRange.remove(0);
                    executorService.execute(discoverWrapperFunc(ip));
                    tasksInitiated++;
                    //Log.d(TAG,"Tasks Initiated: "+tasksInitiated);
                }
            }
        }*/

        return null;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        String ipAd = (String) values[0];
        executionInterface.onHostUp(ipAd);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        executionInterface.onDone();
    }
}
