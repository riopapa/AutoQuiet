package better.life.autoquiet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.activity.ActivityMain;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        Log.e("BootReceiver","onReceive ------  "+intent.getAction());
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent nIntent = new Intent(context, ActivityMain.class);
                nIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(nIntent);
                new Handler(Looper.getMainLooper()).postDelayed(() ->
                    ScheduleNextTask.request("booted"), 5000);
            }, 100);

        }
    }
}