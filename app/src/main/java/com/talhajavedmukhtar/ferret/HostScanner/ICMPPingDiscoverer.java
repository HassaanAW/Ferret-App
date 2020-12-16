package com.talhajavedmukhtar.ferret.HostScanner;

import android.os.AsyncTask;

import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.Utils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Talha on 11/20/18.
 */

public class ICMPPingDiscoverer extends AsyncTask{

    private String networkAddress;
    private int networkSize;    // CIDR /24, /23, etc
    private int timeout;
    private int noOfThreads;

    private DiscoveryInterface discoveryInterface;

    public ICMPPingDiscoverer(String networkAd, int cidr, int tO, int nThreads){
        networkAddress = networkAd;
        networkSize = cidr;
        timeout = tO;
        noOfThreads = nThreads;
    }

    public void setDiscoveryInterface(DiscoveryInterface dInterface){
        discoveryInterface = dInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ArrayList<String> addressRange = Utils.getAddressRange(networkAddress,networkSize);

        final DiscoveryExecutor discoveryExecutor = new DiscoveryExecutor(addressRange,noOfThreads);
        discoveryExecutor.setExecutorInterface(new ExecutionInterface() {
            @Override
            public void onHostUp(String ipAd) {
                publishProgress(ipAd);
                //discoveryInterface.onHostDiscovered(ipAd, Constants.ICMPPing);
            }

            @Override
            public Boolean discover(String ipAd) {
                Runtime runtime = Runtime.getRuntime();

                try {
                    Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 -W " + (timeout/1000) + " " +ipAd);
                    int mExitValue = mIpAddrProcess.waitFor();

                    return mExitValue == 0;
                } catch (Exception ex) {
                    return false;
                }
            }

            @Override
            public void onDone() {
                discoveryInterface.onDiscoveryCompletion();
            }
        });
        discoveryExecutor.executeOnExecutor(THREAD_POOL_EXECUTOR);

        while(discoveryExecutor.getStatus() != android.os.AsyncTask.Status.FINISHED){
            //wait until this is done
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        String ipAd = (String)values[0];
        discoveryInterface.onHostDiscovered(ipAd, Constants.ICMPPing);
    }


    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        //discoveryInterface.onDiscoveryCompletion();
    }
}
