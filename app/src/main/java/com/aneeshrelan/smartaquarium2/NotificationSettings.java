package com.aneeshrelan.smartaquarium2;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;

public class NotificationSettings extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    CompoundButton notificationSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        notificationSwitch = (CompoundButton)findViewById(R.id.notificationSwitch);
        notificationSwitch.setOnCheckedChangeListener(this);

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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error").setTitle("Values cannot be empty").setPositiveButton("OK",null).setCancelable(false);
            AlertDialog a = builder.create();
            a.show();
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

        Toast.makeText(this, "Applying", Toast.LENGTH_SHORT).show();

    }
}
