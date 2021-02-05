package com.talhajavedmukhtar.ferret.BannerGrabber;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.talhajavedmukhtar.ferret.Model.Banner;
import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.Tags;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by Talha on 11/23/18.
 */
public class UPNPBannerGrabber extends AsyncTask {
    private String TAG = Tags.makeTag("UPNPBannerGrabber");

    private String ip;
    private Context context;

    private SubBannerInterface subBannerInterface;

    public UPNPBannerGrabber(String ipAd, Context ctx) {
        ip = ipAd;
        context = ctx;
    }

    public void setSubBannerInterface(SubBannerInterface bInterface) {
        subBannerInterface = bInterface;
    }

    private HashMap<String, String> getUPNPBanners(int timeout) {
        HashMap<String, String> ipToBanner = new HashMap<>();

        String DEFAULT_IP = "239.255.255.250";
        int DEFAULT_PORT = 1900;
        String DISCOVERY_QUERY = "M-SEARCH * HTTP/1.1" + "\r\n" +
                "HOST: 239.255.255.250:1900" + "\r\n" +
                "MAN: \"ssdp:discover\"" + "\r\n" +
                "MX: 1" + "\r\n" +
                "ST: ssdp:all" + "\r\n" + // Use this for all UPnP Devices
                "\r\n";

        Log.d(TAG, "Waiting for UPnP");
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi != null) {
            WifiManager.MulticastLock lock = wifi.createMulticastLock("The Lock");
            lock.acquire();
            DatagramSocket socket = null;
            try {
                InetAddress group = InetAddress.getByName(DEFAULT_IP);

                socket = new DatagramSocket(null); // <-- create an unbound socket first
                socket.setReuseAddress(true);
                socket.setSoTimeout(timeout);
                socket.bind(new InetSocketAddress(DEFAULT_PORT)); // <-- now bind it


                DatagramPacket datagramPacketRequest = new DatagramPacket(DISCOVERY_QUERY.getBytes(), DISCOVERY_QUERY.length(), group, DEFAULT_PORT);
                socket.send(datagramPacketRequest);

                long time = System.currentTimeMillis();
                long curTime = System.currentTimeMillis();

                while (curTime - time < timeout) {
                    DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(datagramPacket);
                    String response = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    Log.d(TAG, response);
                    if (response.substring(0, 12).toUpperCase().equals("HTTP/1.1 200")) {
                        String ip = datagramPacket.getAddress().getHostAddress();
                        ipToBanner.put(ip, response);
                        Log.d(TAG, "Putting >" + ip + " :" + response);
                    }

                    curTime = System.currentTimeMillis();
                    Log.d(TAG, "Difference is: " + Long.toString(curTime - time));
                }

            } catch (final Exception e) {
                e.printStackTrace();
                Log.d(TAG, e.toString() + " : " + e.getMessage());
            } finally {
                if (socket != null) {
                    socket.close();
                }
                Log.d(TAG, "done");
            }
            lock.release();

        }

        return ipToBanner;
    }


    @Override
    protected Object doInBackground(Object[] objects) {
        HashMap<String, String> ipToBanner = getUPNPBanners(10000);

        if (ipToBanner.containsKey(ip)) {
            return ipToBanner.get(ip);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        String bannerText = (String) o;
        subBannerInterface.onGrabbed(new Banner(bannerText, Constants.UPNP));
    }
}
