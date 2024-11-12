package better.life.autoquiet.Sub;

import static androidx.core.content.ContextCompat.startForegroundService;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import better.life.autoquiet.NotificationService;

public class ShowNotification {

    public void show(Context context, Intent intent) {

        try {
            context.startService(intent);  // if started
        } catch (Exception e) {
            try {
                context.startForegroundService(intent);    // if not started already
            } catch (Exception e1) {
                Log.e("ShowNotification", "show error \n" + e + e1);
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
