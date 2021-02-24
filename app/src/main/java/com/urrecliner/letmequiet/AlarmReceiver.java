package com.urrecliner.letmequiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.letmequiet.Vars.STATE_ALARM;
import static com.urrecliner.letmequiet.Vars.STOP_SPEAK;
import static com.urrecliner.letmequiet.Vars.actionHandler;
import static com.urrecliner.letmequiet.Vars.mActivity;
import static com.urrecliner.letmequiet.Vars.mContext;
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
            case "S":   // start?
                loopCount = (quietTask.speaking) ? 10:-1;
                speak_subject();
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

    void speak_subject() {

        textToSpeech = new TextToSpeech(mContext, status -> textToSpeech.setLanguage(Locale.getDefault()));
        Timer speakTimer = new Timer();
        speakTimer.schedule(new TimerTask() {
            public void run() {
                if (loopCount-- > 0) {
                    MannerMode.vibratePhone(mContext);
                    textToSpeech.speak(quietTask.subject, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    speakTimer.cancel();
                    speakTimer.purge();
                    textToSpeech.stop();
                    MannerMode.turnOn(mContext, quietTask.subject, quietTask.vibrate);
                    Intent stopSpeak = new Intent(mActivity, NotificationService.class);
                    stopSpeak.putExtra("operation", STOP_SPEAK);
                    mContext.startService(stopSpeak);

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
