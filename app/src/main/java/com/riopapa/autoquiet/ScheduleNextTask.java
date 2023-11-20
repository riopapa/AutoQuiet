package com.riopapa.autoquiet;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.riopapa.autoquiet.Sub.AlarmTime;
import com.riopapa.autoquiet.Sub.NextTwoTasks;
import com.riopapa.autoquiet.Sub.ShowNotification;
import com.riopapa.autoquiet.models.QuietTask;


import java.util.ArrayList;

public class ScheduleNextTask {
    static String timeInfo, msg;
    static String timeInfoN;

    public final static int AHEAD_TIME = 40000;
    ArrayList<QuietTask> quietTasks;
    static NextTwoTasks nxtTsk;

    public ScheduleNextTask(Context context, String headInfo) {

        quietTasks = new QuietTaskGetPut().get(context);

        nxtTsk = new NextTwoTasks(quietTasks);

        timeInfo = getHourMin(nxtTsk.sHour, nxtTsk.sMin);
        timeInfoN = getHourMin(nxtTsk.sHourN, nxtTsk.sMinN);

        msg = headInfo + "\n" + timeInfo + " " + nxtTsk.subject;

        new Utils(context).log("ScheduleNextTask",msg);
        updateNotyBar(context);

        new AlarmTime().request(context, quietTasks.get(nxtTsk.saveIdx),
                nxtTsk.nextTime, nxtTsk.caseSFO, nxtTsk.several);

    }

    private static void updateNotyBar(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("beg", timeInfo);
        intent.putExtra("end", nxtTsk.beginOrEnd);
        intent.putExtra("subject", nxtTsk.subject);
        intent.putExtra("stop_repeat", false);
        intent.putExtra("icon", nxtTsk.icon);
        int iconNow = nxtTsk.icon;
        if (nxtTsk.caseSFO.equals("S"))
            iconNow = R.drawable.phone_normal;
        intent.putExtra("iconNow", iconNow);

        intent.putExtra("begN", timeInfoN);
        intent.putExtra("endN", nxtTsk.beginOrEndN);
        intent.putExtra("subjectN", nxtTsk.subjectN);
        intent.putExtra("iconN", nxtTsk.iconN);
        new ShowNotification(context, intent);

        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        sharedEditor.putString("begN", timeInfoN);
        sharedEditor.putString("endN", nxtTsk.beginOrEndN);
        sharedEditor.putString("subjectN", nxtTsk.subjectN);
        sharedEditor.putInt("icon", nxtTsk.icon);
        sharedEditor.putInt("iconN", nxtTsk.iconN);
        sharedEditor.apply();

    }

    private String getHourMin(int hour, int min) {
        return (""+ (100+hour)).substring(1) + ":" + (""+(100+min)).substring(1);
    }

}