package com.riopapa.autoquiet.Sub;


import com.riopapa.autoquiet.models.QuietTask;

import java.util.Calendar;

public class CalcNextBegEnd {

    /*
     * calculate upcoming begin , end time
     * input : quietTask, output : public var
     */
    public long begTime, endTime;
    public CalcNextBegEnd(QuietTask qt) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int DD = cal.get(Calendar.DATE);
        int WK = cal.get(Calendar.DAY_OF_WEEK) - 1; // 1 for sunday

        for (int i = WK; ; ) {
            if (qt.week[i]) {
                cal.set(Calendar.HOUR_OF_DAY, qt.begHour);
                cal.set(Calendar.MINUTE, qt.begMin);
                begTime = cal.getTimeInMillis();
                if (qt.endHour == 99) {
                    endTime = begTime + 1000;
                } else {
                    cal.set(Calendar.HOUR_OF_DAY, qt.endHour);
                    cal.set(Calendar.MINUTE, qt.endMin);
                    endTime = cal.getTimeInMillis();
                }
                if (qt.begHour > qt.endHour) {
                    endTime += 24 * 60 * 60000;
                }
                break;
            }
            cal.set(Calendar.DATE, ++DD);
            DD = cal.get(Calendar.DATE);
            i++;
            if (i == 7)
                i = 0;
        }
    }
}