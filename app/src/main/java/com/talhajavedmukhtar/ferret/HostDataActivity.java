package com.talhajavedmukhtar.ferret;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.talhajavedmukhtar.ferret.Util.Tags;

public class HostDataActivity extends  AppCompatActivity {
    TextView vendor;
    TextView deviceName;
    TextView ipAddress;
    TextView vulnerable;
    private String TAG = Tags.makeTag("HostDataActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostdata);
        vendor = findViewById(R.id.vendor);
        deviceName = findViewById(R.id.deviceName);
        ipAddress = findViewById(R.id.ipAddress);
        vulnerable = findViewById(R.id.vulnerable);


//        imageView = findViewById(R.id.imageView);
        Intent intent = getIntent();
        String receivedVendor =  intent.getStringExtra("vendor");
        String receivedDeviceName =  intent.getStringExtra("name");
        String receivedIpAddress =  intent.getStringExtra("ip");
        Boolean receivedVulnerable =  intent.getBooleanExtra("vulnerable", false);

        Log.d(TAG,receivedVendor);
        Log.d(TAG,receivedDeviceName);
        Log.d(TAG,receivedIpAddress);
//        int receivedImage = intent.getIntExtra("image",0);
        vendor.setText(receivedVendor.trim());
        deviceName.setText(receivedDeviceName.trim());
        ipAddress.setText(receivedIpAddress.trim());

        if (receivedVulnerable == true)
        {
            vulnerable.setText("Yes");
            vulnerable.setTextColor(Color.parseColor("#f44336"));
        }
        else
        {
            vulnerable.setText("No");
            vulnerable.setTextColor(Color.parseColor("#4CAF50"));


        }

        Log.d(TAG,"in hostdataactivity");

//        imageView.setImageResource(receivedImage);
        //enable back Button
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    //getting back to listview
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

