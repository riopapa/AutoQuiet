package better.life.autoquiet.common;

import static better.life.autoquiet.activity.ActivityMain.mContext;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

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
    int rVol;
    static String ttsID = "";
    AudioAttributes beepAttr, ringAttr, blueAttr;
    public static AudioFocusRequest mFocusGain = null;

    public Sounds(Context context) {
        this.context = context;
        ringAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        blueAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        beepAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mFocusGain = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .build();

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
//                        mAM.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
//                        mAM.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                        mAM.abandonAudioFocusRequest(mFocusGain);
                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                });
                mTTS.setAudioAttributes(ringAttr);

                mTTS.setLanguage(Locale.getDefault());
                mTTS.setPitch(1.2f);
                mTTS.setSpeechRate(1.3f);
            }
        });

        mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        beepMP = new MediaPlayer();
        beepMP.setAudioAttributes(ringAttr);
    }

    public void setNormalMode() {
        mAM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
    public void setSilentMode() {mAM.setRingerMode(AudioManager.RINGER_MODE_SILENT);}
    public void setVibrateMode() {mAM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);}

    public void beep(BEEP e) {

        getCurrVolumes();

        int soundID;
        switch (e) {
            case NOTY:    soundID = R.raw.tympani_bing; break;
            default:     soundID = R.raw.wood_plank_flicks; break;
        }

        setVolumeTo(12);
        try {
            beepMP.setDataSource(context,
                    Uri.parse("android.resource://" + context.getPackageName() + "/" + soundID));
        } catch (Exception err) {
        }
        beepMP.prepareAsync();
        beepMP.setOnPreparedListener(MediaPlayer::start);
        beepMP.setOnCompletionListener(mp -> setVolumeTo(rVol));
    }

    public boolean isQuiet() {
        return (mAM.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAM.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }

    public void sayTask (String say) {

        String connectedDeviceName = BluetoothUtil.getConnectedTargetDeviceName(mContext);
        Log.w("sayTask", connectedDeviceName + " " + say);
        mAM.requestAudioFocus(mFocusGain);

        getCurrVolumes();
        if (connectedDeviceName == null) {
            mTTS.setAudioAttributes(ringAttr);
            setVolumeTo(12);
        } else{
            mTTS.setAudioAttributes(blueAttr);
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, 13, 0);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                mTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "i");
            }
        }, 1000);
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