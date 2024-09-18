package com.riopapa.autoquiet.Sub;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.riopapa.autoquiet.R;

public class NotificationHelper extends ContextWrapper {
    private static final String CHANNEL_ID = "channel_id";
    private static final int NOTIFICATION_ID = 1;
//    private static String APP_NAME = "";
    public NotificationHelper(Context base) {
        super(base);
        createNotificationChannel();
//        PackageManager packageManager = base.getPackageManager();
//        try {
//            APP_NAME = packageManager.getApplicationLabel(
//                    packageManager.getApplicationInfo(base.getPackageName(), 0)).toString()
//                    +  " - ";
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "ChatRead", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel Description");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void sendNotification(int bellType, String title, String text) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, CHANNEL_ID)
                .setSmallIcon(bellType)
                .setContentTitle(title + " "
                    + TaskFinish.nowDateTimeToString(System.currentTimeMillis()))
                .setContentText(text)
                .setAutoCancel(true)
                ;

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}