package com.aneeshrelan.smartaquarium2;

/**
 * Created by Aneesh on 03/01/17.
 */

public class ScheduleData {

    String number;

    String onTime;
    String offTime;

    String scheduleID;

    public ScheduleData(String number, String onTime, String offTime, String scheduleID)
    {
        this.number = number;
        this.onTime = onTime;
        this.offTime = offTime;

        this.scheduleID = scheduleID;
    }

    public String getNumber()
    {
        return number;
    }

    public String getOnTime()
    {
        return onTime;
    }

    public String getOffTime()
    {
        return offTime;
    }

    public String getScheduleID()
    {
        return scheduleID;
    }


}
