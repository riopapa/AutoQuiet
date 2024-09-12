package com.riopapa.autoquiet.Sub;

import static com.riopapa.autoquiet.Sub.ReadyTTS.myTTS;
import static com.riopapa.autoquiet.activity.ActivityMain.quietTasks;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import com.riopapa.autoquiet.ScheduleNextTask;
import com.riopapa.autoquiet.models.QuietTask;
import com.riopapa.autoquiet.quiettask.QuietTaskGetPut;

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
                myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "a");
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
