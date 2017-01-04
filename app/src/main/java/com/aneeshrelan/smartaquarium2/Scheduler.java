package com.aneeshrelan.smartaquarium2;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Scheduler extends AppCompatActivity implements LoadScheduleResponse {

    ArrayList<ScheduleData> dataModel;
    ListView listView;

    private static CustomAdapter adapter;

    ImageView goback;
    ImageView add;

    TextView heading;

    private static Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

        listView = (ListView)findViewById(R.id.scheduleList);
        goback = (ImageView)findViewById(R.id.goback);
        add = (ImageView)findViewById(R.id.addSchedule);
        heading = (TextView)findViewById(R.id.lightScheduleHeading);

        heading.setText(getIntent().getStringExtra("name") + " Scheduler");

        add.setEnabled(true);
        goback.bringToFront();
        add.bringToFront();



        id = getIntent().getIntExtra("id",0);

    }

    @Override
    protected void onResume() {
        super.onResume();


        if(id > 0)
        {
            new LoadSchedule(id,this).execute();
        }

    }

    public void goback(View view) {

        finish();

    }

    @Override
    public void processFinish(JSONObject result) {

        add.setEnabled(true);

      if(result != null)
      {
          ((TextView)findViewById(R.id.noScheduleMsg)).setVisibility(View.GONE);
          dataModel = new ArrayList<>();

          try {
              JSONArray schedules = result.getJSONArray("schedules");


              for(int i = 0; i<schedules.length(); i++)
              {

                  JSONObject item = schedules.getJSONObject(i);
                  dataModel.add(new ScheduleData((i+1) + "", item.getString("onTime"), item.getString("offTime"), item.getString("scheduleID")));
              }

              adapter = new CustomAdapter(id, dataModel, Scheduler.this,this);
              listView.setAdapter(adapter);

          } catch (JSONException e) {
              Log.e(Constants.log, "JSONArray Exception e: " + e.getMessage());
          }
      }
        else
      {
          ((TextView)findViewById(R.id.noScheduleMsg)).setVisibility(View.VISIBLE);
      }

    }

    protected void addSchedule(View v)
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.schedule);

        final Button setOnTime = (Button)dialog.findViewById(R.id.onSetTime);
        final Button setOffTime = (Button)dialog.findViewById(R.id.offSetTime);

        final TextView onTime = (TextView)dialog.findViewById(R.id.onTimeValue);
        final TextView offTime = (TextView)dialog.findViewById(R.id.offTimeValue);

        final Button confirm = (Button)dialog.findViewById(R.id.confirmButton);

        final Calendar dateAndTime = Calendar.getInstance();

        final ProgressBar loader = (ProgressBar)dialog.findViewById(R.id.confirmLoad);
        loader.setVisibility(View.GONE);
        setOnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(Scheduler.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        onTime.setText(new StringBuilder().append(pad(hourOfDay)).append(":").append(pad(minute)));

                    }
                }, dateAndTime.get(Calendar.HOUR_OF_DAY), dateAndTime.get(Calendar.MINUTE),true).show();
            }
        });

        setOffTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(Scheduler.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        offTime.setText(new StringBuilder().append(pad(hourOfDay)).append(":").append(pad(minute)));

                    }
                }, dateAndTime.get(Calendar.HOUR_OF_DAY), dateAndTime.get(Calendar.MINUTE),true).show();
            }
        });


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loader.setVisibility(View.VISIBLE);
                confirm.setEnabled(false);
                setOnTime.setEnabled(false);
                setOffTime.setEnabled(false);
                addScheduleRequest(onTime.getText().toString(), offTime.getText().toString(),dialog,loader);
            }
        });


        dialog.show();

    }


    protected void deleteSchedule(final String scheduleID, final Integer lightID)
    {
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, Constants.url_delSchedule, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if(response.equals(Constants.validToggle))
                {
                    Toast.makeText(Scheduler.this, "Schedule Deleted Successfully", Toast.LENGTH_SHORT).show();

                    if(dataModel.size() == 1)
                        dataModel.remove(0);

                    adapter.notifyDataSetChanged();

                        new LoadSchedule(lightID, Scheduler.this).execute();



                }
                else
                {
                    Toast.makeText(Scheduler.this, "Unable to Delete Schedule", Toast.LENGTH_SHORT).show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(Constants.log, "ID: " + lightID + " Schedule ID: " + scheduleID + " DelSchedule Error: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("id", lightID + "");
                params.put("scheduleID", scheduleID);

                return params;
            }
        };

        queue.add(request);
        queue.start();
    }


    protected void addScheduleRequest(final String onTime, final String offTime, final Dialog dialog, final ProgressBar loader)
    {
        String blank_time = getString(R.string.blank_time);

        if(onTime.equals(blank_time) || offTime.equals(blank_time))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error").setMessage("Choose a time. Time cannot be empty").setPositiveButton("OK",null).setCancelable(false);

            AlertDialog a = builder.create();
            a.show();

            loader.setVisibility(View.GONE);
            ((Button)dialog.findViewById(R.id.confirmButton)).setEnabled(true);
            ((Button)dialog.findViewById(R.id.onSetTime)).setEnabled(true);
            ((Button)dialog.findViewById(R.id.offSetTime)).setEnabled(true);
        }
        else if(onTime.equals(offTime))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error").setMessage("ON and OFF Time cannot be equal").setPositiveButton("OK",null).setCancelable(false);

            AlertDialog a = builder.create();
            a.show();

            loader.setVisibility(View.GONE);
            ((Button)dialog.findViewById(R.id.confirmButton)).setEnabled(true);
            ((Button)dialog.findViewById(R.id.onSetTime)).setEnabled(true);
            ((Button)dialog.findViewById(R.id.offSetTime)).setEnabled(true);
        }
        else
        {
            RequestQueue queue = Volley.newRequestQueue(this);

            StringRequest request = new StringRequest(Request.Method.POST, Constants.url_addSchedule, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject json = new JSONObject(response);

                        if(json.getString("msg").equals(Constants.validToggle))
                        {
                            Toast.makeText(Scheduler.this, "Schedule Added Successfully", Toast.LENGTH_SHORT).show();

                            new LoadSchedule(id, Scheduler.this).execute();
                        }
                        else if(json.getString("msg").equals("duplicate"))
                        {
                            Toast.makeText(Scheduler.this, "Duplicate Schedule Error", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(Scheduler.this, "Unable to Add Schedule", Toast.LENGTH_SHORT).show();
                        }
                        loader.setVisibility(View.GONE);
                        dialog.dismiss();

                    } catch (JSONException e) {
                        Log.e(Constants.log, "ID: " + id + " AddSchedule JSONException e: " + e.getMessage());
                        loader.setVisibility(View.GONE);
                        dialog.dismiss();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(Constants.log, "ID: " + id + " AddSchedule Error e: " + error.getMessage());
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", id + "");
                    params.put("onTime",onTime);
                    params.put("offTime", offTime);

                    return params;
                }
            };

            queue.add(request);
            queue.start();
        }
    }


    protected String pad(int c){
        if(c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }


    private class LoadSchedule extends AsyncTask<String, Void, Void>{

        Integer id;

        ProgressDialog pd;

        Integer flag = 0;


        JSONObject jsonResponse;

        LoadScheduleResponse delegate;

        public LoadSchedule(Integer id, LoadScheduleResponse delegate)
        {
            this.id = id;
            this.delegate = delegate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(Scheduler.this);
            pd.setTitle("Please Wait");
            pd.setMessage("Getting Schedules");
            pd.setCancelable(false);

            pd.show();

        }

        @Override
        protected Void doInBackground(String... params) {

            RequestQueue queue = Volley.newRequestQueue(Scheduler.this);
            RequestFuture<String> future = RequestFuture.newFuture();

            StringRequest request = new StringRequest(Request.Method.POST, Constants.url_getSchedule, future, future){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("id", id + "");

                    return params;
                }
            };

            queue.add(request);

            try {
                String response = future.get();

                JSONObject json = new JSONObject(response);

                if(json.getString("msg").equals(Constants.validToggle))
                {
                    flag = 1;
                    jsonResponse = json;
                    return null;
                }


            } catch (InterruptedException e) {
                Log.e(Constants.log, "GetSchedule (Multiple) InterruptedException e: " + e.getMessage());
            } catch (ExecutionException e) {
                Log.e(Constants.log, "GetSchedule (Multiple) ExecutionException e: " + e.getMessage());
            } catch (JSONException e) {
                Log.e(Constants.log, "GetSchedule (Multiple) JSONException e: " + e.getMessage());
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            pd.dismiss();


            switch (flag)
            {
                case 0:

                    delegate.processFinish(null);

                    break;

                case 1:

                    delegate.processFinish(jsonResponse);

                    break;
            }

        }
    }
}
