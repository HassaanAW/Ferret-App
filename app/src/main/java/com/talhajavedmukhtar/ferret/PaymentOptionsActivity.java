package com.talhajavedmukhtar.ferret;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;

import com.talhajavedmukhtar.ferret.Util.DataHandler;

public class PaymentOptionsActivity extends AppCompatActivity {
    private CardView option1, option2, option3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_options);

        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);

        option1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataHandler dataHandler = new DataHandler(getApplicationContext());
                dataHandler.pushPaymentData("OneTime",MyApp.getLastTimeStamp());

                Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
                startActivity(intent);

                finish();
            }
        });

        option2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataHandler dataHandler = new DataHandler(getApplicationContext());
                dataHandler.pushPaymentData("Monthly",MyApp.getLastTimeStamp());

                Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
                startActivity(intent);

                finish();
            }
        });

        option3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CustomPlanActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
        //we don't allow that up in here boy
    }
}
