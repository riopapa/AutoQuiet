package com.urrecliner.autoquiet;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

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

    TextToSpeech myTTS;
    ArrayList<QuietTask> quietTasks;
    QuietTask quietTask;
    Context context;
    Activity activity;
    long lastTime = 0;
    String caseSFO;
    Vars vars;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.activity = MainActivity.pActivity;
//        Log.w("onReceive", "received");
        if (lastTime == 0)
            lastTime = System.currentTimeMillis() - 1000;
        if (System.currentTimeMillis() < lastTime ) {
            Log.e("Receive","Duplicated");
            return;
        }
        lastTime = System.currentTimeMillis() + 100*1000;
//        Log.w("time","lastTime =" + sdfHourMin.format(lastTime));
        vars = new VarsGetPut().get(context);
        quietTasks = new QuietTaskGetPut().get(context);
        readyTTS();
//        Bundle args = intent.getBundleExtra("DATA");
        int caseIdx = Objects.requireNonNull(intent.getExtras()).getInt("caseIdx");
        caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        quietTask = quietTasks.get(caseIdx);
        assert caseSFO != null;
        switch (caseSFO) {
            case "S":   // start?
                say_Started(activity, quietTask.subject);
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
                new QuietTaskGetPut().put(quietTasks, context, "OneTime");
                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFO);
        }
        new NextTask(context,"reNew");
        new VarsGetPut().put(vars);
    }

    void say_Started(Activity activity, String subject) {
        new Sounds().beep(context, 2);
        new Timer().schedule(new TimerTask() {
            public void run () {
                String lastCode = subject.substring(subject.length()-1);
                String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
                String s = nowTimeToString(System.currentTimeMillis()) + " 입니다. " + subject
                        + ((lastNFKD.length() == 2) ? "가": "이") +" 시작됩니다";
                // 받침이 있으면 이, 없으면 가
                myTTS.speak(s, TextToSpeech.QUEUE_ADD, null, TTSId);
                Intent notification = new Intent(activity, NotificationService.class);
                notification.putExtra("operation", vars.STOP_SPEAK);
                context.startForegroundService(notification);
            }
        }, 2000);   // after beep
    }

    void say_Finished(String subject) {
        new Sounds().beep(context, 0);
        new Timer().schedule(new TimerTask() {
            public void run () {
                String lastCode = subject.substring(subject.length()-1);
                String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
                String s = nowTimeToString(System.currentTimeMillis()) + " 입니다. " + subject
                        + ((lastNFKD.length() == 2) ? "가": "이") +" 끝났습니다";
                // 받침이 있으면 이, 없으면 가
                myTTS.speak(s, TextToSpeech.QUEUE_ADD, null, null);

                if (quietTask.agenda) { // delete if agenda based
                    for (int i = 0; i < quietTasks.size(); i++) {
                        if (quietTasks.get(i).calId == quietTask.calId) {
                            Log.w("remove", quietTasks.get(i).subject);
                            quietTasks.remove(i);
                            new QuietTaskGetPut().put(quietTasks, context,"Del "+quietTask.subject);
                            break;
                        }
                    }
                }
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
                new Timer().schedule(new TimerTask() {
                    public void run () {
                        new Sounds().beep(context, 1);
                        if (caseSFO.equals("S")) {
                            MannerMode.turn2Quiet(context, vars.sharedManner, quietTask.vibrate);
                        }
                        myTTS.stop();
                    }
                }, 1000);
            }

            @Override
            public void onError(String utteranceId) { }
        });

        int result = myTTS.setLanguage(Locale.getDefault());
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(activity, "Not supported Language", Toast.LENGTH_SHORT).show();
        } else {
            myTTS.setPitch(1.2f);
            myTTS.setSpeechRate(1.3f);
        }
    }

    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }
}