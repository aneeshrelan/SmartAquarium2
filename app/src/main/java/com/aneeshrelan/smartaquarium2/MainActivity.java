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
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements AsyncResponse, CompoundButton.OnCheckedChangeListener {

    CompoundButton filterSwitch, pumpSwitch, daySwitch, nightSwitch;

    Map<Integer,String> items = new HashMap<Integer, String>(){{
        put(1, "Water Filter");
        put(2, "Air Pump");
        put(3, "Day Light");
        put(4, "Night Light");
    }};

    Map<String, CompoundButton> switches;

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
        else
        {
            filterSwitch = (CompoundButton)findViewById(R.id.switch1);
            pumpSwitch = (CompoundButton)findViewById(R.id.switch2);
            daySwitch = (CompoundButton)findViewById(R.id.switch3);
            nightSwitch = (CompoundButton)findViewById(R.id.switch4);

            filterSwitch.setOnCheckedChangeListener(this);
            pumpSwitch.setOnCheckedChangeListener(this);
            daySwitch.setOnCheckedChangeListener(this);
            nightSwitch.setOnCheckedChangeListener(this);

            switches = new HashMap<String, CompoundButton>(){{
                put("1", filterSwitch);
                put("2", pumpSwitch);
                put("3", daySwitch);
                put("4", nightSwitch);
            }};
        }

    }

    @Override
    protected void onResume() {
        super.onResume();


        Log.d(Constants.log, "onResume");
        prepareForRefresh();
        new CheckConnection(this).execute();
    }

    protected void waterTemp()
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Constants.url_temp, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ((TextView)findViewById(R.id.waterTemp)).setEnabled(true);
                ((TextView)findViewById(R.id.waterTemp)).setText(response.trim() + " °C");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constants.log, "Water Temp Error: " + error.getMessage());
            }
        });

        queue.add(request);
        queue.start();
    }

    protected void coreTemps()
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Constants.url_coreTemps, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ((TextView)findViewById(R.id.gpuTemp)).setEnabled(true);
                ((TextView)findViewById(R.id.cpuTemp)).setEnabled(true);

                ((TextView)findViewById(R.id.gpuTemp)).setText(response.split(",")[0].trim() + "  °C");
                ((TextView)findViewById(R.id.cpuTemp)).setText(response.split(",")[1].trim() + "  °C");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constants.log, "CoreTemps Error: " + error.getMessage());
            }
        });

        queue.add(request);
        queue.start();
    }

    protected void updateSwitch(String id, String value)
    {
        CompoundButton sw = switches.get(id);
        sw.setOnCheckedChangeListener(null);
        sw.setEnabled(true);
        sw.setChecked((value.equals("0") ? true : false));
        sw.setOnCheckedChangeListener(this);
    }


    protected void switchStatus()
    {
      RequestQueue queue = Volley.newRequestQueue(this);

        final JsonArrayRequest request = new JsonArrayRequest(Constants.url_status, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                for(int i = 0; i<response.length(); i++)
                {
                    JSONObject obj = null;
                    try {
                        obj = response.getJSONObject(i);
                        updateSwitch(obj.getString("id"), obj.getString("value"));
                    } catch (JSONException e) {
                        Log.e(Constants.log, "Switch Status JSONException e: " + e.getMessage());
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constants.log, "Switch Status Error: " + error.getMessage());
            }
        });

        queue.add(request);
        queue.start();
    }

    protected void loadData()
    {
        waterTemp();
        coreTemps();
        switchStatus();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId())
        {
            case R.id.switch1:

                break;

            case R.id.switch2:

                break;

            case R.id.switch3:

                break;

            case R.id.switch4:

                break;
        }

    }

    @Override
    public void processFinish() {

        if(!Constants.domain.isEmpty())
        {
            toggleScheduleButtons(true);
            loadData();
        }
        else
        {
            Log.e(Constants.log, "Empty Domain");
        }

    }

    protected void disableSwitches()
    {
        for (CompoundButton sw : switches.values())
            sw.setEnabled(false);
    }

    protected void zeroTemps()
    {
        ((TextView)findViewById(R.id.waterTemp)).setEnabled(false);
        ((TextView)findViewById(R.id.gpuTemp)).setEnabled(false);
        ((TextView)findViewById(R.id.cpuTemp)).setEnabled(false);
    }

    protected void toggleScheduleButtons(Boolean state)
    {
        ((Button)findViewById(R.id.schedule1)).setEnabled(state);
        ((Button)findViewById(R.id.schedule2)).setEnabled(state);
        ((Button)findViewById(R.id.schedule3)).setEnabled(state);
        ((Button)findViewById(R.id.schedule4)).setEnabled(state);
    }


    protected void prepareForRefresh()
    {
        disableSwitches();
        zeroTemps();
        toggleScheduleButtons(false);
    }


    public void showOptions(View view) {

        PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.options, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId())
                {
                    case R.id.refresh:
                        Log.d(Constants.log, "Refresh");
                        prepareForRefresh();
                        new CheckConnection(MainActivity.this).execute();

                        break;

                    case R.id.settings:
                        Log.d(Constants.log, "Settings");
                        Intent i = new Intent(MainActivity.this, Settings.class);
                        i.putExtra("goback",true);
                        startActivity(i);
                }
                return true;
            }
        });

        popupMenu.show();

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
