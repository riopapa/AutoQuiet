package better.life.autoquiet.nexttasks;

import static better.life.autoquiet.activity.ActivityMain.nextTasks;

import android.content.Context;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.Utils;
import better.life.autoquiet.common.ContextProvider;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.widget.WidgetProvider;

import java.util.ArrayList;

public class ScheduleNextTask {
    ArrayList<QuietTask> quietTasks;

    public ScheduleNextTask(String headInfo) {
        Context context = ContextProvider.get();
        quietTasks = new QuietTaskGetPut().get();

        new GenerateNexTasks().gen(quietTasks);
        new AlarmTime().request(nextTasks.get(0),
                nextTasks.get(0).time, nextTasks.get(0).SFO, nextTasks.get(0).several);
        new Utils().log("Schedule", headInfo + " " +nextTasks.get(0).subject + " " + nextTasks.get(0).time);
        WidgetProvider.update_All_Widgets(context);
    }
}