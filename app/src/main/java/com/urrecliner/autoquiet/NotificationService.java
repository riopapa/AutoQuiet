package com.urrecliner.autoquiet;

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

public class NotificationService extends Service {

    NotificationCompat.Builder mBuilder = null;
    NotificationChannel mNotificationChannel = null;
    NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    static boolean goOnSpeak = false;
    String start, sSubject, finish;
    Context context;
    final int INVOKE_ONETIME = 100;
    final int STOP_SPEAK = 1022;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_bar);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotification();

        boolean isUpdate;
        try {
            isUpdate = intent.getBooleanExtra("isUpdate", false);
        } catch (Exception e) {
            return START_STICKY;
        }

        if (isUpdate) {
            start = intent.getStringExtra("start");
            finish = intent.getStringExtra("finish");
            sSubject = intent.getStringExtra("subject");
            int icon = intent.getIntExtra("icon", 0);
            updateRemoteViews(sSubject, start, finish, icon);
            startForeground(100, mBuilder.build());
            return START_STICKY;
        }

        int operation = -1;
        try {
            operation = intent.getIntExtra("operation", -1);
        } catch (Exception e) {
            Log.e("operation", e.toString());
        }
        if (operation == INVOKE_ONETIME) {
            Intent oIntent = new Intent(context, OneTimeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, oIntent, 0);
            try {
                pendingIntent.send();
            }
            catch(PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
        startForeground(100, mBuilder.build()); // ??
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

    int [] smallIcons = { R.drawable.phone_normal, R.drawable.phone_vibrate, R.drawable.phone_off};

    void updateRemoteViews(String subject, String start, String finish, int icon) {
        mRemoteViews.setImageViewResource(R.id.stopNow, R.mipmap.quiet_right_now);
        mRemoteViews.setTextViewText(R.id.start, start);
        mRemoteViews.setTextViewText(R.id.calSubject, subject);
        mRemoteViews.setTextViewText(R.id.finish, finish);
        mRemoteViews.setImageViewResource(R.id.state_icon, smallIcons[icon]);
        mRemoteViews.setViewVisibility(R.id.no_speak, (goOnSpeak) ? View.VISIBLE:View.GONE);
        mBuilder.setSmallIcon(smallIcons[icon]);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}