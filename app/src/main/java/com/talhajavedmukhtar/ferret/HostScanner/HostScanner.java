package com.talhajavedmukhtar.ferret.HostScanner;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Model.Host;
import com.talhajavedmukhtar.ferret.MyApp;
import com.talhajavedmukhtar.ferret.Util.Tags;
import com.talhajavedmukhtar.ferret.Util.Utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Talha on 11/20/18.
 */

public class HostScanner extends AsyncTask {
    private String TAG = Tags.makeTag("HostScanner");

    private String networkAddress;
    private int networkSize;
    private int noOfThreads;
    private int timeout;

    private int tasksCompleted;

    private ScannerInterface scannerInterface;

    private DiscoveryInterface discoveryInterface;

    private HashMap<String,Host> hostDetails;

    private MyApp myApp;
    private Context context;

    public HostScanner(Context ctx, String networdAdd, int netSize, int nThreads, int tO){
        networkAddress = networdAdd;
        networkSize = netSize;
        noOfThreads = nThreads;
        timeout = tO;

        tasksCompleted = 0;

        hostDetails = new HashMap<>();

        context = ctx;

        myApp = (MyApp) ctx.getApplicationContext();

        discoveryInterface = new DiscoveryInterface() {
            @Override
            public void onHostDiscovered(String ipAd, String protocol) {
                publishProgress(ipAd,protocol);
            }

            @Override
            public void onDiscoveryCompletion() {
                synchronized (context){
                    tasksCompleted += 1;
                    if (tasksCompleted == 3){
                        scannerInterface.onCompletion();
                    }
                }
            }
        };
    }

    public void setScannerInterface(ScannerInterface sInterface){
        scannerInterface = sInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ArrayList<AsyncTask> tasks = new ArrayList<>();

        TCPSYNDiscoverer tcpsynDiscoverer = new TCPSYNDiscoverer(networkAddress,networkSize,timeout,noOfThreads);
        tcpsynDiscoverer.setDiscoveryInterface(discoveryInterface);

        ICMPPingDiscoverer icmpPingDiscoverer = new ICMPPingDiscoverer(networkAddress,networkSize,timeout,noOfThreads);
        icmpPingDiscoverer.setDiscoveryInterface(discoveryInterface);

        MDNSDiscoverer mdnsDiscoverer = new MDNSDiscoverer(context,timeout);
        mdnsDiscoverer.setDiscoveryInterface(discoveryInterface);

        tasks.add(tcpsynDiscoverer);
        tasks.add(icmpPingDiscoverer);
        tasks.add(mdnsDiscoverer);

        //start parallel execution
        for(AsyncTask aTask: tasks){
            aTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //Log.d(TAG,"Tasks Set");

        //wait for each task to execute

        /*
        for(AsyncTask aTask: tasks){
            while(aTask.getStatus() != android.os.AsyncTask.Status.FINISHED){
                //wait until this is done
            }
        }*/

        //Log.d(TAG,"Tasks Completed");

        return null;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        String ipAd = (String)values[0];
        String protocol = (String)values[1];

        if(!hostDetails.containsKey(ipAd)){
            String macAdd = Utils.getMACAddress(ipAd);
            String vendor;
            if(macAdd != null){
                vendor = myApp.getMap().findVendor(macAdd);
                MyApp.addDeviceDetails(ipAd,macAdd,vendor);
            }else{
                vendor = "Unknown Vendor";
            }

            //Log.d(TAG,"IP: "+ipAd+" Name:"+name);

            String name = "...";


            Host newHost = new Host();
            newHost.setIpAddress(ipAd);
            newHost.addDiscoveredThrough(protocol);
            newHost.setVendor(vendor);
            newHost.setDeviceName(name);

            if(macAdd != null){
                newHost.setMAhash(Utils.md5(macAdd));
            }

            hostDetails.put(ipAd,newHost);

            scannerInterface.onDeviceFound(newHost);
        }else{
            hostDetails.get(ipAd).addDiscoveredThrough(protocol);
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        //scannerInterface.onCompletion();
    }
}
