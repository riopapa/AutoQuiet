package com.urrecliner.autoquiet;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;

public class Sounds {

    private final int[] beepSound = {
            R.raw.say_notification,
            R.raw.say_notification,
            R.raw.leading_sound                   //  event button pressed
    };

    void beep(Context context, int soundId) {

        SoundPool.Builder builder;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        builder = new SoundPool.Builder();
        builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
        SoundPool soundPool1 = builder.build();
        soundPool1.setOnLoadCompleteListener((soundPool, f1, f2) -> {
            int soundNbr = soundPool.load(context, beepSound[soundId], 1);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> soundPool.play(soundNbr, 1f, 1f, 1, 0, 1f), 500);
        });
    }
}