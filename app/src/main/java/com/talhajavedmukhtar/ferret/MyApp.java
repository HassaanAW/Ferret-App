package com.talhajavedmukhtar.ferret;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.talhajavedmukhtar.ferret.Model.DeviceDetails;
import com.talhajavedmukhtar.ferret.Util.MacToVendorMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Talha on 11/20/18.
 */

public class MyApp extends Application {
    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    private static Boolean paymentDataCollected = false;
    private static Boolean paymentQuestionSeen = false;

    private static Boolean nameCollectionConsent = true;

    private static String lastTimeStamp = "0";
    private static Map<String, DeviceDetails> deviceDetails = new HashMap<>();

    public static MacToVendorMap map;

    private static ArrayList<String> ipsForPortScan = new ArrayList<>();

    public static ArrayList<String> getIpsForPortScan() {
        return ipsForPortScan;
    }

    public static void setIpsForPortScan(ArrayList<String> ipsForPortScan) {
        MyApp.ipsForPortScan = ipsForPortScan;
    }

    AsyncTask initTask = new AsyncTask() {
        @Override
        protected Object doInBackground(Object[] objects) {
            map = new MacToVendorMap(getApplicationContext());
            return null;
        }
    };

    private Boolean isMapReady(){
        return initTask.getStatus() == AsyncTask.Status.FINISHED;
    }

    public MacToVendorMap getMap(){
        while(!isMapReady()){
            //wait till map is ready
        }
        return map;
    }

    public synchronized static String id(Context context){
        if(uniqueID == null){
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_UNIQUE_ID,Context.MODE_PRIVATE);
            uniqueID = sharedPreferences.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }

        return uniqueID;
    }

    public static void setPaymentDataCollected(Boolean paymentDataCollected) {
        MyApp.paymentDataCollected = paymentDataCollected;
    }

    public static Boolean PaymentDataCollected() {
        return paymentDataCollected;
    }

    public static void setPaymentQuestionSeen(Boolean paymentQuestionSeen){
        MyApp.paymentQuestionSeen = paymentQuestionSeen;
    }

    public static Boolean PaymentQuestionSeen() {
        return paymentQuestionSeen;
    }

    public static void setLastTimeStamp(String lastTimeStamp){
        MyApp.lastTimeStamp = lastTimeStamp;
    }

    public static String getLastTimeStamp(){
        return lastTimeStamp;
    }

    public static void addDeviceDetails(String ip, String MAC, String Vendor){
        DeviceDetails details = new DeviceDetails(MAC,Vendor);
        MyApp.deviceDetails.put(ip,details);
    }

    public static Map getDeviceDetails(){
        return deviceDetails;
    }

    public static Boolean getNameCollectionConsent() {
        return nameCollectionConsent;
    }

    public static void setNameCollectionConsent(Boolean nameCollectionConsent) {
        MyApp.nameCollectionConsent = nameCollectionConsent;
    }
}
