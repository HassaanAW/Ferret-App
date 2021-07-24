package com.talhajavedmukhtar.ferret.HostScanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.talhajavedmukhtar.ferret.InitialSurveyWebviewActivity;
import com.talhajavedmukhtar.ferret.Model.Host;
import com.talhajavedmukhtar.ferret.MyApp;
import com.talhajavedmukhtar.ferret.R;
import com.talhajavedmukhtar.ferret.Util.Tags;
import com.talhajavedmukhtar.ferret.Util.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Talha on 11/20/18.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class HostScanner extends AsyncTask {

    private String TAG = Tags.makeTag("HostScanner");
    private HashMap<String, String> oui_dict = new HashMap<String, String>();
    private HashMap<String, String> ipadd_macadd = new HashMap<String, String>();

    private String networkAddress;
    private int networkSize;
    private int noOfThreads;
    private int timeout;

    private int tasksCompleted;

    private ScannerInterface scannerInterface;

    private DiscoveryInterface discoveryInterface;
    private NYUAPIInterface nyuapiInterface;

    private HashMap<String, Host> hostDetails;

    private MyApp myApp;
    private Context context;


    public HostScanner(Context ctx, String networdAdd, int netSize, int nThreads, int tO) {

        networkAddress = networdAdd;
        networkSize = netSize;
        noOfThreads = nThreads;
        timeout = tO;

        tasksCompleted = 0;

        hostDetails = new HashMap<>();

        context = ctx;

        myApp = (MyApp) ctx.getApplicationContext();
        // discovery process
        discoveryInterface = new DiscoveryInterface() {
            @Override
            public void onHostDiscovered(String ipAd, String protocol) {
                publishProgress(ipAd, protocol);
            }

            @Override
            public void onDiscoveryCompletion() {
                synchronized (context) {
                    tasksCompleted += 1;
                    if (tasksCompleted == 3) {
                        scannerInterface.onCompletion();
                    }
                }
            }
        };

    }

    public void setScannerInterface(ScannerInterface sInterface) {
        scannerInterface = sInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        readouidict();


        ArrayList<AsyncTask> tasks = new ArrayList<>();
        // discovery task 1
        TCPSYNDiscoverer tcpsynDiscoverer = new TCPSYNDiscoverer(networkAddress, networkSize, timeout, noOfThreads);
        tcpsynDiscoverer.setDiscoveryInterface(discoveryInterface);
        // discovery task 2
        ICMPPingDiscoverer icmpPingDiscoverer = new ICMPPingDiscoverer(networkAddress, networkSize, timeout, noOfThreads);
        icmpPingDiscoverer.setDiscoveryInterface(discoveryInterface);
        // discovery task 3
        MDNSDiscoverer mdnsDiscoverer = new MDNSDiscoverer(context, timeout);
        mdnsDiscoverer.setDiscoveryInterface(discoveryInterface);

        tasks.add(tcpsynDiscoverer);
        tasks.add(icmpPingDiscoverer);
        tasks.add(mdnsDiscoverer);

        //start parallel execution
        for (AsyncTask aTask : tasks) {
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
        String ipAd = (String) values[0];
        String protocol = (String) values[1];
        Log.d("Hassaan", "Here");

        if (!hostDetails.containsKey(ipAd)) {
            String macAdd = Utils.getMACAddress(ipAd);
            final String[] vendor = new String[1];
            Log.d("MACADD", "macAdd: " + macAdd);

            nyuapiInterface = new NYUAPIInterface() {
                @Override
                public void Success(String response) {
                    Log.d(TAG, "VENDER GOTTEN: " + response);

                    if (ipadd_macadd.containsKey(ipAd) == false) {
                        if (macAdd != null) {
                            vendor[0] = "Unknown Vendor";
                            vendor[0] = response.toUpperCase();

                            if (vendor[0] == "Unknown Vendor") {
                                vendor[0] = myApp.getMap().findVendor(macAdd);
                            }

                            MyApp.addDeviceDetails(ipAd, macAdd, vendor[0]);
                        }
                        else {
                            vendor[0] = "Unknown Vendor";
                        }

                        Log.d("Inside Danny", "3");

                        String name = "...";
                        Host newHost = new Host();
                        newHost.setIpAddress(ipAd);
                        newHost.addDiscoveredThrough(protocol);
                        newHost.setVendor(vendor[0]);
                        newHost.setDeviceName(name);

                        if (macAdd != null) {
                            newHost.setMAhash(Utils.md5(macAdd));
                        }

                        hostDetails.put(ipAd, newHost);

                        scannerInterface.onDeviceFound(newHost);
                        ipadd_macadd.put(ipAd, macAdd);
                    }
                }

                @Override
                public void Error(VolleyError error) {
                }
            };


            if (macAdd != null) {
                getVendor(macAdd, nyuapiInterface, macAdd, ipAd, protocol, vendor);
            }

        } else {
            hostDetails.get(ipAd).addDiscoveredThrough(protocol);
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        //scannerInterface.onCompletion();
    }


    private void readouidict() {

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("macToVendor/ouidict.txt")));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] fields = line.split(":");
//                String mackey = fields[0].replaceAll("^[\"']+|[\"']+$", "");
                String mackey = fields[0].trim().replaceAll("^[\"']+|[\"']+$", "").toLowerCase();
                String dictvendor = fields[1].substring(0, fields[1].length() - 1);
                dictvendor = dictvendor.replaceAll("^[\"']+|[\"']+$", "");

                oui_dict.put(mackey, dictvendor);
//                Log.d(TAG, "mackey: " + mackey);
//                Log.d(TAG, "dictvendor: " + dictvendor);
//                Log.d(TAG, "fields: " + fields);
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception ex) {
                    Log.d(TAG, ex.getMessage());
                }
            }
        }

    }


    public void getVendor(String mac, final NYUAPIInterface listener, String macAdd, String ipAd, String protocol, final String[] vendor  ) {

        mac = mac.toLowerCase().replace(" ", "").replace(":", "");
        String oui = mac.substring(0, 6);
        Log.d("Oui", oui);
        //Log.d(TAG, "ouidict: " + oui_dict.toString());
        // Log.d(TAG, "contains key: " + Boolean.toString(oui_dict.containsKey(oui)));

        if ((oui_dict.containsKey(oui)) == true) {

            String Vendor_Oui = oui_dict.get(oui);
            if (Vendor_Oui != null) {
                Log.d(TAG, "found vendor: " + Vendor_Oui + " of " + oui);
                // return foundvendor;
            } else {
                if (oui_dict.containsKey(oui)) {
                    // key exists but it's null
                    Vendor_Oui = "Unknown Vendor";
                    Log.d(TAG, "found vendor null: " + Vendor_Oui+ " of " + oui);
                } else {
                    // No such key
                    Vendor_Oui = "Unknown Vendor";
                    Log.d(TAG, "found vendor no key: " + Vendor_Oui+ " of " + oui);
                }
            }
            Log.d("Hassaan", "Vendor through OUI");

            if (ipadd_macadd.containsKey(ipAd) == false) {
                if (macAdd != null) {
                    vendor[0] = "Unknown Vendor";
                    vendor[0] = Vendor_Oui.toUpperCase();
                    MyApp.addDeviceDetails(ipAd, macAdd, vendor[0]);
                }

                String name = "...";
                Host newHost = new Host();
                newHost.setIpAddress(ipAd);
                newHost.addDiscoveredThrough(protocol);
                newHost.setVendor(vendor[0]);
                newHost.setDeviceName(name);

                if (macAdd != null) {
                    newHost.setMAhash(Utils.md5(macAdd));
                }

                hostDetails.put(ipAd, newHost);
                scannerInterface.onDeviceFound(newHost);
                ipadd_macadd.put(ipAd, macAdd);
            }
        }
        else {
            Log.d("Hassaan", "Not found Vendor through OUI");
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(context);
            String url = "https://iotinspector.org/device_identification/get_vendor/" + oui + "/0";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            listener.Success(response.substring(0, response.length()));
                            Log.d(TAG, "Response from Danny's API: " + response.substring(0, response.length()));
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.Error(error);
                    Log.d(TAG, "That didn't work!");
                    Log.d(TAG, "error from Danny API: " + error);
                }
            });

            stringRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {

                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);

        }
    }
}
