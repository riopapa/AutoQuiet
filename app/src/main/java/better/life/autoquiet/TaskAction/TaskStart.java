package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityAddEdit.BELL_ONETIME;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_SEVERAL;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_WEEKLY;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_OFF;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_WORK;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.Sub.AddSuffixStr;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.models.NextTask;

import java.util.Timer;
import java.util.TimerTask;

public final class TaskStart {

    public static void go(NextTask nt) {

        if (nt.alarmType < PHONE_VIBRATE)
            say_Started99(nt);
        else {
            start_Normal(nt);
        }
    }

    private static void say_Started99(NextTask nt) {

        if      (nt.alarmType == BELL_SEVERAL) {
            BellSeveral.go(nt);

        } else if (nt.alarmType == BELL_WEEKLY)
            BellWeekly.go(nt);

        else if (nt.alarmType == BELL_ONETIME)
            BellOneTime.go(nt);

        else {
            String say = nt.subject + "AlarmType 에러 확인 "+nt.alarmType;
            sounds.sayTask(say);
        }
    }

    private static void start_Normal(NextTask nt) {
        sounds.beep(Sounds.BEEP.START);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            String say = (nt.alarmType == PHONE_WORK) ? nt.subject
                    : new AddSuffixStr().add(nt.subject) + "시작 됩니다";
            sounds.sayTask(say);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (nt.alarmType == PHONE_OFF) {
                        sounds.setSilentMode();
                    } else
                        sounds.setVibrateMode();
                    ScheduleNextTask.request("Start");
                }
            }, 10000);
            }
        }, 3000);
    }
}
