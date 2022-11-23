package com.urrecliner.autoquiet;

import android.content.Intent;
import android.widget.Toast;

import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.CalculateNext;
import com.urrecliner.autoquiet.utility.NextAlarm;

import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTask;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.sdfDateTime;
import static com.urrecliner.autoquiet.Vars.sdfTime;
import static com.urrecliner.autoquiet.Vars.utils;

public class ScheduleNextTask {
    public ScheduleNextTask(String headInfo) {
//        utils.log("Schedule","Create next schedule "+headInfo);
        long nextTime = System.currentTimeMillis() + 240*60*60*1000L;
        int saveIdx = 0;
        String StartFinish = "S";
        boolean[] week;
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qTaskNext = quietTasks.get(idx);
            if (qTaskNext.isActive()) {
                week = qTaskNext.getWeek();
                long nextStart = CalculateNext.calc(false, qTaskNext.getStartHour(), qTaskNext.getStartMin(), week, 0);
                if (nextStart < nextTime) {
                    nextTime = nextStart;
                    saveIdx = idx;
                    StartFinish = "S";
                }

                long nextFinish = CalculateNext.calc(true, qTaskNext.getFinishHour(), qTaskNext.getFinishMin(), week, (qTaskNext.getStartHour()> qTaskNext.getFinishHour()) ? (long)24*60*60*1000 : 0);
                if (nextFinish < nextTime) {
                    nextTime = nextFinish;
                    saveIdx = idx;
                    StartFinish = (idx == 0) ? "O":"F";
                }
            }
        }

        quietTask = quietTasks.get(saveIdx);
        NextAlarm.request(quietTask, nextTime, StartFinish, mContext);
        String msg = headInfo + " " + quietTask.getSubject() + "\n" + sdfDateTime.format(nextTime) + " " + StartFinish;
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        utils.log("schedule",msg);
        Intent updateIntent = new Intent(mActivity, NotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        updateIntent.putExtra("dateTime", sdfTime.format(nextTime));
        updateIntent.putExtra("subject", quietTask.getSubject());
        updateIntent.putExtra("startFinish", StartFinish);
        updateIntent.putExtra("loopCount", (StartFinish.equals("F") ?
                quietTask.getfRepeatCount() : quietTask.getsRepeatCount()));
        mActivity.startService(updateIntent);
    }
}