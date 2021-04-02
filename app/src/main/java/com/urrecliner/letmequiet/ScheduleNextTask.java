package com.urrecliner.letmequiet;

import android.content.Intent;
import android.widget.Toast;

import com.urrecliner.letmequiet.models.QuietTask;
import com.urrecliner.letmequiet.utility.CalculateNext;
import com.urrecliner.letmequiet.utility.NextAlarm;

import static com.urrecliner.letmequiet.Vars.mActivity;
import static com.urrecliner.letmequiet.Vars.mContext;
import static com.urrecliner.letmequiet.Vars.quietTask;
import static com.urrecliner.letmequiet.Vars.quietTasks;
import static com.urrecliner.letmequiet.Vars.sdfDateTime;
import static com.urrecliner.letmequiet.Vars.sdfTime;
import static com.urrecliner.letmequiet.Vars.stateCode;
import static com.urrecliner.letmequiet.Vars.utils;

public class ScheduleNextTask {
    public ScheduleNextTask(String headInfo) {
        utils.log("Schedule","Create next schedule "+headInfo);
        long nextTime = System.currentTimeMillis() + (long)240*60*60*1000;
        int saveIdx = 0;
        String StartFinish = "S";
        boolean[] week;
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask quietTask1 = quietTasks.get(idx);
            if (quietTask1.isActive()) {
                week = quietTask1.getWeek();
                long nextStart = CalculateNext.calc(false, quietTask1.getStartHour(), quietTask1.getStartMin(), week, 0);
                if (nextStart < nextTime) {
                    nextTime = nextStart;
                    saveIdx = idx;
                    StartFinish = "S";
                }

                long nextFinish = CalculateNext.calc(true, quietTask1.getFinishHour(), quietTask1.getFinishMin(), week, (quietTask1.getStartHour()> quietTask1.getFinishHour()) ? (long)24*60*60*1000 : 0);
                if (nextFinish < nextTime) {
                    nextTime = nextFinish;
                    saveIdx = idx;
                    StartFinish = (idx == 0) ? "O":"F";
                }
            }
        }
        quietTask = quietTasks.get(saveIdx);
        NextAlarm.request(quietTask, nextTime, StartFinish, mContext);
        String msg = headInfo + "\n" + quietTask.getSubject() + "\n" + sdfDateTime.format(nextTime) + " " + StartFinish;
//        utils.log(logID, msg);
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        utils.log("schedule",sdfDateTime.format(nextTime) + " " + StartFinish + " " + quietTask.getSubject());
        updateNotificationBar (sdfTime.format(nextTime), quietTask.getSubject(), StartFinish);
        if (stateCode.equals("@back") && StartFinish.equals("F")) {
            MannerMode.turnOn(mContext, quietTask.getSubject(), quietTask.isVibrate());
        }
    }

    void updateNotificationBar(String dateTime, String subject, String startFinish) {
        Intent updateIntent = new Intent(mActivity, NotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        updateIntent.putExtra("dateTime", dateTime);
        updateIntent.putExtra("subject", subject);
        updateIntent.putExtra("startFinish", startFinish);
        mActivity.startService(updateIntent);
    }

}
