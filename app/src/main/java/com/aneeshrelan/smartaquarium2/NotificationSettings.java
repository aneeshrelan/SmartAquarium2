package com.aneeshrelan.smartaquarium2;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class NotificationSettings extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, NotificationResponse {

    CompoundButton notificationSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        notificationSwitch = (CompoundButton)findViewById(R.id.notificationSwitch);
        notificationSwitch.setOnCheckedChangeListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void goback(View view) {

        NotificationSettings.this.finish();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if(isChecked)
        {
            ((TableRow)findViewById(R.id.row_water_heading)).setVisibility(View.VISIBLE);
            ((TableRow)findViewById(R.id.row_water_min)).setVisibility(View.VISIBLE);
            ((TableRow)findViewById(R.id.row_water_max)).setVisibility(View.VISIBLE);
            ((TableRow)findViewById(R.id.row_system_heading)).setVisibility(View.VISIBLE);
            ((TableRow)findViewById(R.id.row_system_min)).setVisibility(View.VISIBLE);
            ((TableRow)findViewById(R.id.row_system_max)).setVisibility(View.VISIBLE);
            ((TableRow)findViewById(R.id.row_save)).setVisibility(View.VISIBLE);
        }
        else
        {
            ((TableRow)findViewById(R.id.row_water_heading)).setVisibility(View.GONE);
            ((TableRow)findViewById(R.id.row_water_min)).setVisibility(View.GONE);
            ((TableRow)findViewById(R.id.row_water_max)).setVisibility(View.GONE);
            ((TableRow)findViewById(R.id.row_system_heading)).setVisibility(View.GONE);
            ((TableRow)findViewById(R.id.row_system_min)).setVisibility(View.GONE);
            ((TableRow)findViewById(R.id.row_system_max)).setVisibility(View.GONE);
            ((TableRow)findViewById(R.id.row_save)).setVisibility(View.GONE);
        }


    }

    public void applySettings(View view) {

        EditText text_water_min = (EditText)findViewById(R.id.water_min);
        EditText text_water_max = (EditText)findViewById(R.id.water_max);
        EditText text_system_min = (EditText)findViewById(R.id.system_min);
        EditText text_system_max = (EditText)findViewById(R.id.system_max);
        
        
        String water_min = text_water_min.getText().toString();
        String water_max = text_water_max.getText().toString();
        String system_min = text_system_min.getText().toString();
        String system_max = text_system_max.getText().toString();
        
        
        if(water_min.isEmpty() || water_max.isEmpty() || system_min.isEmpty() || system_max.isEmpty())
        {
            new AlertDialog.Builder(this).setTitle("Error").setMessage("Values cannot be empty").setPositiveButton("OK",null).setCancelable(false).create().show();
            return;
        }

        Integer w_min = Integer.parseInt(water_min);
        Integer w_max = Integer.parseInt(water_max);
        Integer s_min = Integer.parseInt(system_min);
        Integer s_max = Integer.parseInt(system_max);

        if((w_min > w_max) || (s_min > s_max))
        {
            new AlertDialog.Builder(this).setTitle("Error").setMessage("Minimum Value cannot be greater than Maximum Value").setPositiveButton("OK",null).setCancelable(false).create().show();
            return;
        }


        

    }

    @Override
    public void processFinish(JSONObject result) {

        if(result != null)
        {
            //settings available
            notificationSwitch.setChecked(true);
        }


    }

    private class LoadNotification extends AsyncTask<String, Void, Void>{

        String getNotification;

        public NotificationResponse delegate = null;

        ProgressDialog pd;

        JSONObject json = null;

        public LoadNotification(String getNotification, NotificationResponse delegate)
        {
            this.getNotification = getNotification;
            this.delegate = delegate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(NotificationSettings.this);
            pd.setTitle("Please Wait");
            pd.setMessage("Getting Notification Settings");
            pd.setCancelable(false);

            pd.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            RequestQueue queue = Volley.newRequestQueue(NotificationSettings.this);
            RequestFuture<String> future = RequestFuture.newFuture();

            StringRequest request = new StringRequest(Request.Method.GET, getNotification, future, future);

            queue.add(request);

            try {
                String response = future.get();

                if(response.equals("fail")) {
                    json = null;
                    return null;
                }

                json = new JSONObject(response);

                if(json.getString("msg").equals(Constants.validToggle))
                {
                    return null;
                }
                else
                {
                    json = null;
                }

            } catch (InterruptedException e) {
                Log.e(Constants.log, "GetNotification InterruptedException e: " + e.getMessage());
            } catch (ExecutionException e) {
                Log.e(Constants.log, "GetNotification ExecutionException e: " + e.getMessage());
            } catch (JSONException e) {
                Log.e(Constants.log, "GetNotification JSONException e: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();

            delegate.processFinish(json);

        }
    }
}
