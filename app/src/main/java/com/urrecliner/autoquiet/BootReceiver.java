package com.urrecliner.autoquiet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.urrecliner.autoquiet.Vars.STATE_BOOT;
import static com.urrecliner.autoquiet.Vars.mActivity;
import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.stateCode;
import static com.urrecliner.autoquiet.Vars.utils;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        utils = new Utils();
        mContext = context;
        String logID = STATE_BOOT;
        utils.log(logID, "Activated  BOOT ------------- " + intent.getAction());
        stateCode = STATE_BOOT;
        Log.e("Booted",stateCode);

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("stateCode", stateCode);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(i);
//
//        context.startForegroundService(i);
//        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//        Intent mainIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
//        mainIntent.addFlags(mainIntent.FLAG_ACTIVITY_NEW_TASK);
////        mainIntent.putExtra("stateCode", stateCode);
//        PendingIntent alarmIntent = PendingIntent.getActivity(context, 0, mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        alarmMgr.set(AlarmManager.RTC, System.currentTimeMillis() + 20000, alarmIntent);
//        new ScheduleNextTask("Booted Again");

    }
}
