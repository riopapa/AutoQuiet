package better.life.autoquiet.nexttasks;

import static better.life.autoquiet.activity.ActivityMain.nextTasks;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import android.content.Context;
import android.util.Log;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.Utils;
import better.life.autoquiet.common.ContextProvider;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.widget.WidgetProvider;

import java.util.ArrayList;

public class ScheduleNextTask {

    public ScheduleNextTask(String headInfo) {
        Context context = ContextProvider.get();
        new Utils().log("ScheduleNextTask", headInfo);
        new QuietTaskGetPut().read();

        new GenerateNexTasks().gen(quietTasks);
        new AlarmTime().request(nextTasks.get(0),
                nextTasks.get(0).time, nextTasks.get(0).SFO, nextTasks.get(0).several);
        new Utils().log("Schedule", headInfo + " " +nextTasks.get(0).subject );
        WidgetProvider.update_All_Widgets(context);
    }
}