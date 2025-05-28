package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TaskOneTIme {
    public void go(NextTask nt) {
        sounds.setNormalMode();
//        if (vars.sharedManner) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = "지금은 " + nowTimeToString(System.currentTimeMillis()) +
                        " 입니다. 무음 모드가 끝났습니다";
                sounds.sayTask(say);
            }
        }, 2000);
        QuietTask qt = quietTasks.get(nt.idx);
        qt.active = false;
        quietTasks.set(0, qt);
        new QuietTaskGetPut().save();
        new ScheduleNextTask("After oneTime");
    }

    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }

}
