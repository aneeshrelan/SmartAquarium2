package com.aneeshrelan.smartaquarium2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import org.w3c.dom.Text;

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

                }
            }
            else
            {
                //check only local settings

            }
        }
        
    }


    private class CheckSettings extends AsyncTask<String, Void, Void>{

        Context context;
        String localDomain;
        String remoteDomain;

        ProgressDialog pd;

        public CheckSettings(Context context, String localDomain, String remoteDomain)
        {
            this.context = context;
            this.localDomain = localDomain;
            this.remoteDomain = remoteDomain;
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



            return null;
        }
    }


}
