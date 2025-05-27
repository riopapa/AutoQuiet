package better.life.autoquiet.common;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import better.life.autoquiet.R;

public class Sounds {

    private boolean ttsReady = false;
    public enum BEEP {NOTY, INFO, WEEK, BACK}
    final int [] beepRes = {R.raw.beep_beep , R.raw.msg_inform, R.raw.tympani_bing, R.raw.back2normal};
    final Uri [] dataSrc = new Uri[4];
    public static TextToSpeech mTTS;
    public static AudioManager mAM;
    public static MediaPlayer beepMP;
    int rVol;
    AudioAttributes beepAttr, ringAttr, blueAttr;
    public static AudioFocusRequest mFocusGain = null;
    String blueDevice = "";
    Context context;

    public Sounds() {
        context = ContextProvider.get();
        if (context == null)
            Log.e("sound context","context is null ////////////");
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

        for (int j = 0; j < dataSrc.length; j++)
            dataSrc[j] = Uri.parse("android.resource://" + context.getPackageName() + "/" +   beepRes[j]);

        mTTS = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                    mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    mFocusGain = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                            .build();
                        initializeTTS();
                }, 300);
            }
        });
        beepMP = new MediaPlayer();
        beepMP.setAudioAttributes(beepAttr);
    }

    private void initializeTTS() {
        mTTS.setLanguage(Locale.KOREA);
        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}
            @Override
            public void onDone(String utteranceId) {}
            @Override
            public void onError(String utteranceId) {}
        });

            /*     Samsung voices
               ko-kr-x-ism-network, ko-kr-x-kob-network, ko-kr-x-koc-network, ko-kr-x-kod-network
               ko-KR-language, ko-kr-x-kob-local, ko-kr-x-kod-local, ko-kr-x-koc-local
             */
        List<Voice> voices = getKoreanVoices(mTTS);
        for (Voice voice : voices) {
            if (voice.getName().equals("ko-kr-x-kod-local")) {
                mTTS.setVoice(voice);
                break;
            }
        }
        ttsReady = true;
    }

    private List<Voice> getKoreanVoices(TextToSpeech mTTS) {
        Set<Voice> allVoices = mTTS.getVoices();
        List<Voice> koreanVoices = new ArrayList<>();
        for (Voice voice : allVoices) {
            if (voice.getLocale().equals(Locale.KOREA)) {
                koreanVoices.add(voice);
            }
        }
        return koreanVoices;
    }

    public void setNormalMode() {
        if (mAM == null)
            mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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
        if (mAM == null) {
            context = ContextProvider.get();
            mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        return (mAM.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAM.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }

    public void sayTask (String say) {

        if (isPhoneQuiet()) {
            phoneVibrate.go(2);
            return;
        }
        while (!ttsReady) {
            try {
                Log.e("sounds","tts not ready");
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Context context = ContextProvider.get();
        blueDevice = BluetoothUtil.getConnectedTargetDeviceName(context);
        if (mAM == null)
            mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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