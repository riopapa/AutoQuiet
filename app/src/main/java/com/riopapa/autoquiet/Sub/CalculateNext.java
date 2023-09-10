package com.riopapa.autoquiet.Sub;

import java.util.Calendar;

public class CalculateNext {

    static final long A_LITTLE_GAP = 30 * 1000;
    public static long calc(boolean end, int hour, int min, boolean[] week, long add24Hour) {
        Calendar nextDay = Calendar.getInstance();
        nextDay.set(Calendar.HOUR_OF_DAY, hour);
        nextDay.set(Calendar.MINUTE, min);
        nextDay.set(Calendar.SECOND, 0);
        nextDay.set(Calendar.MILLISECOND, 0);

        int DD = nextDay.get(Calendar.DATE);
        int WK = nextDay.get(Calendar.DAY_OF_WEEK) - 1; // 1 for sunday

        long nowTime = System.currentTimeMillis() + A_LITTLE_GAP + A_LITTLE_GAP;
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

        return (nextEvent+add24Hour - A_LITTLE_GAP);
    }
}