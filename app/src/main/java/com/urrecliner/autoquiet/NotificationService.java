package com.urrecliner.autoquiet;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    boolean end99 = false, end99N = false;
    Context nContext;

    final int RIGHT_NOW = 100;
    final int STOP_SPEAK = 1044;

    final int TO_TOSS = 1066;

    public NotificationService(){}      // do not remove

    public NotificationService(Context nContext) {
        this.nContext = nContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nContext = this;
        mRemoteViews = new RemoteViews(nContext.getPackageName(), R.layout.notification_bar);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.w("onStartCommand", "started ...");
        createNotification();

        int operation = -1;
        try {
            operation = intent.getIntExtra("operation", -1);
        } catch (Exception e) {
            Log.e("operation", e.toString());
        }

        if (operation == RIGHT_NOW) {
            Intent oIntent = new Intent(nContext, ActivityOneTime.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(nContext, 0, oIntent, 0);
            try {
                pendingIntent.send();
            } catch(PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        } else if (operation == STOP_SPEAK) {
            end99 = false;
            updateRemoteViews();
            new SetUpComingTask(this, new QuietTaskGetPut().get(this),"stopped, next is");
        } else if (operation == TO_TOSS) {
            launchToss();
        } else {
            try {
                beg = intent.getStringExtra("beg");
                begN = intent.getStringExtra("begN");
            } catch (Exception e) {
                Log.w("onStartCommand","beg is nothing");
                return START_NOT_STICKY;
            }
            end = intent.getStringExtra("end");
            endN = intent.getStringExtra("endN");
            end99 = intent.getBooleanExtra("end99", false);
            end99N = intent.getBooleanExtra("end99N", false);
            subject = intent.getStringExtra("subject");
            subjectN = intent.getStringExtra("subjectN");
            icon = intent.getIntExtra("icon", 0);
            iconN = intent.getIntExtra("iconN", 0);
            iconNow = intent.getIntExtra("iconNow", 0);
            if (icon == 0) {
                Log.w("onStartCommand", "Icon is missed");
                return START_NOT_STICKY;
            }
            if (iconN == 0)
                iconN = R.drawable.auto_quite_small;
            updateRemoteViews();
        }
        startForeground(100, mBuilder.build()); // ??
        return START_NOT_STICKY;
    }

    private void launchToss() {
        String app = "viva.republica.toss";
        PackageManager managerclock = nContext.getPackageManager();
        Intent appIntent = managerclock.getLaunchIntentForPackage(app);
//        appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        appIntent.addCategory(Intent.ACTION_MAIN);
//        appIntent.addCategory(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
//        appIntent.addCategory(Intent.ACTION_PICK_ACTIVITY);
//        appIntent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        appIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        nContext.startActivity(appIntent);

//                Intent intent = new Intent(rContext, ActivityMain.class);
//                rContext.startActivity(intent);
//                PendingIntent contentIntent = PendingIntent.getActivity(rContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                try {
//                    contentIntent.send();
//
//                    new Timer().schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            Intent appIntent = rContext.getPackageManager().getLaunchIntentForPackage(app);
//                            appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                            rContext.startForegroundService(appIntent);
//                            rContext.startActivity(appIntent);
//                        }
//                    }, 300);
//
//                } catch (PendingIntent.CanceledException e) {
//                    e.printStackTrace();
//                }
    }

    private void createNotification() {

        if (null == mNotificationChannel) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
        if (null == mBuilder) {
            mBuilder = new NotificationCompat.Builder(nContext,"default")
                    .setSmallIcon(R.drawable.auto_quite)
                    .setContent(mRemoteViews)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(false)
                    .setOngoing(true);
        }

        Intent mainIntent = new Intent(nContext, ActivityMain.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(nContext, 0, mainIntent, 0));

        Intent rightNowI = new Intent(this, NotificationService.class);
        rightNowI.putExtra("operation", RIGHT_NOW);
        PendingIntent pi = PendingIntent.getService(nContext, RIGHT_NOW, rightNowI, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mRemoteViews.setOnClickPendingIntent(R.id.right_now, pi);

        Intent tossI = new Intent(this, NotificationService.class);
        tossI.putExtra("operation", TO_TOSS);
        PendingIntent pt = PendingIntent.getService(nContext, TO_TOSS, tossI, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pt);
        mRemoteViews.setOnClickPendingIntent(R.id.to_toss, pt);

        Intent stopTalkI = new Intent(this, NotificationService.class);
        stopTalkI.putExtra("operation", STOP_SPEAK);
        PendingIntent ps = PendingIntent.getService(nContext, STOP_SPEAK, stopTalkI, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(ps);
        mRemoteViews.setOnClickPendingIntent(R.id.no_speak, ps);
    }

    void updateRemoteViews() {

        mBuilder.setSmallIcon(iconNow);
        mRemoteViews.setImageViewResource(R.id.state_icon, icon);
        mRemoteViews.setTextViewText(R.id.calSubject, subject);
        mRemoteViews.setTextViewText(R.id.beg_time, beg + " "+end);
        mRemoteViews.setViewVisibility(R.id.no_speak, (end99) ? View.VISIBLE:View.GONE);

        mRemoteViews.setImageViewResource(R.id.state_iconN, iconN);
        mRemoteViews.setTextViewText(R.id.calSubjectN, subjectN);
        mRemoteViews.setTextViewText(R.id.beg_timeN, begN+" "+endN);
        mRemoteViews.setViewVisibility(R.id.to_toss, (subject.equals("토스"))? View.VISIBLE:View.GONE);
        startForeground(100, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}