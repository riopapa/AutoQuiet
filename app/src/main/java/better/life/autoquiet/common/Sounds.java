package better.life.autoquiet.common;

import static better.life.autoquiet.activity.ActivityMain.mContext;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;


import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import better.life.autoquiet.R;

public class Sounds {

    public enum BEEP {NOTY, ALARM, INFO, BBEEPP, TOSS}
    public static int soundType = 0, soundNow = 0;
    public static TextToSpeech mTTS;
    public static AudioManager mAM;
    int rVol, mVol;
    static String ttsID = "";

    public Sounds(Context context) {
        mTTS = new TextToSpeech(mContext, status -> {
            if (status == TextToSpeech.SUCCESS) {
                mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        ttsID = utteranceId;
                    }

                    @Override
                    // this method will always called from a background thread.
                    public void onDone(String utteranceId) {
                        if (mTTS.isSpeaking())
                            return;
                        mTTS.stop();
                        mAM.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
                        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, mVol, 0);
                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                });
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build();
                mTTS.setAudioAttributes(audioAttributes);

                mTTS.setLanguage(Locale.getDefault());
                mTTS.setPitch(1.2f);
                mTTS.setSpeechRate(1.3f);
            }
        });

        setSoundNow(context);
        mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public boolean checkAudio(Context context) {

        setSoundNow(context);
        if (soundNow == soundType)
            return true;
        soundType = soundNow;
        if (soundType == 1) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            mTTS.setAudioAttributes(audioAttributes);
            return true;
        } else if (soundType == 2) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            mTTS.setAudioAttributes(audioAttributes);
            return true;
        }
        return false;
    }

    private static void setSoundNow(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int rVol = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        int mVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (rVol > 5 && mVol < 5)
            soundNow = 1;
        else if (rVol < 5 && mVol < 5)
            soundNow = 0;
        else
            soundNow = 2;
    }

    public void beep(Context context, BEEP e) {

        int soundID = 0;
        switch (e) {
            case NOTY:    soundID = R.raw.glass_drop_and_roll; break;
            case ALARM:   soundID = R.raw.tympani_bing; break;
            case INFO:   soundID = R.raw.wood_plank_flicks; break;
            case BBEEPP:   soundID = R.raw.beep_beep; break;
            case TOSS:   soundID = R.raw.toss_beep; break;
        }
        SoundPool.Builder builder;
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        builder = new SoundPool.Builder();
        builder.setAudioAttributes(audioAttributes).setMaxStreams(5);
        SoundPool soundPool1 = builder.build();
        int soundNbr = soundPool1.load(context, soundID, 1);
        soundPool1.setOnLoadCompleteListener((soundPool, f1, f2) -> {
            soundPool.play(soundNbr, 1f, 1f, 1, 0, 1f);
        });
    }
    public boolean isQuiet() {
        int ringVol = mAM.getStreamVolume(AudioManager.STREAM_RING);
        return (mAM.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAM.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE ||
                ringVol < 4);
    }

    public void sayTask (String say) {
        getCurrVolumes();
        setVolumeTo(12,12);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "i");
            }
        }, 500);
    }

    public void setVolumeTo(int rVol, int mVol) {
        mAM.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, mVol, 0);
    }

    public void getCurrVolumes() {
        rVol = mAM.getStreamVolume(AudioManager.STREAM_RING);
        mVol = mAM.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

}