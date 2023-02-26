package com.urrecliner.autoquiet.Sub;

import com.urrecliner.autoquiet.models.QuietTask;

import java.util.Calendar;

public class QuietTaskDefault {

    public QuietTask get() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 10);
        int hStart = calendar.get(Calendar.HOUR_OF_DAY);
        int mStart = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        int hFinish = calendar.get(Calendar.HOUR_OF_DAY);
        boolean [] week = new boolean[7];
        week[calendar.get(Calendar.DAY_OF_WEEK) - 1] = true;
        return new QuietTask("추가할 제목", hStart, mStart, hFinish, mStart,
                week, true, true, 1, 1, false);
    }
}