package better.life.autoquiet.common;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class PhoneVibrate {

    VibratorManager vibManager;
    Vibrator vibrator = null;
    VibrationEffect vibEffect = null;
    Context context;
    public PhoneVibrate(Context context) {
        this.context = context;
    }

    public void go(int type) {

        // 0 : short, 1: long
        final long[][] vibPattern = {{70,70,70,70},
                {0, 20, 200, 300, 300, 400, 0, 20, 100, 200, 30, 20, 100, 200, 200, 100}
        };

        if (vibManager == null) {
            vibManager = (VibratorManager) context
                    .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibManager.getDefaultVibrator();
        }
        vibEffect = VibrationEffect.createWaveform(vibPattern[type], -1);
        vibrator.cancel();
        vibrator.vibrate(vibEffect);
    }
}
