package com.urrecliner.autoquiet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.Sub.CalculateNext;
import com.urrecliner.autoquiet.Sub.NextAlarm;

import java.util.ArrayList;

public class NextTask {
    long nextTime;
    static int icon;
    static String timeInfoS, timeInfoF, timeInfo, soonOrUntill, subject, msg;

    public NextTask(Context context, ArrayList<QuietTask> quietTasks, String headInfo) {

        nextTime = System.currentTimeMillis() + 240*60*60*1000L;
        int saveIdx = 0;
        String startFinish = "";
        boolean[] week;
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qTaskNext = quietTasks.get(idx);
            if (qTaskNext.active) {
                week = qTaskNext.week;
                long nextStart = CalculateNext.calc(false, qTaskNext.startHour, qTaskNext.startMin, week, 0);
                if (nextStart < nextTime) {
                    nextTime = nextStart;
                    saveIdx = idx;
                    startFinish = "S";
                    icon = 0;
                }
                if (qTaskNext.finishHour != 99) {
                    long nextFinish = CalculateNext.calc(true, qTaskNext.finishHour, qTaskNext.finishMin, week, (qTaskNext.startHour > qTaskNext.finishHour) ? (long) 24 * 60 * 60 * 1000 : 0);
                    if (nextFinish < nextTime) {
                        nextTime = nextFinish;
                        saveIdx = idx;
                        startFinish = (idx == 0) ? "O" : "F";
                        icon = (qTaskNext.vibrate) ? 1 : 2;
                    }
                }
            }
        }
        QuietTask quietTask = quietTasks.get(saveIdx);
        new NextAlarm().request(context, quietTask, nextTime, startFinish);

        subject = quietTask.subject;
        timeInfoS = getHourMin(quietTask.startHour, quietTask.startMin);
        if (quietTask.finishHour == 99) {
            timeInfoF = "";
            soonOrUntill = "";
            msg = headInfo + " " + subject;
        } else {
            timeInfoF = getHourMin(quietTask.finishHour, quietTask.finishMin);
            if  (startFinish.equals("S")) {
                timeInfo = timeInfoS;
                soonOrUntill = "예정";
            } else {
                timeInfo = timeInfoF;
                soonOrUntill = "까지";
            }
            msg = headInfo + " " + subject + "\n" + timeInfo
                    + " " + soonOrUntill + " " + startFinish;
            if  (startFinish.equals("S"))
                msg += " ~ " + timeInfoF;
        }

        new Utils(context).log("NextTask",msg);
        Intent updateIntent = new Intent(context, NotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        updateIntent.putExtra("start", timeInfo);
        updateIntent.putExtra("finish", soonOrUntill);
        updateIntent.putExtra("subject", subject);
        updateIntent.putExtra("icon", icon);
        context.startForegroundService(updateIntent);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getHourMin(int hour, int min) {
        String hh = "0"+ hour;
        hh = hh.substring(hh.length()-2);
        String mm = "0"+min;
        mm = mm.substring(mm.length()-2);
        return hh + ":" + mm;
    }
}