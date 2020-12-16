package com.talhajavedmukhtar.ferret;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.talhajavedmukhtar.ferret.PortScanner.PortScanner;
import com.talhajavedmukhtar.ferret.PortScanner.PortScannerInterface;
import com.talhajavedmukhtar.ferret.Util.DataHandler;
import com.talhajavedmukhtar.ferret.Util.GetPublicIPTask;
import com.talhajavedmukhtar.ferret.Util.Tags;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Notification.VISIBILITY_PUBLIC;


public class PortScanActivity extends AppCompatActivity {
    private String TAG = Tags.makeTag("PortScanActivity");

    private ArrayList<String> addresses;
    private HashMap<String,ArrayList<Integer>> ipToPortsData;

    private String CHANNEL_ID = "2";
    private int NOTIFICATION_ID = 200;

    private Boolean userAway;
    private Boolean notificationFired;

    private ProgressBar progressBar;

    public static Handler UIHandler;

    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portscan);

        new GetPublicIPTask(MyApp.getLastTimeStamp(),this).execute();

        addresses = MyApp.getIpsForPortScan();

        final ArrayList<String> copy = new ArrayList<>();

        for(String address: addresses){
            copy.add(address);
        }

        ipToPortsData = new HashMap<>();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(addresses.size()*7);

        notificationFired = false;

        createNotificationChannel();

        for(int i = 0; i < addresses.size(); i++){
            final String ip = addresses.get(i);
            final int index = i;
            ipToPortsData.put(ip,new ArrayList<Integer>());
            PortScanner portScanner = new PortScanner(ip);
            portScanner.setPortScannerInterface(new PortScannerInterface() {
                @Override
                public void onPortFound(int port) {
                    ipToPortsData.get(ip).add(port);
                    Log.d(TAG,"port: "+port+" open on ip: "+ip);
                }

                @Override
                public void onCompletion() {
                    DataHandler dataHandler = new DataHandler(getApplication());
                    dataHandler.pushPortsData(index,ip,ipToPortsData.get(ip),MyApp.getLastTimeStamp());
                    Log.d(TAG,"an ip was done");
                    copy.remove(ip);
                    if(copy.isEmpty()){
                        if(userAway){
                            pushNotification();
                        }else{
                            goToNext();
                        }
                    }
                }

                @Override
                public void on10kDone(int progressVal) {
                    incrementProgress();
                }
            });
            portScanner.setMinPort(1025);
            portScanner.setMaxPort(65535);
            portScanner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Log.d(TAG,ip+" set up!");
        }

    }

    @Override
    protected void onStart() {
        userAway = false;

        if(notificationFired){
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
            notificationFired = false;

            goToNext();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        userAway = true;
    }

    private void goToNext(){
        Intent intent = new Intent(this, SecretKeyActivity.class);
        startActivity(intent);
        finish();
    }

    private void incrementProgress(){
        PortScanActivity.runOnUI(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    progressBar.setProgress(progressBar.getProgress()+1);
                }
            }
        });
    }

    private void pushNotification(){
        String content = "The process is complete. Click here to open your secret key";

        Intent intent = new Intent(this, SecretKeyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intent,0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Write down your secret key!")
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setVisibility(VISIBILITY_PUBLIC);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        notificationFired = true;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Ferret";
            String description = "I will notify you when the scan is complete.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
