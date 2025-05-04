package better.life.autoquiet;


import static better.life.autoquiet.activity.ActivityMain.nextTasks;

import android.content.Context;
import android.util.Log;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.Sub.GenerateNexTasks;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;

import java.util.ArrayList;

public class ScheduleNextTask {
    ArrayList<QuietTask> quietTasks;

    public ScheduleNextTask(Context context, String headInfo) {

        quietTasks = new QuietTaskGetPut().get(context);

        new GenerateNexTasks().gen(quietTasks);
        Log.w("schedule Next "+headInfo, "Next Tasks : " + nextTasks.size());
//        timeInfo = getHourMin(nxtTsk.sHour, nxtTsk.sMin);
//        timeInfoN = getHourMin(nxtTsk.sHourN, nxtTsk.sMinN);
//        updateNotyBar(context);

        new AlarmTime().request(context, nextTasks.get(0),
                nextTasks.get(0).time, nextTasks.get(0).SFO, nextTasks.get(0).several);
        LogWidgetProvider.update_All_Widgets(context);
    }

//    private void updateNotyBar(Context context) {
//        NextTask qtB = nextTasks.get(0);
//        NextTask qtE = nextTasks.get(1);
//
//        Intent intent = new Intent(context, NotificationService.class);
//        intent.putExtra("beg", getHourMin(qtB.hour, qtB.min));
//        intent.putExtra("end", );
//        intent.putExtra("subject", qtB.subject);
//        intent.putExtra("stop_repeat", false);
//        intent.putExtra("icon", alarmIcons[qtB.alarmType]);
//        intent.putExtra("iconNow", alarmIcons[qtB.alarmType]);
//
//        intent.putExtra("begN", getHourMin(qtB.begHour, qtE.begMin)));
//        intent.putExtra("endN", getHourMin(qtB.begHour, qtB.begMin)));
//        intent.putExtra("subjectN", nxtTsk.subjectN);
//        intent.putExtra("iconN", nxtTsk.iconN);
//        new ShowNotification().show(context, intent);
//
//        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
//        SharedPreferences.Editor sharedEditor = sharedPref.edit();
//        sharedEditor.putString("begN", timeInfoN);
//        sharedEditor.putString("endN", nxtTsk.beginOrEndN);
//        sharedEditor.putString("subjectN", nxtTsk.subjectN);
//        sharedEditor.putInt("icon", nxtTsk.icon);
//        sharedEditor.putInt("iconN", nxtTsk.iconN);
//        sharedEditor.apply();
//
//    }

    private String getHourMin(int hour, int min) {
        return (String.valueOf(100 + hour)).substring(1) + ":"
                + (String.valueOf(100 + min)).substring(1);
    }
}