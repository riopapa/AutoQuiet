package com.urrecliner.letmequiet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;

import static com.urrecliner.letmequiet.Vars.INVOKE_ONETIME;
import static com.urrecliner.letmequiet.Vars.STOP_SPEAK;
import static com.urrecliner.letmequiet.Vars.mContext;
import static com.urrecliner.letmequiet.Vars.quietTask;

public class NotificationService extends Service {

    private Context context;
    NotificationCompat.Builder mBuilder = null;
    NotificationChannel mNotificationChannel = null;
    NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    static boolean no_speak = true;
    String dateTime, subject, startFinish, sIcon;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        if (null != mRemoteViews) {
            mRemoteViews.removeAllViews(R.layout.notification_bar);
            mRemoteViews = null;
        }
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_bar);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int operation = -1;
        boolean isUpdate;
        try {
            operation = intent.getIntExtra("operation", -1);
        } catch (Exception e) {
            Log.e("operation", e.toString());
        }
        try {
            isUpdate = intent.getBooleanExtra("isUpdate", false);
        } catch (Exception e) {
            return START_STICKY;
        }
        createNotification();
        if (isUpdate) {
            dateTime = intent.getStringExtra("dateTime");
            subject = intent.getStringExtra("subject");
            startFinish = intent.getStringExtra("startFinish");

            no_speak = startFinish.equals("F");
            updateRemoteViews(dateTime, subject, startFinish);
            startForeground(100, mBuilder.build());
            return START_STICKY;
        }
        if (operation == INVOKE_ONETIME) {
            intent = new Intent(mContext, OneTimeActivity.class);
            startActivity(intent);
        }
        if (operation == STOP_SPEAK) {
            AlarmReceiver.speak_off();
            no_speak = false;
            updateRemoteViews(dateTime, subject, startFinish);
        }
        startForeground(100, mBuilder.build());
        return START_STICKY;
    }

    private void createNotification() {

        if (null == mNotificationChannel) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
        if (null == mBuilder) {
            mBuilder = new NotificationCompat.Builder(context,"default")
                    .setSmallIcon(R.mipmap.let_me_quiet)
                    .setContent(mRemoteViews)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setOngoing(true);
        }

        Intent mainIntent = new Intent(context, MainActivity.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(context, 0, mainIntent, 0));

        Intent stopOneTime = new Intent(this, NotificationService.class);
        stopOneTime.putExtra("operation", INVOKE_ONETIME);
        PendingIntent pi = PendingIntent.getService(context, 2, stopOneTime, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mRemoteViews.setOnClickPendingIntent(R.id.stopNow, pi);

        Intent stopSpeak = new Intent(this, NotificationService.class);
        stopSpeak.putExtra("operation", STOP_SPEAK);
        PendingIntent ps = PendingIntent.getService(context, 3, stopSpeak, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mRemoteViews.setOnClickPendingIntent(R.id.no_speak, ps);
    }

    int [] smallIcons = { R.mipmap.phone_normal, R.mipmap.phone_vibrate_blue, R.mipmap.phone_quiet_red};

    void updateRemoteViews(String dateTime, String subject, String startFinish) {
        mRemoteViews.setImageViewResource(R.id.stopNow, R.mipmap.quiet_right_now);
        mRemoteViews.setTextViewText(R.id.dateTime, dateTime);
        mRemoteViews.setTextViewText(R.id.subject, subject);
        mRemoteViews.setTextViewText(R.id.startFinish, startFinish.equals("S")? "시작":"끝남");
        mRemoteViews.setViewVisibility(R.id.no_speak, (no_speak) ? View.VISIBLE:View.GONE);
        int smallIcon = smallIcons[0];
        if (!startFinish.equals("S")) {
            smallIcon = smallIcons[(quietTask.isVibrate()) ? 1:2];
        }
        mBuilder.setSmallIcon(smallIcon);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
