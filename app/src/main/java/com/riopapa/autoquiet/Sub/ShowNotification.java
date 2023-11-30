package com.riopapa.autoquiet.Sub;

import static androidx.core.content.ContextCompat.startForegroundService;
import static com.riopapa.autoquiet.ActivityMain.mContext;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.riopapa.autoquiet.BootReceiver;
import com.riopapa.autoquiet.NotificationService;

public class ShowNotification {

    public void show(Context context, Intent intent) {

        if (!isServiceRunning(context, NotificationService.class)) {
            try {
                context.startService(intent);  // if started
            } catch (Exception e) {
                Log.e("startService","show error \n"+e);
//                startForegroundService(context, intent);    // if not started already
            }
        } else {
            try {
                startForegroundService(context, intent);    // if not started already
            } catch (Exception e) {
                context.startService(intent);  // if started
            }
        }
    }
    boolean isServiceRunning(Context context, Class serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
