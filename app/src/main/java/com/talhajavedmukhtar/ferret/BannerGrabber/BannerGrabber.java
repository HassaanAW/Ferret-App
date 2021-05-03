package com.talhajavedmukhtar.ferret.BannerGrabber;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Model.Banner;
import com.talhajavedmukhtar.ferret.Model.Host;
import com.talhajavedmukhtar.ferret.Util.Tags;

import java.io.IOException;
import java.util.ArrayList;


import org.apache.commons.net.telnet.TelnetClient;

import java.io.InputStream;
import java.io.PrintStream;

/**
 * Created by Talha on 11/23/18.
 */
public class BannerGrabber extends AsyncTask {
    private String TAG = Tags.makeTag("BannerGrabber");

    private String ip;
    private ArrayList<Banner> banners;
    private int counter;
    private int tasks;
    private SubBannerInterface subBannerInterface;
    private Context context;


    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private Host host;

    private BannerInterface bannerInterface;

    public BannerGrabber(Context ctx, String ipAd, Host hostobj) {
        host = hostobj;
        ip = ipAd;
        banners = new ArrayList<>();

        counter = 0;
        tasks = 3;
        subBannerInterface = new SubBannerInterface() {
            @Override
            public void onGrabbed(Banner b) {
                if (b.getText() != null) {
                    banners.add(b);
                    publishProgress(b);
                }
                synchronized (this) {
                    counter += 1;
                }
                if (counter == tasks) {
                    bannerInterface.onCompletion(banners);
                }
            }
        };

        context = ctx;

        Log.d(TAG, "Banner Grabber created for " + ip);
    }

    public void setBannerInterface(BannerInterface bInterface) {
        bannerInterface = bInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ArrayList<AsyncTask> tasks = new ArrayList<>();
//send port as parameter s
        HTTPBannerGrabber httpBannerGrabber = new HTTPBannerGrabber(ip);
        httpBannerGrabber.setSubBannerInterface(subBannerInterface);

        SSHBannerGrabber sshBannerGrabber = new SSHBannerGrabber(ip);
        sshBannerGrabber.setSubBannerInterface(subBannerInterface);

        UPNPBannerGrabber upnpBannerGrabber = new UPNPBannerGrabber(ip, context);
        upnpBannerGrabber.setSubBannerInterface(subBannerInterface);

        TelnetBannerGrabber telnetBannerGrabber = new TelnetBannerGrabber(ip);
        telnetBannerGrabber.setSubBannerInterface(subBannerInterface);

        SMTPBannerGrabber smtpBannerGrabber = new SMTPBannerGrabber(ip);
        smtpBannerGrabber.setSubBannerInterface(subBannerInterface);

        FTPBannerGrabber ftpBannerGrabber = new FTPBannerGrabber(ip);
        ftpBannerGrabber.setSubBannerInterface(subBannerInterface);

        DNSBannerGrabber dnsBannerGrabber = new DNSBannerGrabber(ip);
        dnsBannerGrabber.setSubBannerInterface(subBannerInterface);

        DHCPBannerGrabber dhcpBannerGrabber = new DHCPBannerGrabber(ip);
        dhcpBannerGrabber.setSubBannerInterface(subBannerInterface);

        tasks.add(httpBannerGrabber);
        tasks.add(sshBannerGrabber);
        tasks.add(upnpBannerGrabber);
        tasks.add(telnetBannerGrabber);
        tasks.add(smtpBannerGrabber);
        tasks.add(ftpBannerGrabber);
        tasks.add(dnsBannerGrabber);
        tasks.add(dhcpBannerGrabber);


        //start parallel execution
        for (AsyncTask aTask : tasks) {
            aTask.execute();
        }

        Log.d(TAG, "Banner Grabber initiated for " + ip);


        //wait for each task to execute
        /*
        for(AsyncTask aTask: tasks){
            while(aTask.getStatus() != android.os.AsyncTask.Status.FINISHED){
                //wait until this is done
            }
        }*/

        //Log.d(TAG,"Banner Grabber done for "+ip);

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        //bannerInterface.onCompletion(banners);
    }

    @Override
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);

        Banner banner = (Banner) values[0];
        bannerInterface.onFound(banner);
    }
}
