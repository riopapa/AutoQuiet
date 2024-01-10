package com.riopapa.autoquiet.Sub;

import static androidx.core.content.ContextCompat.startForegroundService;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.riopapa.autoquiet.NotificationService;

public class ShowNotification {

    public void show(Context context, Intent intent) {

        if (!isServiceRunning(context)) {
            try {
                startForegroundService(context, intent);    // if not started already
            } catch (Exception e) {
                Log.e("s ForegroundService","show error \n"+e);
            }
        } else {
            try {
                context.startService(intent);  // if started
            } catch (Exception e) {
                Log.e("s Service","show error \n"+e);
            }
        }
    }

    boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
