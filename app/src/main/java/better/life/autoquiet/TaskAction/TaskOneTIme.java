package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.AlarmReceiver.sounds;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import android.content.Context;

import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.Sub.MannerMode;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TaskOneTIme {
    public void go(Context context, QuietTask qt) {
        new MannerMode().turn2Normal(context);
//        if (vars.sharedManner) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = "지금은 " + nowTimeToString(System.currentTimeMillis()) +
                        " 입니다. 무음 모드가 끝났습니다";
                sounds.myTTS.sayTask(say);
            }
        }, 2000);
        qt.active = false;
        quietTasks.set(0, qt);
        new QuietTaskGetPut().put(quietTasks);
        new ScheduleNextTask(context, "After oneTime");
    }

    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }

}
