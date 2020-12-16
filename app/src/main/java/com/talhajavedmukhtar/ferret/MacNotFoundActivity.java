package com.talhajavedmukhtar.ferret;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.talhajavedmukhtar.ferret.Util.DataHandler;

public class MacNotFoundActivity extends AppCompatActivity {
    private Button restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_macnotfound);



        restart = findViewById(R.id.restartbutton);


        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });



    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
