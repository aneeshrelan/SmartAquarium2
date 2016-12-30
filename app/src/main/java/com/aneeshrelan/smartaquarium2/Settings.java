package com.aneeshrelan.smartaquarium2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ImageView;
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

public class Settings extends AppCompatActivity implements CheckBox.OnCheckedChangeListener, AsyncResponse {

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


        Boolean goback;

        try
        {
            goback = getIntent().getBooleanExtra("goback",false);
            if(goback)
            {
                ImageView gobackImage = (ImageView)findViewById(R.id.goback);
                gobackImage.setVisibility(View.VISIBLE);

                gobackImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Settings.this.finish();
                    }
                });

                SharedPreferences sp = getSharedPreferences(Constants.PREF_NAME, MODE_PRIVATE);

                String sp_localDomain = sp.getString(Constants.key_domain_local,"");
                String sp_remoteDomain = sp.getString(Constants.key_domain_remote,"");

                String localDomain = sp_localDomain.substring(7,sp_localDomain.lastIndexOf(':'));
                String localPort = sp_localDomain.substring(sp_localDomain.lastIndexOf(':')+1,sp_localDomain.lastIndexOf('/'));

                ((EditText)findViewById(R.id.text_localDomain)).setText(localDomain);
                ((EditText)findViewById(R.id.text_localPort)).setText(localPort);

                if(!sp_remoteDomain.isEmpty())
                {
                    ((CheckBox)findViewById(R.id.remoteCheckbox)).setChecked(true);

                    String remoteDomain = sp_remoteDomain.substring(7,sp_remoteDomain.lastIndexOf(':'));
                    String remotePort = sp_remoteDomain.substring(sp_remoteDomain.lastIndexOf(':')+1, sp_remoteDomain.lastIndexOf('/'));

                    ((EditText)findViewById(R.id.text_remoteDomain)).setText(remoteDomain);
                    ((EditText)findViewById(R.id.text_remotePort)).setText(remotePort);
                }



            }
        }
        catch (Exception e)
        {
            Log.e(Constants.log, "Goback Exception e: " + e.getMessage());
        }

    }
    

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked) {
            ((TableRow) findViewById(R.id.heading_remote)).setVisibility(View.VISIBLE);
            ((TableRow) findViewById(R.id.row_remoteDomain)).setVisibility(View.VISIBLE);
            ((TableRow) findViewById(R.id.row_remotePort)).setVisibility(View.VISIBLE);
            ((View)this.findViewById(R.id.separator)).setVisibility(View.VISIBLE);
        }
        else
        {
            ((TableRow) findViewById(R.id.heading_remote)).setVisibility(View.GONE);
            ((TableRow) findViewById(R.id.row_remoteDomain)).setVisibility(View.GONE);
            ((TableRow) findViewById(R.id.row_remotePort)).setVisibility(View.GONE);
            ((View)this.findViewById(R.id.separator)).setVisibility(View.GONE);
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

                   new CheckSettings(this,local_domain,remoteCheckbox.isChecked(),remote_domain).execute();
                }
            }
            else
            {
                //check only local settings
                local_domain = "http://" + local_domain + ":" + local_port + "/";
                remote_domain = "http://" + remote_domain + ":" + remote_port + "/";
                new CheckSettings(this,local_domain,remoteCheckbox.isChecked(),remote_domain).execute();

            }
        }
        
    }

    @Override
    public void processFinish() {

        Intent i = new Intent(Settings.this, MainActivity.class);
        startActivity(i);
        finish();

    }


    private class CheckSettings extends AsyncTask<String, Void, Void>{

        ProgressDialog pd;

        String localDomain;
        String remoteDomain;

        Integer flag = 0;
        
        boolean isRemote;

        public AsyncResponse delegate = null;

        public CheckSettings(AsyncResponse delegate, String localDomain, boolean isRemote, String remoteDomain)
        {

            this.delegate = delegate;

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

            StringRequest request = new StringRequest(localDomain, future, future);

            queue.add(request);

            try {
                String localResponse = future.get(3,TimeUnit.SECONDS);

                if(localResponse.equals(Constants.validConnection))
                    flag++;


            } catch (InterruptedException e) {
                Log.e(Constants.log, "InterruptedException e: " + e.getMessage());
            } catch (ExecutionException e) {
                Log.e(Constants.log, "ExecutionException e: " + e.getMessage());
            } catch (TimeoutException e) {
                Log.e(Constants.log, "TimeoutException e: " + e.getMessage());
            }


            publishProgress();

            queue = Volley.newRequestQueue(Settings.this);
            future = RequestFuture.newFuture();

            if(isRemote)
            {
                request = new StringRequest(remoteDomain, future, future);

                queue.add(request);

                String remoteResponse = null;
                try {
                    remoteResponse = future.get(15, TimeUnit.SECONDS);
                    if(remoteResponse.equals(Constants.validConnection))
                        flag += 2;
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
            pd.dismiss();
            final SharedPreferences.Editor editor = getSharedPreferences(Constants.PREF_NAME, Settings.this.MODE_PRIVATE).edit();
          switch (flag)
          {
              case -1:
                  AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                  builder.setTitle("Error").setMessage("No Network Connection available").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {

                      }
                  });
                  AlertDialog a = builder.create();
                  a.show();
                  break;

              case 0:
                  builder = new AlertDialog.Builder(Settings.this);
                  builder.setTitle("Connection Failed").setMessage("Unable to connect to " + ((isRemote) ? "Local/Remote" : "Local Network") + " server").setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
                      builder.setTitle("Remote Connection Failed").setMessage("Unable to connect to Remote Server").setPositiveButton("Continue with Local Settings", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              //save only local server settings


                              editor.putString(Constants.key_domain_local, localDomain);
                              editor.commit();
                              
                              delegate.processFinish();

                          }
                      }).setNegativeButton("Change Settings", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                              //do nothing, dismiss dialog
                          }
                      });
                      a = builder.create();
                      a.show();
                  }
                  else
                  {


                      editor.putString(Constants.key_domain_local, localDomain);
                      editor.commit();
                      delegate.processFinish();
                  }
                  break;

              case 2:
                  builder = new AlertDialog.Builder(Settings.this);
                  builder.setTitle("Local Connection Failed").setMessage("Unable to connect to Local Network Server").setPositiveButton("Continue with Remote Server", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          //save only remote server settings

                          editor.putString(Constants.key_domain_remote, remoteDomain);
                          editor.commit();
                          delegate.processFinish();
                      }
                  }).setNegativeButton("Change Settings", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          //do nothing, dismiss dialog
                      }
                  });
                  a = builder.create();
                  a.show();
                  break;

              case 3:
                  editor.putString(Constants.key_domain_local,localDomain);
                  editor.putString(Constants.key_domain_remote,remoteDomain);
                  editor.commit();
                  delegate.processFinish();
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
