package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.urrecliner.autoquiet.Sub.SetAlarmTime;
import com.urrecliner.autoquiet.models.NextTwoTasks;
import com.urrecliner.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class SetUpComingTask {
    static int several, severalN;
    static String timeInfoS, timeInfo, msg;
    static String timeInfoN;
    static NextTwoTasks n2;

    public SetUpComingTask(Context context, ArrayList<QuietTask> quietTasks, String headInfo) {

        n2 = new NextTwoTasks(quietTasks);
        Log.w("icon", n2.icon+" vs "+n2.iconN);
        QuietTask qN = quietTasks.get(n2.saveIdxN);
        QuietTask qT = quietTasks.get(n2.saveIdx);
        timeInfoS = getHourMin(qT.begHour, qT.begMin);
        boolean end99N = qN.endHour == 99;
        boolean end99 = qT.endHour == 99;
        if (!end99N) {
            timeInfoN = (n2.begEndN.equals("S")) ?
                    getHourMin(qN.begHour, qN.begMin) : getHourMin(qN.endHour, qN.endMin);
        } else {
            timeInfoN = getHourMin(qN.begHour, qN.begMin);
        }
        if (!end99) {
            timeInfo = (n2.begEnd.equals("S")) ?
                        getHourMin(qT.begHour, qT.begMin) : getHourMin(qT.endHour, qT.endMin);
            msg = headInfo + " " + n2.subject + "\n" + timeInfo
                    + " " + n2.soonOrUntil + " " + n2.begEnd;
            if  (n2.begEnd.equals("S"))
                msg += " ~ " + getHourMin(qT.endHour, qT.endMin);
            several = (qT.sayDate) ? 3:0;  // finish 인데 sayDate 가 있으면 여러 번 울림
        } else {
            timeInfo = getHourMin(qT.begHour, qT.begMin);
            msg = headInfo + "\n" + timeInfo + " " + n2.subject;
            several = (n2.icon == R.drawable.bell_several) ? 3:0;
        }

        new Utils(context).log("SetUpComingTask",msg);
        updateNotyBar(context);
        new SetAlarmTime().request(context, qT, n2.nextTime, n2.begEnd, several);

    }

    private static void updateNotyBar(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("beg", timeInfo);
        intent.putExtra("end", n2.soonOrUntil);
        intent.putExtra("subject", n2.subject);
        intent.putExtra("end99", false);
        intent.putExtra("icon", n2.icon);

        intent.putExtra("begN", timeInfoN);
        intent.putExtra("endN", n2.soonOrUntilN);
        intent.putExtra("subjectN", n2.subjectN);
        intent.putExtra("iconN", n2.iconN);
        context.startForegroundService(intent);

        SharedPreferences sharedPref
                = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        sharedEditor.putString("begN", timeInfoN);
        sharedEditor.putString("endN", n2.soonOrUntilN);
        sharedEditor.putString("subjectN", n2.subjectN);
        sharedEditor.putInt("iconN", n2.iconN);
        sharedEditor.apply();

    }

    private String getHourMin(int hour, int min) {
        return (""+ (100+hour)).substring(1) + ":" + (""+(100+min)).substring(1);
    }

}