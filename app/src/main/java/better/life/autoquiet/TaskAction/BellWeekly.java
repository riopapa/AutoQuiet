package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.AlarmReceiver.sounds;

import android.content.Intent;

import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.common.FloatingClockService;
import better.life.autoquiet.common.VibratePhone;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.NextTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellWeekly {

    public void go(NextTask nt) {
        sounds.beep(Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (nt.clock) {
                    Intent serviceIntent = new Intent(mContext, FloatingClockService.class);
                    mContext.startService(serviceIntent);
                }
                if (nt.vibrate)
                    new VibratePhone(mContext, 1);
                String say = nt.subject + " 를 확인";
                sounds.sayTask(say);
//                NotificationHelper notificationHelper = new NotificationHelper(mContext);
//                notificationHelper.sendNotification(R.drawable.bell_weekly,
//                        nt.subject, "Weekly Check "+nt.subject);
                new ScheduleNextTask(mContext, "event");
            }
        }, 1500);
    }

}
