package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import android.content.Context;
import android.content.Intent;

import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.Sub.FloatingClockService;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.models.QuietTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class BellOneTime {

    public void go(NextTask nt) {
        Context context = ContextProvider.get();
        if (nt.vibrate)
            phoneVibrate.go(1);
        sounds.beep(Sounds.BEEP.BEEP);
        new Timer().schedule(new TimerTask() {
            public void run() {
                String say = nt.subject + " 체크";
                sounds.sayTask(say);
                if (nt.clock) {
                    Intent serviceIntent = new Intent(context, FloatingClockService.class);
                    context.startService(serviceIntent);
                }
                if (quietTasks == null)
                    new QuietTaskGetPut().read();
                QuietTask qt = quietTasks.get(nt.idx);
                if (qt.nextDay) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());
                    int WKStart = cal.get(Calendar.DAY_OF_WEEK) - 1; // 1 for sunday
                    qt.week[WKStart] = false;
                    if (WKStart < 5)
                        WKStart++;
                    else
                        WKStart = 1;
                    qt.week[WKStart] = true;
                }
                qt.active = false;
                quietTasks.set(nt.idx, qt);
                new QuietTaskGetPut().save();
                new ScheduleNextTask("BellOneTime");
            }
        }, 2500);
    }
}
