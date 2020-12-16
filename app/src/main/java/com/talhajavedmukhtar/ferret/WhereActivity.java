package com.talhajavedmukhtar.ferret;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.talhajavedmukhtar.ferret.Util.DataHandler;
import com.talhajavedmukhtar.ferret.Util.NothingSelectedSpinnerAdapter;
import com.talhajavedmukhtar.ferret.Util.Tags;

public class WhereActivity extends AppCompatActivity {
    private String TAG = Tags.makeTag("WhereActivity");

    private String place;
    private String details;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_where);

        place = "";
        details = "";

        context = this;

        final EditText whereExactly = findViewById(R.id.nameSpace);

        final Button enter = findViewById(R.id.enter);

        enter.setEnabled(false);

        final Spinner spinner = findViewById(R.id.where);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.placeOptions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //spinner.setPrompt("Where did you run the application?");
        spinner.setAdapter(new NothingSelectedSpinnerAdapter(adapter,
                R.layout.contact_spinner_row_nothing_selected,
                // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                this));
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selection = (String)adapterView.getItemAtPosition(i);

                if(selection != null){
                    Log.d(TAG,selection);
                    if(selection.equals("A Home")){
                        place = selection;
                    }else{
                        place = selection;
                        spinner.setVisibility(View.GONE);
                        whereExactly.setVisibility(View.VISIBLE);
                    }

                    enter.setEnabled(true);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lastTimeStamp = MyApp.getLastTimeStamp();

                details = whereExactly.getText().toString();

                DataHandler dataHandler = new DataHandler(context);
                dataHandler.pushPlaceData(place,details,lastTimeStamp);
                finish();
            }
        });

    }
}
