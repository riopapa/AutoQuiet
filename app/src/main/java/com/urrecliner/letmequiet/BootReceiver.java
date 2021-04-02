package com.urrecliner.letmequiet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.urrecliner.letmequiet.Vars.STATE_BOOT;
import static com.urrecliner.letmequiet.Vars.mContext;
import static com.urrecliner.letmequiet.Vars.stateCode;
import static com.urrecliner.letmequiet.Vars.utils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        utils = new Utils();
        mContext = context;
        String logID = STATE_BOOT;
        utils.log(logID, "Activated  ------------- " + intent.getAction());
        stateCode = STATE_BOOT;
        Log.e("Booted",stateCode);
        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("stateCode", stateCode);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startForegroundService(i);

        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent mainIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        mainIntent.addFlags(mainIntent.FLAG_ACTIVITY_NEW_TASK);
//        mainIntent.putExtra("stateCode", stateCode);
        PendingIntent alarmIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Log.e(logID,"ACTIVATED");
        alarmMgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, alarmIntent);
//        Runtime.getRuntime().exit(0);

    }
}
