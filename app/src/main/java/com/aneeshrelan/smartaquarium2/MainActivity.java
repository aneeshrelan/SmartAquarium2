package com.aneeshrelan.smartaquarium2;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;

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

    @Override
    protected void onResume() {
        super.onResume();

    }

    private class CheckConnection extends AsyncTask<String, Void, Void>
    {

        String localDomain;
        String remoteDomain;

        ImageView connection;

        Integer flag = 0;

        ObjectAnimator anim;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MainActivity.this.MODE_PRIVATE);

            localDomain = sp.getString(Constants.key_domain_local,"");
            remoteDomain = sp.getString(Constants.key_domain_remote,"");

            connection = (ImageView)MainActivity.this.findViewById(R.id.connection);
            connection.setImageResource(R.mipmap.ic_connecting);

            anim = ObjectAnimator.ofFloat(connection,"rotationY",0.0f, 360f);
            anim.setDuration(1000);
            anim.setRepeatCount(Animation.INFINITE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.start();



        }

        @Override
        protected Void doInBackground(String... params) {

            return null;
        }
    }

}
