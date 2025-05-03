package better.life.autoquiet.common;

import static better.life.autoquiet.activity.ActivityMain.mContext;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import better.life.autoquiet.R;

public class FloatingClockService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingView;
    private TextView timeTextView;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mUpdateTimeTask;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_clock_layout, null);

        // Set layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Important for drawing over other apps
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // Allows interaction with apps behind the overlay
                PixelFormat.TRANSLUCENT);

        // Specify the initial position
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 50;
        params.width = 400;
        params.y = 300;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingView, params);

        timeTextView = mFloatingView.findViewById(R.id.timeTextView);
        ImageView closeButton = mFloatingView.findViewById(R.id.closeButton);

        mUpdateTimeTask = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                String currentTime = sdf.format(new Date());
                timeTextView.setText(currentTime);
                String sec = currentTime.substring(currentTime.length() - 2);
                int compVal = sec.compareTo("50");
                if (compVal > 0)
                    timeTextView.setTextColor(0xFFFF0000);
                else
                    timeTextView.setTextColor(0xFFFFFFFF);
                compVal = sec.compareTo("56");
                if (compVal > 0)
                    new VibratePhone(mContext, 2);
                long nxtDelay = 1002 - (System.currentTimeMillis() % 1000);
                mHandler.postDelayed(this, nxtDelay);
            }
        };
        mHandler.postDelayed(mUpdateTimeTask, 1000);

        closeButton.setOnClickListener(v -> {
            stopSelf(); // Stop the service and remove the view
        });

        mFloatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) {
            mWindowManager.removeView(mFloatingView);
        }
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
}