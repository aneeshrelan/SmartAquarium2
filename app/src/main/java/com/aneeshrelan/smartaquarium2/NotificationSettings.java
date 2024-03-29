package com.aneeshrelan.smartaquarium2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class NotificationSettings extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, NotificationResponse, TextWatcher {

    CompoundButton notificationSwitch;


    Button apply;

    EditText text_water_min;
    EditText text_water_max;
    EditText text_system_min;
    EditText text_system_max;

    Boolean available = false;

    String w_min;
    String w_max;
    String s_min;
    String s_max;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        notificationSwitch = (CompoundButton)findViewById(R.id.notificationSwitch);
        notificationSwitch.setOnCheckedChangeListener(this);

        apply = (Button)findViewById(R.id.apply);

         text_water_min = (EditText)findViewById(R.id.water_min);
         text_water_max = (EditText)findViewById(R.id.water_max);
         text_system_min = (EditText)findViewById(R.id.system_min);
         text_system_max = (EditText)findViewById(R.id.system_max);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!available)
        {
            notificationSwitch.setChecked(false);
        }
        else
        {
            notificationSwitch.setChecked(true);
        }
        new LoadNotification(Constants.url_getNotification, NotificationSettings.this).execute();
    }

    public void goback(View view) {

        NotificationSettings.this.finish();

    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {

        if(isChecked)
        {
            if(!available)
            {
                text_water_min.setText("");
                text_water_max.setText("");
                text_system_min.setText("");
                text_system_max.setText("");
            }
            toggleRows(View.VISIBLE);
        }
        else
        {
            if(available)
            {
                new AlertDialog.Builder(this).setTitle("Confirm").setMessage("Are you sure you want to disable temperature alerts").setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        buttonView.setChecked(!isChecked);
                    }
                }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //delete notification here

                        toggleRows(View.GONE);

                        ((ProgressBar)findViewById(R.id.notificationProgress)).setVisibility(View.VISIBLE);
                        deleteNotification();

                        //refresh through loadNotification

                    }
                }).setCancelable(false).create().show();

                return;

            }

            toggleRows(View.GONE);
        }


    }

    protected void deleteNotification()
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, Constants.url_deleteNotification, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.equals(Constants.validToggle))
                {
                    available = false;
                    ((ProgressBar)findViewById(R.id.notificationProgress)).setVisibility(View.GONE);
                    Toast.makeText(NotificationSettings.this, "Temperature Alerts Disabled", Toast.LENGTH_SHORT).show();

                    new LoadNotification(Constants.url_getNotification,NotificationSettings.this).execute();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constants.log, "Delete Notification Error: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("confirm","yes");

                return params;
            }
        };

        queue.add(request);
        queue.start();

    }

    protected void toggleRows(int visibility)
    {
        ((TableRow)findViewById(R.id.row_water_heading)).setVisibility(visibility);
        ((TableRow)findViewById(R.id.row_water_min)).setVisibility(visibility);
        ((TableRow)findViewById(R.id.row_water_max)).setVisibility(visibility);
        ((TableRow)findViewById(R.id.row_system_heading)).setVisibility(visibility);
        ((TableRow)findViewById(R.id.row_system_min)).setVisibility(visibility);
        ((TableRow)findViewById(R.id.row_system_max)).setVisibility(visibility);
        ((TableRow)findViewById(R.id.row_save)).setVisibility(visibility);
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

        Integer i_w_min = Integer.parseInt(water_min);
        Integer i_w_max = Integer.parseInt(water_max);
        Integer i_s_min = Integer.parseInt(system_min);
        Integer i_s_max = Integer.parseInt(system_max);

        if((i_w_min > i_w_max) || (i_s_min > i_s_max))
        {
            new AlertDialog.Builder(this).setTitle("Error").setMessage("Minimum Value cannot be greater than Maximum Value").setPositiveButton("OK",null).setCancelable(false).create().show();
            return;
        }


        if(available)
        {
            //edit settings

            if(water_min.equals(w_min) && water_max.equals(w_max) && system_min.equals(s_min) && system_max.equals(s_max))
            {
                new AlertDialog.Builder(this).setTitle("Error").setMessage("Values same as previous. Modify the values to edit settings").setPositiveButton("OK",null).setCancelable(false).create().show();;
                return;
            }


            ((ProgressBar)findViewById(R.id.notificationProgress)).setVisibility(View.VISIBLE);

            editNotification(water_min,water_max,system_min,system_max);
        }
        else
        {
            //new notification settings
            ((ProgressBar)findViewById(R.id.notificationProgress)).setVisibility(View.VISIBLE);
            addNotification(water_min,water_max,system_min,system_max);
        }

    }

    protected void addNotification(final String water_min,final String water_max, final String system_min, final String system_max)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, Constants.url_setNotification, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.equals(Constants.validToggle))
                {
                    ((ProgressBar)findViewById(R.id.notificationProgress)).setVisibility(View.GONE);
                    Toast.makeText(NotificationSettings.this, "Temperature Alerts Set", Toast.LENGTH_SHORT).show();
                    new LoadNotification(Constants.url_getNotification,NotificationSettings.this).execute();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constants.log, "Add Notification Error: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("water_min",water_min);
                params.put("water_max",water_max);
                params.put("system_min",system_min);
                params.put("system_max",system_max);

                return params;
            }
        };

        queue.add(request);
        queue.start();
    }

    protected void editNotification(final String water_min, final String water_max, final String system_min, final String system_max)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, Constants.url_editNotification, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.equals(Constants.validToggle))
                {
                    ((ProgressBar)findViewById(R.id.notificationProgress)).setVisibility(View.GONE);
                    Toast.makeText(NotificationSettings.this, "Notification Settings Updated", Toast.LENGTH_SHORT).show();

                    new LoadNotification(Constants.url_getNotification, NotificationSettings.this).execute();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constants.log, "Edit Notification Error: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("water_min",water_min);
                params.put("water_max",water_max);
                params.put("system_min",system_min);
                params.put("system_max",system_max);

                return params;
            }
        };

        queue.add(request);
        queue.start();
    }

    @Override
    public void processFinish(JSONObject result) {

        if(result != null)
        {
            //settings available
            notificationSwitch.setChecked(true);
            available = true;
            try {
                result = result.getJSONObject("result");

                 w_min = result.getString("water_min").trim();
                 w_max = result.getString("water_max").trim();
                 s_min = result.getString("system_min").trim();
                 s_max = result.getString("system_max").trim();


                text_water_min.setText(w_min);
                text_water_max.setText(w_max);
                text_system_min.setText(s_min);
                text_system_max.setText(s_max);

                text_water_min.addTextChangedListener(this);
                text_water_max.addTextChangedListener(this);
                text_system_min.addTextChangedListener(this);
                text_system_max.addTextChangedListener(this);

                apply.setText("Edit");
                apply.setEnabled(false);

            } catch (JSONException e) {
                Log.e(Constants.log, "ProcessFinish JSONArrayException e: "+ e.getMessage());
            }


        }


    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

        apply.setEnabled(true);

    }

    @Override
    public void afterTextChanged(Editable s) {


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
