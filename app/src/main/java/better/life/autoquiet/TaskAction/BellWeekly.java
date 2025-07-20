package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.sounds;

import android.content.Context;
import android.content.Intent;

import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.Sub.PhoneVibrate;
import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.Sub.FloatingClockService;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.models.NextTask;

import java.util.Timer;
import java.util.TimerTask;

public final class BellWeekly {

    public static void go(NextTask nt) {
        if (nt.vibrate)
            PhoneVibrate.go(1);
        sounds.beep(Sounds.BEEP.WEEK);
        new Timer().schedule(new TimerTask() {
            public void run() {
            if (nt.clock) {
                Context context = ContextProvider.get();
                Intent serviceIntent = new Intent(context, FloatingClockService.class);
                context.startService(serviceIntent);
            }
            String say = nt.subject + " 를 확인";
            sounds.sayTask(say);
            ScheduleNextTask.request("event");
            }
        }, 1500);
    }

}
