package com.talhajavedmukhtar.ferret.Util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class GetPublicIPTask extends AsyncTask {
    private String TAG = "GetPublicIPTask";

    String lastTimeStamp;
    private Context context;

    public GetPublicIPTask(String ts, Context ctx){
        lastTimeStamp = ts;
        context = ctx;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String publicIP;
        try {
            java.util.Scanner s = new java.util.Scanner(new java.net.URL("https://api.ipify.org").openStream(), "UTF-8").useDelimiter("\\A");
            publicIP = s.next();
            Log.d(TAG,"My current IP address is " + publicIP);

        } catch (java.io.IOException e) {
            publicIP = "grab failed";
            Log.d(TAG,"That failed");
        }

        DataHandler dataHandler = new DataHandler(context);
        dataHandler.pushPublicIP(publicIP,lastTimeStamp);

        return null;
    }
}
