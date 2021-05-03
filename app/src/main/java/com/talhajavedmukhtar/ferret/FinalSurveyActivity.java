package com.talhajavedmukhtar.ferret;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.talhajavedmukhtar.ferret.Util.Tags;

public class FinalSurveyActivity extends AppCompatActivity {

    private String TAG = Tags.makeTag("FinalSurveyActivity");
    private Button opensurvey;
    private Button proceed;
    private int FinalSurveyOpened;


    @Override
    public void onResume() {
        super.onResume();
        LoadPreferences();
        if (FinalSurveyOpened == 1) {
            proceed.getBackground().setAlpha(255);
            proceed.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(getApplicationContext(), ThankYouActivity.class);
                    startActivity(intent);

                    finish();


                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_survey);
        proceed = findViewById(R.id.proceed);
        proceed.getBackground().setAlpha(45);
        final Context context = this;


        opensurvey = findViewById(R.id.opensurvey);
        opensurvey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), FinalSurveyWebviewActivity.class);
                startActivity(intent);


            }
        });


//        proceed.getBackground().setAlpha(255);


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

    private void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        FinalSurveyOpened = sharedPreferences.getInt("FinalSurveyOpened", 0);
        Log.d(TAG, "UserID: " + Long.toString(sharedPreferences.getLong("UserID", 0)));
        Log.d(TAG, "FinalSurveyOpened:" + Integer.toString(sharedPreferences.getInt("FinalSurveyOpened", 0)));
    }

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
