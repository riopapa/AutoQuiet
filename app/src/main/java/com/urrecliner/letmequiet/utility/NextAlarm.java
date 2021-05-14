package com.urrecliner.letmequiet.utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.urrecliner.letmequiet.AlarmReceiver;
import com.urrecliner.letmequiet.models.QuietTask;

import static com.urrecliner.letmequiet.Vars.utils;

public class NextAlarm {
    public static void request(QuietTask quietTask, long nextTime, String StartFinish, Context context) {
        String logID = "NextAlarm";
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(context, AlarmReceiver.class);
        Bundle args = new Bundle();
        args.putSerializable("quietTask", quietTask);

        intent.putExtra("DATA",args);
        intent.putExtra("case",StartFinish);   // "S" : Start, "F" : Finish, "O" : One time
        int uniqueId = 123456; // (int) System.currentTimeMillis() & 0xffff;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, uniqueId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (!quietTask.isActive()) {
            alarmManager.cancel(pendingIntent);
            utils.log(logID,StartFinish+" TASK Canceled : "+ quietTask.getSubject());
        }
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);
        }
    }
}
