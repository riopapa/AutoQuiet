package com.urrecliner.letmequiet.utility;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;

import com.urrecliner.letmequiet.models.Agenda;

import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.urrecliner.letmequiet.Vars.agendas;
import static com.urrecliner.letmequiet.Vars.utils;

public class GetAgenda {

    public static void get (Context context) {
        Long oldTime = System.currentTimeMillis() - (long) 60*24*60*60*1000;
        Long newTime = oldTime + (long) 90*24*60*60*1000;
        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + oldTime + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + newTime + " ))";
//        Log.e("selection","----- selection ----");
        Log.e("selection",selection);
        Cursor cursor = context.getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                new String[]{"_id",     // 0
                         "title",       // 1
                        "description",  // 2
                        "dtstart",      // 3
                        "dtend",        // 4
                        "eventLocation", // 5
                        CalendarContract.Events.ALL_DAY,        // 6    1, 0
                        CalendarContract.Events.CALENDAR_DISPLAY_NAME, // 7 rio papa
                        CalendarContract.Events.CALENDAR_TIME_ZONE, // 8    Asia/Seoul
                        CalendarContract.Events.RRULE,   // 9
                        CalendarContract.Events.DURATION
//                        CalendarContract.Events.LAST_DATE
//                        CalendarContract.Events.SYNC_DATA9, // 10
//                        CalendarContract.Events.ACCOUNT_TYPE, //   com.google
                        },
                        selection, null, null); // CalendarContract.Events.DTSTART + " ASC");

        cursor.moveToFirst();
        // fetching calendars name

        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss ");
        int count = cursor.getCount();
        Log.e("Total","Cursor count="+count);
        while (cursor.moveToNext()) {
            Integer eID = cursor.getInt(0);
            String eTitle = cursor.getString(1);
            if (eTitle == null)
                continue;
            String eDesc = cursor.getString(2);
            Long eStart = cursor.getLong(3);
            Long eFinish = cursor.getLong(4);
            String eLocation = cursor.getString(5);
            boolean eAllay = (cursor.getInt(6) == 1);
            String eCalName = cursor.getString(7);
            String eZone = cursor.getString(8);
            String eRule = cursor.getString(9);
            String eDuration = cursor.getString(10);
            if (eRule != null)
                utils.log("RULE",eRule+" finish="+eFinish);
            Log.w("id="+eID,"title="+eTitle
                    +", time="+sdf.format(eStart)+"~"+sdf.format(eFinish)
                    +", loc="+eLocation+", all="+eAllay +", zone="+eZone +", disp="+eCalName +", rule="+eRule
                    +", dur="+eDuration
            );
            Agenda agenda=new Agenda();
            agenda.id       = eID;
            agenda.title    = eTitle;
            agenda.desc     = eDesc;
            agenda.startTime =eStart;
            agenda.finishTime =eFinish;
            agenda.location=eLocation;
            agenda.isAllDay =eAllay;
            agenda.calName = eCalName;
            agenda.timeZone =eZone;
            agenda.rule = eRule;
            agenda.quietStart = 1;
            agenda.quietFinish = 1;
            agendas.add(agenda);
        }
    }
}