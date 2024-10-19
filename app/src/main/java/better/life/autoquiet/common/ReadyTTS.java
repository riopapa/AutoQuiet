package better.life.autoquiet.common;

import static better.life.autoquiet.activity.ActivityMain.mContext;

import android.media.AudioAttributes;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

public class ReadyTTS {

    public static Sounds sounds;
    public static TextToSpeech myTTS;
    public ReadyTTS() {

        if (sounds == null)
            sounds = new Sounds();
        myTTS = null;
        myTTS = new TextToSpeech(mContext, status -> {
            if (status == TextToSpeech.SUCCESS) {
                initializeTTS();
            }
        });
    }

    String TTSId = "tts";

    private void initializeTTS() {

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        myTTS.setAudioAttributes(audioAttributes);

        myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                TTSId = utteranceId;
            }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                if (myTTS.isSpeaking())
                    return;
                myTTS.stop();
            }

            @Override
            public void onError(String utteranceId) { }
        });

        myTTS.setLanguage(Locale.getDefault());
        myTTS.setPitch(1.2f);
        myTTS.setSpeechRate(1.3f);
    }
}
