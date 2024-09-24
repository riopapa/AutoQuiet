package better.life.autoquiet;


import static better.life.autoquiet.activity.ActivityMain.nextAlertTime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.Sub.NextTwoTasks;
import better.life.autoquiet.Sub.ShowNotification;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;

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

        new Utils(context).log("SchdNextTask",msg);
        updateNotyBar(context);

        nextAlertTime = nxtTsk.nextTime;
        new AlarmTime().request(context, quietTasks.get(nxtTsk.saveIdx),
                nxtTsk.nextTime, nxtTsk.caseSFOW, nxtTsk.several);

    }

    private static void updateNotyBar(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.putExtra("beg", timeInfo);
        intent.putExtra("end", nxtTsk.beginOrEnd);
        intent.putExtra("subject", nxtTsk.subject);
        intent.putExtra("stop_repeat", false);
        intent.putExtra("icon", nxtTsk.icon);
        int iconNow = nxtTsk.icon;
        if (nxtTsk.caseSFOW.equals("S"))
            iconNow = R.drawable.phone_normal;

        intent.putExtra("iconNow", iconNow);

        intent.putExtra("begN", timeInfoN);
        intent.putExtra("endN", nxtTsk.beginOrEndN);
        intent.putExtra("subjectN", nxtTsk.subjectN);
        intent.putExtra("iconN", nxtTsk.iconN);
        new ShowNotification().show(context, intent);

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
        return (String.valueOf(100 + hour)).substring(1) + ":"
                + (String.valueOf(100 + min)).substring(1);
    }

}