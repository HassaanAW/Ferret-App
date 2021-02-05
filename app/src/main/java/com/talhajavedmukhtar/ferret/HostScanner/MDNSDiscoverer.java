package com.talhajavedmukhtar.ferret.HostScanner;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.Tags;

import java.util.ArrayList;

/**
 * Created by Talha on 11/20/18.
 */

public class MDNSDiscoverer extends AsyncTask {
    private String TAG = Tags.makeTag("MDNSDiscover");

    private int timeout;

    private NsdManager mNsdManager;
    private String mServiceName;
    private String SERVICE_TYPE = "_services._dns-sd._udp";

    private ArrayList<NsdManager.DiscoveryListener> discoveryListeners;

    private DiscoveryInterface discoveryInterface;

    public MDNSDiscoverer(Context context, int tO) {
        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mServiceName = "NSD";

        discoveryListeners = new ArrayList<>();
        timeout = tO;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        //initiate discovery listeners
        discoverServices("");

        //wait for specified timeout
        waitTillTimeout(timeout);

        //remove all discovery listeners
        stopDiscovery();

        return null;
    }

    public void setDiscoveryInterface(DiscoveryInterface dInterface) {
        discoveryInterface = dInterface;
    }

    private class initializeResolveListener implements NsdManager.ResolveListener {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, "Resolve failed " + errorCode);
            switch (errorCode) {
                case NsdManager.FAILURE_ALREADY_ACTIVE:
                    Log.e(TAG, "FAILURE ALREADY ACTIVE");
                    mNsdManager.resolveService(serviceInfo, new initializeResolveListener());
                    break;
                case NsdManager.FAILURE_INTERNAL_ERROR:
                    Log.e(TAG, "FAILURE_INTERNAL_ERROR");
                    break;
                case NsdManager.FAILURE_MAX_LIMIT:
                    Log.e(TAG, "FAILURE_MAX_LIMIT");
                    break;
            }
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            final String host = (serviceInfo.getHost().toString()).substring(1);
            Log.d(TAG, "Service Type: " + serviceInfo.getServiceType());

            Log.d(TAG, "Service Info: " + serviceInfo.toString());
            discoveryInterface.onHostDiscovered(host, Constants.MDNS);

            if (serviceInfo.getServiceName().equals(mServiceName)) {
                Log.d(TAG, "Same IP.");
                return;
            }

        }
    }

    private void stopDiscovery() {
        for (NsdManager.DiscoveryListener dListener : discoveryListeners) {
            mNsdManager.stopServiceDiscovery(dListener);
        }
        discoveryInterface.onDiscoveryCompletion();
    }

    private void discoverServices(String serviceType) {
        if (serviceType.equals("")) {
            NsdManager.DiscoveryListener rootDiscoveryListener = new NsdManager.DiscoveryListener() {
                @Override
                public void onDiscoveryStarted(String regType) {

                }


                @Override
                public void onServiceFound(NsdServiceInfo service) {

                    Log.d(TAG, "Service Type: " + service.getServiceType());

                    Log.d(TAG, "Service Info: " + service.toString());

                    if (service.getServiceType().contains("local.")) {
                        String newServiceType = service.getServiceName() + "." + service.getServiceType().substring(0, service.getServiceType().length() - 6);
                        discoverServices(newServiceType);
                        mNsdManager.resolveService(service, new initializeResolveListener());
                    }

                }

                @Override
                public void onServiceLost(NsdServiceInfo service) {
                }

                @Override
                public void onDiscoveryStopped(String serviceType) {
                }

                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }

                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }
            };
            discoveryListeners.add(rootDiscoveryListener);
            mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, rootDiscoveryListener);
        } else {
            NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {

                @Override
                public void onDiscoveryStarted(String regType) {

                }

                @Override
                public void onServiceFound(NsdServiceInfo service) {

                    mNsdManager.resolveService(service, new initializeResolveListener());

                    if (service.getServiceName().contains(mServiceName)) {
                        mNsdManager.resolveService(service, new initializeResolveListener());
                    }
                }

                @Override
                public void onServiceLost(NsdServiceInfo service) {

                }

                @Override
                public void onDiscoveryStopped(String serviceType) {

                }

                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }

                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    mNsdManager.stopServiceDiscovery(this);
                }
            };
            discoveryListeners.add(discoveryListener);
            mNsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        }

    }

    public void waitTillTimeout(final int timeout) {
        Thread waitingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        waitingThread.start();
        try {
            waitingThread.join();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
    }
}
