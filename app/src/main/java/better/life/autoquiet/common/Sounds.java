package better.life.autoquiet.common;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import better.life.autoquiet.R;

public class Sounds {

    public enum BEEP {NOTY, INFO}
    public static TextToSpeech mTTS;
    public static AudioManager mAM;
    public static MediaPlayer beepMP;
    Context context;
    int rVol, mVol;
    static String ttsID = "";
    AudioAttributes beepAttr;

    public Sounds(Context context) {
        this.context = context;
        mTTS = new TextToSpeech(context, status -> {
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

        mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        beepAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE) // Or USAGE_ASSISTANCE_SONIFICATION, etc.
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
    }

    public void setNormalMode() {
        mAM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
    public void setSilentMode() {
        mAM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    }

    public void beep(BEEP e) {

        getCurrVolumes();
//        if (beepMP != null) {
//            if (beepMP.isPlaying()) {
//                beepMP.stop();
//            }
//            beepMP.release();
//            beepMP = null; // Set to null after releasing
//        }
        int soundID;
        switch (e) {
            case NOTY:    soundID = R.raw.tympani_bing; break;
            default:     soundID = R.raw.wood_plank_flicks; break;
        }

        setVolumeTo(12);
        beepMP = new MediaPlayer();
        try {
            beepMP.setDataSource(context,
                    Uri.parse("android.resource://" + context.getPackageName() + "/" + soundID));
        } catch (Exception err) {
            err.printStackTrace();
        }
        beepMP.setAudioAttributes(beepAttr);
        beepMP.prepareAsync();
        beepMP.setOnPreparedListener(MediaPlayer::start);
        beepMP.setOnCompletionListener(mp -> setVolumeTo(rVol));

    }

    public boolean isQuiet() {
        int ringVol = mAM.getStreamVolume(AudioManager.STREAM_RING);
        return (mAM.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAM.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE ||
                ringVol < 4);
    }

    public void sayTask (String say) {
        getCurrVolumes();
        setVolumeTo(12);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "i");
            }
        }, 700);    // after complete of beep
    }

    public void setVolumeTo(int rVol) {
        mAM.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
//        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, mVol, 0);
    }

    private void getCurrVolumes() {
        rVol = mAM.getStreamVolume(AudioManager.STREAM_RING);
//        mVol = mAM.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

}