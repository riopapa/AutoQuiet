package com.riopapa.autoquiet.Sub;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class VibratePhone {
    public VibratePhone(Context context) {

        VibratorManager vibratorManager;
        Vibrator vibrator = null;
        VibrationEffect vibrationEffect = null;
        final long[] vibPattern = {0, 20, 200, 300, 300, 400, 400, 500, 550, 300, 400, 400, 500, 550};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibratorManager =
                        (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                vibrator = vibratorManager.getDefaultVibrator();
                vibrationEffect = VibrationEffect.createWaveform(vibPattern, -1);
        }
        vibrator.cancel();
        vibrator.vibrate(vibrationEffect);
    }
}
