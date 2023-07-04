package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.Intent;

import com.urrecliner.autoquiet.Sub.Alarm99Icon;
import com.urrecliner.autoquiet.Sub.CalculateNext;
import com.urrecliner.autoquiet.Sub.SetAlarmTime;
import com.urrecliner.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class SetUpComingTask {
    long nextTime, nextTimeN;
    static int icon, several;
    static String timeInfoS, timeInfoF, timeInfo, soonOrUntill, subject, msg;
    static int iconN, severalN;
    static String timeInfoSN, timeInfoFN, timeInfoN, soonOrUntillN, subjectN, msgN;

    public SetUpComingTask(Context context, ArrayList<QuietTask> quietTasks, String headInfo) {

        nextTime = System.currentTimeMillis() + 240*60*60*1000L;
        nextTimeN = nextTime;
        int saveIdx = 0, saveIdxN = 0;
        String begEnd = "", begEndN = "";
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
        for (int idx = 0; idx < quietTasks.size(); idx++) {
            QuietTask qThat = quietTasks.get(idx);
            if (qThat.active) {
                long thisBeg = CalculateNext.calc(false, qThat.begHour, qThat.begMin, qThat.week, 0);
                if (thisBeg < nextTimeN && thisBeg > nextTime) {
                    nextTimeN = thisBeg;
                    begEndN = "S";
                    saveIdxN = idx;
                }
                if (qThat.endHour != 99) {
                    long thisEnd = CalculateNext.calc(true, qThat.endHour, qThat.endMin, qThat.week, (qThat.begHour > qThat.endHour) ? (long) 24 * 60 * 60 * 1000 : 0);
                    if (thisEnd < nextTimeN && thisEnd > nextTime) {
                        nextTimeN = thisEnd;
                        begEndN = (idx == 0) ? "O" : "F";
                        saveIdxN = idx;
                    }
                }
            }
        }
        QuietTask qN = quietTasks.get(saveIdxN);
        QuietTask qT = quietTasks.get(saveIdx);
        subjectN = qN.subject; subject = qT.subject;
        timeInfoSN = getHourMin(qN.begHour, qN.begMin); timeInfoS = getHourMin(qT.begHour, qT.begMin);
        boolean end99N = qN.endHour == 99;
        boolean end99 = qT.endHour == 99;
        if (!end99N) {
            timeInfoFN = getHourMin(qN.endHour, qN.endMin);
            if  (begEndN.equals("S")) {
                timeInfoN = timeInfoSN;
                soonOrUntillN = "예정";
            } else {
                timeInfoN = timeInfoFN;
                soonOrUntillN = "까지";
            }
            severalN = (qN.sayDate) ? 3: 0;  // finish 인데 sayDate 가 있으면 여러 번 울림
            iconN = (qN.vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_normal;
        } else {
            timeInfoN = timeInfoSN;
            soonOrUntillN = "알림";
            iconN = new Alarm99Icon().getRscId(qN.begLoop, qN.endLoop);
            severalN = (iconN == R.drawable.bell_several) ? 3:0;
        }
        if (!end99) {
            timeInfoF = getHourMin(qT.endHour, qT.endMin);
            if  (begEnd.equals("S")) {
                timeInfo = timeInfoS;
                soonOrUntill = "예정";
                icon = R.drawable.phone_normal;
            } else {
                timeInfo = timeInfoF;
                soonOrUntill = "까지";
                icon = (qT.vibrate) ? R.drawable.phone_vibrate : R.drawable.phone_off;
            }
            msg = headInfo + " " + subject + "\n" + timeInfo
                    + " " + soonOrUntill + " " + begEnd;
            if  (begEnd.equals("S"))
                msg += " ~ " + timeInfoF;
            several = (qT.sayDate) ? 3: 0;  // finish 인데 sayDate 가 있으면 여러 번 울림
        } else {
            timeInfo = timeInfoS;
            soonOrUntill = "알림";
            msg = headInfo + "\n" + timeInfo + " " + subject;
            icon = new Alarm99Icon().getRscId(qT.begLoop, qT.endLoop);
            several = (icon == R.drawable.bell_several) ? 3:0;
        }

        new Utils(context).log("SetUpComingTask",msg);
        updateNotiBar(context);
        new SetAlarmTime().request(context, qT, nextTime, begEnd, several);

    }

    private static void updateNotiBar(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("beg", timeInfo);
        intent.putExtra("end", soonOrUntill);
        intent.putExtra("subject", subject);
        intent.putExtra("end99", false);
        intent.putExtra("icon", icon);

        intent.putExtra("begN", timeInfoN);
        intent.putExtra("endN", soonOrUntillN);
        intent.putExtra("subjectN", subjectN);
        intent.putExtra("end99N", false);
        intent.putExtra("iconN", iconN);
        context.startForegroundService(intent);
    }

    private String getHourMin(int hour, int min) {
        return (""+ (100+hour)).substring(1) + ":" + (""+(100+min)).substring(1);
    }

}