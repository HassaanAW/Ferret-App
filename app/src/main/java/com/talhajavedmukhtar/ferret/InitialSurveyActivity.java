package com.talhajavedmukhtar.ferret;

import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.talhajavedmukhtar.ferret.Util.Tags;

public class InitialSurveyActivity extends AppCompatActivity {

    private String TAG = Tags.makeTag("InitialSurveyActivity");
    private Button opensurvey;
    private Button proceed;
    private int InitialSurveyOpened;

    @Override
    public void onResume() {
        super.onResume();
        LoadPreferences();
        if (InitialSurveyOpened == 1) {
            proceed.getBackground().setAlpha(255);
            proceed.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                    startActivity(intent);

                    finish();


                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_initial_survey);
        proceed = findViewById(R.id.proceed);
        proceed.getBackground().setAlpha(45);
        final Context context = this;


        opensurvey = findViewById(R.id.opensurvey);
        opensurvey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), InitialSurveyWebviewActivity.class);
                startActivity(intent);

            }
        });


    }

    private void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        InitialSurveyOpened = sharedPreferences.getInt("InitialSurveyOpened", 0);
        Log.d(TAG, "UserID: " + Long.toString(sharedPreferences.getLong("UserID", 0)));
        Log.d(TAG, "InitialSurveyOpened:" + Integer.toString(sharedPreferences.getInt("InitialSurveyOpened", 0)));
    }

//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        // Always call the superclass so it can restore the view hierarchy
//        super.onRestoreInstanceState(savedInstanceState);
//
//        // Restore state members from saved instance
//        InitialSurveyOpened = savedInstanceState.getInt("InitialSurveyOpened");
//
//    }


//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        // Save the user's current game state
//        savedInstanceState.putInt("InitialSurveyOpened", 0);
//
//
//        // Always call the superclass so it can save the view hierarchy state
//        super.onSaveInstanceState(savedInstanceState);
//    }

    @Override
    public void onBackPressed() {

//        myWebView.destroy();

        //We don't allow that up in here boyy
    }

//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        super.onSaveInstanceState(savedInstanceState);
//        // Save UI state changes to the savedInstanceState.
//        // This bundle will be passed to onCreate if the process is
//        // killed and restarted.
//
//        savedInstanceState.putInt("InitialSurveyOpened", 0);
//
//        // etc.
//    }


}
