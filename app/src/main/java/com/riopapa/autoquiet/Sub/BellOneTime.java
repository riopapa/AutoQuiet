package com.riopapa.autoquiet.Sub;

import static com.riopapa.autoquiet.activity.ActivityMain.mContext;
import static com.riopapa.autoquiet.activity.ActivityMain.quietTasks;
import static com.riopapa.autoquiet.Sub.ReadyTTS.myTTS;
import static com.riopapa.autoquiet.Sub.ReadyTTS.sounds;

import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import com.riopapa.autoquiet.quiettask.QuietTaskGetPut;
import com.riopapa.autoquiet.ScheduleNextTask;
import com.riopapa.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellOneTime {

    public void go(AudioManager mAudioManager, QuietTask qt, int qtIdx) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        if (new IsSilent().now(mAudioManager)) {
            new VibratePhone(mContext, (qt.vibrate)? 1:0);
        } else {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    String say = qt.subject + " 체크";
                    myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "one");
                    qt.active = false;
                    quietTasks.set(qtIdx, qt);
                    new QuietTaskGetPut().put(quietTasks);
                }
            }, 1500);
        }
        new ScheduleNextTask(mContext, "ended1");
    }

}
