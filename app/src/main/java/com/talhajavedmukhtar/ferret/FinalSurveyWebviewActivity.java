package com.talhajavedmukhtar.ferret;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.talhajavedmukhtar.ferret.Util.Tags;

import java.util.Random;

public class FinalSurveyWebviewActivity extends AppCompatActivity {

    private String TAG = Tags.makeTag("FinalSurveyWebviewActivity");
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


        myWebView.loadUrl("https://docs.google.com/forms/d/e/1FAIpQLSf3B8cQqfWq8ieu3LBqE6wjAv0Ej7w__XAZnbfCSBbC6sabAQ/viewform?usp=pp_url&entry.131608939=" + Long.toString(UserID));


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
//        savedInstanceState.putInt("InitialSurveyOpened", 1);
//
//
//        // Always call the superclass so it can save the view hierarchy state
//        super.onSaveInstanceState(savedInstanceState);
//    }
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        // Always call the superclass so it can restore the view hierarchy
//        super.onRestoreInstanceState(savedInstanceState);
//
//        // Restore state members from saved instance
//        UserID = savedInstanceState.getLong("UserID");
//
//    }

    private void LoadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        UserID = sharedPreferences.getLong("UserID", 0);
        Log.d(TAG, "UserID: " + Long.toString(sharedPreferences.getLong("UserID", 0)));
    }

    private void SavePreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("FinalSurveyOpened", 1);
        Log.d(TAG, "UserID at final:" + Long.toString(UserID));
//        editor.commit();   // I missed to save the data to preference here,.
        editor.apply();

    }

    @Override
    public void onBackPressed() {

//        myWebView.destroy();
        SavePreferences();
        super.onBackPressed();
        //We don't allow that up in here boyy
    }


}
