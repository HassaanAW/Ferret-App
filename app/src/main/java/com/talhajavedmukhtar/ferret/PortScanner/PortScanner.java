package com.talhajavedmukhtar.ferret.PortScanner;

import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Util.Tags;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PortScanner extends AsyncTask {
    private String TAG = Tags.makeTag("PortScanner");

    private String ipAddress;

    private int timeout;

    private int minPort;
    private int maxPort;
    private ArrayList<Integer> portsList;

    private int tasksAdded;
    private int tasksDone;

    private ExecutorService executorService;

    private PortScannerInterface portScannerInterface;

    public PortScanner(String ip){
        ipAddress = ip;

        timeout = 10000; //ms

        //just scanning the first 1024 reserved ports
        minPort = 0;
        maxPort = 1025;

        portsList = new ArrayList<>();

        tasksAdded = 0;
        tasksDone = 0;
    }

    public void setPortScannerInterface(PortScannerInterface pInterface){
        portScannerInterface = pInterface;
    }

    public void setMinPort(int minPort){
        this.minPort = minPort;
    }

    public void setMaxPort(int maxPort){
        this.maxPort = maxPort;
    }

    private void portIsOpen(final String ip , final int port, final int timeout){
        Socket socket = new Socket();
        InetSocketAddress addr = new InetSocketAddress(ip, port);
        Boolean open = false;
        try {
            socket.connect(addr, timeout);
            socket.close();
            open = true;
        } catch (Exception ex) {
            //Log.d(TAG+".SocketError",ex.getMessage() + " for ip: " + ip + " and port: " + port );
        } finally {
            socket = null;
            addr = null;
            System.gc();

            if(open){
                portScannerInterface.onPortFound(port);
            }

            synchronized (this){

                tasksDone += 1;
                //Log.d(TAG,Integer.toString(tasksDone));
                //Log.d(TAG,"Tasks completed: "+tasksDone);

                if(tasksDone == (maxPort-minPort)){
                    //Log.d(TAG,"Tasks completed: all");
                    executorService.shutdown();
                    publishProgress("done");
                }else{
                    if(!portsList.isEmpty()){
                        final int nextPort = portsList.get(0);
                        portsList.remove(0);

                        if(portsList.size() % 10000 == 0){
                            int progressVal = 7 - (portsList.size()/10000);
                            publishProgress("running",progressVal);
                        }

                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                portIsOpen(ip,nextPort,timeout);
                            }
                        });
                        tasksAdded++;
                    }
                }
            }

        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        for(int i = minPort; i <= maxPort; i++){
            portsList.add(i);
        }

        int noOfThreads = 40;

        executorService = Executors.newFixedThreadPool(noOfThreads);

        synchronized (this){
            while(tasksAdded < noOfThreads){
                if(!portsList.isEmpty()){
                    final int port = portsList.remove(0);
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            portIsOpen(ipAddress,port,timeout);
                        }
                    });
                    tasksAdded++;
                }

                if(tasksDone == maxPort){
                    portScannerInterface.onCompletion();
                }
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);

        String status = (String)values[0];

        if(status.equals("done")){
            portScannerInterface.onCompletion();
        }else{
            int progressVal = (int)values[1];
            portScannerInterface.on10kDone(progressVal);
        }

    }
}
