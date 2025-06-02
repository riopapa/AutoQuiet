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
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.NextTask;

import java.util.Timer;
import java.util.TimerTask;

public class TaskStart {

    NextTask nt;

    public void go(NextTask nt) {
        this.nt = nt;

        if (nt.alarmType < PHONE_VIBRATE)
            say_Started99();
        else {
            start_Normal();
        }
    }

    private void say_Started99() {

        if      (nt.alarmType == BELL_SEVERAL) {
            new BellSeveral().go(nt);

        } else if (nt.alarmType == BELL_WEEKLY)
            new BellWeekly().go(nt);

        else if (nt.alarmType == BELL_ONETIME)
            new BellOneTime().go(nt);

        else {
            String say = nt.subject + "AlarmType 에러 확인 "+nt.alarmType;
            sounds.sayTask(say);
        }
    }

    private void start_Normal() {
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
                    new ScheduleNextTask("Start");
                }
            }, 5000);
            }
        }, 2000);

    }
}
