package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.ActivityMain.mContext;

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
    String beg, subject, end, begN, subjectN, endN;
    int icon, iconN, iconNow;
    boolean stop_repeat = false;

    final int RIGHT_NOW = 100;
    final int STOP_SPEAK = 144;
    final int TO_TOSS = 166;

    public NotificationService(){}      // do not remove

    public NotificationService(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_bar);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotification();

        int operation = -1;
        try {
            operation = intent.getIntExtra("operation", -1);
        } catch (Exception e) {
            Log.e("operation", e.toString());
        }

//        Log.w("onStartCommand","operation = "+operation);
        if (operation == TO_TOSS) {
            launchToss();
        } else if (operation == RIGHT_NOW) {
            Intent oIntent = new Intent(mContext, ActivityOneTime.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, oIntent, PendingIntent.FLAG_MUTABLE);
            try {
                pendingIntent.send();
            } catch(PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else if (operation == STOP_SPEAK) {
            stop_repeat = false;
            updateRemoteViews();
            new SetUpComingTask(this, "stopped, next is");
        } else {
            beg = intent.getStringExtra("beg");
            begN = intent.getStringExtra("begN");
            end = intent.getStringExtra("end");
            endN = intent.getStringExtra("endN");
            stop_repeat = intent.getBooleanExtra("stop_repeat", false);
            subject = intent.getStringExtra("subject");
            subjectN = intent.getStringExtra("subjectN");
            icon = intent.getIntExtra("icon", 0);
            iconN = intent.getIntExtra("iconN", 0);
            iconNow = intent.getIntExtra("iconNow", 0);
            if (icon == 0)
                return START_NOT_STICKY;
            if (iconN == 0)
                iconN = R.drawable.auto_quite;
            updateRemoteViews();
        }
        startForeground(100, mBuilder.build()); // ??
        return START_NOT_STICKY;
    }

    private void launchToss() {
        Intent appIntent = mContext.getPackageManager().getLaunchIntentForPackage(
                "viva.republica.toss");
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                            Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        mContext.startForegroundService(appIntent);

    }

    private void createNotification() {

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(mNotificationChannel);
        mBuilder = new NotificationCompat.Builder(mContext,"default")
                .setSmallIcon(iconNow)
                .setContent(mRemoteViews)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false)
                .setOngoing(true);

        Intent mainIntent = new Intent(mContext, ActivityMain.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(mContext, 0, mainIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));

        Intent rightNowI = new Intent(this, NotificationService.class);
        rightNowI.putExtra("operation", RIGHT_NOW);
        PendingIntent rightNowP = PendingIntent.getService(mContext, RIGHT_NOW, rightNowI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(rightNowP);
        mRemoteViews.setOnClickPendingIntent(R.id.right_now, rightNowP);

        Intent tossI = new Intent(this, NotificationService.class);
        tossI.putExtra("operation", TO_TOSS);
        PendingIntent tossP = PendingIntent.getService(mContext, TO_TOSS, tossI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(tossP);
        mRemoteViews.setOnClickPendingIntent(R.id.to_toss, tossP);

        Intent stopTalkI = new Intent(this, NotificationService.class);
        stopTalkI.putExtra("operation", STOP_SPEAK);
        PendingIntent stopTalkP = PendingIntent.getService(mContext, STOP_SPEAK, stopTalkI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(stopTalkP);
        mRemoteViews.setOnClickPendingIntent(R.id.no_speak, stopTalkP);
    }

    void updateRemoteViews() {

        mBuilder.setSmallIcon(iconNow);
        mRemoteViews.setImageViewResource(R.id.state_icon, icon);
        mRemoteViews.setTextViewText(R.id.calSubject, subject);
        mRemoteViews.setTextViewText(R.id.beg_time, beg + " "+end);
        mRemoteViews.setViewVisibility(R.id.no_speak, (stop_repeat) ? View.VISIBLE:View.GONE);

        mRemoteViews.setImageViewResource(R.id.state_iconN, iconN);
        mRemoteViews.setTextViewText(R.id.calSubjectN, subjectN);
        mRemoteViews.setTextViewText(R.id.beg_timeN, begN+" "+endN);
        mRemoteViews.setViewVisibility(R.id.to_toss, (subject.equals("삐이"))? View.VISIBLE:View.GONE);

        mNotificationManager.notify(100,mBuilder.build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}