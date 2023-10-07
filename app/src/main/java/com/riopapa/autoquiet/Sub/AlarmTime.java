package com.riopapa.autoquiet.Sub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.riopapa.autoquiet.AlarmReceiver;
import com.riopapa.autoquiet.Utils;
import com.riopapa.autoquiet.models.QuietTask;

public class AlarmTime {

    public void request(Context context, QuietTask quietTask,
                        long nextTime, String StartFinish, int several) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(context, AlarmReceiver.class);
        Bundle args = new Bundle();
        args.putSerializable("quietTask", quietTask);
        intent.putExtra("DATA",args);
        intent.putExtra("several", several);
        intent.putExtra("case",StartFinish);   // "S" : Start, "F" : Finish, "O" : One time
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23456, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        if (!quietTask.active) {
            alarmManager.cancel(pendingIntent);
            new Utils(context).log("req1",StartFinish+" TASK Canceled : "+ quietTask.subject);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);
        }
    }
}