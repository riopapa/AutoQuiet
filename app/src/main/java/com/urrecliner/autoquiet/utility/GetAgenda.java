package com.urrecliner.autoquiet.utility;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;

import com.urrecliner.autoquiet.models.GCal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.urrecliner.autoquiet.Vars.gCals;
import static com.urrecliner.autoquiet.Vars.utils;
import static com.urrecliner.autoquiet.Vars.ONE_DAY;

public class GetAgenda {


    public static void get (Context context) {

        // 반복 설정이 있는 것을 고려 calendar 를 360일전 부터 다음 달 까지 읽어 옴
        long TimeRangeFrom = System.currentTimeMillis() - 360*ONE_DAY;
        long TimeRangeTo = System.currentTimeMillis() + (long) 30*ONE_DAY;
        long TimeTODAY = System.currentTimeMillis() - 4*60*60*1000;

        String selection = "(( " + CalendarContract.Events.DTSTART + " >= " + TimeRangeFrom + " ) AND ( " + CalendarContract.Events.DTSTART + " <= " + TimeRangeTo + " ))";
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

        int count = cursor.getCount();
        Log.e("Total","Cursor count="+count);
        while (cursor.moveToNext()) {
            int eID = cursor.getInt(0);
            String eTitle = cursor.getString(1);
            if (eTitle == null)
                continue;
            String eDesc = cursor.getString(2);
            long eStart = cursor.getLong(3);
            long eFinish = cursor.getLong(4);
            String eLocation = cursor.getString(5);
            boolean eAllay = (cursor.getInt(6) == 1);
            String eCalName = cursor.getString(7);
            String eZone = cursor.getString(8);
            String eRule = cursor.getString(9);
            String eDuration = cursor.getString(10);
//            Log.w("id="+eID,"title="+eTitle
//                    +", time="+sdf.format(eStart)+"~"+sdf.format(eFinish)
//                    +", loc="+eLocation+", all="+eAllay +", zone="+eZone +", disp="+eCalName +", rule="+eRule
//                    +", dur="+eDuration
//            );
            if (eRule == null) {
                if (eStart > TimeTODAY && eStart < TimeRangeTo) {
                    GCal gCal =new GCal();
                    gCal.id       = eID;
                    gCal.title    = eTitle;
                    gCal.desc     = eDesc;
                    gCal.startTime =eStart;
                    gCal.finishTime =eFinish;
                    gCal.location=eLocation;
                    gCal.isAllDay =eAllay;
                    gCal.calName = eCalName;
                    gCal.timeZone =eZone;
                    gCal.rule = eRule;
                    gCal.repeat = false;
                    gCals.add(gCal);
                }
            }
            else {
                ArrayList<sfTime> sfTimes = calcRepeat (eTitle, eStart, eRule, eDuration,
                        TimeTODAY, TimeRangeTo);
                for (int i = 0; i < sfTimes.size() ; i++) {
                    if (sfTimes.get(i).sTime > TimeTODAY && sfTimes.get(i).sTime < TimeRangeTo) {
                        GCal g = new GCal();
                        g.id = eID;
                        g.title = eTitle;
                        g.desc = eDesc;
                        g.startTime = sfTimes.get(i).sTime;
                        g.finishTime = sfTimes.get(i).fTime;
                        g.location = eLocation;
                        g.isAllDay = eAllay;
                        g.calName = eCalName;
                        g.timeZone = eZone;
                        g.rule = eRule;
                        g.repeat = true;
                        gCals.add(g);
                    }
                }
            }
        }
    }

