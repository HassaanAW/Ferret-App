package com.talhajavedmukhtar.ferret;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.talhajavedmukhtar.ferret.Util.DataHandler;

public class PaymentActivity extends AppCompatActivity {

    private TextView message;
    private Button yes, no;
    private ImageView vulnImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Boolean vulnerable = getIntent().getExtras().getBoolean("vulnerable");

        message = findViewById(R.id.messagePart1);
        vulnImage = findViewById(R.id.vulnerable);

        if(vulnerable){
            message.setText(R.string.message1a);
            vulnImage.setImageResource(R.drawable.vulnerable);
        }else{
            message.setText(R.string.message1b);
            vulnImage.setImageResource(R.drawable.safe);
        }

        yes = findViewById(R.id.positiveButton);
        no = findViewById(R.id.negativeButton);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PaymentOptionsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataHandler dataHandler = new DataHandler(getApplicationContext());
                dataHandler.pushPaymentData("Not Interested",MyApp.getLastTimeStamp());
                Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
                startActivity(intent);
                finish();
            }
        });

        MyApp.setPaymentQuestionSeen(true);

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
