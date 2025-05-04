package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.AlarmReceiver.sounds;
import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import android.content.Intent;
import better.life.autoquiet.common.FloatingClockService;
import better.life.autoquiet.common.VibratePhone;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellOneTime {

    public void go(NextTask nt) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        if (nt.vibrate)
            new VibratePhone(mContext, 1);
        new Timer().schedule(new TimerTask() {
            public void run() {
                String say = nt.subject + " 체크";
                sounds.sayTask(say);
//                if (nt.nextDay) {
//                    Calendar cal = Calendar.getInstance();
//                    cal.setTimeInMillis(System.currentTimeMillis());
//                    int WKStart = cal.get(Calendar.DAY_OF_WEEK) - 1; // 1 for sunday
//                    nt.week[WKStart] = false;
//                    if (WKStart < 5)
//                        WKStart++;
//                    else
//                        WKStart = 1;
//                    nt.week[WKStart] = true;
//                } else
                if (nt.clock) {
                    Intent serviceIntent = new Intent(mContext, FloatingClockService.class);
                    mContext.startService(serviceIntent);
                }
                if (nt.vibrate)
                    new VibratePhone(mContext, 1);
                QuietTask qt = quietTasks.get(nt.idx);
                qt.active = false;
                quietTasks.set(nt.idx, qt);
                new QuietTaskGetPut().put(quietTasks);

//                NotificationHelper notificationHelper = new NotificationHelper(mContext);
//                notificationHelper.sendNotification(R.drawable.bell_onetime,
//                        nt.subject, "OneTime Check");
            }
        }, 1500);
        new ScheduleNextTask(mContext, "BellOneTime");
    }
}
