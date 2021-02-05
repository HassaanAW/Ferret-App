package com.talhajavedmukhtar.ferret.HostScanner;

import android.os.AsyncTask;

import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.Tags;
import com.talhajavedmukhtar.ferret.Util.Utils;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Talha on 11/18/18.
 */

public class TCPSYNDiscoverer extends AsyncTask {
    final String TAG = Tags.makeTag("TCPSYNDiscoverer");

    private String networkAddress;
    private int networkSize;    // CIDR /24, /23, etc
    private int timeout;
    private int noOfThreads;

    private DiscoveryInterface discoveryInterface;

    public TCPSYNDiscoverer(String networkAd, int cidr, int tO, int nThreads) {
        networkAddress = networkAd;
        networkSize = cidr;
        timeout = tO;
        noOfThreads = nThreads;
    }

    public void setDiscoveryInterface(DiscoveryInterface dInterface) {
        discoveryInterface = dInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ArrayList<String> addressRange = Utils.getAddressRange(networkAddress, networkSize);

        DiscoveryExecutor discoveryExecutor = new DiscoveryExecutor(addressRange, noOfThreads);
        discoveryExecutor.setExecutorInterface(new ExecutionInterface() {
            @Override
            public void onHostUp(String ipAd) {
                publishProgress(ipAd);
                //discoveryInterface.onHostDiscovered(ipAd, Constants.TCPSYN);
            }

            @Override
            public Boolean discover(String ipAd) {
                try {
                    //host is up if connection is successful
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(ipAd, 7), timeout);
                    socket.close();
                    return true;
                } catch (Exception ex) {
                    //host is up if the host refused connection; otherwise host is down
                    return ex.getMessage().contains("ECONNREFUSED");
                }
            }

            @Override
            public void onDone() {
                discoveryInterface.onDiscoveryCompletion();
            }
        });
        discoveryExecutor.executeOnExecutor(THREAD_POOL_EXECUTOR);

        /*
        while(discoveryExecutor.getStatus() != android.os.AsyncTask.Status.FINISHED){
            //wait until this is done
        }*/

        return null;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        String ipAd = (String) values[0];
        discoveryInterface.onHostDiscovered(ipAd, Constants.TCPSYN);
    }


    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        //discoveryInterface.onDiscoveryCompletion();
    }
}
