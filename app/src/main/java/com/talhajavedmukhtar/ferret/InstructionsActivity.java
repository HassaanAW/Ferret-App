package com.talhajavedmukhtar.ferret;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.talhajavedmukhtar.ferret.Util.Tags;

public class InstructionsActivity extends AppCompatActivity {
    private String TAG = Tags.makeTag("InstructionsActivity");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        final Context context = this;

        Boolean backCase = false;

        try{
            backCase = getIntent().getExtras().getBoolean("back");
        }catch (Exception ex){
            //Log.d(TAG,ex.getMessage());
        }


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

        final Boolean back = backCase;

        Button proceed = findViewById(R.id.proceed);
        proceed.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(!back){
                    Intent intent = new Intent(getApplicationContext(), LabellingActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        //We don't allow that up in here boyy
    }
}
