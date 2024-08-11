package com.riopapa.autoquiet.Sub;

import android.media.AudioManager;

public class IsSilent {
    public boolean now(AudioManager mAudioManager) {
        int mVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE ||
                mVol < 4);
    }
}
