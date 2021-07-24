package com.talhajavedmukhtar.ferret.Util;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.talhajavedmukhtar.ferret.HostScanner.NYUAPIInterface;
import com.talhajavedmukhtar.ferret.Model.Banner;
import com.talhajavedmukhtar.ferret.Model.Host;
import com.talhajavedmukhtar.ferret.MyApp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.Buffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Talha on 11/18/18.
 */

public class Utils {
    private static String TAG = Tags.makeTag("Utils");
    private Context context;
    private static NYUAPIInterface nyuapiInterface;


    private static ArrayList<String> generateAddresses(String[] octetArray, int octetNo, int intFromOctet) {
        ArrayList<String> adds = new ArrayList<>();

        switch (octetNo) {
            case 1:
                for (int i = 0; i < 256; i++) {
                    octetArray[3] = Integer.toString(i);
                    adds.add(TextUtils.join(".", octetArray));
                }
                return adds;
            case 2:
                for (int i = 0; i < 256; i++) {
                    for (int j = 0; j < 256; j++) {
                        octetArray[2] = Integer.toString(i);
                        octetArray[3] = Integer.toString(j);
                        adds.add(TextUtils.join(".", octetArray));
                    }
                }
                return adds;
            case 3:
                for (int i = 0; i < 256; i++) {
                    for (int j = 0; j < 256; j++) {
                        for (int k = 0; k < 256; k++) {
                            octetArray[1] = Integer.toString(i);
                            octetArray[2] = Integer.toString(j);
                            octetArray[3] = Integer.toString(k);
                            adds.add(TextUtils.join(".", octetArray));
                        }
                    }
                }
                return adds;
            default:
                return adds;
        }
    }

    public static ArrayList<String> getAddressRange(String ip, int cidr) {
        //double numHosts = Math.pow(2,(32 - cidr));

        //hardcode Network Size to always be 24
        cidr = 24;

        int hostBits = 32 - cidr;
        ArrayList<String> addresses = new ArrayList<>();

        int octet = 0;
        while (hostBits > 8) {
            hostBits -= 8;
            octet += 1;
        }

        String[] octetArray = ip.split("\\.");

        String oct = Integer.toBinaryString(Integer.parseInt(octetArray[3 - octet]));


        //initial octet after applying subnet mask
        int pointer = oct.length() - 1;
        for (int i = 0; i < hostBits && pointer != -1; i++) {
            char[] octChars = oct.toCharArray();
            octChars[pointer] = '0';
            oct = String.valueOf(octChars);
            pointer--;
        }


        for (int i = 0; i <= Math.pow(2, hostBits) - 1; i++) {
            String hostPart = Integer.toBinaryString(i);
            //adding this host to the masked ip
            String updatedOctet;
            try {
                updatedOctet = oct.substring(0, oct.length() - hostPart.length()) + hostPart;
            } catch (Exception ex) {
                updatedOctet = hostPart;
            }

            int intFromOctet = Integer.parseInt(updatedOctet, 2);
            octetArray[3 - octet] = Integer.toString(intFromOctet);

            //Commented out the code for networks larger than /24 because it causes OOM error in some devices
            /*
            if(octet == 0){
                Log.d(TAG,"Octet was zero");
                addresses.add(TextUtils.join(".",octetArray));
            }else{
                Log.d(TAG,"Octet was "+Integer.toString(octet));
                addresses.addAll(generateAddresses(octetArray,octet,intFromOctet));
            }*/

            //For now just dealing with first 256 addresses will do.
            addresses.add(TextUtils.join(".", octetArray));
        }
        //allHosts = addresses;
        return addresses;
    }


