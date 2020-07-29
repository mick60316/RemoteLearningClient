package com.example.remotecontroller;

import android.content.Intent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ExtraTools {

    public static final int S1 = 0;
    public static final int S2 = 1;
    public static final int S3 = 2;
    public static final int S4 = 3;
   // public static final int S5 = 4;
    static String getCurrentTime ()
    {
        String timeString = getTime();
        String [] timeStringSplit= timeString.split(":");
        int hours = Integer.valueOf(timeStringSplit[0]);
        if (hours>=12)return timeString+" PM";
        else return timeString +" AM";
    }
    static String getClassTime ()
    {

        String timeString = getTime();
        String [] timeStringSplit= timeString.split(":");
        int hours = Integer.valueOf(timeStringSplit[0]);
        int minute =Integer.valueOf(timeStringSplit[1]);
        minute+=15;
        if (minute>=60)
        {
            minute-=60;
            hours++;
        }

        return String.format("%02d:%02d",hours,minute);
    }
    static String getTime ()
    {
        String timeString = new SimpleDateFormat("HH:mm").format(new Date());
        return timeString;


    }



}
