package com.talhajavedmukhtar.ferret;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SecretKeyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_key);

        String secret = MyApp.id(this).substring(0,13);

        TextView secretKey = findViewById(R.id.secretKey);

        secretKey.setText(secret);

        Button next = findViewById(R.id.enter);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNext();
            }
        });
    }

    private void goToNext(){
        Intent intent = new Intent(this, WhereActivity.class);
        startActivity(intent);
        finish();
    }
}
