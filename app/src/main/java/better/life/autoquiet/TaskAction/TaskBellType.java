package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import android.content.Context;
import android.content.Intent;

import better.life.autoquiet.Sub.AlarmTime;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.Sub.FloatingClockService;
import better.life.autoquiet.Sub.PhoneVibrate;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.QuietTaskGetPut;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public final class TaskBellType {

    public static void one(NextTask nt) {
        Context context = ContextProvider.get();
        if (nt.vibrate)
            PhoneVibrate.go(1);
        sounds.beep(Sounds.BEEP.FLICK);
        new Timer().schedule(new TimerTask() {
            public void run() {
                String say = nt.subject + " 체크";
                sounds.sayTask(say);
                if (nt.clock) {
                    Intent serviceIntent = new Intent(context, FloatingClockService.class);
                    context.startService(serviceIntent);
                }
                if (quietTasks == null)
                    QuietTaskGetPut.get();
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
                QuietTaskGetPut.put();
                ScheduleNextTask.request("TaskBellType");
            }
        }, 2500);
    }


    public static void several(NextTask nt) {

        if (nt.vibrate)
            PhoneVibrate.go(1);
        int gapSec = secRemaining(nt, System.currentTimeMillis());
        if (gapSec < 60 && gapSec > 5 && nt.several > 0)
            sounds.beep(Sounds.BEEP.FLICK);
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
                    QuietTaskGetPut.put();
                }
            }
        }, 1000);
    }

    static int secRemaining(NextTask nt, long time) {
        Calendar toDay = Calendar.getInstance();
        toDay.set(Calendar.HOUR_OF_DAY, nt.hour);
        toDay.set(Calendar.MINUTE, nt.min);
        toDay.set(Calendar.SECOND, 0);
        return (int) ((toDay.getTimeInMillis() - time)/1000);
    }

    static String nowDateToString(long time) {
        String s =  new SimpleDateFormat(" MM 월 d 일 EEEE ", Locale.getDefault()).format(time);
        return s + s;
    }

    public static void week(NextTask nt) {
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
