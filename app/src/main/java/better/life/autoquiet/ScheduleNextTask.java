package better.life.autoquiet;


import static better.life.autoquiet.activity.ActivityMain.nextAlertTime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.Sub.NextTwoTasks;
import better.life.autoquiet.Sub.ShowNotification;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;

import java.util.ArrayList;

public class ScheduleNextTask {
    String timeInfo;
    String timeInfoN;

    public final static int AHEAD_TIME = 40000;
    ArrayList<QuietTask> quietTasks;
    static NextTwoTasks nxtTsk;

    public ScheduleNextTask(Context context, String headInfo) {

        quietTasks = new QuietTaskGetPut().get(context);

        nxtTsk = new NextTwoTasks(quietTasks);

        timeInfo = getHourMin(nxtTsk.sHour, nxtTsk.sMin);
        timeInfoN = getHourMin(nxtTsk.sHourN, nxtTsk.sMinN);

//        if (headInfo.isEmpty()) {
//            String msg = headInfo + "\n" + timeInfo + " " + nxtTsk.subject;
//            new Utils(context).log("SchdNextTask", msg);
//        }
        updateNotyBar(context);

        nextAlertTime = nxtTsk.nextTime;
        new AlarmTime().request(context, quietTasks.get(nxtTsk.saveIdx),
                nxtTsk.nextTime, nxtTsk.caseSFOW, nxtTsk.several);
//        NextTask nextTask = new NextTask();
//        nextTask.subject = nxtTsk.subject;
//        nextTask.begHour = nxtTsk.sHour;
//        nextTask.begMin = nxtTsk.sMin;
//        nextTask.time = nxtTsk.nextTime;
//        nextTask.caseSFOW = nxtTsk.caseSFOW;
//
//        Bundle args = new Bundle();
//        args.putSerializable("nextTask", nextTask);
//        SharedPreferences sharedPref = context.getSharedPreferences("next", Context.MODE_PRIVATE);
//        sharedPref.edit().putString("next", args.toString()).apply();
    }

    private void updateNotyBar(Context context) {
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