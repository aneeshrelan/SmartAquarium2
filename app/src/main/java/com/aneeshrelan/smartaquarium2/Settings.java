package com.aneeshrelan.smartaquarium2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Settings extends AppCompatActivity implements CheckBox.OnCheckedChangeListener {

    TextView localDomain;
    TextView localPort;

    CheckBox remoteCheckbox;

    TextView remoteDomain;
    TextView remotePort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        remoteCheckbox = (CheckBox)findViewById(R.id.remoteCheckbox);
        localDomain = (TextView)findViewById(R.id.text_localDomain);
        localPort = (TextView)findViewById(R.id.text_localPort);
        remoteDomain = (TextView)findViewById(R.id.text_remoteDomain);
        remotePort = (TextView)findViewById(R.id.text_remotePort);

        remoteCheckbox.setOnCheckedChangeListener(this);
        
    }
    

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked) {
            ((TableRow) findViewById(R.id.heading_remote)).setVisibility(View.VISIBLE);
            ((TableRow) findViewById(R.id.row_remoteDomain)).setVisibility(View.VISIBLE);
            ((TableRow) findViewById(R.id.row_remotePort)).setVisibility(View.VISIBLE);
        }
        else
        {
            ((TableRow) findViewById(R.id.heading_remote)).setVisibility(View.GONE);
            ((TableRow) findViewById(R.id.row_remoteDomain)).setVisibility(View.GONE);
            ((TableRow) findViewById(R.id.row_remotePort)).setVisibility(View.GONE);
        }
    }

    public void saveSettings(View view) {

        String local_domain = localDomain.getText().toString();
        String local_port = localPort.getText().toString();

        String remote_domain = "";
        String remote_port = "";

        if(local_domain.isEmpty() || local_port.isEmpty())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error").setMessage("Local Network settings cannot be empty").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog a = builder.create();
            a.show();
        }
        else
        {
            if(remoteCheckbox.isChecked())
            {
                remote_domain = remoteDomain.getText().toString();
                remote_port = remotePort.getText().toString();

                if(remote_domain.isEmpty() || remote_port.isEmpty())
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Error").setMessage("Remote Server Network settings cannot be empty").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog a = builder.create();
                    a.show();
                }
                else
                {
                    //check local and remote settings
                    local_domain = "http://" + local_domain + ":" + local_port + "/";
                    remote_domain = "http://" + remote_domain + ":" + remote_port + "/";

                   new CheckSettings(local_domain,remoteCheckbox.isChecked(),remote_domain).execute();
                }
            }
            else
            {
                //check only local settings

                new CheckSettings(local_domain,remoteCheckbox.isChecked(),remote_domain).execute();

            }
        }
        
    }


    private class CheckSettings extends AsyncTask<String, Void, Void>{

        ProgressDialog pd;

        String localDomain;
        String remoteDomain;

        Integer flag = 0;
        
        boolean isRemote;

        public CheckSettings(String localDomain, boolean isRemote, String remoteDomain)
        {
            this.localDomain = localDomain;

            this.isRemote = isRemote;
            
            if(isRemote)
            {
                this.remoteDomain = remoteDomain;
            }
            else
                this.remoteDomain = "";
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Settings.this);
            pd.setTitle("Please Wait");
            pd.setMessage("Checking Local Network Connection");
            pd.setCancelable(false);
            pd.show();
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

            RequestQueue queue = Volley.newRequestQueue(Settings.this);
            RequestFuture<String> future = RequestFuture.newFuture();
            
            StringRequest request = new StringRequest(Request.Method.GET, localDomain, future,future);
            queue.add(request);
            try {
                String localResponse = future.get(5, TimeUnit.SECONDS);
                
                if(localResponse.equals(Constants.validConnection))
                    flag++;
                
                publishProgress();
                
                queue = Volley.newRequestQueue(Settings.this);
                future = RequestFuture.newFuture();
                
                request = new StringRequest(Request.Method.GET, remoteDomain, future,future);
                
                queue.add(request);
                
                String remoteResponse = future.get(15,TimeUnit.SECONDS);
                
                if(remoteResponse.equals(Constants.validConnection))
                    flag += 2;
                
                
                
            } catch (InterruptedException e) {
                Log.e(Constants.log, "InterruptedException e: " + e.getMessage());
            } catch (ExecutionException e) {
                Log.e(Constants.log, "ExecutionException e: " + e.getMessage());
            } catch (TimeoutException e) {
                Log.e(Constants.log, "TimeoutException e: " + e.getMessage());
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();
            switch (flag)
            {
                case -1:
                    AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                    builder.setTitle("Error").setMessage("No network connection").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog a = builder.create();
                    a.show();

                    break;
                
                
                case 0:
                    builder = new AlertDialog.Builder(Settings.this);
                    builder.setTitle("Connection Error").setMessage("Cannot connect to local/remote network. Please check settings and try again").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    a = builder.create();
                    a.show();
                    
                    break;
                
                case 1:
                    
                    if(isRemote)
                    {
                        builder = new AlertDialog.Builder(Settings.this);
                        builder.setTitle("Remote Connection Error").setMessage("Cannot connect to Remote server").setPositiveButton("Continue with Local Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton("Change Settings", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                
                            }
                        });
                        a = builder.create();
                        a.show();
                        
                    }
                    else
                    {
                        Toast.makeText(Settings.this, "Local Correct", Toast.LENGTH_SHORT).show();
                    }
                    
                    break;
                    
                case 2:
                    builder = new AlertDialog.Builder(Settings.this);
                    builder.setTitle("Local Network Connection Error").setMessage("Cannot connect through Local Network").setPositiveButton("Continue with Remote Server Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).setNegativeButton("Change Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    a = builder.create();
                    a.show();
                    
                    break;
                
                case 3:

                    Toast.makeText(Settings.this, "Local & Remote Correct", Toast.LENGTH_SHORT).show();
                    
                    break;
                    
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            pd.setMessage("Checking Remote Server Connection");
        }
    }


}
