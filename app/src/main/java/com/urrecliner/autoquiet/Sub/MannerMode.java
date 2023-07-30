package com.urrecliner.autoquiet.Sub;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.urrecliner.autoquiet.R;

public class MannerMode {

    public void turn2Quiet(Context context, boolean sharedManner, boolean vibrate) {

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
                Thread.sleep(1000);
                am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_PLAY_SOUND);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void turn2Normal(boolean sharedManner, Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        assert am != null;
        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
//        int mx = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//        am.setStreamVolume(AudioManager.STREAM_MUSIC, mx-3, AudioManager.FLAG_PLAY_SOUND);

        if (sharedManner) {
            MediaPlayer mpFinish = MediaPlayer.create(context, R.raw.back2normal);
            mpFinish.setOnPreparedListener(MediaPlayer::start);
            mpFinish.setOnCompletionListener(mediaPlayer -> {
                mediaPlayer.reset();
                mediaPlayer.release();
            });
        }
    }
}