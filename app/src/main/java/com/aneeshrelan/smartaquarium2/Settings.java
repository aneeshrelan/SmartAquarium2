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
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Text;

import java.net.InetAddress;

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
        
        ((TableRow)findViewById(R.id.heading_remote)).setVisibility(View.VISIBLE);
        ((TableRow)findViewById(R.id.row_remoteDomain)).setVisibility(View.VISIBLE);
        ((TableRow)findViewById(R.id.row_remotePort)).setVisibility(View.VISIBLE);
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

                    new CheckSettings(getApplicationContext(),local_domain,remoteCheckbox.isChecked(), remote_domain).execute();
                }
            }
            else
            {
                //check only local settings
                new CheckSettings(getApplicationContext(),local_domain,remoteCheckbox.isChecked(), remote_domain).execute();
            }
        }
        
    }


    private class CheckSettings extends AsyncTask<String, Void, Void>{

        Context context;
        String localDomain;
        String remoteDomain;

        ProgressDialog pd;

        Integer flag = 0;

        public CheckSettings(Context context, String localDomain, boolean isRemote, String remoteDomain)
        {
            this.context = context;
            this.localDomain = localDomain;

            if(isRemote)
                this.remoteDomain = remoteDomain;
            else
                this.remoteDomain = "";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(context);
            pd.setMessage("Checking Local Network Connection");
            pd.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            RequestQueue queue = Volley.newRequestQueue(context);
            RequestFuture<String> future = RequestFuture.newFuture();

            ConnectivityManager mgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = mgr.getActiveNetworkInfo();

            if(activeNetworkInfo == null)
            {
                flag = -1;
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            switch (flag)
            {
                case -1:
                    Toast.makeText(context, "No Network Connection", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


}
