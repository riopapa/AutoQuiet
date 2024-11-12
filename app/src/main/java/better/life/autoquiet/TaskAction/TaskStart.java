package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityAddEdit.BELL_ONETIME;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_SEVERAL;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_WEEKLY;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_OFF;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_WORK;
import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.common.MyTTS.sounds;

import better.life.autoquiet.AlarmReceiver;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.Sub.AddSuffixStr;
import better.life.autoquiet.Sub.AdjVolumes;
import better.life.autoquiet.Sub.MannerMode;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class TaskStart {

    QuietTask qt;
    public void go(QuietTask qT, int several, int qtIdx) {
        this.qt = qT;
        if (qt.alarmType < PHONE_VIBRATE)
            say_Started99(several, qtIdx);
        else {
            start_Normal();
        }
    }

    private void say_Started99(int several, int qtIdx) {

        String subject = qt.subject;

        if      (qt.alarmType == BELL_SEVERAL) {
            new BellSeveral().go(qt, several, qtIdx);

        } else if (qt.alarmType == BELL_WEEKLY)
            new BellWeekly().go(qt);

        else if (qt.alarmType == BELL_ONETIME)
            new BellOneTime().go(qt, qtIdx);

        else {
            String say = subject + "AlarmType 에러 확인 "+qt.alarmType;
            AlarmReceiver.myTTS.sayTask(say);
            new ScheduleNextTask(mContext, "ended Err");
        }
    }

    private void start_Normal() {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = (qt.alarmType == PHONE_WORK) ? qt.subject : new AddSuffixStr().add(qt.subject) + "시작 됩니다";
                AlarmReceiver.myTTS.sayTask(say);
            }
        }, 800);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if ((qt.alarmType == PHONE_WORK))
                    new AdjVolumes(mContext, AdjVolumes.VOL.WORK_ON);
                else {
                    new AdjVolumes(mContext, AdjVolumes.VOL.FORCE_OFF);
                    new MannerMode().turn2Quiet(mContext, qt.alarmType != PHONE_OFF);
                }
                new ScheduleNextTask(mContext, "Norm");
            }
        }, 5000);
    }

}
