package com.aneeshrelan.smartaquarium2;

import android.animation.ObjectAnimator;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements AsyncResponse {

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


        Log.d(Constants.log, "onResume");
        new CheckConnection(this).execute();
    }

    @Override
    public void processFinish() {
        Log.d(Constants.log, "Finished - Domain: " + Constants.domain);
    }


    private class CheckConnection extends AsyncTask<String, Void, Void>
    {

        String localDomain;
        String remoteDomain;

        ImageView connection;

        Integer flag = 0;

        ObjectAnimator anim;

        public AsyncResponse delegate = null;

        public CheckConnection(AsyncResponse delegate)
        {
            this.delegate = delegate;
        }


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

            Log.d(Constants.log, localDomain + " - " + remoteDomain);

        }

        @Override
        protected Void doInBackground(String... params) {

           ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = mgr.getActiveNetworkInfo();

            if(info == null)
            {
                flag = -1;
                return null;
            }

            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            RequestFuture<String> future = RequestFuture.newFuture();

            StringRequest localRequest = new StringRequest(localDomain, future, future);

            queue.add(localRequest);
            if(!localDomain.isEmpty()) {
                try {
                    if (InetAddress.getByName(localDomain.substring(7, localDomain.lastIndexOf(':'))).isReachable(1000)) {
                        String localResponse = future.get(3, TimeUnit.SECONDS);

                        if (localResponse.equals(Constants.validConnection)) {
                            flag = 1;
                            return null;
                        }
                    }
                } catch (IOException e) {
                    Log.e(Constants.log, "IOException e: " + e.getMessage());
                } catch (InterruptedException e) {
                    Log.e(Constants.log, "InterruptedException e: " + e.getMessage());
                } catch (ExecutionException e) {
                    Log.e(Constants.log, "ExecutionException e: " + e.getMessage());
                } catch (TimeoutException e) {
                    Log.e(Constants.log, "TimeoutException e: " + e.getMessage());
                }
            }

            if(!remoteDomain.isEmpty()) {
                queue = Volley.newRequestQueue(MainActivity.this);
                future = RequestFuture.newFuture();

                StringRequest remoteRequest = new StringRequest(remoteDomain, future, future);

                queue.add(remoteRequest);


                try {
                    String remoteResponse = future.get(15, TimeUnit.SECONDS);

                    if (remoteResponse.equals(Constants.validConnection)) {
                        flag = 2;
                        return null;
                    }

                } catch (InterruptedException e) {
                    Log.e(Constants.log, "InterruptedException e: " + e.getMessage());
                } catch (ExecutionException e) {
                    Log.e(Constants.log, "ExecutionException e: " + e.getMessage());
                } catch (TimeoutException e) {
                    Log.e(Constants.log, "TimeoutException e: " + e.getMessage());
                }
            }


            return null;

        }



        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            anim.end();
            anim.cancel();

            Log.d(Constants.log, flag + "");

            switch (flag)
            {
                case -1:
                    connection.setImageResource(R.mipmap.ic_no);
                    Toast.makeText(MainActivity.this, "No Network Connection", Toast.LENGTH_SHORT).show();

                    break;

                case 0:
                    connection.setImageResource(R.mipmap.ic_no);
                    Toast.makeText(MainActivity.this, "Cannot connect to Aquarium", Toast.LENGTH_SHORT).show();

                    break;

                case 1:
                    Constants.domain = localDomain;
                    Constants.createURL();
                    connection.setImageResource(R.mipmap.ic_local);
                    Toast.makeText(MainActivity.this, "Connected in Local Network", Toast.LENGTH_SHORT).show();
                    delegate.processFinish();
                    break;

                case 2:
                    Constants.domain = remoteDomain;
                    Constants.createURL();
                    connection.setImageResource(R.mipmap.ic_internet);
                    Toast.makeText(MainActivity.this, "Connected via Remote Server", Toast.LENGTH_SHORT).show();
                    delegate.processFinish();
                    break;
            }

        }
    }

}
