package com.urrecliner.autoquiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import com.urrecliner.autoquiet.models.QuietTask;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.autoquiet.Vars.STOP_SPEAK;
import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTask;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.utils;

public class AlarmReceiver extends BroadcastReceiver {

    private static int loopCount;
    TextToSpeech textToSpeech;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (utils == null)
            utils = new Utils();

        Bundle args = intent.getBundleExtra("DATA");
        assert args != null;
        quietTask = (QuietTask) args.getSerializable("quietTask");
        assert quietTask != null;
        String caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        assert caseSFO != null;
        loopCount = quietTask.getsRepeatCount();
        switch (caseSFO) {
            case "S":   // start?
                say_Started(quietTask.getSubject(), quietTask.isVibrate());
                break;
            case "F":   // finish
                MannerMode.turn2Normal(context);
                if (loopCount > 0) {
                    loopCount = 1;
                    say_Finished(quietTask.getSubject());
                }
                break;
            case "O":   // onetime
                MannerMode.turn2Normal(context);
                quietTask.setActive(false);
                quietTasks.set(0, quietTask);
                utils.saveQuietTasksToShared();
                break;
            default:
                utils.log("Alarm Receive","Case Error " + caseSFO);
        }
        new ScheduleNextTask("Next");
    }

    void say_Started(String subject, boolean vibrate) {
        ready_TTS();
        Timer speakTimer = new Timer();
        speakTimer.schedule(new TimerTask() {
            public void run() {
                if (loopCount-- > 0) {
                    MannerMode.vibratePhone(mContext);
                    String lastCode = subject.substring(subject.length()-1);
                    String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
                    String s = nowTimeToString() + " 입니다. " + subject // 받침이 있으면 이, 없으면 가
                            + ((lastNFKD.length() == 2) ? "가": "이") +" 시작됩니다";
                    textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);
                } else {
                    speakTimer.cancel();
                    speakTimer.purge();
                    textToSpeech.stop();
                    MannerMode.turn2Quiet(mContext, vibrate);
                    Intent notification = new Intent(mActivity, NotificationService.class);
                    notification.putExtra("operation", STOP_SPEAK);
                    mContext.startService(notification);
                }
            }
        }, 3000, 5000);
    }

    void say_Finished(String subject) {
        ready_TTS();
        Timer speakTimer = new Timer();
        speakTimer.schedule(new TimerTask() {
            public void run() {
                if (loopCount-- > 0) {
                    MannerMode.vibratePhone(mContext);
                    String lastCode = subject.substring(subject.length()-1);
                    String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
                    String s = nowTimeToString() + " 입니다. " + subject // 받침이 있으면 이, 없으면 가
                            + ((lastNFKD.length() == 2) ? "가": "이") +" 종료 되었습니다";
                    textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);
                } else {
                    speakTimer.cancel();
                    speakTimer.purge();
                    textToSpeech.stop();
                }
            }
        }, 3000, 5000);
    }

    void ready_TTS() {
        textToSpeech = new TextToSpeech(mContext, status -> textToSpeech.setLanguage(Locale.getDefault()));
        textToSpeech.setPitch(1.4f);
        textToSpeech.setSpeechRate(1.3f);
    }

    String nowTimeToString() {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(System.currentTimeMillis());
    }

    static void speak_off() {
        loopCount = -1;
    }
}
