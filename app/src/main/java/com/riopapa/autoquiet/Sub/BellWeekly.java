package com.riopapa.autoquiet.Sub;

import static com.riopapa.autoquiet.activity.ActivityMain.mContext;
import static com.riopapa.autoquiet.Sub.ReadyTTS.myTTS;
import static com.riopapa.autoquiet.Sub.ReadyTTS.sounds;

import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import com.riopapa.autoquiet.ScheduleNextTask;
import com.riopapa.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellWeekly {

    public void go(AudioManager mAudioManager, QuietTask qt) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (new IsSilent().now(mAudioManager)) {
                    new VibratePhone(mContext, (qt.vibrate)? 1:0);
                } else {
                    String say = qt.subject + " 를 확인";
                    myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "now");
                }
                new ScheduleNextTask(mContext, "event");
            }
        }, 1500);

    }

}
