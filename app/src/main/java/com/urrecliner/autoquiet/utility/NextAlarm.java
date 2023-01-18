package com.urrecliner.autoquiet.utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.urrecliner.autoquiet.AlarmReceiver;
import com.urrecliner.autoquiet.Utils;
import com.urrecliner.autoquiet.models.QuietTask;

public class NextAlarm {

    public void request(Context context, QuietTask quietTask,
                        long nextTime, String StartFinish, int caseIdx) {
        String logID = "NextAlarm";
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(context, AlarmReceiver.class);
        Bundle args = new Bundle();
        args.putSerializable("quietTask", quietTask);
        int task = (int) System.currentTimeMillis() & 0xffff;

        intent.putExtra("DATA",args);
        intent.putExtra("case",StartFinish);   // "S" : Start, "F" : Finish, "O" : One time
        intent.putExtra("caseIdx",caseIdx);
        intent.putExtra("task",task); // unique task number
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23456, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (!quietTask.active) {
            alarmManager.cancel(pendingIntent);
            new Utils(context).log(logID,StartFinish+" TASK Canceled : "+ quietTask.subject);
        }
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);
        }
    }
}