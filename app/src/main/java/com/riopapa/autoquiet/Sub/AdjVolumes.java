package com.riopapa.autoquiet.Sub;

import static com.riopapa.autoquiet.AlarmReceiver.isSilentNow;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

public class AdjVolumes {

    public enum VOL { COND_OFF, COND_ON, FORCE_ON, FORCE_OFF}
    int rVol, mVol, nVol, sVol, aVol;

    // 0: off, 1: conditional on, 2: force to on
    public AdjVolumes(Context context, VOL OnOff) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();

        switch (OnOff) {
            case COND_OFF:
                rVol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                mVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                nVol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
                sVol = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
                aVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

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
                break;
            case COND_ON:
                if (isSilentNow())
                    return;
                rVol = sharedPref.getInt("ring", 12);
                mVol = sharedPref.getInt("music", 12);
                nVol = sharedPref.getInt("notify", 12);
                sVol = sharedPref.getInt("system", 4);
                aVol = sharedPref.getInt("alarm", 12);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVol, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, nVol, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, sVol, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, aVol, 0);
                break;
            case FORCE_ON:
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 12, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 12, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 12, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 4, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 12, 0);
                break;
            default:    // force Off
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, 0);
                break;
        }
    }
}
