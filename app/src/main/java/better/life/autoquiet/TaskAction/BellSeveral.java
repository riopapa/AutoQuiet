package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.AlarmReceiver.sounds;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.common.VibratePhone;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BellSeveral {

    public void go(QuietTask qt, int several, int qtIdx) {

        int gapSec = secRemaining(qt, System.currentTimeMillis());
        if (gapSec < 60 && gapSec > 5 && several > 0)
            sounds.beep(mContext, (qt.subject.contains("삐이")) ? Sounds.BEEP.TOSS:Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                int afterSec = secRemaining(qt, System.currentTimeMillis()) - 2;
                if (qt.vibrate)
                    new VibratePhone(mContext, 1);
                if (several > 0 && afterSec > 5) {
                    if (afterSec > 60) {
                        afterSec = 20;
                    } else if (sounds.isQuiet()) {
                        afterSec = afterSec / 2;
                    } else {
                        String s = (qt.sayDate) ? nowDateToString(System.currentTimeMillis()) : "";
                        s += " " + qt.subject + " 를 " + " 확인하세요, ";
                        sounds.myTTS.sayTask(s);
                        if (afterSec < 20)
                            afterSec = 10;
                        else
                            afterSec = afterSec / 2;
                    }
                    long nextTime = System.currentTimeMillis() + afterSec * 1000L;
                    new AlarmTime().request(mContext, qt, nextTime, "S", several);
                } else {
//                    Log.w("a Schedule ","New task");
                    qt.active = false;
                    quietTasks.set(qtIdx, qt);
                    new QuietTaskGetPut().put(quietTasks);
                    new ScheduleNextTask(mContext, "end F");
                }
            }
        }, 600);

    }

    int secRemaining(QuietTask qt, long time) {
        Calendar toDay = Calendar.getInstance();
        toDay.set(Calendar.HOUR_OF_DAY, qt.begHour);
        toDay.set(Calendar.MINUTE, qt.begMin);
        toDay.set(Calendar.SECOND, 0);
        return (int) ((toDay.getTimeInMillis() - time)/1000);
    }


    String nowDateToString(long time) {
        String s =  new SimpleDateFormat(" MM 월 d 일 EEEE ", Locale.getDefault()).format(time);
        return s + s;
    }

}
