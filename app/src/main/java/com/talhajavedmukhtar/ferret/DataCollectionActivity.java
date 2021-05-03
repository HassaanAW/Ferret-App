package com.talhajavedmukhtar.ferret;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class DataCollectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);

        final CheckBox nameCollectionConsent = findViewById(R.id.nameCollectionConsent);
        nameCollectionConsent.setChecked(true);
        Button proceed = findViewById(R.id.proceed);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.setNameCollectionConsent(nameCollectionConsent.isChecked());
                goToInitialSurveyActivity();
            }
        });
    }

    public void goToInitialSurveyActivity() {
        Intent intent = new Intent(this, InitialSurveyActivity.class);
        startActivity(intent);
        finish();
    }
}
