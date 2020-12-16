package com.talhajavedmukhtar.ferret;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.talhajavedmukhtar.ferret.Util.DataHandler;

public class NameActivity extends AppCompatActivity {

    private EditText nameSpace;
    private Button enter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        nameSpace = findViewById(R.id.nameSpace);
        enter = findViewById(R.id.enter);

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameSpace.getText().toString();

                if(!name.equals("")){
                    DataHandler dataHandler = new DataHandler(getApplicationContext());
                    dataHandler.pushName(name);
                    finish();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        //we don't allow that up in here boy
    }
}
