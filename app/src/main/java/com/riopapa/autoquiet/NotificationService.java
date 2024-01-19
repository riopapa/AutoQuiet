package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_VIBRATE;
import static com.riopapa.autoquiet.ActivityMain.mContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.riopapa.autoquiet.Sub.AlarmTime;
import com.riopapa.autoquiet.Sub.AdjVolumes;
import com.riopapa.autoquiet.models.QuietTask;

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
    final int A_MINUTE = 166;
    final int FIVE_MINUTES = 555;
    final int VOLUMES = 666;

    public NotificationService(){}      // do not remove

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
        if (operation == A_MINUTE) {
            quiet_minute(60);
        } else if (operation == FIVE_MINUTES) {
            quiet_minute(60*5);
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
            new ScheduleNextTask(this, "stopped, next is");
        } else if (operation == VOLUMES) {
            show_Volumes();
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
            show_Volumes();
        }
        startForeground(100, mBuilder.build());
        return START_NOT_STICKY;
    }

    private void show_Volumes() {

        Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        Paint txtPaint = new Paint();
        txtPaint.setTextAlign(Paint.Align.LEFT);
        txtPaint.setAntiAlias(true);
        txtPaint.setTextSize(24);
        txtPaint.setColor(0xFFFFFFFF);
        txtPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        txtPaint.setStrokeWidth(4);
        txtPaint.setTypeface(mContext.getResources().getFont(R.font.nanumbarungothic));
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linePaint.setTextAlign(Paint.Align.LEFT);
        linePaint.setColor(0xFFFFFFFF);
        int rVol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        int mVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int nVol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        drawVolume(canvas, "R", 28, rVol, txtPaint, linePaint);
        drawVolume(canvas, "M", 64, mVol, txtPaint, linePaint);
        drawVolume(canvas, "N", 100, nVol, txtPaint, linePaint);

        mRemoteViews.setImageViewBitmap(R.id.volumes, bitmap);
        mNotificationManager.notify(100,mBuilder.build());

    }

    void drawVolume(Canvas canvas, String s, int yPos, int vol, Paint txtPaint, Paint lnPaint) {
        final int shift = 40;
        canvas.drawText(s, 8, yPos+6, txtPaint);
        lnPaint.setStrokeWidth(12);
        canvas.drawLine(shift, yPos, shift+vol * 4, yPos, lnPaint);
        lnPaint.setStrokeWidth(3);
        canvas.drawLine(shift+vol*4, yPos, shift+(15-vol) * 4, yPos, lnPaint);
    }

    private void quiet_minute(int secs) {

        new AdjVolumes(this, AdjVolumes.VOL.COND_OFF);
        QuietTask qt = new QuietTask("One min", 0, 0, 0, 0,
                new boolean[7], true,  PHONE_VIBRATE, false);
        long nextTime = System.currentTimeMillis() + secs * 1000L;
        new AlarmTime().request(mContext, qt, nextTime, "T", 1);   // several 0 : no more
        Toast.makeText(this, "quiet minute "+secs+" secs", Toast.LENGTH_SHORT).show();
        show_Volumes();
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

        Intent toss5 = new Intent(this, NotificationService.class);
        toss5.putExtra("operation", FIVE_MINUTES);
        PendingIntent fiveP = PendingIntent.getService(mContext, FIVE_MINUTES, toss5, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(fiveP);
        mRemoteViews.setOnClickPendingIntent(R.id.five_minute, fiveP);

        Intent tossI = new Intent(this, NotificationService.class);
        tossI.putExtra("operation", A_MINUTE);
        PendingIntent oneP = PendingIntent.getService(mContext, A_MINUTE, tossI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(oneP);
        mRemoteViews.setOnClickPendingIntent(R.id.a_minute, oneP);

        Intent volI = new Intent(this, NotificationService.class);
        volI.putExtra("operation", VOLUMES);
        PendingIntent volP = PendingIntent.getService(mContext, VOLUMES, volI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(volP);
        mRemoteViews.setOnClickPendingIntent(R.id.volumes, volP);

    }

    void updateRemoteViews() {

        mBuilder.setSmallIcon(iconNow);
        mRemoteViews.setImageViewResource(R.id.state_icon, icon);
        mRemoteViews.setTextViewText(R.id.calSubject, subject);
        mRemoteViews.setTextViewText(R.id.beg_time, beg + " "+end);

        mRemoteViews.setImageViewResource(R.id.state_iconN, iconN);
        mRemoteViews.setTextViewText(R.id.calSubjectN, subjectN);
        mRemoteViews.setTextViewText(R.id.beg_timeN, begN+" "+endN);
        mNotificationManager.notify(100,mBuilder.build());

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}