package com.aneeshrelan.smartaquarium2;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Scheduler extends AppCompatActivity implements LoadScheduleResponse {

    ArrayList<ScheduleData> dataModel;
    ListView listView;

    private static CustomAdapter adapter;

    ImageView goback;

    private static Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);

        listView = (ListView)findViewById(R.id.scheduleList);
        goback = (ImageView)findViewById(R.id.goback);
        goback.bringToFront();

        dataModel = new ArrayList<>();

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

      if(result != null)
      {
          try {
              JSONArray schedules = result.getJSONArray("schedules");

              for(int i = 0; i<schedules.length(); i++)
              {
                  JSONObject item = schedules.getJSONObject(i);
                  dataModel.add(new ScheduleData((i+1) + "", item.getString("onTime"), item.getString("offTime"), item.getString("scheduleID")));
              }

              adapter = new CustomAdapter(dataModel, getApplicationContext());
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
