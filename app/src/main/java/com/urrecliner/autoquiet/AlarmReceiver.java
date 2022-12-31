package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.STOP_SPEAK;
import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.quietTask;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.sounds;
import static com.urrecliner.autoquiet.Vars.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.urrecliner.autoquiet.models.QuietTask;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmReceiver extends BroadcastReceiver {

    private static int loopCount, savedId;
    private static boolean savedAgenda;
    TextToSpeech textToSpeech;
    Context tContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        tContext = context;
        Bundle args = intent.getBundleExtra("DATA");
        assert args != null;
        quietTask = (QuietTask) args.getSerializable("quietTask");
        assert quietTask != null;
        String caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        assert caseSFO != null;
//        int dataTask = Objects.requireNonNull(intent.getExtras()).getInt("task");
//        int sharedTask = sharedPref.getInt("task",-1);
//        String s = "dataTask="+dataTask+", sharedTask="+sharedTask;
//        if (dataTask != sharedTask) {
//            s = caseSFO+" task different "+s+"  "+quietTask.subject;
//            Log.w("task pass",s);
//            Toast.makeText(mContext, s, Toast.LENGTH_LONG).show();
//            return;
//        }
        loopCount = quietTask.getsRepeatCount();
//        Log.w("onReceive", "caseSFO="+caseSFO);
        switch (caseSFO) {
            case "S":   // start?
                say_Started(quietTask.subject, quietTask.vibrate);
                savedId = quietTask.calId;
                savedAgenda = quietTask.agenda;
                break;
            case "F":   // finish
                utils.deleteOldLogFiles();
                MannerMode.turn2Normal(tContext);
//                if (loopCount > 0) {    // 끝날 때는 여러번 울리기 없음
                    loopCount = 1;
                    say_Finished(quietTask.subject);
//                }
                break;
            case "O":   // onetime
                MannerMode.turn2Normal(tContext);
                quietTask.setActive(false);
                quietTasks.set(0, quietTask);
                utils.saveQuietTasksToShared();
                break;
            default:
                utils.log("Alarm Receive","Case Error " + caseSFO);
        }

        utils = new Utils(MainActivity.getContext());
        new ScheduleNextTask("after receive");
//        sharedCode = STATE_ALARM;
//        sharedEditor.putString(SHARED_CODE, sharedCode);
//        sharedEditor.apply();
//        Intent mIntent = new Intent(tContext, MainActivity.class);
//        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        tContext.startActivity(mIntent);
//        PackageManager pm = context.getPackageManager();
//        Intent mainInt = pm.getLaunchIntentForPackage(context.getPackageName());
//        mainInt.putExtra("stateCode",STATE_NEXT);
//        context.startActivity(mainInt);

    }

    void say_Started(String subject, boolean vibrate) {
        ready_TTS();
        Timer speakTimer = new Timer();
        speakTimer.schedule(new TimerTask() {
            public void run() {
                if (loopCount-- > 0) {
                    sounds.beep(2);
                    String lastCode = subject.substring(subject.length()-1);
                    String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
                    String s = nowTimeToString() + " 입니다. " + subject // 받침이 있으면 이, 없으면 가
                            + ((lastNFKD.length() == 2) ? "가": "이") +" 시작됩니다";
                    textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);
                } else {
                    speakTimer.cancel();
                    speakTimer.purge();
                    MannerMode.turn2Quiet(tContext, vibrate);
                    Intent notification = new Intent(mActivity, NotificationService.class);
                    notification.putExtra("operation", STOP_SPEAK);
                    tContext.startService(notification);
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
                    sounds.beep(2);
                    String lastCode = subject.substring(subject.length()-1);
                    String lastNFKD = Normalizer.normalize(lastCode, Normalizer.Form.NFKD);
                    String s = nowTimeToString() + " 입니다. " + subject // 받침이 있으면 이, 없으면 가
                            + ((lastNFKD.length() == 2) ? "가": "이") +" 종료 되었습니다";
                    textToSpeech.speak(s, TextToSpeech.QUEUE_ADD, null, null);
                } else {
                    speakTimer.cancel();
                    speakTimer.purge();
                    if (savedAgenda) { // delete if agenda based
                        for (int i = 0; i < quietTasks.size(); i++) {
                            Log.w("id "+i, savedId+" vs "+quietTasks.get(i).calId+quietTasks.get(i).subject
                                    +quietTasks.get(i).finishHour+quietTasks.get(i).finishMin);
                            if (quietTasks.get(i).calId == savedId) {
                                Log.w("remove", quietTasks.get(i).subject);
                                quietTasks.remove(i);
                                utils.saveQuietTasksToShared();
                                break;
                            }
                        }
                    }
                }
            }
        }, 2000, 6000);
    }

    void ready_TTS() {
        textToSpeech = new TextToSpeech(tContext, status -> textToSpeech.setLanguage(Locale.getDefault()));
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
                        sounds.beep(1);
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