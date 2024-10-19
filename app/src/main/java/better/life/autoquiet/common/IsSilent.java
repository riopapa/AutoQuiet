package better.life.autoquiet.common;

import static better.life.autoquiet.AlarmReceiver.mAudioManager;

import android.media.AudioManager;

public class IsSilent {
    public boolean now() {
        int ringVol = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
        return (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE ||
                ringVol < 4);
    }
}
