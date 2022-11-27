package com.urrecliner.autoquiet;

import static com.urrecliner.autoquiet.Vars.sharedManner;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

class MannerMode {

    static void turn2Quiet(Context context, boolean vibrate) {

        if (sharedManner) {
            MediaPlayer mpStart = MediaPlayer.create(context, R.raw.manner_starting);
            mpStart.setOnPreparedListener(MediaPlayer::start);
            mpStart.setOnCompletionListener(mediaPlayer -> {
                mediaPlayer.reset();
                mediaPlayer.release();
            });
        }

        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        if (vibrate)
            am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        else {
            if(am.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
        }
    }

    static void turn2Normal(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        int mx = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, mx-3, AudioManager.FLAG_PLAY_SOUND);

        if (sharedManner) {
            MediaPlayer mpFinish = MediaPlayer.create(context, R.raw.back2normal);
            mpFinish.setOnPreparedListener(MediaPlayer::start);
            mpFinish.setOnCompletionListener(mediaPlayer -> {
                mediaPlayer.reset();
                mediaPlayer.release();
            });
        }
    }

//    static void vibratePhone(Context context) {
////        long[] pattern = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
//        long[] pattern = {0, 100, 300, 200, 300, 100, 200};
//        Vibrator v = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
//        assert v != null;
//        v.vibrate(VibrationEffect.createWaveform(pattern, -1));
//    }
}