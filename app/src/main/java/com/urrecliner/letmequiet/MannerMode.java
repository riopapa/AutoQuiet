package com.urrecliner.letmequiet;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.VibrationEffect;
import android.os.Vibrator;

import static android.content.Context.VIBRATOR_SERVICE;
import static com.urrecliner.letmequiet.Vars.mContext;
import static com.urrecliner.letmequiet.Vars.sharedManner;

class MannerMode {

    private static MediaPlayer mpStart, mpFinish;

    static void turn2Quiet(Context context, boolean vibrate) {
        beepStartQuiet();
        vibratePhone(context);

        AudioManager am = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        if (vibrate)
            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        else {
            if(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }

    }

    static void turn2Normal(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        vibratePhone(context);
        beepFinishQuiet();
    }

    static void vibratePhone(Context context) {
//        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
        long[] pattern = {0, 100, 300, 200, 300, 100, 200};
        Vibrator v = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        assert v != null;
        v.vibrate(VibrationEffect.createWaveform(pattern, -1));
    }

    private static void beepStartQuiet() {

        if (sharedManner) {
            if (mpStart == null) {
                mpStart = MediaPlayer.create(mContext, R.raw.manner_starting);
                mpStart.setOnPreparedListener(MediaPlayer::start);
            }
            else
                mpStart.start();
        }
    }

    private static void beepFinishQuiet() {

        if (sharedManner) {
            if (mpFinish == null) {
                mpFinish = MediaPlayer.create(mContext, R.raw.back2normal);
                mpFinish.setOnPreparedListener(MediaPlayer::start);
            }
            else
                mpFinish.start();
        }
    }

}
