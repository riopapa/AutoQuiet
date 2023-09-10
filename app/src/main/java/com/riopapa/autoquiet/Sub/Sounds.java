package com.riopapa.autoquiet.Sub;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.riopapa.autoquiet.R;

public class Sounds {

    public enum BEEP {NOTY, ALARM, INFO, BBEEPP, TOSS}

    public void beep(Context context, BEEP e) {

        int soundID = 0;
        switch (e) {
            case NOTY:    soundID = R.raw.glass_drop_and_roll; break;
            case ALARM:   soundID = R.raw.tympani_bing; break;
            case INFO:   soundID = R.raw.wood_plank_flicks; break;
            case BBEEPP:   soundID = R.raw.beep_beep; break;
            case TOSS:   soundID = R.raw.toss_beep; break;
        }
        SoundPool.Builder builder;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        builder = new SoundPool.Builder();
        builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
        SoundPool soundPool1 = builder.build();
        int soundNbr = soundPool1.load(context, soundID, 1);
        soundPool1.setOnLoadCompleteListener((soundPool, f1, f2) -> {
            soundPool.play(soundNbr, 1f, 1f, 1, 0, 1f);
//            Handler handler = new Handler(Looper.getMainLooper());
//            handler.postDelayed(() -> soundPool.play(soundNbr, 1f, 1f, 1, 0, 1f), 500);
        });
    }
}