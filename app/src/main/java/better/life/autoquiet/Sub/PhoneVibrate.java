package better.life.autoquiet.Sub;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public final class PhoneVibrate {

    private static VibratorManager vibManager;
    private static Vibrator vibrator = null;
    private static VibrationEffect vibEffect = null;

    public static void go(int type) {

        // 0 : short, 1: long, 2: phone in vibrate mode
        final long[][] vibPattern = {{30,30,30,30},
                {100,100,100,100,100,100,100,100,100,100,100,100, 100, 200, 30, 20, 100, 200, 200, 100},
                {100,100,100,100,100,100,100,100,100,100,100,100}
        };

        if (vibManager == null) {
            Context context = ContextProvider.get();
            vibManager = (VibratorManager) context
                    .getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibManager.getDefaultVibrator();
        }
        vibEffect = VibrationEffect.createWaveform(vibPattern[type], -1);
        vibrator.cancel();
        vibrator.vibrate(vibEffect);
    }
}
