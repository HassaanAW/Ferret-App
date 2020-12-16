package com.talhajavedmukhtar.ferret.NameGrabber;

import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Model.Host;
import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.Tags;
import com.talhajavedmukhtar.ferret.Util.Utils;

import java.net.InetAddress;

public class InetNameGrabber extends AsyncTask {
    private String TAG = Tags.makeTag("InetNameGrabber");
    private String ipAddress;
    private NameGrabInterface nameGrabInterface;

    public InetNameGrabber(String ip){
        ipAddress = ip;
    }

    public void setNameGrabInterface(NameGrabInterface nameGrabInterface) {
        this.nameGrabInterface = nameGrabInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String name;
        byte[] address = Utils.getBytesFromString(ipAddress);

        try{
            InetAddress inetAddress = InetAddress.getByAddress(address);
            name = inetAddress.getCanonicalHostName();
        }catch (Exception ex){
            //Log.d(TAG,"Other");
            name = null;
        }

        return name;
    }

    @Override
    protected void onPostExecute(Object o) {
        String name = (String)o;
        nameGrabInterface.onGrab(name,Constants.INET);
        nameGrabInterface.onCompletion();
        super.onPostExecute(o);
    }
}
