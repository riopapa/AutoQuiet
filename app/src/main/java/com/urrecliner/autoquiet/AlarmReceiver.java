package com.urrecliner.autoquiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
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
    QuietTask quietTask;
    Context context;
    int loop;
    String caseSFO;
    Vars vars;
    final int STOP_SPEAK = 1022;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Bundle args = intent.getBundleExtra("DATA");
        quietTask = (QuietTask) args.getSerializable("quietTask");
        quietTasks = new QuietTaskGetPut().get(context);
        caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        loop = Objects.requireNonNull(intent.getExtras()).getInt("loop");
        vars = new VarsGetPut().get(context);
        readyTTS();

        assert caseSFO != null;

        switch (caseSFO) {
            case "S":   // start?
                say_Started(quietTask);
                break;
            case "F":   // finish
                new MannerMode().turn2Normal(vars.sharedManner, context);
                say_Finished(quietTask);
                break;
            case "O":   // onetime
                new MannerMode().turn2Normal(vars.sharedManner, context);
                quietTask.setActive(false);
                quietTasks.set(0, quietTask);
                new QuietTaskGetPut().put(quietTasks, context, "OneTime");
                new NextTask(context, quietTasks, "After oneTime");
                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFO);
        }
        new VarsGetPut().put(vars, context);
    }

    void say_Started(QuietTask quietTask) {

        boolean finish99 = quietTask.finishHour == 99;
        new Sounds().beep(context, 2);
        new Timer().schedule(new TimerTask() {
            public void run () {
                String say = "";
                String subject = quietTask.subject;
                String lastCode = subject.substring(subject.length() - 1);
                String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
                say = nowTimeToString(System.currentTimeMillis()) + " 입니다. ";
                if (!finish99) {
                    say += subject + ((lastNFKD.length() == 2) ? "가" : "이") + " 시작됩니다";
                } else
                    say += subject + " 를 확인하세요, " + subject + " 를 확인하세요";
                if (finish99) {     // repeat saying several times
                    if (loop > 0) {
                        loop--;
                        long nextTime = System.currentTimeMillis() + 60 * 2 * 1000;
                        new NextAlarm().request(context, quietTask, nextTime,
                                "S", loop);   // loop 0 : no more
                        Intent uIntent = new Intent(context, NotificationService.class);
                        uIntent.putExtra("start", nowTimeToString(nextTime));
                        uIntent.putExtra("finish", "다시" + loop);
                        uIntent.putExtra("finish99", true);
                        uIntent.putExtra("subject", quietTask.subject);
                        uIntent.putExtra("icon", 3);
                        uIntent.putExtra("isUpdate", true);
                        context.startForegroundService(uIntent);
                        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                    } else
                        new NextTask(context, quietTasks, "finish99ed");
                } else {
                    if (quietTask.sRepeatCount > 0)
                        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                    new NextTask(context, quietTasks, "say_Started()");
                }
            }
        }, 2200);   // after beep

        new Timer().schedule(new TimerTask() {
            public void run () {
                new Sounds().beep(context, 1);
                if (caseSFO.equals("S") && !finish99) {
                    new MannerMode().turn2Quiet(context, vars.sharedManner, quietTask.vibrate);
                }
            }
        }, 6000);

        Intent notification = new Intent(context, NotificationService.class);
        notification.putExtra("operation", STOP_SPEAK);
        context.startForegroundService(notification);
    }

    void say_Finished(QuietTask quietTask) {
        new Sounds().beep(context, 0);
        new Timer().schedule(new TimerTask() {
            public void run () {
                if (quietTask.sRepeatCount > 0) {
                    String subject = quietTask.subject;
                    String lastCode = subject.substring(subject.length() - 1);
                    String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
                    String s = nowTimeToString(System.currentTimeMillis()) + " 입니다. " + subject
                            + ((lastNFKD.length() == 2) ? "가" : "이") + " 끝났습니다";
                    // 받침이 있으면 이, 없으면 가
                    myTTS.speak(s, TextToSpeech.QUEUE_ADD, null, null);
                }
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
                new NextTask(context, quietTasks, "say_Finished()");
                ActivityMain.created = true;
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

    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }
}