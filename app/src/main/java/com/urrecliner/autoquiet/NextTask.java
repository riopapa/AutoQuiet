package com.urrecliner.autoquiet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.CalculateNext;
import com.urrecliner.autoquiet.utility.NextAlarm;

import java.util.ArrayList;

public class NextTask {
    long nextTime;
    static int count, icon;
    static String timeInfo, soonOrUntill, subject;

    public NextTask(Context context, String headInfo) {

        nextTime = System.currentTimeMillis() + 240*60*60*1000L;
        int saveIdx = 0;
        String startFinish = "";
        boolean[] week;
        ArrayList<QuietTask> quietTasks = new QuietTaskGetPut().get(context);
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qTaskNext = quietTasks.get(idx);
            if (qTaskNext.isActive()) {
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
        int taskNbr = new NextAlarm().request(context, quietTask, nextTime- 30000, startFinish, saveIdx);
        String msg = headInfo + " " + subject + "\n" + timeInfo
                + " " + soonOrUntill + " " + startFinish;
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        new Utils(context).log("NextTask",msg);
        count = 0;
        String s = timeInfo+" " +count++ + ") " + subject+ " "+ soonOrUntill;
        Activity activity = MainActivity.pActivity;
        if (activity == null)
            Log.e("Activity"," si null");
        Intent updateIntent = new Intent(activity, NotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        updateIntent.putExtra("start", timeInfo);
        updateIntent.putExtra("finish", soonOrUntill);
        updateIntent.putExtra("subject", subject);
        updateIntent.putExtra("icon", icon);
        activity.startService(updateIntent);
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