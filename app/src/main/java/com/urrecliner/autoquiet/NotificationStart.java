package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.Intent;

public class NotificationStart {
    public NotificationStart(Context context) {
        NotificationService notificationService  = new NotificationService(context);
        if (!BootReceiver.isServiceRunning(context, notificationService.getClass())) {
            Intent mBackgroundServiceIntent;
            mBackgroundServiceIntent = new Intent(context, notificationService.getClass());
            context.startForegroundService(mBackgroundServiceIntent);
        }
    }
}
