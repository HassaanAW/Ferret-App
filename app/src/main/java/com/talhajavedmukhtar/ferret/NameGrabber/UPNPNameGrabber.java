package com.talhajavedmukhtar.ferret.NameGrabber;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;
import android.os.AsyncTask;


import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.Tags;
import com.talhajavedmukhtar.ferret.Util.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jcifs.netbios.NbtAddress;

public class UPNPNameGrabber extends AsyncTask {
    private String TAG = Tags.makeTag("UPNPNameGrabber");
    private String ipAddress;
    private NameGrabInterface nameGrabInterface;
    private Context context;

    private String DEFAULT_IP = "239.255.255.250";
    private int DEFAULT_PORT = 1900;
    private String DISCOVERY_QUERY = "M-SEARCH * HTTP/1.1" + "\r\n" +
            "HOST: 239.255.255.250:1900" + "\r\n" +
            "MAN: \"ssdp:discover\"" + "\r\n" +
            "MX: 1"+ "\r\n" +
            "ST: ssdp:all" + "\r\n" + // Use this for all UPnP Devices
            "\r\n";

    private static HashMap<String,String> ipToBannerMap = new HashMap<>();
    private static Boolean initialized = false;

    public UPNPNameGrabber(String ip, Context ctx){
        ipAddress = ip;
        context = ctx;
    }

    public void setNameGrabInterface(NameGrabInterface nameGrabInterface) {
        this.nameGrabInterface = nameGrabInterface;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        synchronized (this){
            if(!initialized){
                initialBroadcast();
                initialized = true;
            }
        }

        String dummyName;
        if(ipToBannerMap.containsKey(ipAddress)){
            String banner = ipToBannerMap.get(ipAddress);

            String location = Utils.extractUPNPLocation(banner);

            Log.d(TAG,"This ip was found: "+ipAddress+" ,location: "+location);

            String response = getXML(location);

            Log.d(TAG, "This was the response: "+response);


            dummyName = Utils.extractDeviceName(response);
        }else{
            dummyName = null;
        }

        return dummyName;
    }

    private void initialBroadcast(){
        int timeout = 10000;

        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifi != null) {
            WifiManager.MulticastLock lock = wifi.createMulticastLock("The Lock");
            lock.acquire();
            DatagramSocket socket = null;
            try {
                InetAddress group = InetAddress.getByName(DEFAULT_IP);
                int port = DEFAULT_PORT;
                String query = DISCOVERY_QUERY;

                socket = new DatagramSocket(null); // <-- create an unbound socket first
                socket.setReuseAddress(true);
                socket.setSoTimeout(timeout);
                socket.bind(new InetSocketAddress(DEFAULT_PORT)); // <-- now bind it


                DatagramPacket datagramPacketRequest = new DatagramPacket(query.getBytes(), query.length(), group, port);
                socket.send(datagramPacketRequest);

                long time = System.currentTimeMillis();
                long curTime = System.currentTimeMillis();

                //responses.clear();
                //Toast.makeText(mContext,"Socket for UPnP Discovery Opened",Toast.LENGTH_SHORT).show();
                //ArrayList<String> responses = new ArrayList<>();
                while (curTime - time < timeout) {
                    Log.d(TAG, "Waiting for UPnP");


                    DatagramPacket datagramPacket = new DatagramPacket(new byte[1024], 1024);
                    socket.receive(datagramPacket);
                    String response = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    Log.d(TAG, response);
                    if (response.substring(0, 12).toUpperCase().equals("HTTP/1.1 200")) {
                        final String ip = datagramPacket.getAddress().getHostAddress();
                        final String resp = ip + " : discovered through UPnP";
                        if (!ipToBannerMap.containsKey(ip)) {
                            ipToBannerMap.put(ip, response);
                        }
                    }

                    curTime = System.currentTimeMillis();
                    Log.d(TAG, "Difference is: " + Long.toString(curTime - time));
                }

            } catch (final Exception e) {
                Log.d(TAG, e.toString() + " : " + e.getMessage());
            } finally {
                if (socket != null) {
                    socket.close();
                }
                Log.d(TAG, "done");
            }
            lock.release();
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        String name = (String)o;
        nameGrabInterface.onGrab(name,Constants.UPNP);
        nameGrabInterface.onCompletion();
        super.onPostExecute(o);
    }

    private String getXML(String location){
        final String USER_AGENT = "Mozilla/5.0";
        final String CONTENT_LENGTH = "131";
        StringBuffer response = new StringBuffer();

        try {

            URL obj = new URL(location);

            String sub = location.substring(0,5);
            Log.d(TAG,"SUB: "+sub);


            BufferedReader in;
            if(sub.equals("http:")){
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent",USER_AGENT);
                con.setRequestProperty("Content-Length",CONTENT_LENGTH);

                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }else{
                //create the http connection

                HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent",USER_AGENT);
                con.setRequestProperty("Content-Length",CONTENT_LENGTH);

                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }


            String inputLine;

            while((inputLine = in.readLine()) != null) {
                response.append(inputLine.toString());
            }

            //close input stream
            in.close();

            System.out.println("response is: " + response);

        } catch (MalformedURLException e) {
            //Log.d(TAG,e.getMessage());

        } catch (IOException e) {
            //Log.d(TAG,e.getMessage());
        }

        return response.toString();
    }
}
