package com.urrecliner.autoquiet.Sub;

import java.util.Calendar;

public class CalculateNext {

    public static long calc(boolean end, int hour, int min, boolean[] week, long add24Hour) {
        Calendar nextDay = Calendar.getInstance();
        nextDay.set(Calendar.HOUR_OF_DAY, hour);
        nextDay.set(Calendar.MINUTE, min);
        nextDay.set(Calendar.SECOND, 0);
        nextDay.set(Calendar.MILLISECOND, 0);

        int DD = nextDay.get(Calendar.DATE);
        int WK = nextDay.get(Calendar.DAY_OF_WEEK) - 1; // 1 for sunday

        long nowTime = System.currentTimeMillis();
        long nextEvent;
        for (int i = WK; ; ) {
            if (week[i]) {
                nextEvent = nextDay.getTimeInMillis();
                if ((nextEvent+add24Hour) > nowTime)
                    break;
                if (end) {
                    nextEvent += 24*60*60000;
                    break;
                }
            }
            nextDay.set(Calendar.DATE, ++DD);
            DD = nextDay.get(Calendar.DATE);
            i++;
            if (i == 7)
                i = 0;
        }
        return (nextEvent+add24Hour - 50*1000);
    }

}