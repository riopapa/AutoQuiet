package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.urrecliner.autoquiet.Sub.CalculateNext;
import com.urrecliner.autoquiet.Sub.IsScreen;
import com.urrecliner.autoquiet.Sub.NextAlarm;
import com.urrecliner.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class NextTask {
    long nextTime;
    static int icon, loop;
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
        loop = (quietTask.finishHour == 99) ? 3 : 0; // if alarm repeat 3 times
        new NextAlarm().request(context, quietTask, nextTime, startFinish, loop);
        subject = quietTask.subject;
        timeInfoS = getHourMin(quietTask.startHour, quietTask.startMin);
        boolean finish99 = quietTask.finishHour == 99;
        if (!finish99) {
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
        } else {
            timeInfo = timeInfoS;
            soonOrUntill = "알림";
            msg = headInfo + "\n" + timeInfo + " " + subject;
            icon = 3;
        }

        new Utils(context).log("NextTask",msg);
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("start", timeInfo);
        intent.putExtra("finish", soonOrUntill);
        intent.putExtra("subject", subject);
        intent.putExtra("isUpdate", true);
        intent.putExtra("finish99", false);
        intent.putExtra("icon", icon);
        context.startForegroundService(intent);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (IsScreen.On(context))
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getHourMin(int hour, int min) {
        return (""+ (100+hour)).substring(1) + ":" + (""+(100+min)).substring(1);
    }

}