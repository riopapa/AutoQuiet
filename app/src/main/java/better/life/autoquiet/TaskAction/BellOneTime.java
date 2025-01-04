package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.AlarmReceiver.sounds;
import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import better.life.autoquiet.R;
import better.life.autoquiet.Sub.NotificationHelper;
import better.life.autoquiet.common.VibratePhone;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.models.QuietTask;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class BellOneTime {

    public void go(QuietTask qt, int qtIdx) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        if (qt.vibrate)
            new VibratePhone(mContext, 1);
        new Timer().schedule(new TimerTask() {
            public void run() {
                String say = qt.subject + " 체크";
                sounds.myTTS.sayTask(say);
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
                } else
                    qt.active = false;
                quietTasks.set(qtIdx, qt);
                NotificationHelper notificationHelper = new NotificationHelper(mContext);
                notificationHelper.sendNotification(R.drawable.bell_onetime,
                        qt.subject, "OneTime Check");
                new QuietTaskGetPut().put(quietTasks);
            }
        }, 1500);
        new ScheduleNextTask(mContext, "ended1");
    }

}
