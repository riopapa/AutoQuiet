package com.urrecliner.letmequiet;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.letmequiet.Vars.STATE_ALARM;
import static com.urrecliner.letmequiet.Vars.actionHandler;
import static com.urrecliner.letmequiet.Vars.mainActivity;
import static com.urrecliner.letmequiet.Vars.mainContext;
import static com.urrecliner.letmequiet.Vars.quietTask;
import static com.urrecliner.letmequiet.Vars.quietTasks;
import static com.urrecliner.letmequiet.Vars.stateCode;
import static com.urrecliner.letmequiet.Vars.utils;

public class AlarmReceiver extends BroadcastReceiver {

    private static int loopCount;
    TextToSpeech textToSpeech;

    @Override
    public void onReceive(Context context, Intent intent) {
        String subject;
        String logID = "ALARM RCV";
        if (utils == null)
            utils = new Utils();
//        utils.log(logID, "action: " + intent.getAction()+" stateCode: "+ stateCode);

        Bundle args = intent.getBundleExtra("DATA");
        assert args != null;
        quietTask = (QuietTask) args.getSerializable("quietTask");
        assert quietTask != null;
        subject = quietTask.subject;
        String caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
//        utils.log(logID,"case:"+ caseSFO + " subject: "+subject);
        utils.log("Activated ","// case:"+ caseSFO + " subject: "+subject+" ///");
        assert caseSFO != null;
        switch (caseSFO) {
            case "S":   // start
                if (quietTask.speaking)
                    speak_subject(context);
                else {
                    MannerMode.turnOn(context, subject, quietTask.vibrate);
                    final int STOP_SPEAK = 10022;
                    Intent stopSpeak = new Intent(mainActivity, NotificationService.class);
                    stopSpeak.putExtra("operation", STOP_SPEAK);
                    mainContext.startService(stopSpeak);
                }
                break;
            case "F":   // finish
                MannerMode.turnOff(context, subject);
                break;
            case "O":   // onetime
                MannerMode.turnOff(context, subject);
                quietTask.setActive(false);
                quietTasks.set(0, quietTask);
                utils.saveSharedPrefTables();
                break;
            default:
                utils.log(logID,"Case Error " + caseSFO);
        }
        stateCode = STATE_ALARM;
        actionHandler.sendEmptyMessage(0);
    }

    void speak_subject(Context context) {

        loopCount = 10;
        textToSpeech = new TextToSpeech(mainContext, status -> textToSpeech.setLanguage(Locale.getDefault()));
        Timer speakTimer = new Timer();
        speakTimer.schedule(new TimerTask() {
            public void run() {
                if (loopCount-- > 0) {
                    MannerMode.vibratePhone(context);
                    textToSpeech.speak(quietTask.subject, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    speakTimer.cancel();
                    speakTimer.purge();
                    textToSpeech.stop();
                    MannerMode.turnOn(context, quietTask.subject, quietTask.vibrate);
                    final int STOP_SPEAK = 10022;
                    Intent stopSpeak = new Intent(mainActivity, NotificationService.class);
                    stopSpeak.putExtra("operation", STOP_SPEAK);
                    mainContext.startService(stopSpeak);

                }
            }
        }, 2000, 2000);

    }
    static void speak_off() {
        loopCount = -1;
    }

//    private static void dumpIntent(Intent i){
//        String LOG_TAG = "dump";
//        Bundle bundle = i.getExtras();
//        if (bundle != null) {
//            Log.e(LOG_TAG,"-- Dumping Intent start");
//            Log.e(LOG_TAG,bundle.toString());
//            Set<String> keys = bundle.keySet();
//            for (String key : keys) {
//                Log.e(LOG_TAG, "[" + key + "=" + bundle.get(key) + "]");
//            }
//            Log.e(LOG_TAG,"-- Dumping Intent end");
//        }
//    }

}
