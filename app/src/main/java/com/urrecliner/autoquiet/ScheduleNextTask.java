package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTask;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.sharedEditor;
import static com.urrecliner.autoquiet.Vars.utils;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.CalculateNext;
import com.urrecliner.autoquiet.utility.NextAlarm;

import java.util.Timer;
import java.util.TimerTask;

public class ScheduleNextTask {
    long nextTime;
    Timer timer = null;
    TimerTask timerTask = null;
    static int count, icon;
    static String timeInfo, soonOrUntill, subject;
    final int period = 140 * 60 * 1000;
    static long lastTime = 0;
    public ScheduleNextTask(String headInfo) {

//        if (timer != null)
//            timer.cancel();
//        if (timerTask != null)
//            timerTask.cancel();
        nextTime = System.currentTimeMillis() + 240*60*60*1000L;
        int saveIdx = 0;
        String startFinish = "";
        boolean[] week;
        quietTasks = utils.readQuietTasksFromShared();
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
        subject = quietTask.subject;
        if (startFinish.equals("S")) {
            timeInfo = getHourMin(quietTask.startHour, quietTask.startMin);
            soonOrUntill = "예정";
        }
        else {
            timeInfo = getHourMin(quietTask.finishHour, quietTask.finishMin);
            soonOrUntill = "까지";
        }
        int taskNbr = NextAlarm.request(quietTask, nextTime- 30000, startFinish, mContext);
        sharedEditor.putInt("task", taskNbr);
        sharedEditor.apply();
        String msg = headInfo + " " + subject + "\n" + timeInfo
                + " " + soonOrUntill + " " + startFinish;
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        utils.log("ScheduleNextTask",msg);
        count = 0;
        updateNaviBar(0);
//        timer = new Timer();
//        timerTask = new TimerTask() {
//            @Override
//            public void run () {
//                long nowTime = System.currentTimeMillis();
//                long deltaTime = nowTime - lastTime;
//                if (deltaTime > 10000) {
//                    lastTime = nowTime;
//                    updateNaviBar(deltaTime);
//                } else {
//                    Log.w("TimerTask","Ignore quick Update TimerTask");
//                }
//            }
//        };
//        timer.schedule(timerTask, period, period);
    }

    private void updateNaviBar(long deltaTime) {
        String s = timeInfo+" " +count++ + ") " + subject+ " "+ soonOrUntill;
        Log.w("bar "+deltaTime, s);
        Intent updateIntent = new Intent(mActivity, NotificationService.class);
        updateIntent.putExtra("isUpdate", true);
        updateIntent.putExtra("start", timeInfo);
        updateIntent.putExtra("finish", soonOrUntill);
        updateIntent.putExtra("subject", subject);
        updateIntent.putExtra("icon", icon);
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