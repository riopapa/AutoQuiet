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
    String beg, subject, end;
    int icon;
    boolean end99 = false;
    Context context;

    final int RIGHT_NOW = 100;
    final int STOP_SPEAK = 1044;

    public NotificationService() {
    }
    public NotificationService(Context context) {
        this.context = context;
    }

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
        try {
            beg = intent.getStringExtra("beg");
        } catch (Exception e) {
            return START_STICKY;
        }
        end = intent.getStringExtra("end");
        end99 = intent.getBooleanExtra("end99", false);
        subject = intent.getStringExtra("subject");
        icon = intent.getIntExtra("icon", 0);
        updateRemoteViews();
        startForeground(100, mBuilder.build());
        boolean isUpdate = intent.getBooleanExtra("isUpdate", false);
        if (isUpdate)
            return START_STICKY;

        int operation = -1;
        try {
            operation = intent.getIntExtra("operation", -1);
        } catch (Exception e) {
            Log.e("operation", e.toString());
        }
        if (operation == RIGHT_NOW) {
            Intent oIntent = new Intent(context, ActivityOneTime.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, oIntent, 0);
            try {
                pendingIntent.send();
            } catch(PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else if (operation == STOP_SPEAK) {
            end99 = false;
            updateRemoteViews();
            new NextTask(this, new QuietTaskGetPut().get(this),"stopped, next is");
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

        Intent mainIntent = new Intent(context, ActivityMain.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(context, 0, mainIntent, 0));

        Intent rightNowI = new Intent(this, NotificationService.class);
        rightNowI.putExtra("operation", RIGHT_NOW);
        PendingIntent pi = PendingIntent.getService(context, 2, rightNowI, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mRemoteViews.setOnClickPendingIntent(R.id.stopNow, pi);

        Intent stopTalkI = new Intent(this, NotificationService.class);
        stopTalkI.putExtra("operation", STOP_SPEAK);
        PendingIntent ps = PendingIntent.getService(context, 3, stopTalkI, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mRemoteViews.setOnClickPendingIntent(R.id.no_speak, ps);
    }

    int [] smallIcons = { R.drawable.phone_normal, R.drawable.phone_vibrate, R.drawable.phone_off, R.drawable.alarm};

    void updateRemoteViews() {

        mBuilder.setSmallIcon(smallIcons[icon]);
//        if (icon == 3) {  // make drawable bitmap icon
//            Bitmap bitmap = new IconTime().make(this, beg); // now time -> bitmap
//            IconCompat smallIcon = IconCompat.createWithBitmap(bitmap); // bitmap -> icon
//            mBuilder.setSmallIcon(smallIcon);
//        }
        mRemoteViews.setImageViewResource(R.id.state_icon, smallIcons[icon]);
        mRemoteViews.setTextViewText(R.id.beg_time, beg);
        mRemoteViews.setTextViewText(R.id.calSubject, subject);
        mRemoteViews.setTextViewText(R.id.end_time, end);
        mRemoteViews.setImageViewResource(R.id.stopNow, R.drawable.no_disturb);
        mRemoteViews.setImageViewResource(R.id.no_speak, R.drawable.stop_talking);
        mRemoteViews.setViewVisibility(R.id.no_speak, (end99) ? View.VISIBLE:View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}