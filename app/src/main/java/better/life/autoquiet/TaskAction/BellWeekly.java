package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.AlarmReceiver.sounds;
import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;

import android.content.Intent;

import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.common.FloatingClockService;
import better.life.autoquiet.common.PhoneVibrate;
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
                Intent serviceIntent = new Intent(mContext, FloatingClockService.class);
                mContext.startService(serviceIntent);
            }
            String say = nt.subject + " 를 확인";
            sounds.sayTask(say);
            new ScheduleNextTask(mContext, "event");
            }
        }, 1500);
    }

}
