package com.urrecliner.letmequiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Objects;

import static com.urrecliner.letmequiet.Vars.STATE_ALARM;
import static com.urrecliner.letmequiet.Vars.actionHandler;
import static com.urrecliner.letmequiet.Vars.mainContext;
import static com.urrecliner.letmequiet.Vars.quietTask;
import static com.urrecliner.letmequiet.Vars.quietTasks;
import static com.urrecliner.letmequiet.Vars.stateCode;
import static com.urrecliner.letmequiet.Vars.utils;

public class AlarmReceiver extends BroadcastReceiver {

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
                    speak_subject();
                MannerMode.turnOn(context, subject, quietTask.vibrate);
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

//        Intent i = new Intent(context, MainActivity.class);
//        i.putExtra("stateCode",stateCode);
//        i.putExtra("DATA",args);
//        context.startActivity(i);
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
    void speak_subject() {

        Text2Speech text2Speech = new Text2Speech();
        text2Speech.initiateTTS(mainContext);
        text2Speech.speak(quietTask.subject);
    }
}
