package com.aneeshrelan.smartaquarium2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);

        if(sp.getString(Constants.key_domain_local,"").isEmpty() && sp.getString(Constants.key_domain_remote,"").isEmpty())
        {
            Intent i = new Intent(MainActivity.this, Settings.class);
            startActivity(i);
            finish();
        }
    }
}
