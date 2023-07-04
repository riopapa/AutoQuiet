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
    String beg, subject, end, begN, subjectN, endN;
    int icon, iconN;
    boolean end99 = false, end99N = false;
    Context context;

    final int RIGHT_NOW = 100;
    final int STOP_SPEAK = 1044;

    public NotificationService(){}      // do not remove

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

        Log.w("onStartCommand", "started ...");
        createNotification();
        try {
            beg = intent.getStringExtra("beg");
            begN = intent.getStringExtra("begN");
        } catch (Exception e) {
            Log.w("onStartCommand","beg is nothing");
            return START_STICKY;
        }
        end = intent.getStringExtra("end");
        endN = intent.getStringExtra("endN");
        end99 = intent.getBooleanExtra("end99", false);
        end99N = intent.getBooleanExtra("end99N", false);
        subject = intent.getStringExtra("subject");
        subjectN = intent.getStringExtra("subjectN");
        icon = intent.getIntExtra("icon", 0);
        iconN = intent.getIntExtra("iconN", 0);
        if (icon == 0) {
            Log.w("onStartCommand", "Icon is missed");
            icon = R.drawable.auto_quite_small;
        }
        if (iconN == 0)
            iconN = R.drawable.auto_quite_small;
        updateRemoteViews();

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
            new SetUpComingTask(this, new QuietTaskGetPut().get(this),"stopped, next is");
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
                    .setSmallIcon(R.drawable.auto_quite)
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
        mRemoteViews.setOnClickPendingIntent(R.id.right_now, pi);

        Intent stopTalkI = new Intent(this, NotificationService.class);
        stopTalkI.putExtra("operation", STOP_SPEAK);
        PendingIntent ps = PendingIntent.getService(context, 3, stopTalkI, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mRemoteViews.setOnClickPendingIntent(R.id.no_speak, ps);
    }

    void updateRemoteViews() {

        mBuilder.setSmallIcon(icon);
        mRemoteViews.setImageViewResource(R.id.state_icon, icon);
        mRemoteViews.setTextViewText(R.id.calSubject, subject);
        mRemoteViews.setTextViewText(R.id.beg_time, beg);
        mRemoteViews.setTextViewText(R.id.end_time, end);
        mRemoteViews.setImageViewResource(R.id.right_now, R.drawable.right_now);
        mRemoteViews.setImageViewResource(R.id.no_speak, R.drawable.stop_talking);
        mRemoteViews.setViewVisibility(R.id.no_speak, (end99) ? View.VISIBLE:View.GONE);

        mRemoteViews.setImageViewResource(R.id.state_iconN, iconN);
        mRemoteViews.setTextViewText(R.id.calSubjectN, subjectN);
        mRemoteViews.setTextViewText(R.id.beg_timeN, begN);
        mRemoteViews.setTextViewText(R.id.end_timeN, endN);
        startForeground(100, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}