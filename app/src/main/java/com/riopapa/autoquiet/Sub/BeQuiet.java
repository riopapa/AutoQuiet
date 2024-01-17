package com.riopapa.autoquiet.Sub;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class BeQuiet {

    // 0: off, 1: conditional on, 2: force to on
    public BeQuiet(Context context, int OnOff) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();

        if (OnOff == 0) {
            int rVol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            int mVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int nVol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
            int sVol = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
            int aVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            if (mVol > 9 || nVol > 9) {
                sharedEditor.putInt("ring", rVol);
                sharedEditor.putInt("music", mVol);
                sharedEditor.putInt("notify", nVol);
                sharedEditor.putInt("system", sVol);
                sharedEditor.putInt("alarm", aVol);
                sharedEditor.apply();
                sharedEditor.commit();
            }
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 3, 0);

        } else if (OnOff == 1) {
            int rVolNow = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            int mVolNow = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (rVolNow < 10 || mVolNow < 10) {
                int rVol = sharedPref.getInt("ring", 12);
                int mVol = sharedPref.getInt("music", 12);
                int nVol = sharedPref.getInt("notify", 12);
                int sVol = sharedPref.getInt("system", 4);
                int aVol = sharedPref.getInt("alarm", 12);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVol, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, nVol, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, sVol, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, aVol, 0);
            }

        } else {
            audioManager.setStreamVolume(AudioManager.STREAM_RING, 12, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 12, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 12, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 4, 0);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 12, 0);
        }
    }
}
