package com.talhajavedmukhtar.ferret.NameGrabber;

import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.Tags;
import com.talhajavedmukhtar.ferret.Util.Utils;

import java.net.InetAddress;

import jcifs.netbios.NbtAddress;

public class NETBIOSNameGrabber extends AsyncTask {
    private String TAG = Tags.makeTag("NETBIOSNameGrabber");
    private String ipAddress;
    private NameGrabInterface nameGrabInterface;

    public NETBIOSNameGrabber(String ip){
        ipAddress = ip;
    }

    public void setNameGrabInterface(NameGrabInterface nameGrabInterface) {
        this.nameGrabInterface = nameGrabInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String name;
        try{
            NbtAddress[] nbts = NbtAddress.getAllByAddress(ipAddress);
            name = nbts[0].getHostName();
            Log.d(TAG,name);
        }catch (Exception ex){
            Log.d(TAG,"Could not grab name");
            name = null;
        }
        return name;
    }

    @Override
    protected void onPostExecute(Object o) {
        String name = (String)o;
        nameGrabInterface.onGrab(name,Constants.NETBIOS);
        nameGrabInterface.onCompletion();
        super.onPostExecute(o);
    }
}
