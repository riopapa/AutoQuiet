package com.urrecliner.autoquiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import com.urrecliner.autoquiet.Sub.NextAlarm;
import com.urrecliner.autoquiet.Sub.Sounds;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.Sub.MannerMode;
import com.urrecliner.autoquiet.Sub.VarsGetPut;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmReceiver extends BroadcastReceiver {

    TextToSpeech myTTS;
    ArrayList<QuietTask> quietTasks;
    QuietTask qT;
    Context context;
    int loop;
    String caseSFO;
    Vars vars;
    final int STOP_SPEAK = 1022;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Bundle args = intent.getBundleExtra("DATA");
        qT = (QuietTask) args.getSerializable("quietTask");
        quietTasks = new QuietTaskGetPut().get(context);
        caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        loop = Objects.requireNonNull(intent.getExtras()).getInt("loop");
        vars = new VarsGetPut().get(context);
        readyTTS();

        assert caseSFO != null;
        boolean beep = vars.sharedManner && (qT.endLoop == 1);

        switch (caseSFO) {
            case "S":   // beg?
                say_Started();
                break;
            case "F":   // end
                new MannerMode().turn2Normal(beep, context);
                say_Finished();
                break;
            case "O":   // onetime
                new MannerMode().turn2Normal(beep, context);
                if (qT.endLoop > 1) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            String say = "지금은 " + nowTimeToString(System.currentTimeMillis()) +
                                        " 입니다. 무음 모드가 끝났습니다";
                            myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                        }
                    }, 3000);
                }
                qT.setActive(false);
                quietTasks.set(0, qT);
                new QuietTaskGetPut().put(quietTasks);
                new NextTask(context, quietTasks, "After oneTime");
            break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFO);
        }
        new VarsGetPut().put(vars, context);
    }

    void say_Started() {

        boolean end99 = qT.endHour == 99;
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (end99)
                    say_Started99();
                else {
                    say_StartedNormal();
                }
            }
        }, 1500);   // after beep

        Intent notification = new Intent(context, NotificationService.class);
        notification.putExtra("operation", STOP_SPEAK);
        context.startForegroundService(notification);
    }

    /*
        loop    begLoop     endLoop     action
        0                   0           beep only
        0                   1           say task ended
        1-3                 1           say task once
        1-3                 11          say task info loop times
     */
    private void say_Started99() {
        String subject = qT.subject;
        if (loop > 0) {
            loop--;
            String say;
            if (isQuiet()) {
                say = subject;
                loop = 0;
            } else {
                say = subject + " 를 확인하세요, " +
                        ((loop == 0) ? "마지막 안내입니다 " : "") + subject + " 를 확인하세요";
            }
            myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);

            long nextTime = System.currentTimeMillis() + 70 * 1000;
            new NextAlarm().request(context, qT, nextTime,
                    "S", loop);   // loop 0 : no more
            Intent uIntent = new Intent(context, NotificationService.class);
            uIntent.putExtra("beg", nowTimeToString(nextTime));
            uIntent.putExtra("end", "다시");
            uIntent.putExtra("end99", true);
            uIntent.putExtra("subject", qT.subject);
            uIntent.putExtra("icon", 3);
            uIntent.putExtra("isUpdate", true);
            context.startForegroundService(uIntent);
        } else {
            if (qT.endLoop > 1) {
                String say = addPostPosition(subject) + "끝났습니다";
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            } else if (qT.endLoop == 1) {
                String say = subject + " 를 확인하세요";
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                new Sounds().beep(context, Sounds.BEEP.INFO);
                qT.setActive(false);
                for (int i = 0; i < quietTasks.size(); i++) {
                    QuietTask qT1 = quietTasks.get(i);
                    if (qT1.begHour == qT.begHour && qT1.begMin == qT.begMin &&
                            qT1.endHour == qT.endHour && qT1.endMin == qT.endMin) {
                        qT.active = false;
                        quietTasks.set(i, qT);  // make inActive
                    }
                }
                new QuietTaskGetPut().put(quietTasks);
            } else {
                new Sounds().beep(context, Sounds.BEEP.INFO);
            }
            new NextTask(context, quietTasks, "ended");
        }
    }

    private void say_StartedNormal() {
        if (qT.begLoop > 1) {
            new Sounds().beep(context, Sounds.BEEP.NOTY);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    String subject = qT.subject;
                    String say = addPostPosition(subject) + "시작됩니다";
                    myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                }
            }, 3000);
        } else if (qT.begLoop == 1){
            new Sounds().beep(context, Sounds.BEEP.INFO);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                boolean beep = vars.sharedManner && (qT.begLoop > 1);
                new MannerMode().turn2Quiet(context, beep, qT.vibrate);
            }
        }, 6000);
        new NextTask(context, quietTasks, "say_Started()");
    }

    String addPostPosition(String s) {
        // 받침이 있으면 이, 없으면 가
        String lastNFKD = Normalizer.normalize(s.substring(s.length() - 1), Normalizer.Form.NFKD);
        return s + ((lastNFKD.length() == 2) ? "가 " : "이 ");
    }

    void say_Finished() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                if (qT.endLoop > 1) {
                    new Sounds().beep(context, Sounds.BEEP.NOTY);
                    String subject = qT.subject;
                    String d = (qT.sayDate) ? "지금은 " + nowDateToString(System.currentTimeMillis()) : "";
                    String t = nowTimeToString(System.currentTimeMillis());
                    String s = d + t +  " 입니다. ";
                    s += addPostPosition(subject) + "끝났습니다";
                    myTTS.speak(s, TextToSpeech.QUEUE_ADD, null, null);
                } else if (qT.endLoop == 1) {
                    new Sounds().beep(context, Sounds.BEEP.ALARM);
                }
                if (qT.agenda) { // delete if agenda based
                    for (int i = 0; i < quietTasks.size(); i++) {
                        if (quietTasks.get(i).calId == qT.calId) {
                            quietTasks.remove(i);
                            new QuietTaskGetPut().put(quietTasks);
                            break;
                        }
                    }
                }
                new NextTask(context, quietTasks, "say_Finished()");
                new Utils(context).deleteOldLogFiles();
            }
        }, 3000);
    }

    private void readyTTS() {

        myTTS = null;
        myTTS = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                initializeTTS();
            }
        });
    }

    String TTSId = "";

    private void initializeTTS() {

        myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                TTSId = utteranceId;
            }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                if (myTTS.isSpeaking())
                    return;
                myTTS.stop();
            }

            @Override
            public void onError(String utteranceId) { }
        });

        int result = myTTS.setLanguage(Locale.getDefault());
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(context, "Not supported Language", Toast.LENGTH_SHORT).show();
        } else {
            myTTS.setPitch(1.2f);
            myTTS.setSpeechRate(1.3f);
        }
    }

    String nowDateToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat(" MM 월 d 일 EEEE ", Locale.getDefault());
        final SimpleDateFormat sdfWeek = new SimpleDateFormat(" EEEE ", Locale.US);
        String s = sdfTime.format(time) + sdfWeek.format(time);
        return s + s;
    }
    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }

    boolean isQuiet() {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }

}