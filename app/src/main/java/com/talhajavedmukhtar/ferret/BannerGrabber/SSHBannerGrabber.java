package com.talhajavedmukhtar.ferret.BannerGrabber;

import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Model.Banner;
import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.Tags;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Talha on 11/23/18.
 */
public class SSHBannerGrabber extends AsyncTask {
    private String TAG = Tags.makeTag("SSHBannerGrabber");

    private String ip;
    private Socket connectionSocket;

    private SubBannerInterface subBannerInterface;

    public SSHBannerGrabber(String ipAd) {
        ip = ipAd;
        connectionSocket = new Socket();
    }

    public void setSubBannerInterface(SubBannerInterface bInterface) {
        subBannerInterface = bInterface;
    }

    private Boolean formConnection(String ip, int timeout) {
        try {
            connectionSocket.connect(new InetSocketAddress(ip, 22), timeout);
            return true;
        } catch (Exception ex) {
            //Log.d(TAG,"Could not connect to "+ ip + " at port "+ Integer.toString(22) + " " + ex.getMessage());
            return false;
        }
    }

    private String getResponse() {
        String response = "";

        try {
            InputStream is = connectionSocket.getInputStream();
            byte[] buffer = new byte[1024];
            int read;

            while ((read = is.read(buffer)) != -1) {
                String output = new String(buffer, 0, read);
                Log.d(TAG, output);

                if (output.length() > 0) {
                    response += output;
                    break;
                }
                /*if(output.length() > 0){
                    response += output.split("\\n")[0];
                    break;
                }*/
            }
            ;

        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        return response;
    }

    private String grabBanner(String ip, int connectionTimeout) {
        String message = "";
        if (formConnection(ip, connectionTimeout)) {
            Log.d(TAG, "Connected to host " + ip);
            String response = getResponse();
            if (response.equals("")) {
                message = null;
            } else {
                message += response;
            }
        } else {
            message = null;
        }

        try {
            connectionSocket.close();
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }
        Log.d(TAG, "message from ssh: " + message);

        return message;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        ExecutorService service = Executors.newSingleThreadExecutor();

        Future<String> f = service.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return grabBanner(ip, 10000);
            }
        });

        String banner;
        try {
            banner = f.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            //Log.d(TAG,e.getMessage());
            banner = null;
        }

        return banner;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        String bannerText = (String) o;
        subBannerInterface.onGrabbed(new Banner(bannerText, Constants.SSH));
    }
}
