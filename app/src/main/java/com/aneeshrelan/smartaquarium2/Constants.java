package com.aneeshrelan.smartaquarium2;

/**
 * Created by Aneesh on 30/12/16.
 */

public class Constants {

    public static String PREF_NAME = "SmartAqua2";

    public static String key_domain_local = "localDomain";
    public static String key_domain_remote = "remoteDomain";

    public static String log = "::SMARTAQUA-LOGS::";


    public static String error_noDomain = "No Domain Settings!";


    public static String validConnection = "hello";

    public static String validToggle = "success";

    public static String domain;
    public static String url_toggle;
    public static String url_status;
    public static String url_addSchedule;
    public static String url_delSchedule;
    public static String url_getSchedule;
    public static String url_temp;
    public static String url_coreTemps;


    public static void createURL()
    {
        url_toggle = domain + "toggle";
        url_status = domain + "status";
        url_addSchedule = domain + "addSchedule";
        url_delSchedule = domain + "delSchedule";
        url_getSchedule = domain + "getSchedule";
        url_temp = domain + "temp";
        url_coreTemps = domain + "coreTemps";
    }


}
