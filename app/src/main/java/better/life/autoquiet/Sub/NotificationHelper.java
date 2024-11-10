package better.life.autoquiet.Sub;


import static better.life.autoquiet.NotificationService.MAKE_SILENT;
import static better.life.autoquiet.activity.ActivityMain.mContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import better.life.autoquiet.NotificationService;
import better.life.autoquiet.TaskAction.TaskFinish;

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

//        Intent intent = new Intent(this, NotificationService.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, MAKE_SILENT,
//                intent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, CHANNEL_ID)
                .setSmallIcon(bellType)
                .setContentTitle(title + " "
                    + TaskFinish.nowTimeDateToString(System.currentTimeMillis()))
                .setContentText(text)
//                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                ;

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}