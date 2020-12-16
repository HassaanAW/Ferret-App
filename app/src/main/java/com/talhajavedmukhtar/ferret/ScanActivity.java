package com.talhajavedmukhtar.ferret;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
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
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.talhajavedmukhtar.ferret.HostScanner.HostScanner;
import com.talhajavedmukhtar.ferret.HostScanner.ScannerInterface;
import com.talhajavedmukhtar.ferret.Model.DataItem;
import com.talhajavedmukhtar.ferret.Model.Host;
import com.talhajavedmukhtar.ferret.NameGrabber.NameGrabInterface;
import com.talhajavedmukhtar.ferret.NameGrabber.NameGrabber;
import com.talhajavedmukhtar.ferret.PortScanner.PortScanner;
import com.talhajavedmukhtar.ferret.PortScanner.PortScannerInterface;
import com.talhajavedmukhtar.ferret.Util.Constants;
import com.talhajavedmukhtar.ferret.Util.DataHandler;
import com.talhajavedmukhtar.ferret.Util.HostAdapter;
import com.talhajavedmukhtar.ferret.Util.Tags;
import com.talhajavedmukhtar.ferret.Util.Utils;
import com.talhajavedmukhtar.ferret.VulnerabilityFinder.FinderInterface;
import com.talhajavedmukhtar.ferret.VulnerabilityFinder.VulnerabilityFinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.app.Notification.VISIBILITY_PUBLIC;

public class ScanActivity extends AppCompatActivity {
    private String TAG = Tags.makeTag("ScanActivity");

    private String CHANNEL_ID = "1";
    private int NOTIFICATION_ID = 100;

    private Boolean userAway;
    private Boolean notificationFired;

    private ArrayList<Host> hosts;
    private ListView hostView;

    private Boolean hostScanEnded;

    private int vulnerabilityChecked;
    private int portsScanned;

    private ArrayList<DataItem> allData;
    private HashMap<String,ArrayList<Integer>> ipToPortsData;

    private Context context;

    private ProgressBar progressBar;
    private TextView progressStatus;

    public static Handler UIHandler;

    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    private ThreadPoolExecutor threadsExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        hosts = new ArrayList<>();
        hostView = findViewById(R.id.hostData);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(120);

        progressStatus = findViewById(R.id.progressStatus);

        updateProgress(0);

        hostScanEnded = false;
        vulnerabilityChecked = 0;
        portsScanned = 0;

        allData = new ArrayList<>();
        ipToPortsData = new HashMap<>();

        String networkAddress = Utils.getNetworkAddress(this);
        int networkSize = Utils.getNetworkSize(this);

        final HostAdapter hostAdapter = new HostAdapter(hosts,this);

        hostView.setAdapter(hostAdapter);

        context = this;

        notificationFired = false;

        createNotificationChannel();

