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
    static int several;
    static String timeInfoS, timeInfo, msg;
    static String timeInfoN;
    static NextTwoTasks n2;

    public SetUpComingTask(Context context, ArrayList<QuietTask> quietTasks, String headInfo) {

        n2 = new NextTwoTasks(quietTasks);
        QuietTask qNxt = quietTasks.get(n2.saveIdxN);
        QuietTask qThis = quietTasks.get(n2.saveIdx);
        timeInfoS = getHourMin(qThis.begHour, qThis.begMin);
        boolean end99Nxt = qNxt.endHour == 99;
        boolean end99Now = qThis.endHour == 99;
        if (!end99Nxt) {
            timeInfoN = (n2.begEndN.equals("S")) ?
                    getHourMin(qNxt.begHour, qNxt.begMin) : getHourMin(qNxt.endHour, qNxt.endMin);
        } else {
            timeInfoN = getHourMin(qNxt.begHour, qNxt.begMin);
        }
        if (!end99Now) {
            timeInfo = (n2.begEnd.equals("S")) ?
                        getHourMin(qThis.begHour, qThis.begMin) : getHourMin(qThis.endHour, qThis.endMin);
            msg = headInfo + " " + n2.subject + "\n" + timeInfo
                    + " " + n2.soonOrUntil + " " + n2.begEnd;
            if  (n2.begEnd.equals("S"))
                msg += " ~ " + getHourMin(qThis.endHour, qThis.endMin);
            several = (qThis.sayDate) ? 3:0;  // finish 인데 sayDate 가 있으면 여러 번 울림
        } else {
            timeInfo = getHourMin(qThis.begHour, qThis.begMin);
            msg = headInfo + "\n" + timeInfo + " " + n2.subject;
            several = (n2.icon == R.drawable.bell_several) ? 2:0;
        }

        new Utils(context).log("SetUpComingTask",msg);
        updateNotyBar(context);
        new SetAlarmTime().request(context, qThis, n2.nextTime, n2.begEnd, several);

    }

    private static void updateNotyBar(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("beg", timeInfo);
        intent.putExtra("end", n2.soonOrUntil);
        intent.putExtra("subject", n2.subject);
        intent.putExtra("stop_repeat", false);
        intent.putExtra("icon", n2.icon);
        int iconNow = n2.icon;
        if (n2.icon != R.drawable.bell_several && n2.icon != R.drawable.bell_tomorrow &&
            n2.icon != R.drawable.bell_onetime && n2.icon != R.drawable.bell_once_gone &&
            n2.begEnd.equals("S"))
            iconNow = R.drawable.phone_normal;
        intent.putExtra("iconNow", iconNow);

        intent.putExtra("begN", timeInfoN);
        intent.putExtra("endN", n2.soonOrUntilN);
        intent.putExtra("subjectN", n2.subjectN);
        intent.putExtra("iconN", n2.iconN);
        context.startForegroundService(intent);

        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        sharedEditor.putString("begN", timeInfoN);
        sharedEditor.putString("endN", n2.soonOrUntilN);
        sharedEditor.putString("subjectN", n2.subjectN);
        sharedEditor.putInt("icon", n2.icon);
        sharedEditor.putInt("iconN", n2.iconN);
        sharedEditor.apply();

    }

    private String getHourMin(int hour, int min) {
        return (""+ (100+hour)).substring(1) + ":" + (""+(100+min)).substring(1);
    }

}