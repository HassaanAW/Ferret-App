package com.talhajavedmukhtar.ferret;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.talhajavedmukhtar.ferret.Util.Tags;

import java.util.Random;

public class InitialSurveyWebviewActivity extends AppCompatActivity {

    private String TAG = Tags.makeTag("InitialSurveyWebviewActivity");
    private WebView myWebView;
    private long UserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LoadPreferences();
        super.onCreate(savedInstanceState);


        myWebView = new WebView(this);
        myWebView.setWebViewClient(new myWebClient());
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        setContentView(myWebView);
        if (UserID == 0) {
            Random random = new Random(System.nanoTime());
            UserID = Math.abs(random.nextLong());
            SavePreferences();

        }


        myWebView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSeKdcJPGXzuhivVirSm2kKf_7L_03__D-ysSNpZS55Z1XP8iw/viewform?usp=pp_url&entry.1333948571=" + Long.toString(UserID));


    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub

            view.loadUrl(url);
            return true;

        }
    }

//    @Override
//    public void onSaveInstanceState(Bundle savedInstanceState) {
//        // Save the user's current game state
//        savedInstanceState.putLong("UserID", UserID);
//
//
//        // Always call the superclass so it can save the view hierarchy state
//        super.onSaveInstanceState(savedInstanceState);
//    }

    private void SavePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("UserID", UserID);
        editor.putInt("InitialSurveyOpened", 1);
        Log.d(TAG, "UserID saved:" + Long.toString(UserID));
//        editor.commit();   // I missed to save the data to preference here,.
        editor.apply();

    }

    private void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        UserID = sharedPreferences.getLong("UserID", 0);
        Log.d(TAG, "UserID: " + Long.toString(sharedPreferences.getLong("UserID", 0)));
    }

//    private void LoadPreferences() {
//        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
//        UserID = sharedPreferences.getLong("UserID", 0);
//
//    }

    @Override
    public void onBackPressed() {

//        myWebView.destroy();

        super.onBackPressed();
        //We don't allow that up in here boyy
    }


}
