package better.life.autoquiet.Sub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import better.life.autoquiet.AlarmReceiver;
import better.life.autoquiet.Utils;
import better.life.autoquiet.models.QuietTask;

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
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND); // add this flag to trigger even if phone if flipped
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23456
                , intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        if (!quietTask.active) {
            alarmManager.cancel(pendingIntent);
            new Utils(context).log("req1",StartFinish+" TASK Canceled : "+ quietTask.subject);
        } else {
//            alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                if (alarmManager.canScheduleExactAlarms())
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, nextTime, pendingIntent);
//            }
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTime, pendingIntent);
        }
    }
}