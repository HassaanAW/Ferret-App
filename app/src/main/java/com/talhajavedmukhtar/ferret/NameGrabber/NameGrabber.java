package com.talhajavedmukhtar.ferret.NameGrabber;

import android.content.Context;

import com.talhajavedmukhtar.ferret.HostScanner.ICMPPingDiscoverer;
import com.talhajavedmukhtar.ferret.HostScanner.MDNSDiscoverer;
import com.talhajavedmukhtar.ferret.HostScanner.TCPSYNDiscoverer;
import com.talhajavedmukhtar.ferret.Model.Host;

import java.util.ArrayList;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

public class NameGrabber extends AsyncTask {
    private Host host;
    private Context context;

    private int tasksCompleted;

    private NameGrabInterface nameGrabInterface;
    private NameGrabInterface subNameGrabInterface;

    public NameGrabber(Host h, Context ctx){
        host = h;
        context = ctx;

        tasksCompleted = 0;

        subNameGrabInterface = new NameGrabInterface() {
            @Override
            public void onGrab(String name, String protocol) {
                publishProgress(name,protocol);
            }

            @Override
            public void onCompletion() {
                synchronized (context){
                    tasksCompleted += 1;
                    if (tasksCompleted == 2){
                        nameGrabInterface.onCompletion();
                    }
                }
            }
        };
    }

    public void setNameGrabInterface(NameGrabInterface nameGrabInterface) {
        this.nameGrabInterface = nameGrabInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ArrayList<android.os.AsyncTask> tasks = new ArrayList<>();

        /*InetNameGrabber inetNameGrabber = new InetNameGrabber(host.getIpAddress());
        inetNameGrabber.setNameGrabInterface(subNameGrabInterface);*/

        NETBIOSNameGrabber netbiosNameGrabber = new NETBIOSNameGrabber(host.getIpAddress());
        netbiosNameGrabber.setNameGrabInterface(subNameGrabInterface);

        UPNPNameGrabber upnpNameGrabber = new UPNPNameGrabber(host.getIpAddress(),context);
        upnpNameGrabber.setNameGrabInterface(subNameGrabInterface);


        //tasks.add(inetNameGrabber);
        tasks.add(netbiosNameGrabber);
        tasks.add(upnpNameGrabber);

        //start parallel execution
        for(android.os.AsyncTask aTask: tasks){
            try{
                aTask.executeOnExecutor(android.os.AsyncTask.THREAD_POOL_EXECUTOR);
            }catch (Exception ex){
                //Grabbing names is not worth the app crashing
            }

        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        String name = (String)values[0];
        String protocol = (String)values[1];

        if(name != null){
            nameGrabInterface.onGrab(name,protocol);
            cancel(true);
        }

        super.onProgressUpdate(values);
    }
}
