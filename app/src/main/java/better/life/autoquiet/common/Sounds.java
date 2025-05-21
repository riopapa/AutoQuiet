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
import android.speech.tts.Voice;
import android.util.Log;

import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import better.life.autoquiet.R;

public class Sounds {

    public enum BEEP {NOTY, INFO, WEEK, BACK}
    final Uri [] dataSrc = new Uri[4];
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
                initializeTTS();
            }
        });

        mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        beepMP = new MediaPlayer();
        beepMP.setAudioAttributes(ringAttr);
        // NOTY
        dataSrc[0] = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.tympani_bing);
        // WEEK
        dataSrc[1] = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.glass_drop_and_roll);
        // INFO
        dataSrc[2] = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.wood_plank_flicks);
        // BACK
        dataSrc[3] = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.back2normal);

    }

    private void initializeTTS() {
        mTTS.setLanguage(Locale.KOREAN);
        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {

            }

            @Override
            public void onError(String utteranceId) {

            }
        });

            /*     Samsung voices
               ko-kr-x-ism-network, ko-kr-x-kob-network, ko-kr-x-koc-network, ko-kr-x-kod-network
               ko-KR-language, ko-kr-x-kob-local, ko-kr-x-kod-local, ko-kr-x-koc-local
             */
        Set<Voice> voices = mTTS.getVoices();
        if (voices != null) {
            // Example: Try to find a female voice for the default language
            Voice selectedVoice = null;
            for (Voice voice : voices) {
                if (voice.getLocale().equals(Locale.getDefault()) ) {
                    if (voice.getName().equals("ko-kr-x-kod-local")) {
                        selectedVoice = voice;
                        break;
                    }
                }
            }

            if (selectedVoice != null) {
                int setVoiceResult = mTTS.setVoice(selectedVoice);
                if (setVoiceResult == TextToSpeech.SUCCESS) {
                    Log.w("TTS", "///   Successfully set voice: " + selectedVoice.getName());
                } else {
                    Log.e("TTS", "Failed to set voice: " + selectedVoice.getName() + " Result: " + setVoiceResult);
                }
            } else {
                Log.w("TTS", "No specific voice found for default locale or desired features. Using default.");
            }
        } else {
            Log.w("TTS", "No voices available from TTS engine.");
        }
    }

    public void setNormalMode() {
        mAM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
    public void setSilentMode() {mAM.setRingerMode(AudioManager.RINGER_MODE_SILENT);}
    public void setVibrateMode() {mAM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);}

    public void beep(BEEP beep) {

        if (isPhoneQuiet())
            return;
        getCurrVolumes();

        setVolumeTo(12);
        Context context = ContextProvider.get();
        try {
            beepMP.setDataSource(context, dataSrc[beep.ordinal()]);
        } catch (Exception err) {
            Log.e("sounds","source "+beep+" Error: "+err);
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