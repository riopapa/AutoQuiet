package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BellSeveral {

    public void go(NextTask nt) {

        if (nt.vibrate)
            phoneVibrate.go(1);
        int gapSec = secRemaining(nt, System.currentTimeMillis());
        if (gapSec < 60 && gapSec > 5 && nt.several > 0)
            sounds.beep(Sounds.BEEP.BEEP);
        new Timer().schedule(new TimerTask() {
            public void run() {
            int afterSec = secRemaining(nt, System.currentTimeMillis()) - 2;
            if (nt.several > 0 && afterSec > 5) {
                if (afterSec > 60) {
                    afterSec = 20;
                } else if (sounds.isPhoneQuiet()) {
                    afterSec = afterSec / 2;
                } else {
                    String s = (nt.sayDate) ? nowDateToString(System.currentTimeMillis()) : "";
                    s += " " + nt.subject + " 를 " + " 확인하세요, ";
                    sounds.sayTask(s);
                    if (afterSec < 20)
                        afterSec = 10;
                    else
                        afterSec = afterSec / 2;
                }
                long nextTime = System.currentTimeMillis() + afterSec * 1000L;
                new AlarmTime().request(nt, nextTime, "S", nt.several);
            } else {
                QuietTask qt = quietTasks.get(nt.idx);
                qt.active = false;
                quietTasks.set(nt.idx, qt);
                new QuietTaskGetPut().save();
            }
            }
        }, 1000);
    }

    int secRemaining(NextTask nt, long time) {
        Calendar toDay = Calendar.getInstance();
        toDay.set(Calendar.HOUR_OF_DAY, nt.hour);
        toDay.set(Calendar.MINUTE, nt.min);
        toDay.set(Calendar.SECOND, 0);
        return (int) ((toDay.getTimeInMillis() - time)/1000);
    }

    String nowDateToString(long time) {
        String s =  new SimpleDateFormat(" MM 월 d 일 EEEE ", Locale.getDefault()).format(time);
        return s + s;
    }

}
