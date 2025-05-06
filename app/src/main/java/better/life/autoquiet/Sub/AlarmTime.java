package better.life.autoquiet.Sub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import better.life.autoquiet.AlarmReceiver;
import better.life.autoquiet.models.NextTask;

public class AlarmTime {

    public void request(Context context, NextTask nt,
                        long nextTime, String SFO, int several) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        Intent intent = new Intent(context, AlarmReceiver.class);
        Bundle args = new Bundle();
        args.putSerializable("nextTask", nt);
        intent.putExtra("DATA",args);
        intent.putExtra("several", several);
        intent.putExtra("case",SFO);   // "S" : Start, "F" : Finish, "O" : One time "T" for temperary
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND); // add this flag to trigger even if phone if flipped
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 23456
                , intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            alarmManager.cancel(pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, nextTime, pendingIntent);
        }
    }
}