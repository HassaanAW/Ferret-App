package com.talhajavedmukhtar.ferret;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.talhajavedmukhtar.ferret.Model.DeviceDetails;
import com.talhajavedmukhtar.ferret.Util.DataHandler;
import com.talhajavedmukhtar.ferret.Util.NothingSelectedSpinnerAdapter;
import com.talhajavedmukhtar.ferret.Util.Tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class LabellingActivity extends AppCompatActivity {
    private String TAG = Tags.makeTag("LabellingActivity");

    private Map deviceDetails;
    private Iterator iterator;

    private TextView devicesLabelled;

    private int devicesLabelledSoFar;

    private String currentDeviceType;

    //private Stack<DeviceDetails> toAskFor;

    private TextView macAd;
    private TextView vendor;

    private Button submit;
    private Button cantFind;

    private Spinner spinner;
    private EditText whatDeviceType;
    private EditText whatDeviceModel;

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_labelling);

        context = this;

        macAd = findViewById(R.id.macAd);
        vendor = findViewById(R.id.vendor);

        deviceDetails = MyApp.getDeviceDetails();
        iterator = deviceDetails.entrySet().iterator();

        /*
        toAskFor = new Stack<DeviceDetails>();

        while (it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            String ip = (String)pair.getKey();
            DeviceDetails details = (DeviceDetails)pair.getValue();

            toAskFor.add(details);
            Log.d(TAG,"IP: "+ip + " ,MAC: "+details.getMACAddress() + " ,Vendor: "+details.getVendor());
            it.remove();
        }*/

        devicesLabelled = findViewById(R.id.devicesDone);

        devicesLabelledSoFar = 0;

        TextView helpTV = findViewById(R.id.helpTV);
        SpannableString content = new SpannableString(getResources().getString(R.string.MACAddressHelp));
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        helpTV.setText(content);

        LinearLayout helpMe = findViewById(R.id.helpMe);
        helpMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, HelpActivity.class);
                startActivity(intent);
            }
        });

        whatDeviceType = findViewById(R.id.whatDeviceType);
        whatDeviceModel = findViewById(R.id.whatDeviceModel);

        submit = findViewById(R.id.submit);
        cantFind = findViewById(R.id.cantFind);

        submit.setEnabled(false);

        spinner = findViewById(R.id.selectDeviceType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.deviceTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt("Select a device type");
        spinner.setAdapter(new NothingSelectedSpinnerAdapter(adapter,
                R.layout.contact_spinner_row_nothing_selected,
                // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                this));
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentDeviceType = (String)adapterView.getItemAtPosition(i);

                if(currentDeviceType != null){
                    if(currentDeviceType.equals("Other")){
                        //hide spinner to show editText instead
                        spinner.setVisibility(View.GONE);
                        whatDeviceType.setVisibility(View.VISIBLE);
                    }

                    submit.setEnabled(true);
                }

            }

            public void onNothingSelected(AdapterView<?> adapterView){

            }
        });



        initialize();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
        intent.putExtra("back",true);
        startActivity(intent);
    }

    private void initializeDevicesLabelled(){
        int noOfDevices = deviceDetails.size();

        String message = "0/"+Integer.toString(noOfDevices)+" devices labelled";

        devicesLabelled.setText(message);
    }

    private void updateDevicesLabelled(int labelled){
        int noOfDevices = deviceDetails.size();

        String message = Integer.toString(labelled)+"/"+Integer.toString(noOfDevices)+" devices labelled";

        devicesLabelled.setText(message);
    }

    private void initialize(){
        Log.d("Labeling Activity", String.valueOf(deviceDetails));
        if(deviceDetails.size() == 0){
            goToMacNotFound();
            return;
        }

        initializeDevicesLabelled();

        final DeviceDetails details;

        Map.Entry pair = (Map.Entry)iterator.next();
        final String ip = (String)pair.getKey();
        details = (DeviceDetails)pair.getValue();

        macAd.setText(details.getMACAddress());
        vendor.setText(details.getVendor());

        final String lastTimeStamp = MyApp.getLastTimeStamp();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deviceType = "";
                if(currentDeviceType.equals("Other")){
                    deviceType = whatDeviceType.getText().toString();
                }else{
                    deviceType = currentDeviceType;
                }

                String deviceModel = whatDeviceModel.getText().toString();

                DataHandler dataHandler = new DataHandler(context);
                dataHandler.pushLabelData(ip,deviceType,deviceModel,lastTimeStamp);

                devicesLabelledSoFar += 1;
                update();
            }
        });

        cantFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devicesLabelledSoFar += 1;
                DataHandler dataHandler = new DataHandler(context);
                dataHandler.pushLabelData(ip,"","",lastTimeStamp);
                update();
            }
        });
    }

    private void update(){
        if(devicesLabelledSoFar == deviceDetails.size()){
            goToNext();
            return;
        }

        updateDevicesLabelled(devicesLabelledSoFar);

        spinner.setVisibility(View.VISIBLE);
        whatDeviceType.setVisibility(View.GONE);
        spinner.setSelection(0);
        whatDeviceModel.setText("");
        submit.setEnabled(false);

        final DeviceDetails details;

        Map.Entry pair = (Map.Entry)iterator.next();
        final String ip = (String)pair.getKey();
        details = (DeviceDetails)pair.getValue();

        macAd.setText(details.getMACAddress());
        vendor.setText(details.getVendor());

        final String lastTimeStamp = MyApp.getLastTimeStamp();

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String deviceType = "";
                if(currentDeviceType.equals("Other")){
                    deviceType = whatDeviceType.getText().toString();
                }else{
                    deviceType = currentDeviceType;
                }

                String deviceModel = whatDeviceModel.getText().toString();

                DataHandler dataHandler = new DataHandler(context);
                dataHandler.pushLabelData(ip,deviceType,deviceModel,lastTimeStamp);

                devicesLabelledSoFar += 1;
                update();
            }
        });

        cantFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                devicesLabelledSoFar += 1;
                DataHandler dataHandler = new DataHandler(context);
                dataHandler.pushLabelData(ip,"","",lastTimeStamp);
                update();
            }
        });
    }

    private void goToNext(){
        Intent intent = new Intent(this, ThankYouActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMacNotFound(){
        Intent intent = new Intent(this, MacNotFoundActivity.class);
        startActivity(intent);
        finish();
    }


}
