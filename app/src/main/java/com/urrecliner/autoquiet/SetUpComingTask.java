package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.urrecliner.autoquiet.Sub.Alarm99Icon;
import com.urrecliner.autoquiet.Sub.CalculateNext;
import com.urrecliner.autoquiet.Sub.IsScreen;
import com.urrecliner.autoquiet.Sub.SetAlarmTime;
import com.urrecliner.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class SetUpComingTask {
    long nextTime;
    static int icon, loop;
    static String timeInfoS, timeInfoF, timeInfo, soonOrUntill, subject, msg;

    public SetUpComingTask(Context context, ArrayList<QuietTask> quietTasks, String headInfo) {

        nextTime = System.currentTimeMillis() + 240*60*60*1000L;
        int saveIdx = 0;
        String begEnd = "";
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qThis = quietTasks.get(idx);
            if (qThis.active) {
                long thisBeg = CalculateNext.calc(false, qThis.begHour, qThis.begMin, qThis.week, 0);
                if (thisBeg < nextTime) {
                    nextTime = thisBeg;
                    saveIdx = idx;
                    begEnd = "S";
                }
                if (qThis.endHour != 99) {
                    long thisEnd = CalculateNext.calc(true, qThis.endHour, qThis.endMin, qThis.week, (qThis.begHour > qThis.endHour) ? (long) 24 * 60 * 60 * 1000 : 0);
                    if (thisEnd < nextTime) {
                        nextTime = thisEnd;
                        saveIdx = idx;
                        begEnd = (idx == 0) ? "O" : "F";
                    }
                }
            }
        }
        QuietTask qT = quietTasks.get(saveIdx);
        subject = qT.subject;
        timeInfoS = getHourMin(qT.begHour, qT.begMin);
        boolean end99 = qT.endHour == 99;
        if (!end99) {
            timeInfoF = getHourMin(qT.endHour, qT.endMin);
            if  (begEnd.equals("S")) {
                timeInfo = timeInfoS;
                soonOrUntill = "예정";
            } else {
                timeInfo = timeInfoF;
                soonOrUntill = "까지";
            }
            msg = headInfo + " " + subject + "\n" + timeInfo
                    + " " + soonOrUntill + " " + begEnd;
            if  (begEnd.equals("S"))
                msg += " ~ " + timeInfoF;
            loop = 0;
            icon = (qT.vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_normal;
        } else {
            timeInfo = timeInfoS;
            soonOrUntill = "알림";
            msg = headInfo + "\n" + timeInfo + " " + subject;
            icon = new Alarm99Icon().setId(qT.begLoop, qT.endLoop);
            loop = (icon == R.drawable.bell_several) ? 3:0;
        }

        new Utils(context).log("SetUpComingTask",msg);
        updateNotiBar(context);
        new SetAlarmTime().request(context, qT, nextTime, begEnd, loop);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (IsScreen.On(context))
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void updateNotiBar(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("beg", timeInfo);
        intent.putExtra("end", soonOrUntill);
        intent.putExtra("subject", subject);
        intent.putExtra("isUpdate", true);
        intent.putExtra("end99", false);
        intent.putExtra("icon", icon);
        context.startForegroundService(intent);
    }

    private String getHourMin(int hour, int min) {
        return (""+ (100+hour)).substring(1) + ":" + (""+(100+min)).substring(1);
    }

}