package com.talhajavedmukhtar.ferret;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.talhajavedmukhtar.ferret.Util.GetPublicIPTask;
import com.talhajavedmukhtar.ferret.Util.Tags;

public class MainActivity extends AppCompatActivity {
    private String TAG = Tags.makeTag("MainActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Intent intent = new Intent(this, WhereActivity.class);
        startActivity(intent);*/

        /*
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("vulnerable",false);
        startActivity(intent);*/

        //Start
        /*
        Runtime runtime = Runtime.getRuntime();

        try {
            Process process = runtime.exec("nbtstat -A 192.168.100.1");
            BufferedReader reader=new BufferedReader( new InputStreamReader(process.getInputStream()));
            String s;
            while ((s = reader.readLine()) != null){
                Log.d(TAG,"msg: "+s);
            }
        }catch (Exception ex){
            Log.d(TAG,ex.getMessage());
        }

        String base = "192.168.100.";
        for(int i = 0; i < 20; i++){
            String ip = base + Integer.toString(i);
            Log.d(TAG,ip);
        }*/

        /*
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        String base = "192.168.100.";
        for(int i = 0; i < 20; i++){
            String ip = base + Integer.toString(i);
            Log.d(TAG,ip);
            try{
                NbtAddress[] nbts = NbtAddress.getAllByAddress(ip);
                String netbiosname = nbts[0].getHostName();
                Log.d(TAG,netbiosname);
            }catch (Exception ex){
                Log.d(TAG,"Could not grab name");
            }catch (ExceptionInInitializerError err){
                Log.d(TAG,err.getMessage());
            }

        }*/




        //End

        Button begin = (Button)findViewById(R.id.beginButton);

        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNext();
            }
        });

        MyApp myApp = (MyApp) getApplication();


        Boolean alreadyInitiated = (myApp.initTask.getStatus() == AsyncTask.Status.FINISHED); /*||
                (myApp.initTask.getStatus() == AsyncTask.Status.PENDING) ||
                (myApp.initTask.getStatus() == AsyncTask.Status.RUNNING);*/


        if(!alreadyInitiated){
            myApp.initTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        //myApp.initTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void goToNext(){
        Intent intent = new Intent(this, DataCollectionActivity.class);
        startActivity(intent);
    }
}