    public static String getNetworkAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);

        try {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            int gatewayInt = dhcpInfo.gateway;
            return (gatewayInt & 0xFF) + "." +
                    ((gatewayInt >>>= 8) & 0xFF) + "." +
                    ((gatewayInt >>>= 8) & 0xFF) + "." +
                    ((gatewayInt >>>= 8) & 0xFF);
        } catch (Exception ex) {
            try {
                return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
            } catch (Exception exception) {
                return "0.0.0.0";
            }
        }
    }

    public static int getNetworkSize(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);

        try {
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            int i = dhcpInfo.netmask;

            ArrayList<String> octets = new ArrayList<>();
            octets.add(Integer.toBinaryString(i & 0xFF));

            for (int j = 1; j < 4; j++) {
                octets.add(Integer.toBinaryString((i >>>= 8) & 0xFF));
            }

            int CIDR = 0;


            for (String octet : octets) {
                if (octet.equals("11111111")) {
                    CIDR += 8;
                } else {
                    int numOfOnes = 0;

                    for (int j = 0; j < 8; j++) {
                        if (octet.charAt(j) == '1') {
                            numOfOnes += 1;
                        } else {
                            break;
                        }
                    }
                    CIDR += numOfOnes;
                    break;
                }
            }

            return CIDR;
        } catch (Exception ex) {
            return 24;
        }
    }

    public static String getMACAddress(String ipAd) {
        String NEIGHBOR_INCOMPLETE = "INCOMPLETE";
        String NEIGHBOR_FAILED = "FAILED";
        BufferedReader bufferedReader = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("ip neighbor");
            proc.waitFor();

            if (proc.exitValue() != 0) {
                throw new Exception("Unable to access ARP entries");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));


//            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d(TAG, "arpFile: " + line);
                String[] splitted = line.split("\\s+");
                for (String a : splitted)
                    Log.d(TAG, "splitted: " + a);
//                    System.out.println(a);

                if (splitted.length <= 4) {
                    continue;
                }

                String ip = splitted[0];

                InetAddress addr = InetAddress.getByName(ip);
                if (addr.isLinkLocalAddress() || addr.isLoopbackAddress()) {
                    continue;
                }
                String mac = splitted[4];
                String state = splitted[splitted.length - 1];
//                    Log.d("IP", "ip: " + ip);
//                    Log.d("Mac", "mac: " + mac);


