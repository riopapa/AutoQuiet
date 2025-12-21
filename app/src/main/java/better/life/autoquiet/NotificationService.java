package better.life.autoquiet;

import static better.life.autoquiet.activity.ActivityMain.sounds;

import android.app.Notification;
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

import androidx.core.app.NotificationCompat;

import better.life.autoquiet.activity.ActivityMain;
import better.life.autoquiet.activity.ActivityOneTime;

public class NotificationService extends Service {

    public static String blueDevice = "";
    public static boolean isBlueToothOn = false;
    NotificationCompat.Builder mBuilder = null;
    NotificationChannel mNotificationChannel = null;
    NotificationManager mNotificationManager;
    RemoteViews mRemoteViews;

    String beg, subject, end, begN, subjectN, endN;
    int icon, iconN;
    boolean stop_repeat = false;

    final int RIGHT_NOW = 100;
    final int STOP_SPEAK = 144;
    final int A_MINUTE = 166;
    public final static int MAKE_SILENT = 555;
    final int VOLUMES = 666;
    final int VOLUME_ON = 678;

    public NotificationService(){}      // do not remove

    @Override
    public void onCreate() {
    }

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            return START_STICKY;
        }
        createNotification();

        int operation = -1;
        try {
            operation = intent.getIntExtra("operation", -1);
        } catch (Exception e) {
            Log.e("operation", e.toString());
        }

//        if (operation == A_MINUTE) {
//            quiet_minute(20 * 60);
        if (operation == MAKE_SILENT) {
//            if (several > 0) {
//                several = 0;
//                ScheduleNextTask.request("make Silent");
//            }
//            else
                make_silent();
        } else if (operation == RIGHT_NOW) {
            Intent oIntent = new Intent(this, ActivityOneTime.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, oIntent, PendingIntent.FLAG_MUTABLE);
            try {
                pendingIntent.send();
            } catch(PendingIntent.CanceledException e) {
                Log.e("startCommand","errror RIGHT_NOW "+e);
            }
        } else if (operation == STOP_SPEAK) {
            stop_repeat = false;
            updateRemoteViews();
            ScheduleNextTask.request("stopped, next is");
        } else if (operation == VOLUMES) {
            updateRemoteViews();
        }
        startForeground(100, mBuilder.build());
        return START_STICKY;
    }

    private void show_Volumes() {

        Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint txtPaint = new Paint();
        txtPaint.setTextAlign(Paint.Align.LEFT);
        txtPaint.setAntiAlias(true);
        txtPaint.setTextSize(32);
        txtPaint.setColor(0xFFFFFFFF);
        txtPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        txtPaint.setStrokeWidth(2);
        Paint linePaint = new Paint();
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linePaint.setTextAlign(Paint.Align.LEFT);
        linePaint.setColor(0xFF00FFFF);

        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        int rVol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        int mVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        drawVolume(canvas, "M", 40, mVol, txtPaint, linePaint);
        drawVolume(canvas, "R", 100, rVol, txtPaint, linePaint);

        mRemoteViews.setImageViewBitmap(R.id.volume_now, bitmap);
        mNotificationManager.notify(100,mBuilder.build());
    }

    void drawVolume(Canvas canvas, String s, int yPos, int vol, Paint txtPaint, Paint lnPaint) {
        final int shift = 40;
        canvas.drawText(s, 8, yPos+6, txtPaint);
        lnPaint.setStrokeWidth(20);
        canvas.drawLine(shift, yPos, shift+vol * 5, yPos, lnPaint);
        lnPaint.setStrokeWidth(2);
        canvas.drawLine(shift+vol*5, yPos, shift + 15 * 5, yPos, txtPaint);
    }

    private void make_silent() {
        // while 만보 적용, 사무실 ...
        sounds.setSilentMode();
        show_Volumes();
        updateRemoteViews();
    }

    private void createNotification() {

        mRemoteViews = new RemoteViews(this.getPackageName(), R.layout.notification_bar);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationChannel = new NotificationChannel("default","default", NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(mNotificationChannel);
        mBuilder = new NotificationCompat.Builder(this,"default")
                .setSmallIcon(R.drawable.auto_quite)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // added 24.09.30
                .setLocalOnly(true)  // Ensures notification is local to the device
                .setContent(mRemoteViews)
                .setCategory(Notification.CATEGORY_EVENT)
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(false)
        ;

        Intent mainIntent = new Intent(this, ActivityMain.class);
        mRemoteViews.setOnClickPendingIntent(R.id.ll_customNotification, PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));

        Intent rightNowI = new Intent(this, NotificationService.class);
        rightNowI.putExtra("operation", RIGHT_NOW);
        PendingIntent rightNowP = PendingIntent.getService(this, RIGHT_NOW, rightNowI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(rightNowP);
        mRemoteViews.setOnClickPendingIntent(R.id.right_now, rightNowP);

        Intent toss5 = new Intent(this, NotificationService.class);
        toss5.putExtra("operation", MAKE_SILENT);
        PendingIntent fiveP = PendingIntent.getService(this, MAKE_SILENT, toss5, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(fiveP);
        mRemoteViews.setOnClickPendingIntent(R.id.make_silent, fiveP);

        Intent tossI = new Intent(this, NotificationService.class);
        tossI.putExtra("operation", A_MINUTE);
        PendingIntent oneP = PendingIntent.getService(this, A_MINUTE, tossI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(oneP);
        mRemoteViews.setOnClickPendingIntent(R.id.a_minute, oneP);

        Intent vOnI = new Intent(this, NotificationService.class);
        vOnI.putExtra("operation", VOLUME_ON);
        PendingIntent vOnP = PendingIntent.getService(this, VOLUME_ON, vOnI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(vOnP);
        mRemoteViews.setOnClickPendingIntent(R.id.volume_on, vOnP);

        Intent volI = new Intent(this, NotificationService.class);
        volI.putExtra("operation", VOLUMES);
        PendingIntent volP = PendingIntent.getService(this, VOLUMES, volI, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(volP);
        mRemoteViews.setOnClickPendingIntent(R.id.volume_now, volP);

    }

    void updateRemoteViews() {

        mRemoteViews.setImageViewResource(R.id.wState, icon);
        mRemoteViews.setTextViewText(R.id.calSubject, subject);
        mRemoteViews.setTextViewText(R.id.beg_time, beg + " "+end);

        mRemoteViews.setImageViewResource(R.id.state_iconN, iconN);
        mRemoteViews.setTextViewText(R.id.wSubject, subjectN);
        mRemoteViews.setTextViewText(R.id.wTime, begN+" "+endN);
        mNotificationManager.notify(100,mBuilder.build());
        show_Volumes();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}