package better.life.autoquiet.nexttasks;


import static better.life.autoquiet.activity.ActivityMain.nextTasks;

import android.content.Context;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.widget.WidgetProvider;

import java.util.ArrayList;

public class ScheduleNextTask {
    ArrayList<QuietTask> quietTasks;

    public ScheduleNextTask(Context context, String headInfo) {

        quietTasks = new QuietTaskGetPut().get(context);

        new GenerateNexTasks().gen(quietTasks);
//        Log.w("schedule Next "+headInfo, "Next Tasks : " + nextTasks.size());
//        timeInfo = getHourMin(nxtTsk.sHour, nxtTsk.sMin);
//        timeInfoN = getHourMin(nxtTsk.sHourN, nxtTsk.sMinN);
//        updateNotyBar(context);

        new AlarmTime().request(context, nextTasks.get(0),
                nextTasks.get(0).time, nextTasks.get(0).SFO, nextTasks.get(0).several);
        WidgetProvider.update_All_Widgets(context);
    }
}