package better.life.autoquiet.common;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Timer;
import java.util.TimerTask;
import better.life.autoquiet.R;

public class Sounds {

    public enum BEEP {NOTY, INFO, WEEK, BACK}
    public static TextToSpeech mTTS;
    public static AudioManager mAM;
    public static MediaPlayer beepMP;
    int rVol;
    static String ttsID = "";
    AudioAttributes beepAttr, ringAttr, blueAttr;
    public static AudioFocusRequest mFocusGain = null;
    String blueDevice = "";

    public Sounds() {
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

        Context context = ContextProvider.get();
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
                        if (blueDevice.isEmpty()) {
                            mAM.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
                            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                        }
                        mAM.abandonAudioFocusRequest(mFocusGain);
                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                });
                mTTS.setAudioAttributes(ringAttr);

//                mTTS.setLanguage(Locale.getDefault());
//                mTTS.setPitch(1.2f);
//                mTTS.setSpeechRate(1.3f);
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

        if (isPhoneQuiet())
            return;
        getCurrVolumes();

        int soundID;
        if (e == BEEP.NOTY) {
            soundID = R.raw.tympani_bing;
        } else if (e == BEEP.WEEK) {
                soundID = R.raw.glass_drop_and_roll;
        } else if (e == BEEP.INFO){
            soundID = R.raw.wood_plank_flicks;
        } else if (e == BEEP.BACK){
            soundID = R.raw.back2normal;
        } else
            soundID = R.raw.manner_starting;

        setVolumeTo(12);
        Context context = ContextProvider.get();
        try {
            beepMP.setDataSource(context,
                    Uri.parse("android.resource://" + context.getPackageName() + "/" + soundID));
        } catch (Exception err) {
        }
        beepMP.prepareAsync();
        beepMP.setOnPreparedListener(MediaPlayer::start);
        beepMP.setOnCompletionListener(mp -> setVolumeTo(rVol));
    }

    public boolean isPhoneQuiet() {
        return (mAM.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAM.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }

    public void sayTask (String say) {

        if (isPhoneQuiet()) {
            phoneVibrate.go(2);
            return;
        }
        Context context = ContextProvider.get();
        blueDevice = BluetoothUtil.getConnectedTargetDeviceName(context);
        mAM.requestAudioFocus(mFocusGain);

        getCurrVolumes();
        if (blueDevice.isEmpty()) {
            mTTS.setAudioAttributes(ringAttr);
            setVolumeTo(12);
        } else{
            mTTS.setAudioAttributes(blueAttr);
            mAM.setStreamVolume(AudioManager.STREAM_MUSIC, 15, 0);
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