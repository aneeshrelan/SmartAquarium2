package com.aneeshrelan.smartaquarium2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TableRow;

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
}
