package better.life.autoquiet.common;

import static better.life.autoquiet.AlarmReceiver.sounds;
import static better.life.autoquiet.activity.ActivityMain.mContext;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MyTTS {

    public static TextToSpeech mTTS;
    public static AudioManager mAM;

    public MyTTS() {
        mTTS = new TextToSpeech(mContext, status -> {
            if (status == TextToSpeech.SUCCESS) {
                initializeTTS();
            }
        });
    }

    String TTSId = "tts";

    private void initializeTTS() {

        mAM = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        mTTS.setAudioAttributes(audioAttributes);

        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                TTSId = utteranceId;
            }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                if (mTTS.isSpeaking())
                    return;
                mTTS.stop();
            }

            @Override
            public void onError(String utteranceId) { }
        });

        mTTS.setLanguage(Locale.getDefault());
        mTTS.setPitch(1.2f);
        mTTS.setSpeechRate(1.3f);
    }

    public void sayTask (String say) {
//        if (isBlueToothActive())
//           setVolume2EarPhone();
//        else 
//            setVolume2NormalRingTone();
        if (sounds.checkAudio(mContext)) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    mTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "i");
                }
            }, 500);

        }
    }

    void setVolume2EarPhone() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        mTTS.setAudioAttributes(audioAttributes);
    }

    void setVolume2NormalRingTone() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        mTTS.setAudioAttributes(audioAttributes);
    }

    public boolean isBlueToothActive() {

        AudioDeviceInfo[] audioDevices = mAM.getDevices(AudioManager.GET_DEVICES_INPUTS);
        for(AudioDeviceInfo deviceInfo : audioDevices){
            if (deviceInfo.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO     // 007
                    || deviceInfo.getType()==AudioDeviceInfo.TYPE_BLUETOOTH_A2DP    // 008
                    || deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADSET
                    || deviceInfo.getType()==AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                )
                return true;
        }
        return false;
    }
}
