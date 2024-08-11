package com.riopapa.autoquiet.Sub;

import static com.riopapa.autoquiet.ActivityMain.mContext;
import static com.riopapa.autoquiet.ActivityMain.quietTasks;
import static com.riopapa.autoquiet.Sub.ReadyTTS.myTTS;
import static com.riopapa.autoquiet.Sub.ReadyTTS.sounds;

import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.riopapa.autoquiet.QuietTaskGetPut;
import com.riopapa.autoquiet.ScheduleNextTask;
import com.riopapa.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BellSeveral {

    public void go(AudioManager mAudioManager, QuietTask qt, int several, int qtIdx) {

        int gapSec = secRemaining(qt, System.currentTimeMillis());
        if (gapSec < 60 && gapSec > 5 && several > 0)
            sounds.beep(mContext, (qt.subject.contains("삐이")) ? Sounds.BEEP.TOSS:Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                int afterSec = secRemaining(qt, System.currentTimeMillis()) - 2;
                if (several > 0 && afterSec > 5) {
                    if (afterSec > 60) {
                        afterSec = 20;
                    } else if (new IsSilent().now(mAudioManager)) {
                        new VibratePhone(mContext, (qt.vibrate)? 1:0);
                        afterSec = afterSec / 2;
                    } else {
                        String s = (qt.sayDate) ? nowDateToString(System.currentTimeMillis()) : "";
                        s += " " + qt.subject + " 를 " + " 확인하세요, ";
                        myTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null, "svrl");
                        if (afterSec < 20)
                            afterSec = 10;
                        else
                            afterSec = afterSec / 2;
                    }
                    if (afterSec > 5) {
                        long nextTime = System.currentTimeMillis() + afterSec * 1000L;
                        new AlarmTime().request(mContext, qt, nextTime, "S", several);
                    } else {
                        qt.active = false;
                        quietTasks.set(qtIdx, qt);
                        new QuietTaskGetPut().put(quietTasks);
                        new ScheduleNextTask(mContext, "end3");
                    }
                } else {
                    Log.w("a Schedule ","New task");
                    qt.active = false;
                    quietTasks.set(qtIdx, qt);
                    new QuietTaskGetPut().put(quietTasks);
                    new ScheduleNextTask(mContext, "end F");
                }
            }
        }, 600);

    }

    int secRemaining(QuietTask qt, long time) {
        Calendar toDay = Calendar.getInstance();
        toDay.set(Calendar.HOUR_OF_DAY, qt.begHour);
        toDay.set(Calendar.MINUTE, qt.begMin);
        toDay.set(Calendar.SECOND, 0);
        return (int) ((toDay.getTimeInMillis() - time)/1000);
    }


    String nowDateToString(long time) {
        String s =  new SimpleDateFormat(" MM 월 d 일 EEEE ", Locale.getDefault()).format(time);
        return s + s;
    }

}