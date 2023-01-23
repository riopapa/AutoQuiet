package com.urrecliner.autoquiet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.Sub.CalculateNext;
import com.urrecliner.autoquiet.Sub.NextAlarm;

import java.util.ArrayList;

public class NextTask {
    long nextTime;
    static int icon;
    static String timeInfo, soonOrUntill, subject;

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

                long nextFinish = CalculateNext.calc(true, qTaskNext.finishHour, qTaskNext.finishMin, week, (qTaskNext.startHour> qTaskNext.finishHour) ? (long)24*60*60*1000 : 0);
                if (nextFinish < nextTime) {
                    nextTime = nextFinish;
                    saveIdx = idx;
                    startFinish = (idx == 0) ? "O":"F";
                    icon = (qTaskNext.vibrate) ? 1:2;
                }
            }
        }
        QuietTask quietTask = quietTasks.get(saveIdx);
        subject = quietTask.subject;
        if (startFinish.equals("S")) {
            timeInfo = getHourMin(quietTask.startHour, quietTask.startMin);
            soonOrUntill = "예정";
        }
        else {
            timeInfo = getHourMin(quietTask.finishHour, quietTask.finishMin);
            soonOrUntill = "까지";
        }
        new NextAlarm().request(context, quietTask, nextTime - 30000, startFinish);
        String msg = headInfo + " " + subject + "\n" + timeInfo
                + " " + soonOrUntill + " " + startFinish;
        new Utils(context).log("NextTask",msg);
        Activity activity;
        activity = (Activity) context;

        Intent updateIntent = new Intent(activity, NotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        updateIntent.putExtra("start", timeInfo);
        updateIntent.putExtra("finish", soonOrUntill);
        updateIntent.putExtra("subject", subject);
        updateIntent.putExtra("icon", icon);
        activity.startForegroundService(updateIntent);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
//
    }

    @NonNull
    private String getHourMin(int hour, int min) {
        String hh = "0"+ hour;
        hh = hh.substring(hh.length()-2);
        String mm = "0"+min;
        mm = mm.substring(mm.length()-2);
        return hh + ":" + mm;
    }
}