//                    String mac = splitted[3];
                if (!NEIGHBOR_FAILED.equals(state) && !NEIGHBOR_INCOMPLETE.equals(state)) {
                    if (mac.matches("..:..:..:..:..:..")) {
                        if (!mac.equals("00:00:00:00:00:00")) {
                            if (ip.equals(ipAd)) {
                                return mac;
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//            System.out.println(e);
        }
//        catch (IOException e) {
////            e.printStackTrace();
//        }
        finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        return null;
    }

    public static ArrayList<Host> getRemainingHostsFromARP(ArrayList<Host> discovered, Context context) {
        ArrayList<Host> additionalHosts = new ArrayList<>();

        ArrayList<String> ipAddresses = new ArrayList<>();
        for (Host h : discovered) {
            ipAddresses.add(h.getIpAddress());
        }

        MyApp myApp = (MyApp) context.getApplicationContext();
        // new ip neigh code
        String NEIGHBOR_INCOMPLETE = "INCOMPLETE";
        String NEIGHBOR_FAILED = "FAILED";
        BufferedReader bufferedReader = null;
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("ip neighbor");
            proc.waitFor();

            if (proc.exitValue() != 0) {
                throw new Exception("Unable to access ARP entries");
            }
            bufferedReader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.d(TAG, "arpFile: " + line);
                String[] splitted = line.split("\\s+");
                for (String a : splitted)
                    Log.d(TAG, "splitted: " + a);
//                    System.out.println(a);

                if (splitted.length <= 4) {
                    continue;
                }

                String ip = splitted[0];

                InetAddress addr = InetAddress.getByName(ip);
                if (addr.isLinkLocalAddress() || addr.isLoopbackAddress()) {
                    continue;
                }
                String mac = splitted[4];
                String state = splitted[splitted.length - 1];
//                    Log.d("IP", "ip: " + ip);
//                    Log.d("Mac", "mac: " + mac);


//                    String mac = splitted[3];
                if (!NEIGHBOR_FAILED.equals(state) && !NEIGHBOR_INCOMPLETE.equals(state)) {
                    if (mac.matches("..:..:..:..:..:..")) {
                        if (!mac.equals("00:00:00:00:00:00")) {
                            if (!ipAddresses.contains(ip)) {
                                final String[] vendor = new String[1];
                                nyuapiInterface = new NYUAPIInterface() {
                                    @Override
                                    public void Success(String response) {
                                        Log.d(TAG, "VENDER GOTTEN: " + response);


                                        if (mac != null) {

                                            vendor[0] = "Unknown Vendor";
                                            vendor[0] = response.toUpperCase();

                                            if (vendor[0] == "Unknown Vendor") {
                                                vendor[0] = myApp.getMap().findVendor(mac);

                                            }


                                        } else {
                                            vendor[0] = "Unknown Vendor";
                                        }

                                        //Log.d(TAG,"IP: "+ipAd+" Name:"+name);
                                        String name;
                                        byte[] address = getBytesFromString(ip);

                                        try {
                                            InetAddress inetAddress = InetAddress.getByAddress(address);
                                            Log.d(TAG, "Passed this");

                                    /*Boolean reachable = inetAddress.isReachable(1000);

                                    if(reachable){
                                        Log.d(TAG,"Was reached");
                                    }else{
                                        Log.d(TAG, "Could not reach");
                                    }*/

                                            name = inetAddress.getCanonicalHostName();
                                        } catch (Exception ex) {
                                            //Log.d(TAG,"Other");
                                            name = "Unknown Name";
                                        }

// === commented this out as it was adding repeating hosts ===
                                        Host newHost = new Host();
                                        newHost.setIpAddress(ip);
                                        newHost.addDiscoveredThrough(Constants.ARP);
                                        newHost.setVendor(vendor[0]);
                                        newHost.setDeviceName(name);


                                        newHost.setMAhash(md5(mac));

                                        Log.d("Manchester", vendor[0]);
                                        MyApp.addDeviceDetails(ip, mac, vendor[0]);

                                        additionalHosts.add(newHost);


                                    }

                                    @Override
                                    public void Error(VolleyError error) {

                                    }
                                };

//
//                                String vendor;
//                                try {
//                                    vendor = myApp.getMap().findVendor(mac);
////                                    vendor = getVendor(mac);
//                                } catch (Exception ex) {
//                                    vendor = "Unknown";
//
//                                }
//
//                                String name;
//                                byte[] address = getBytesFromString(ip);
//
//                                try {
//                                    InetAddress inetAddress = InetAddress.getByAddress(address);
//                                    Log.d(TAG, "Passed this");
//
//                                    /*Boolean reachable = inetAddress.isReachable(1000);
//
//                                    if(reachable){
//                                        Log.d(TAG,"Was reached");
//                                    }else{
//                                        Log.d(TAG, "Could not reach");
//                                    }*/
//
//                                    name = inetAddress.getCanonicalHostName();
//                                } catch (Exception ex) {
//                                    //Log.d(TAG,"Other");
//                                    name = "Unknown Name";
//                                }
//
//// === commented this out as it was adding repeating hosts ===
//                                Host newHost = new Host();
//                                newHost.setIpAddress(ip);
//                                newHost.addDiscoveredThrough(Constants.ARP);
//                                newHost.setVendor(vendor);
//                                newHost.setDeviceName(name);
//
//
//                                newHost.setMAhash(md5(mac));
//
//                                MyApp.addDeviceDetails(ip, mac, vendor);
//
//                                additionalHosts.add(newHost);
//// ============================================================
                            }
                        }
                    }
                }

            }


        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(e.getMessage());
//            System.out.println(e);
        }
//        catch (IOException e) {
////            e.printStackTrace();
//        }
        finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }

        //old arp code