        threadsExecutor = new ThreadPoolExecutor(20,20,20000, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

        HostScanner hostScanner = new HostScanner(this,networkAddress,networkSize,50,10000);
        hostScanner.setScannerInterface(new ScannerInterface() {
            @Override
            public void onDeviceFound(final Host h) {
                hosts.add(h);

                ScanActivity.runOnUI(new Runnable() {
                    @Override
                    public void run() {
                        hostAdapter.notifyDataSetChanged();
                    }
                });

                Log.d(TAG,"Host Found: "+h.getIpAddress());

                final int index = hosts.indexOf(h);

                NameGrabber nameGrabber = new NameGrabber(h,context);
                nameGrabber.setNameGrabInterface(new NameGrabInterface() {
                    @Override
                    public void onGrab(String name, String protocol) {
                        hosts.get(index).setDeviceName(name);

                        ScanActivity.runOnUI(new Runnable() {
                            @Override
                            public void run() {
                                hostAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onCompletion() {
                        if (hosts.get(index).getDeviceName().equals("...")){
                            hosts.get(index).setDeviceName("Unknown Name");
                        }

                        ScanActivity.runOnUI(new Runnable() {
                            @Override
                            public void run() {
                                hostAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
                //nameGrabber.executeOnExecutor(io.fabric.sdk.android.services.concurrency.AsyncTask.THREAD_POOL_EXECUTOR);
                nameGrabber.executeOnExecutor(threadsExecutor);

                //Start Vulnerability Finder
                VulnerabilityFinder vulnerabilityFinder = new VulnerabilityFinder(context,h);
                FinderInterface finderInterface = new FinderInterface() {
                    @Override
                    public void onCompletion(DataItem dataItem) {
                        Boolean vulnerable = dataItem.vulnerable;
                        Log.d(TAG,"IP: "+h.getIpAddress() + " Vulnerable: "+vulnerable);
                        int index = hosts.indexOf(h);
                        hosts.get(index).setVulnerable(vulnerable);

                        ScanActivity.runOnUI(new Runnable() {
                            @Override
                            public void run() {
                                hostAdapter.notifyDataSetChanged();
                            }
                        });

                        allData.add(dataItem);

                        synchronized (context){
                            vulnerabilityChecked += 1;
                            updateProgressDetailed();

                            Log.d(TAG,"Vulnerabilities Checked for: "+ Integer.toString(vulnerabilityChecked) + " devices");

                            if(hostScanEnded && (vulnerabilityChecked == hosts.size()) && (portsScanned == hosts.size())){
                                //Vulnerabilities checked
                                /*
                                Log.d(TAG,"Vulnerability Checks ended; About to push data");
                                DataHandler dataHandler = new DataHandler(context);
                                dataHandler.pushData(allData);*/
                                saveData();
                                updateProgress(3);
                                Log.d(TAG,"Data saved from VF");
                            }
                        }
                    }
                };
                vulnerabilityFinder.setFinderInterface(finderInterface);
                //vulnerabilityFinder.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                vulnerabilityFinder.executeOnExecutor(threadsExecutor);

                //Start Port Scan
                PortScanner portScanner = new PortScanner(h.getIpAddress());
                PortScannerInterface portScannerInterface = new PortScannerInterface() {
                    @Override
                    public void onPortFound(int port) {
                        String ip = h.getIpAddress();
                        if(ipToPortsData.containsKey(ip)){
                            ipToPortsData.get(ip).add(port);
                        }else{
                            ipToPortsData.put(ip,new ArrayList<Integer>());
                        }
                    }

                    @Override
                    public void onCompletion() {
                        synchronized (context) {
                            portsScanned += 1;
                            updateProgressDetailed();
                            Log.d(TAG,"Ports Scanned for: "+ Integer.toString(portsScanned) + " devices");

                            if (hostScanEnded && (vulnerabilityChecked == hosts.size()) && (portsScanned == hosts.size())) {
                                saveData();
                                updateProgress(3);
                                Log.d(TAG,"Data saved from PS");
                            }
                        }
                    }

                    @Override
                    public void on10kDone(int progressVal) {

                    }
                };
                portScanner.setPortScannerInterface(portScannerInterface);
                //portScanner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                portScanner.executeOnExecutor(threadsExecutor);
            }

            @Override
            public void onCompletion() {

                ArrayList<Host> additionalHosts = Utils.getRemainingHostsFromARP(hosts,context);
                for(final Host h: additionalHosts){
                    Log.d(TAG,"Additional Host Found: "+ h.getIpAddress());
                    hosts.add(h);

                    ScanActivity.runOnUI(new Runnable() {
                        @Override
                        public void run() {
                            hostAdapter.notifyDataSetChanged();
                        }
                    });

                    Log.d(TAG,"Host Found: "+h.getIpAddress());

                    final int index = hosts.indexOf(h);

                    NameGrabber nameGrabber = new NameGrabber(h,context);
                    nameGrabber.setNameGrabInterface(new NameGrabInterface() {
                        @Override
                        public void onGrab(String name, String protocol) {
                            hosts.get(index).setDeviceName(name);

                            ScanActivity.runOnUI(new Runnable() {
                                @Override
                                public void run() {
                                    hostAdapter.notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void onCompletion() {
                            if (!hosts.get(index).getDeviceName().equals("...")){
                                hosts.get(index).setDeviceName("Unknown Name");
                            }

                            ScanActivity.runOnUI(new Runnable() {
                                @Override
                                public void run() {
                                    hostAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    });
                    //nameGrabber.executeOnExecutor(io.fabric.sdk.android.services.concurrency.AsyncTask.THREAD_POOL_EXECUTOR);
                    nameGrabber.executeOnExecutor(threadsExecutor);

                    VulnerabilityFinder vulnerabilityFinder = new VulnerabilityFinder(context,h);
                    FinderInterface finderInterface = new FinderInterface() {
                        @Override
                        public void onCompletion(DataItem dataItem) {
                            Boolean vulnerable = dataItem.vulnerable;
                            Log.d(TAG,"IP: "+h.getIpAddress() + " Vulnerable: "+vulnerable);
                            int index = hosts.indexOf(h);
                            hosts.get(index).setVulnerable(vulnerable);

                            ScanActivity.runOnUI(new Runnable() {
                                @Override
                                public void run() {
                                    hostAdapter.notifyDataSetChanged();
                                }
                            });

                            allData.add(dataItem);

                            synchronized (context){
                                vulnerabilityChecked += 1;
                                updateProgressDetailed();
                                Log.d(TAG,"Vulnerabilities Checked for: "+ Integer.toString(vulnerabilityChecked) + " devices");

                                if(hostScanEnded && (vulnerabilityChecked == hosts.size()) && (portsScanned == hosts.size())){
                                    //Vulnerabilities checked
                                    /*
                                    Log.d(TAG,"Vulnerability Checks ended; About to push data");
                                    DataHandler dataHandler = new DataHandler(context);
                                    dataHandler.pushData(allData);*/
                                    saveData();
                                    updateProgress(3);
                                    Log.d(TAG,"Data saved from VF");
                                }
                            }
                        }
                    };
                    vulnerabilityFinder.setFinderInterface(finderInterface);
                    //vulnerabilityFinder.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    vulnerabilityFinder.executeOnExecutor(threadsExecutor);

                    //Start Port Scan
                    PortScanner portScanner = new PortScanner(h.getIpAddress());
                    PortScannerInterface portScannerInterface = new PortScannerInterface() {
                        @Override
                        public void onPortFound(int port) {
                            String ip = h.getIpAddress();
                            if(ipToPortsData.containsKey(ip)){
                                ipToPortsData.get(ip).add(port);
                            }else{
                                ipToPortsData.put(ip,new ArrayList<Integer>());
                            }
                        }

                        @Override
                        public void onCompletion() {
                            synchronized (context) {
                                portsScanned += 1;
                                updateProgressDetailed();
                                Log.d(TAG,"Ports Scanned for: "+ Integer.toString(portsScanned) + " devices");

                                if (hostScanEnded && (vulnerabilityChecked == hosts.size()) && (portsScanned == hosts.size())) {
                                    saveData();
                                    updateProgress(3);
                                    Log.d(TAG,"Data saved from PS");
                                }
                            }
                        }

                        @Override
                        public void on10kDone(int progressVal) {

                        }
                    };
                    portScanner.setPortScannerInterface(portScannerInterface);
                    //portScanner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    portScanner.executeOnExecutor(threadsExecutor);
                }

                Log.d(TAG,"Host Scanning Done!");
                hostScanEnded = true;

                if(hosts.size() == 0){
                    updateProgress(3);
                    //No hosts found
                }else{
                    //in vulnerability checking phase
                    updateProgress(2);
                }
            }
        });

        updateProgress(1);

        //hostScanner.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        hostScanner.executeOnExecutor(threadsExecutor);

    }

    @Override
    protected void onStart() {
        userAway = false;

        if(notificationFired){
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(NOTIFICATION_ID);
            notificationFired = false;
            //goToPortScanActivity();
            openPaymentActivity();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        userAway = true;
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(MyApp.PaymentQuestionSeen()){
            if(!MyApp.PaymentDataCollected()){
                final LinearLayout paymentMessage = findViewById(R.id.paymentMessage);
                paymentMessage.setVisibility(View.VISIBLE);

                Button yes = findViewById(R.id.positiveButton);
                Button no = findViewById(R.id.negativeButton);
                Button learnMore = findViewById(R.id.learnMore);

                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(), PaymentOptionsActivity.class);
                        startActivity(intent);
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DataHandler dataHandler = new DataHandler(getApplicationContext());
                        dataHandler.pushPaymentData("Not Interested",MyApp.getLastTimeStamp());
                        paymentMessage.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
                        startActivity(intent);
                    }
                });

                learnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openPaymentActivity();
                    }
                });
            }else{
                //jump to Port Scan Activity
                //goToPortScanActivity();
                finish();
            }
        }

    }

    @Override
    public void onBackPressed() {
        //we don't allow that up in here boy
    }

    private void updateProgress(final int progress){
        ScanActivity.runOnUI(new Runnable() {
            @Override
            public void run() {
                switch (progress){
                    case 0:
                        progressBar.setProgress(0);
                        progressStatus.setText(Constants.PROGRESS0);
                        break;
                    case 1:
                        progressBar.setProgress(40);
                        progressStatus.setText(Constants.PROGRESS1);
                        break;
                    case 2:
                        progressBar.setProgress(80);
                        progressStatus.setText(Constants.PROGRESS2);
                        break;
                    case 3:
                        progressBar.setProgress(120);
                        int vulnDevices = getNoOfVulnDevices();
                        if(vulnDevices == 0){
                            progressStatus.setText(Constants.PROGRESS3A);
                        }else{
                            progressStatus.setText(Integer.toString(vulnDevices)+Constants.PROGRESS3B);
                        }

                        break;
                    default:
                }
            }
        });
    }

    private void updateProgressDetailed(){
        ScanActivity.runOnUI(new Runnable() {
            @Override
            public void run() {
                if(progressBar.getProgress() >= 80){
                    int progress = 80 + (((vulnerabilityChecked+portsScanned)*40/(2*hosts.size())));
                    Log.d(TAG,"New progress: "+Integer.toString(progress));
                    progressBar.setProgress(progress);
                }
            }
        });
    }

    private void saveData(){
        for(DataItem d: allData){
            String ip = d.getHost().getIpAddress();
            if (ipToPortsData.containsKey(ip)){
                d.setOpenPorts(ipToPortsData.get(ip));
            }else{
                d.setOpenPorts(new ArrayList<Integer>());
            }
        }

        DataHandler dataHandler = new DataHandler(context);
        dataHandler.pushData(allData);

        ArrayList<String> ips = new ArrayList<>();
        for(int i = 0; i < hosts.size(); i++){
            ips.add(hosts.get(i).getIpAddress());
        }
        MyApp.setIpsForPortScan(ips);


        openPaymentActivity();
    }

    /*private void openPaymentDialog(){
        //Check if any vulnerabilities found
        Boolean vulnerable = false;

        for(DataItem d: allData){
            if (d.vulnerable){
                vulnerable = true;
            }
        }

        PaymentDialog paymentDialog = new PaymentDialog(this,vulnerable);
        paymentDialog.show();

    }*/


    private void goToPortScanActivity(){
        Intent intent = new Intent(this, PortScanActivity.class);

        String[] addresses = new String[hosts.size()];

        for(int i = 0; i < hosts.size(); i++){
            addresses[i] = hosts.get(i).getIpAddress();
        }
        intent.putExtra("hosts",addresses);

        startActivity(intent);
    }

    private void openPaymentActivity(){
        Boolean vulnerable = isNetworkVulnerable();

        if(userAway){
            pushNotification(vulnerable);
        }else{
            //

            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("vulnerable",vulnerable);

            startActivity(intent);
        }
    }

    private Boolean isNetworkVulnerable(){
        Boolean vulnerable = false;

        for(DataItem d: allData){
            if (d.vulnerable){
                vulnerable = true;
            }
        }

        return vulnerable;
    }

    private int getNoOfVulnDevices(){
        int vulnerable = 0;

        for(DataItem d: allData){
            if (d.vulnerable){
                vulnerable += 1;
            }
        }

        return vulnerable;
    }

    private void pushNotification(Boolean vulnerable){
        String content;
        if(vulnerable){
            content = getResources().getString(R.string.message1a) + " " + getResources().getString(R.string.message1c);
        }else{
            content = getResources().getString(R.string.message1b) + " " + getResources().getString(R.string.message1c);
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("vulnerable",vulnerable);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,intent,0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Scan complete")
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