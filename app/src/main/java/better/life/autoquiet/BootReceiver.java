package better.life.autoquiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import better.life.autoquiet.activity.ActivityMain;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, ActivityMain.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}