package com.riopapa.autoquiet.Sub;

import static com.riopapa.autoquiet.ActivityAddEdit.BELL_ONETIME;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_SEVERAL;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_WEEKLY;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_OFF;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_VIBRATE;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_WORK;
import static com.riopapa.autoquiet.ActivityMain.mContext;
import static com.riopapa.autoquiet.Sub.ReadyTTS.myTTS;
import static com.riopapa.autoquiet.Sub.ReadyTTS.sounds;

import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import com.riopapa.autoquiet.ScheduleNextTask;
import com.riopapa.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class TaskStart {

    QuietTask qt;
    public void go(AudioManager mAudioManager, QuietTask qT, int several, int qtIdx) {
        this.qt = qT;
        if (qt.alarmType < PHONE_VIBRATE)
            say_Started99(mAudioManager, several, qtIdx);
        else {
            start_Normal();
        }
    }

    private void say_Started99(AudioManager mAudioManager, int several, int qtIdx) {

        String subject = qt.subject;

        if      (qt.alarmType == BELL_SEVERAL) {
            new BellSeveral().go(mAudioManager, qt, several, qtIdx);

        } else if (qt.alarmType == BELL_WEEKLY)
            new BellWeekly().go(mAudioManager, qt);

        else if (qt.alarmType == BELL_ONETIME)
            new BellOneTime().go(mAudioManager, qt, qtIdx);

        else {
            String say = subject + "AlarmType 에러 확인 "+qt.alarmType;
            myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "99");
            new ScheduleNextTask(mContext, "ended Err");
        }
    }

    private void start_Normal() {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = (qt.alarmType == PHONE_WORK) ? qt.subject : new AddSuffixStr().add(qt.subject) + "시작 됩니다";
                myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "startN");
            }
        }, 800);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if ((qt.alarmType == PHONE_WORK))
                    new AdjVolumes(mContext, AdjVolumes.VOL.WORK);
                else {
                    new AdjVolumes(mContext, AdjVolumes.VOL.FORCE_OFF);
                    new MannerMode().turn2Quiet(mContext, qt.alarmType != PHONE_OFF);
                }
                new ScheduleNextTask(mContext, "Norm");
            }
        }, 5000);
    }

}
