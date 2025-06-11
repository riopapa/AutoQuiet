package better.life.autoquiet.nexttasks;

import static better.life.autoquiet.activity.ActivityMain.nextTasks;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import android.content.Context;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.Utility;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.widget.WidgetProvider;

public class ScheduleNextTask {

    public ScheduleNextTask(String headInfo) {
        Context context = ContextProvider.get();
        new QuietTaskGetPut().read();
        new GenerateNexTasks().gen(quietTasks);
        new AlarmTime().request(nextTasks.get(0),
                nextTasks.get(0).time, nextTasks.get(0).SFO, nextTasks.get(0).several);
        new Utility().log("Schedule", headInfo + " " +nextTasks.get(0).subject );
        WidgetProvider.update_All_Widgets(context);
    }
}