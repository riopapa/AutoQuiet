package com.urrecliner.autoquiet.Sub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.urrecliner.autoquiet.AlarmReceiver;
import com.urrecliner.autoquiet.Utils;
import com.urrecliner.autoquiet.models.QuietTask;

public class NextAlarm {

    public void request(Context context, QuietTask quietTask,
                        long nextTime, String StartFinish, int loop) {
        Log.w("NextAlarm","requested");
        String logID = "NextAlarm";
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(context, AlarmReceiver.class);
        Bundle args = new Bundle();
        args.putSerializable("quietTask", quietTask);
        intent.putExtra("DATA",args);
        intent.putExtra("loop", loop);
        intent.putExtra("isUpdate", true);
        intent.putExtra("case",StartFinish);   // "S" : Start, "F" : Finish, "O" : One time
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23456, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (!quietTask.active) {
            alarmManager.cancel(pendingIntent);
            new Utils(context).log(logID,StartFinish+" TASK Canceled : "+ quietTask.subject);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);
        }
    }
}