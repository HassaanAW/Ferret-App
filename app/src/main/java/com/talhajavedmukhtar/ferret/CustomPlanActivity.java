package com.talhajavedmukhtar.ferret;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.talhajavedmukhtar.ferret.Util.DataHandler;

public class CustomPlanActivity extends AppCompatActivity {
    private EditText plan;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_plan);

        plan = findViewById(R.id.plan);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String customPlan = plan.getText().toString();

                DataHandler dataHandler = new DataHandler(getApplicationContext());
                dataHandler.pushPaymentData(customPlan,MyApp.getLastTimeStamp());

                Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
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
