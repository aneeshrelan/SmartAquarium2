package com.aneeshrelan.smartaquarium2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Aneesh on 03/01/17.
 */

class CustomAdapter extends ArrayAdapter<ScheduleData> {

   private ArrayList<ScheduleData> dataSet;
    Context context;

    private Integer lightID;


    private static class ViewHolder{
        TextView scheduleHeading;
        TextView onTime;
        TextView offTime;
        TextView scheduleID;

        
        Button modify;
        Button delete;

        ProgressBar deleteSpinner;

    }


    public CustomAdapter(Integer lightID, ArrayList<ScheduleData> data, Context context)
    {
        super(context, R.layout.custom_row, data);
        this.lightID = lightID;
        this.dataSet = data;
        this.context = context;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ScheduleData dataModel = getItem(position);
        final ViewHolder viewHolder;

        final View result;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_row, parent, false);

            viewHolder.scheduleHeading = (TextView)convertView.findViewById(R.id.scheduleHeading);
            viewHolder.onTime = (TextView)convertView.findViewById(R.id.scheduleOn);
            viewHolder.offTime = (TextView)convertView.findViewById(R.id.scheduleOff);
            viewHolder.scheduleID = (TextView)convertView.findViewById(R.id.scheduleID);
            viewHolder.modify = (Button)convertView.findViewById(R.id.modifySchedule);
            viewHolder.delete = (Button)convertView.findViewById(R.id.deleteSchedule); 
            viewHolder.deleteSpinner = (ProgressBar)convertView.findViewById(R.id.deleteLoad);

            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }


        lastPosition = position;

        viewHolder.scheduleHeading.setText("Schedule #" + dataModel.getNumber());
        viewHolder.onTime.setText(dataModel.getOnTime());
        viewHolder.offTime.setText(dataModel.getOffTime());
        viewHolder.scheduleID.setText(dataModel.getScheduleID());


        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String scheduleID = dataModel.getScheduleID();
                final Integer lightID = CustomAdapter.this.lightID;

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Confirm").setMessage("Are you sure you want to delete this schedule?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        viewHolder.deleteSpinner.setVisibility(View.VISIBLE);
                        deleteSchedule(scheduleID, lightID,viewHolder.deleteSpinner);
                        
                    }
                }).setNegativeButton("No",null).setCancelable(false);

                AlertDialog a = builder.create();
                a.show();
            }
        });
        

        return convertView;

    }

    private void deleteSchedule(String scheduleID, Integer lightID, ProgressBar spinner)
    {

    }


}
