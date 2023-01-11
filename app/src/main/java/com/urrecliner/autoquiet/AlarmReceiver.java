package com.urrecliner.autoquiet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.utility.VarsGetPut;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmReceiver extends BroadcastReceiver {

    private static int savedId;
    private static boolean savedAgenda;
    TextToSpeech textToSpeech;
    ArrayList<QuietTask> quietTasks;
    static Context context;
    static Activity activity;
    Vars vars;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.activity = MainActivity.pActivity;
        vars = new VarsGetPut().get(context);
        quietTasks = new QuietTaskGetPut().get(context);
        Bundle args = intent.getBundleExtra("DATA");
        int caseIdx = Objects.requireNonNull(intent.getExtras()).getInt("caseIdx");
        String caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        QuietTask quietTask = quietTasks.get(caseIdx);
        assert caseSFO != null;
//        Log.w("onReceive", "caseSFO="+caseSFO);
        switch (caseSFO) {
            case "S":   // start?
                say_Started(activity, quietTask.subject, quietTask.vibrate);
                savedId = quietTask.calId;
                savedAgenda = quietTask.agenda;
                break;
            case "F":   // finish
                new Utils(context).deleteOldLogFiles();
                MannerMode.turn2Normal(vars.sharedManner, context);
                say_Finished(quietTask.subject);
                break;
            case "O":   // onetime
                MannerMode.turn2Normal(vars.sharedManner, context);
                quietTask.setActive(false);
                quietTasks.set(0, quietTask);
                new QuietTaskGetPut().put(quietTasks);
                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFO);
        }
        new ScheduleNextTask(context,"after receive");
        new VarsGetPut().put(vars);
    }

    void say_Started(Activity activity, String subject, boolean vibrate) {
        ready_TTS();
        new Sounds().beep(context, 2);
        String lastCode = subject.substring(subject.length()-1);
        String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
        String s = nowTimeToString() + " 입니다. " + subject // 받침이 있으면 이, 없으면 가
                + ((lastNFKD.length() == 2) ? "가": "이") +" 시작됩니다";
        textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);

        Timer speakTimer = new Timer();
        speakTimer.schedule(new TimerTask() {
            public void run() {
                speakTimer.cancel();
                speakTimer.purge();
                MannerMode.turn2Quiet(context, vars.sharedManner, vibrate);
                Intent notification = new Intent(activity, NotificationService.class);
                notification.putExtra("operation", vars.STOP_SPEAK);
                context.startService(notification);
            }
        }, 3000, 5000);
    }

    void say_Finished(String subject) {
        ready_TTS();
        Timer speakTimer = new Timer();
        speakTimer.schedule(new TimerTask() {
            public void run() {
                speakTimer.cancel();
                speakTimer.purge();
                if (savedAgenda) { // delete if agenda based
                    for (int i = 0; i < quietTasks.size(); i++) {
                        Log.w("id "+i, savedId+" vs "+quietTasks.get(i).calId+quietTasks.get(i).subject
                                +quietTasks.get(i).finishHour+quietTasks.get(i).finishMin);
                        if (quietTasks.get(i).calId == savedId) {
                            Log.w("remove", quietTasks.get(i).subject);
                            quietTasks.remove(i);
                            new QuietTaskGetPut().put(quietTasks);
                            break;
                        }
                    }
                }
            }
        }, 2000, 6000);
    }

    void ready_TTS() {
        textToSpeech = new TextToSpeech(context, status -> textToSpeech.setLanguage(Locale.getDefault()));
        textToSpeech.setPitch(1.2f);
        textToSpeech.setSpeechRate(1.3f);

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            public void onDone(String utteranceId) {
                textToSpeech.stop();
                new Timer().schedule(new TimerTask() {
                    public void run () {
                        new Sounds().beep(context, 1);
                    }
                }, 1000);
            }

            @Override
            public void onError(String utteranceId) { }
        });
    }

    String nowTimeToString() {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(System.currentTimeMillis());
    }
}