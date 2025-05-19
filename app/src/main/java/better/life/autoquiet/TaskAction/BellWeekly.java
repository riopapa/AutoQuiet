package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import android.content.Context;
import android.content.Intent;

import better.life.autoquiet.common.ContextProvider;
import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.common.FloatingClockService;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.NextTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellWeekly {

    public void go(NextTask nt) {
        if (nt.vibrate)
            phoneVibrate.go(1);
        sounds.beep(Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
            if (nt.clock) {
                Context context = ContextProvider.get();
                Intent serviceIntent = new Intent(context, FloatingClockService.class);
                context.startService(serviceIntent);
            }
            String say = nt.subject + " 를 확인";
            sounds.sayTask(say);
            new ScheduleNextTask("event");
            }
        }, 1500);
    }

}
