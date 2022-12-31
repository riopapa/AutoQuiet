package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.mContext;

import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;

public class Sounds {

    private SoundPool soundPool = null;
    private final int[] beepSound = {
            R.raw.say_notification,
            R.raw.say_notification,
            R.raw.leading_sound                   //  event button pressed
    };
    private final int[] soundNbr = new int[beepSound.length];

    void beep(int soundId) {

        SoundPool.Builder builder;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        builder = new SoundPool.Builder();
        builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
        soundPool = builder.build();
        soundPool.setOnLoadCompleteListener((soundPool, f1, f2) -> {
            int soundNbr = soundPool.load(mContext, beepSound[soundId], 1);
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    soundPool.play(soundNbr, 1f, 1f, 1, 0, 1f);
                }
            }, 500);
        });
//
//        if (soundPool == null) {
//            initiate(1);
//        } else {
//            soundPool.play(soundNbr[soundId], 1f, 1f, 1, 0, 1f);
//        }
    }
}