package better.life.autoquiet.Sub;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import better.life.autoquiet.NotificationService;
import better.life.autoquiet.TaskAction.TaskFinish;
import better.life.autoquiet.activity.ActivityAddEdit;
import better.life.autoquiet.activity.ActivityMain;

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

        Intent intent = new Intent(this, ActivityMain.class); // Replace with your target activity
        intent.putExtra("id", NOTIFICATION_ID); // Pass data if needed
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, CHANNEL_ID)
                .setSmallIcon(bellType)
                .setContentTitle(title + " "
                    + TaskFinish.nowDateTimeToString(System.currentTimeMillis()))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                ;

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}