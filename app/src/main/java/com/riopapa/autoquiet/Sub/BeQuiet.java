package com.riopapa.autoquiet.Sub;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

public class BeQuiet {

    public BeQuiet(Context context, boolean OnOff) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();

        if (OnOff) {
            int vol;
            vol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            if (vol > 5) {
                sharedEditor.putInt("ring", vol);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
                vol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                sharedEditor.putInt("music", vol);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
                vol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                sharedEditor.putInt("notify", vol);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, 0);
                vol = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                sharedEditor.putInt("system", vol);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, 0);
                vol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
                sharedEditor.putInt("alarm", vol);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 1, 0);
                sharedEditor.apply();
                sharedEditor.commit();
            }

        } else {
            int vol;
            vol = sharedPref.getInt("ring", 5);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, vol, 0);

            vol = sharedPref.getInt("music", 5);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);

            vol = sharedPref.getInt("notify", 5);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, vol, 0);

            vol = sharedPref.getInt("system", 5);
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, vol, 0);

            vol = sharedPref.getInt("alarm", 5);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, vol, 0);

        }
    }
}
