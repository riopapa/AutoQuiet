package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTask;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.utils;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.CalculateNext;
import com.urrecliner.autoquiet.utility.NextAlarm;

public class ScheduleNextTask {
    long nextTime;
    public ScheduleNextTask(String headInfo) {

        nextTime = System.currentTimeMillis() + 240*60*60*1000L;
        int saveIdx = 0, icon = 0;
        String startFinish = "";
        boolean[] week;
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
        quietTask = quietTasks.get(saveIdx);
//        int loopCount = 1;
        String start = " "+getHourMin(quietTask.startHour, quietTask.startMin);
        String finish = "~"+getHourMin(quietTask.finishHour, quietTask.finishMin);
        NextAlarm.request(quietTask, nextTime, startFinish, mContext);
        String msg = headInfo + " " + quietTask.subject + "\n" + start + finish
                + " " + startFinish;
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        utils.log("ScheduleNextTask",msg);
//        int loopCount = (startFinish.equals("F") ? quietTask.fRepeatCount : quietTask.sRepeatCount);
        Intent updateIntent = new Intent(mActivity, NotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        updateIntent.putExtra("start", start);
        updateIntent.putExtra("finish", finish);
        updateIntent.putExtra("subject", quietTask.subject);
        updateIntent.putExtra("icon", icon);
//        updateIntent.putExtra("loopCount", loopCount);
        mActivity.startService(updateIntent);
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