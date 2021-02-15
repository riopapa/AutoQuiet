package com.urrecliner.letmequiet;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

public class NotificationService extends Service {

    private Context context;
    NotificationCompat.Builder mBuilder = null;
    NotificationChannel mNotificationChannel = null;
    NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;
    private static final int STOP_ONETIME = 10011;

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
        try {
            operation = intent.getIntExtra("operation", -1);
        } catch (Exception e) {
            Log.e("operation", e.toString());
        }
        boolean isUpdate = intent.getBooleanExtra("isUpdate", false);
        String dateTime = intent.getStringExtra("dateTime");
        String subject = intent.getStringExtra("subject");
        String startFinish = intent.getStringExtra("startFinish");
        createNotification();
        if (isUpdate) {
            updateRemoteViews(dateTime, subject, startFinish);
            startForeground(100, mBuilder.build());
            return START_STICKY;
        }
        if (operation == STOP_ONETIME) {
            intent = new Intent(context, OneTimeActivity.class);
            startActivity(intent);
        }
        startForeground(100, mBuilder.build());
        return START_STICKY;
    }

    private void createNotification() {

        if (null == mNotificationChannel) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
                mNotificationManager.createNotificationChannel(mNotificationChannel);
//            }
        }
        if (null == mBuilder) {
            mBuilder = new NotificationCompat.Builder(context,"default")
                    .setSmallIcon(R.mipmap.let_me_quiet_small)
                    .setContent(mRemoteViews)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setOngoing(true);
        }

        Intent mainIntent = new Intent(context, MainActivity.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(context, 0, mainIntent, 0));

        Intent stopOneTime = new Intent(this, NotificationService.class);
        stopOneTime.putExtra("operation", STOP_ONETIME);
        stopOneTime.putExtra("isFromNotification", true);
        PendingIntent pi = PendingIntent.getService(context, 2, stopOneTime, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mRemoteViews.setOnClickPendingIntent(R.id.stopNow, pi);
    }

    private void updateRemoteViews(String dateTime, String subject, String startFinish) {
        mRemoteViews.setImageViewResource(R.id.stopNow, R.mipmap.quiet_now);
        mRemoteViews.setTextViewText(R.id.dateTime, dateTime);
        mRemoteViews.setTextViewText(R.id.subject, subject);
        mRemoteViews.setTextViewText(R.id.startFinish, startFinish.equals("S")? "시작":"끝남");
//        int color = startFinish.equals("S") ? Color.GREEN : Color.BLUE;
//        mRemoteViews.setTextColor(R.id.dateTime, color);
//        mRemoteViews.setTextColor(R.id.subject, color);
//        mRemoteViews.setTextColor(R.id.startFinish, color);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
