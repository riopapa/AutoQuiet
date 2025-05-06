package better.life.autoquiet.common;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class VibratePhone {
    public VibratePhone(Context context, int type) {

        // 0 : short, 1: long
        final long[][] pattern = {{100, 20, 200, 400, 500, 550}, {100, 120, 100, 300, 300, 250,
                100, 120, 100, 300, 300, 250, 0, 120, 100, 300, 300, 250, 0, 120, 100, 300, 300, 250},
                {100,100,100,100}};
        VibratorManager vibratorManager =
                (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        Vibrator vibrator = vibratorManager.getDefaultVibrator();
        VibrationEffect vibrationEffect =
                VibrationEffect.createWaveform(pattern[type], -1);
        vibrator.cancel();
        vibrator.vibrate(vibrationEffect);
    }
}
