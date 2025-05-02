package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.AlarmReceiver.sounds;

import android.content.Intent;

import better.life.autoquiet.R;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.Sub.NotificationHelper;
import better.life.autoquiet.common.FloatingClockService;
import better.life.autoquiet.common.VibratePhone;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellWeekly {

    public void go(QuietTask qt) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (qt.clock) {
                    Intent serviceIntent = new Intent(mContext, FloatingClockService.class);
                    mContext.startService(serviceIntent);
                }
                if (qt.vibrate)
                    new VibratePhone(mContext, 1);
                String say = qt.subject + " 를 확인";
                sounds.sayTask(say);
//                NotificationHelper notificationHelper = new NotificationHelper(mContext);
//                notificationHelper.sendNotification(R.drawable.bell_weekly,
//                        qt.subject, "Weekly Check "+qt.subject);
                new ScheduleNextTask(mContext, "event");
            }
        }, 1500);
    }

}
