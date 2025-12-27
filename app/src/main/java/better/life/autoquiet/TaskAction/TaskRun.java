package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityAddEdit.BELL_ONETIME;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_SEVERAL;
import static better.life.autoquiet.activity.ActivityAddEdit.BELL_WEEKLY;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_OFF;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_WORK;
import static better.life.autoquiet.activity.ActivityMain.mainRecycleAdapter;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import better.life.autoquiet.QuietTaskGetPut;
import better.life.autoquiet.R;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.Sub.PhoneVibrate;
import better.life.autoquiet.Utils;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.Sub.AddSuffixStr;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.models.NextTask;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public final class TaskRun {

    public static void start(NextTask nt) {

        if (nt.alarmType < PHONE_VIBRATE)
            say_Started99(nt);
        else {
            start_Normal(nt);
        }
    }

    private static void say_Started99(NextTask nt) {

        switch (nt.alarmType) {
            case BELL_SEVERAL:
               TaskBellType.several(nt);
               break;

            case BELL_WEEKLY:
                TaskBellType.week(nt);
                break;
            case  BELL_ONETIME:
                TaskBellType.one(nt);
                break;

            default:
                String say = nt.subject + "AM Type 에러 "+nt.alarmType;
                sounds.sayText(say);
        }
    }

    private static void start_Normal(NextTask nt) {
        sounds.dingDong(Sounds.BEEP.START);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            String say = (nt.alarmType == PHONE_WORK) ? nt.subject
                    : new AddSuffixStr().add(nt.subject) + "시작 됩니다";
            sounds.sayText(say);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (nt.alarmType == PHONE_OFF) {
                        sounds.setSilentMode();
                    } else
                        sounds.setVibrateMode();
                    ScheduleNextTask.request("Start");
                }
            }, 30000);
            }
        }, 2000);
    }

    public static void one(NextTask nt) {
        sounds.setNormalMode();
//        if (vars.sharedManner) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = "지금은 " + nowTimeToString(System.currentTimeMillis()) +
                        " 입니다. 무음 모드 끄읏";
                sounds.sayText(say);
            }
        }, 2000);
        QuietTask qt = quietTasks.get(nt.idx);
        qt.active = false;
        quietTasks.set(0, qt);
        QuietTaskGetPut.put();
        ScheduleNextTask.request("#o");
    }

    static String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }

    public static void finish(NextTask nt) {
        sounds.setNormalMode();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                sounds.setVolumeTo(10);
                finish_Normal(nt);
            }
        }, 1000);
    }

    private static void finish_Normal(NextTask nt) {
        sounds.dingDong(Sounds.BEEP.BACK);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String s = new AddSuffixStr().add(nt.subject) + ContextProvider.get().getString(R.string.finishing_completed);
                sounds.sayText(s);
                if (nt.alarmType < PHONE_VIBRATE) {
                    QuietTask qt = quietTasks.get(nt.idx);
                    qt.active = false;
                    quietTasks.set(nt.idx, qt);
                    mainRecycleAdapter.notifyItemChanged(nt.idx);
                }
                ScheduleNextTask.request("Fin Normal");
                new Utils().deleteOldLogFiles();
                PhoneVibrate.go(1);
            }
        }, 2200);
    }

}