    static ArrayList <sfTime> calcRepeat(String title, long startDateTime,
                                         String ruleStr, String durStr, long timeToday,
                                         long timeRangeTo) {

        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm ", Locale.getDefault());

        String startYMD = sdf.format(startDateTime);
        Log.w("calc "+title,startYMD+ ", rule="+ruleStr+", dur="+durStr);
        ArrayList<sfTime> sfTimes = new ArrayList<>();
        long durMin,untilTime, interval;
        int count;
        boolean [] weekDays; // 0: BYDAY YES/NO, sun = 1, sat = 7;
        boolean daily, weekly, monthly;

        durMin = getDurationMinutes(durStr);
        count = getCountValue(ruleStr);         // default to 999
        interval = getIntervalValue(ruleStr);   // default to 1
        untilTime = getUntilTime(ruleStr, timeRangeTo);      // default to 60 days after
        weekDays = getWeekDay(ruleStr);         // default to false

        daily = ruleStr.contains("DAILY");
        weekly = ruleStr.contains("WEEKLY");
        monthly = ruleStr.contains("MONTHLY");
        long lDateTime = startDateTime;
        if (daily) {
            if (count != 999) {
                if (weekDays[0]) {  // week constraint
                    for (int i = 1; i < count;    ) {
                        if (lDateTime > timeToday && lDateTime < untilTime)
                            sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                        lDateTime += interval * ONE_DAY;
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(lDateTime);
                        int weekNbr = calendar.get(Calendar.DAY_OF_WEEK);
                        if (weekDays[weekNbr]) {
                            sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                            i++;
                        }
                    }
                } else {
                    for (int i = 0; i < count; i++) {
                        if (untilTime > lDateTime)
                            sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                        lDateTime += interval * ONE_DAY;
                    }
                }
            } else {
                for (int i = 0; i < 5; i++) {   // 5번 정도만 insert
                    if (lDateTime > untilTime)
                        break;
                    sfTimes.add(new sfTime(lDateTime ,lDateTime + durMin));
                    lDateTime += interval * ONE_DAY;
                }
            }
            return sfTimes;
        }
        if (weekly) {
            if (count != 999) {
                if (!weekDays[0]) {
                    for (int i = 0; i < count; i++) {
                        if (lDateTime < untilTime)
                            sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                        lDateTime += interval * ONE_DAY;
                    }
                    return sfTimes;
                } else {
                    sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                    for (int i = 1; i < count;    ) {
                        lDateTime += interval * ONE_DAY * 7;
                        if (lDateTime > untilTime)
                            break;
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(lDateTime);
                        int weekNbr = calendar.get(Calendar.DAY_OF_WEEK);
                        if (weekDays[weekNbr]) {
                            sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                            i++;
                        }
                    }
                }
                return sfTimes;
            } else {
                if (!weekDays[0]) { // weekly, countless, noWeek
                    while (lDateTime < untilTime) {
                        sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                        lDateTime += interval * ONE_DAY * 7;
                    }
                    return sfTimes;
                } else {
                    sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                    for (int i = 1; i < count;    ) {
                        lDateTime += interval * ONE_DAY;
                        if (lDateTime > untilTime)
                            break;
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(lDateTime);
                        int weekNbr = calendar.get(Calendar.DAY_OF_WEEK);
                        if (weekDays[weekNbr]) {
                            sfTimes.add(new sfTime(lDateTime, lDateTime + durMin));
                            i++;
                        }
                    }
                }
                return sfTimes;
            }
        }

        return  sfTimes;
    }

    private static boolean[] getWeekDay(String ruleStr) {
        int pos = ruleStr.indexOf("BYDAY");
        boolean [] weekDays = new boolean[8];
        if (pos > 0) {
            weekDays[0] = true;     // we have weekDay values
            String byDay = ruleStr.substring(pos+6);
            String [] byDays = byDay.split(",");
            for (String day : byDays) {
                switch (day) {
                    case "SU": weekDays[1] = true;break;
                    case "MO": weekDays[2] = true;break;
                    case "TU": weekDays[3] = true;break;
                    case "WE": weekDays[4] = true;break;
                    case "TH": weekDays[5] = true;break;
                    case "FR": weekDays[6] = true;break;
                    case "SA": weekDays[7] = true;break;
                }
            }
        }
        return weekDays;
    }

    private static long getUntilTime(String ruleStr, long timeRangeTo) {
        SimpleDateFormat sdfZ = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm ", Locale.getDefault());

        long untilTime = timeRangeTo; // limited 2 months max
        int pos = ruleStr.indexOf("UNTIL");
        if (pos > 0) {
            String time = ruleStr.substring(pos + 6, pos + 14); // pos+ pos + 22);
            try {
                Date date = sdfZ.parse(time);
                untilTime = date.getTime() +ONE_DAY;
//                utils.log("time",time+" > "+sdf.format(untilTime));
            } catch (ParseException e) {
//                utils.log("Time", "error" + time);
            }
        }
        return untilTime;
    }

    private static long getIntervalValue(String ruleStr) {
        long interval = 1;
        int pos = ruleStr.indexOf("INTERVAL");
        if (pos > 0) {
            if (ruleStr.substring(pos+10).equals(";"))
                interval = Integer.parseInt(ruleStr.substring(pos + 9, pos + 9));
            else
                interval = Integer.parseInt(ruleStr.substring(pos + 9, pos + 10));
        }
        return interval;
    }

    private static int getCountValue(String ruleStr) {
        int count = 999;
        int pos = ruleStr.indexOf("COUNT");
        if (pos > 0)
            count = Integer.parseInt(ruleStr.substring(pos + 6, pos + 7));
        return count;
    }

    private static long getDurationMinutes(String durStr) {
        long durMin = -1;
        if (durStr.startsWith("P")) {
             durMin = Long.parseLong(durStr.substring(1, durStr.length()-1));
            if (durStr.endsWith("S"))
                durMin *= 1000;
        } else {
            utils.log("Error ","Duration format error ["+ durStr +"]");
        }
        return durMin;
    }

    static class sfTime {
        long sTime;
        long fTime;

        public sfTime(long sTime, long fTime) {
            this.sTime = sTime;
            this.fTime = fTime;
        }
    }
}
