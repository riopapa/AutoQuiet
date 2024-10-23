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

        switch (OnOff) {
            case COND_OFF:

                audioManager.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, 1, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 3, 0);
                break;

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
