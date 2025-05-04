package better.life.autoquiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import better.life.autoquiet.activity.ActivityMain;
import better.life.autoquiet.nexttasks.ScheduleNextTask;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent nIntent = new Intent(context, ActivityMain.class);
                nIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(nIntent);
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                    new ScheduleNextTask(context,"booted"), 5000);
            }, 100);

        }
    }
}