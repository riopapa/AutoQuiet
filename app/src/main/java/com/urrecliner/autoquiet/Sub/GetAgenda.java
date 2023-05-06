package com.urrecliner.autoquiet.Sub;

import android.content.Context;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.util.Log;
import com.urrecliner.autoquiet.models.GCal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class GetAgenda {

    static final long ONE_DAY = 24*60*60*1000;

    static long TimeRangeFrom, TimeRangeTo, TimeTODAY;
    static int bySetPos = -1;

    public ArrayList<GCal> get(Context context) {

        ArrayList<GCal> gCals = new ArrayList<>();

        // 반복 설정이 있는 것을 고려 calendar 를 400일전 부터 다음 달 까지 읽어 옴
        TimeRangeFrom = System.currentTimeMillis() - 400*ONE_DAY;
        TimeRangeTo = System.currentTimeMillis() + (long) 50*ONE_DAY;
        TimeTODAY = System.currentTimeMillis() - 24*60*60*1000; // 어제 이후만 list Up

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

//        int count = cursor.getCount();
//        Log.e("Total","Cursor count="+count);
        while (cursor.moveToNext()) {
            int eID = cursor.getInt(0);
            String eTitle = cursor.getString(1);
            if (eTitle == null)
                continue;
            if (cursor.getInt(6) == 1)
                continue;
            String eDesc = cursor.getString(2);
            long eBeg = cursor.getLong(3);
            long eEnd = cursor.getLong(4);
            String eLocation = cursor.getString(5);
//            boolean eAllay = (cursor.getInt(6) == 1);
            String eCalName = cursor.getString(7);
            String eZone = cursor.getString(8);
            String eRule = cursor.getString(9);
            String eDuration = cursor.getString(10);
            if (eRule == null) {
                if (eBeg > TimeTODAY && eBeg < TimeRangeTo) {
                    GCal gCal =new GCal();
                    gCal.id       = eID;
                    gCal.title    = eTitle;
                    gCal.desc     = (eDesc == null)? "":eDesc;
                    gCal.begTime =eBeg;
                    gCal.endTime =eEnd;
                    gCal.location =(eLocation == null)? "":eLocation;
                    gCal.calName = eCalName;
                    gCal.timeZone =eZone;
                    gCal.rule = "";
                    gCal.repeat = false;
                    gCals.add(gCal);
                }
            }
            else {
                ArrayList<begEndTime> begEndTimes = calcRepeat (eTitle, eBeg, eRule, eDuration);
                for (int i = 0; i < begEndTimes.size() ; i++) {
                    if (begEndTimes.get(i).sTime > TimeTODAY &&
                            begEndTimes.get(i).sTime < TimeRangeTo) {
                        GCal g = new GCal();
                        g.id = eID;
                        g.title = eTitle;
                        g.desc = (eDesc == null)? "":eDesc;
                        g.begTime = begEndTimes.get(i).sTime;
                        g.endTime = begEndTimes.get(i).fTime;
                        g.location = (eLocation == null)? "":eLocation;
                        g.calName = eCalName;
                        g.timeZone = eZone;
                        g.rule = eRule;
                        g.repeat = true;
                        gCals.add(g);
                    }
                }
            }
        }
        gCals.sort((arg0, arg1) -> Long.compare(arg0.begTime, arg1.begTime));
        return gCals;
    }

    ArrayList <begEndTime> calcRepeat(String title, long startDateTime,
                                      String ruleStr, String durStr) {

//        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm ", Locale.getDefault());
//        String startYMD = sdf.format(startDateTime);
//        Log.w("calc "+title,startYMD+ ", rule="+ruleStr+", dur="+durStr);
        ArrayList<begEndTime> begEndTimes = new ArrayList<>();
        long durMin, untilTime;
        int count, interval;
        boolean [] weekDays; // 0: BYDAY YES/NO, sun = 1, sat = 7;
        boolean daily, weekly, monthly;

        daily = ruleStr.contains("DAILY");
        weekly = ruleStr.contains("WEEKLY");
        monthly = ruleStr.contains("MONTHLY");
        if (ruleStr.contains("YEARLY"))
            return begEndTimes;

        durMin = getDurationMinutes(durStr);
        count = getKeywordValue(ruleStr,"COUNT",9999);
        interval = getKeywordValue(ruleStr,"INTERVAL",1);
        untilTime = getUntilTime(ruleStr);      // max to TimeRangeTo
        weekDays = getByDay(ruleStr);         // default to false

        if (daily)
            repeatDaily(begEndTimes, durMin, untilTime, count, interval, weekDays, startDateTime);

        if (weekly)
            repeatWeekly(begEndTimes, durMin, untilTime, count, interval, weekDays, startDateTime);

        if (monthly)
            repeatMonthly(ruleStr, begEndTimes, durMin, untilTime, count, weekDays[0], startDateTime);

        return begEndTimes;
    }

    private void repeatMonthly(String ruleStr, ArrayList<begEndTime> begEndTimes, long durMin, long untilTime, int count, boolean weekBased, long lDateTime) {
        if (weekBased) {
            if (bySetPos == -1)
                bySetPos = getKeywordValue(ruleStr, "BYSETPOS",-1);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(lDateTime);
            int weekNbr = c.get(Calendar.DAY_OF_WEEK);
            for (int i = 0; i < count; i++) {
                if (lDateTime > untilTime)
                    break;
                begEndTimes.add(new begEndTime(lDateTime, lDateTime + durMin));
                c.add(Calendar.MONTH,1);
                c.set(Calendar.DATE,1);
                while (c.get(Calendar.DAY_OF_WEEK) != weekNbr)
                    c.add(Calendar.DATE,1);
                c.add(Calendar.DATE, (bySetPos-1)*7);
                lDateTime = c.getTimeInMillis();
            }
        } else {
            for (int i = 0; i < count; i++) {
                if (lDateTime > untilTime)
                    break;
                if (lDateTime > TimeTODAY) {
                    begEndTimes.add(new begEndTime(lDateTime, lDateTime + durMin));
                }
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(lDateTime);
                c.add(Calendar.MONTH,1);
                lDateTime = c.getTimeInMillis();
            }
        }
    }

    private void repeatWeekly(ArrayList<begEndTime> begEndTimes, long durMin, long untilTime, int count, int interval, boolean[] weekDays, long lDateTime) {
        if (weekDays[0]) {
            for (int i = 0; i < count;    ) {
                if (lDateTime > untilTime)
                    break;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(lDateTime);
                if (weekDays[c.get(Calendar.DAY_OF_WEEK)]) {
                    begEndTimes.add(new begEndTime(lDateTime, lDateTime + durMin));
                    i++;
                }
                lDateTime += interval * ONE_DAY;
            }
        } else {
            for (int i = 0; i < count; i++) {
                if (lDateTime > untilTime)
                    break;
                if (lDateTime > TimeTODAY) {
                    begEndTimes.add(new begEndTime(lDateTime, lDateTime + durMin));
                }
                lDateTime += interval * ONE_DAY * 7;
            }
        }
    }

    private void repeatDaily(ArrayList<begEndTime> begEndTimes, long durMin, long untilTime, int count, int interval, boolean[] weekDays, long lDateTime) {
        if (weekDays[0]) {  // week constraint
            for (int i = 1; i < count;    ) {
                if (lDateTime > untilTime)
                    break;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(lDateTime);
                if (weekDays[c.get(Calendar.DAY_OF_WEEK)]) {
                    begEndTimes.add(new begEndTime(lDateTime, lDateTime + durMin));
                    i++;
                }
                lDateTime += interval * ONE_DAY;
            }
        } else {
            for (int i = 0; i < count; i++) {
                if (lDateTime > untilTime)
                    break;
                begEndTimes.add(new begEndTime(lDateTime, lDateTime + durMin));
                lDateTime += interval * ONE_DAY;
            }
        }
    }

    private boolean[] getByDay(String ruleStr) {
        int pos = ruleStr.indexOf("BYDAY");
        boolean [] weekDays = new boolean[8];
        if (pos > 0) {
            weekDays[0] = true;     // we have weekDay values
            String byDay = ruleStr.substring(pos+6);
            int by1stCode = Character.getNumericValue(byDay.charAt(0));
            if (by1stCode >= 0 && by1stCode <= 9)
                bySetPos = by1stCode;
            else
                bySetPos = -1;
            if (byDay.contains(";")) {
                byDay = byDay.substring(0,byDay.indexOf(";"));
            }
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

    private long getUntilTime(String ruleStr) {
        SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        long untilTime = TimeRangeTo; // limited 2 months max
        int pos = ruleStr.indexOf("UNTIL");
        if (pos > 0) {
            String time = ruleStr.substring(pos + 6, pos + 14); // pos+ pos + 22);
            try {
                Date date = sdfYMD.parse(time);
                untilTime = date.getTime() + ONE_DAY;
                if (untilTime > TimeRangeTo)
                    untilTime = TimeRangeTo;
            } catch (Exception e) {
//                utils.log("Time", "error" + time);
            }
        }
        return untilTime;
    }

    private int getKeywordValue(String ruleStr, String keyword, int init) {
        int count = init;
        int pos = ruleStr.indexOf(keyword);
        if (pos > 0) {
            String countStr = ruleStr.substring(pos + keyword.length() + 1);
            pos = countStr.indexOf(";");
            if (pos > 0)
                countStr = countStr.substring(0, pos);
            count = Integer.parseInt(countStr);
        }
        return count;
    }

    private long getDurationMinutes(String durStr) {
        long durMin = -1;
        if (durStr.startsWith("P")) {
             durMin = Long.parseLong(durStr.substring(1, durStr.length()-1));
            if (durStr.endsWith("S"))
                durMin *= 1000;
        } else {
            Log.e("Error ","Duration format error ["+ durStr +"]");
        }
        return durMin;
    }

    static class begEndTime {
        long sTime;
        long fTime;

        public begEndTime(long sTime, long fTime) {
            this.sTime = sTime;
            this.fTime = fTime;
        }
    }
}