//        BufferedReader bufferedReader = null;
//        try {
//            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
//
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                //Log.d(TAG, "arpFile: " + line);
//                String[] splitted = line.split(" +");
//                if (splitted != null && splitted.length >= 4) {
//                    String ip = splitted[0];
//                    String mac = splitted[3];
//                    if (mac.matches("..:..:..:..:..:..")) {
//                        if (!mac.equals("00:00:00:00:00:00")) {
//                            if(!ipAddresses.contains(ip)){
//                                String vendor;
//                                try{
//                                    vendor = myApp.getMap().findVendor(mac);
//                                }catch (Exception ex){
//                                    vendor = "Unknown";
//                                }
//
//                                String name;
//                                byte[] address = getBytesFromString(ip);
//
//                                try{
//                                    InetAddress inetAddress = InetAddress.getByAddress(address);
//                                    Log.d(TAG,"Passed this");
//
//                                    /*Boolean reachable = inetAddress.isReachable(1000);
//
//                                    if(reachable){
//                                        Log.d(TAG,"Was reached");
//                                    }else{
//                                        Log.d(TAG, "Could not reach");
//                                    }*/
//
//                                    name = inetAddress.getCanonicalHostName();
//                                }catch (Exception ex){
//                                    //Log.d(TAG,"Other");
//                                    name = "Unknown Name";
//                                }
//
//
//                                Host newHost = new Host();
//                                newHost.setIpAddress(ip);
//                                newHost.addDiscoveredThrough(Constants.ARP);
//                                newHost.setVendor(vendor);
//                                newHost.setDeviceName(name);
//
//
//                                newHost.setMAhash(md5(mac));
//
//                                MyApp.addDeviceDetails(ip,mac,vendor);
//
//                                additionalHosts.add(newHost);
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally{
//            try {
//                if(bufferedReader != null)
//                bufferedReader.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        return additionalHosts;
    }

    public void getVendor(String mac, final NYUAPIInterface listener) {

        mac = mac.toLowerCase().replace(" ", "").replace(":", "");
        Log.d("originalMac", "mac: " + mac);

        String oui = mac.substring(0, 6);


        Log.d(TAG, "oui: " + oui);
//        Log.d(TAG, "ouidict: " + oui_dict.toString());

//        Log.d(TAG, "contains key: " + Boolean.toString(oui_dict.containsKey(oui)));

//        if ((oui_dict.containsKey(oui)) == true) {
//            Log.d("found vendor: ", vendor);
//
////                    vendor = vendor.replaceAll("^[\"']+|[\"']+$", "");
//        }

        final String[] foundvendor = {"Unknown Vendor"};
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
//                        foundvendor[0] =

//                        Log.d(TAG, "VENDER GOTTEN: " + foundvendor[0]);
                        Log.d(TAG, "Response from Danny API: " + response.substring(0, response.length()));
//                        textView.setText("Response is: " + response.substring(0, 500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                textView.setText("That didn't work!");
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
        Log.d("StringRequest", String.valueOf(stringRequest));

//        String foundvendor = oui_dict.get(oui);
//        if (foundvendor != null) {
//
//            Log.d(TAG, "found vendor: " + foundvendor);
//            return foundvendor;
//        } else {
//            if (oui_dict.containsKey(oui)) {
//                // key exists but it's null
//                foundvendor = "Unknown Vendor";
//                Log.d(TAG, "found vendor null: " + foundvendor);
//            } else {
//                // No such key
//                foundvendor = "Unknown Vendor";
//                Log.d(TAG, "found vendor no key: " + foundvendor);
//
//            }
//        }


    }

    public static String getProductFromBanner(Banner b) {
        String protocol = b.getProtocol();
        String banner = b.getText();

        try {
            if (protocol.equals(Constants.SSH)) {
                return extractSSHProduct(banner);
            }

            if (protocol.equals(Constants.HTTP)) {
                return extractHTTPProduct(banner);
            }

            if (protocol.equals(Constants.UPNP)) {
                return extractUPNPProduct(banner);
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        return null;
    }

    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }

    public static String getVersionFromBanner(Banner b) {
        String protocol = b.getProtocol();
        String banner = b.getText();

        try {
            if (protocol.equals(Constants.SSH)) {
                return extractSSHVersion(banner);
            }

            if (protocol.equals(Constants.HTTP)) {
                return extractHTTPVersion(banner);
            }

            if (protocol.equals(Constants.UPNP)) {
                return extractUPNPVersion(banner);
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }


        return null;
    }

    //SSH
    private static String extractSSHProduct(String banner) {
        try {
            String product = banner.split("\n")[0];

            Log.d(TAG, "Doing regex on: " + product);

            //remove SSH-x.x
            product = product.replaceAll("(SSH-\\d+(\\.\\d+)?-)", "");

            Log.d(TAG, "Doing regex on: " + product);

            //remove version
            product = product.replaceAll("(v\\s?)?\\d+(\\.\\d+)*", "");

            //remove ending underscores or hyphens
            product = product.replaceAll("(_|-)+$", "");

            //replace in between underscores or hypens with space
            product = product.replaceAll("(_|-)", " ");

            product = product.toLowerCase();

            if (product.trim().equals("dropbear")) {
                Log.d(TAG, "This equaled dropbear: " + product);
                return "dropbear_ssh";
            } else {
                Log.d(TAG, "This did not equal dropbear: " + product);
            }

            return product;
        } catch (Exception ex) {
            throw ex;
        }
    }

    private static String extractSSHVersion(String banner) {
        try {
            String product = banner.split("\n")[0];

            //remove SSH-x.x
            product = product.replaceAll("(SSH-\\d+(\\.\\d+)?-)", "");

            Pattern p = Pattern.compile("(v\\s?)?\\d+(\\.\\d+)*");

            Matcher m = p.matcher(product);

            if (m.find()) {
                String version = m.group(0);

                return version;
            } else {
                return "*";
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    //HTTP
    private static String extractHTTPProduct(String banner) {
        Pattern p = Pattern.compile("(Server:)|(SERVER:)\\s.*");

        Matcher m = p.matcher(banner);

        try {
            if (m.find()) {
                String product = m.group(0);
                product = product.split("\\s\\(")[0];

                //remove version identifiers
                product = product.replaceAll("/?\\d+(\\.\\d+)?", "");

                //remove ending space
                product = product.replaceAll("\\s+$", "");

                return product;
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    private static String extractHTTPVersion(String banner) {
        Pattern p = Pattern.compile("(Server:)|(SERVER:)\\s.*");

        Matcher m = p.matcher(banner);

        try {
            if (m.find()) {
                String product = m.group(0);

                //Remove the initial Server:
                product = product.substring(8);

                product = product.split("\\s\\(")[0];

                Pattern p2 = Pattern.compile("/?\\d+(\\.\\d+)?");
                Matcher m2 = p2.matcher(product);

                if (m2.find()) {
                    String version = m2.group(0);

                    if (version.charAt(0) == '/') {
                        return version.substring(1);
                    } else {
                        return version;
                    }
                } else {
                    return "*";
                }
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    private static String extractUPNPProduct(String banner) {
        Pattern p = Pattern.compile("((Server:)|(SERVER:))\\s.*");

        Matcher m = p.matcher(banner);

        Pattern p2 = Pattern.compile("MiniUPnPd");
        Matcher m2 = p2.matcher(banner);

        try {
            if (m2.find()) {
                return "miniupnpd";
            } else {
                if (m.find()) {
                    String product = m.group(0);
                    System.out.println("About to get substring on this: " + product);
                    product = product.substring(8);
                    product = product.split("(, )|(\\s)")[0];

                    //remove version identifiers
                    product = product.split("/")[0];

                    if (product.toLowerCase().equals("linux")) {
                        return "linux_kernel";
                    } else if (product.toLowerCase().equals("posix")) {
                        return "posix";
                    } else if (product.toLowerCase().equals("vxworks")) {
                        return "vxworks";
                    } else {
                        return product;
                    }
                } else {
                    return null;
                }
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    private static String extractUPNPVersion(String banner) {
        String extractedProd = extractUPNPProduct(banner);
        //Add specific case for posix
        if (extractedProd.equals("posix")) {
            return "*";
        }

        //Add specific case for miniupnpd
        if (extractedProd.equals("miniupnpd")) {
            Pattern p = Pattern.compile("MiniUPnPd.*");
            Matcher m = p.matcher(banner);

            if (m.find()) {
                String product = m.group(0);
                product = product.substring(8);
                product = product.split("(, )|(\\s)")[0];

                String version = product.split("/")[1];

                return version.split("_|[a-zA-Z]")[0];
            }
        }

        Pattern p = Pattern.compile("(Server:)|(SERVER:)\\s.*");

        Matcher m = p.matcher(banner);

        try {
            if (m.find()) {
                String product = m.group(0);
                product = product.substring(8);
                product = product.split("(, )|(\\s)")[0];

                String version = product.split("/")[1];

                version = version.split("_|\\-|\\+")[0];

                return version;
            } else {
                return null;
            }
        } catch (Exception ex) {
            throw ex;
        }

    }

    public static String extractUPNPLocation(String banner) {
        Pattern p = Pattern.compile("(Location:)|(LOCATION:)\\s.*");

        Matcher m = p.matcher(banner);

        try {
            if (m.find()) {
                String location = m.group(0);
                location = location.substring(10);

                return location;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
            return null;
        }
    }

    public static String extractDeviceName(String xml) {
        Pattern p = Pattern.compile("<friendlyName>([^<]+)");
        Matcher m = p.matcher(xml);

        try {
            if (m.find()) {
                String name = m.group(1);

                return name;
            } else {
                return null;
            }
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
            return null;
        }
    }

    public static byte[] getBytesFromString(String ip) {
        String[] pieces = ip.split("\\.");
        byte[] address = new byte[4];

        for (int i = 0; i < 4; i++) {
            int pieceVal = Integer.parseInt(pieces[i]);
            address[i] = (byte) pieceVal;
        }

        return address;
    }

    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.d(TAG, e.getMessage());
        }
        return "";
    }


}
