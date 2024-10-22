package better.life.autoquiet.Sub;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;

public class AdjVolumes {

    public enum VOL { COND_OFF, FORCE_ON, FORCE_OFF, WORK_ON}
//    int rVol, mVol, nVol, sVol, aVol;

    // 0: off, 1: conditional on, 2: force to on
    public AdjVolumes(Context context, VOL OnOff) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();

        switch (OnOff) {
            case COND_OFF:
//                rVol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
//                mVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                nVol = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
//                sVol = audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
//                aVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
//
//                if (mVol > 8 || nVol > 8) {
//                    sharedEditor.putInt("ring", rVol);
//                    sharedEditor.putInt("music", mVol);
//                    sharedEditor.putInt("notify", nVol);
//                    sharedEditor.putInt("system", sVol);
//                    sharedEditor.putInt("alarm", aVol);
//                    sharedEditor.apply();
//                    sharedEditor.commit();
//                }
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 3, 0);
                break;
//            case COND_ON:
//                if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
//                    audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE)
//                    return;
//                rVol = sharedPref.getInt("ring", 12);
//                mVol = sharedPref.getInt("music", 12);
//                nVol = sharedPref.getInt("notify", 12);
//                sVol = sharedPref.getInt("system", 5);
//                aVol = sharedPref.getInt("alarm", 12);
//                audioManager.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
//                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVol, 0);
//                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, nVol, 0);
//                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, sVol, 0);
//                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, aVol, 0);
//                break;
            case FORCE_ON:
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 12, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 12, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 5, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 8, 0);
                break;
            case WORK_ON:
                audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 1, 0);
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
