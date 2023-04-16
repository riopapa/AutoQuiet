package com.urrecliner.autoquiet;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.urrecliner.autoquiet.Sub.NextAlarm;
import com.urrecliner.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {

    private final static String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            new Handler().postDelayed(new Runnable() {

                @Override public void run() {
                    ArrayList<QuietTask> quietTasks;
                    quietTasks = new QuietTaskGetPut().get(context);
                    new NextTask(context, quietTasks, "After Boot");
                }
            }, 10000);
        }
    }

    public static boolean isServiceRunning(Context context, Class serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i (TAG,"ServiceRunning? = "+true);
                return true;
            }
        }
        Log.i(TAG,"ServiceRunning? = "+ false);
        return false;
    }
}