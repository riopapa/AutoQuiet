package com.riopapa.autoquiet.quiettask;

import static com.riopapa.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;

import com.riopapa.autoquiet.models.QuietTask;

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
        return new QuietTask("제목은 여기에", hStart, mStart, hFinish, mStart,
                week, true, PHONE_VIBRATE, false);
    }
}