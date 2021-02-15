package com.urrecliner.letmequiet;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;

import static com.urrecliner.letmequiet.Vars.mainContext;
import static com.urrecliner.letmequiet.Vars.utils;

class Text2Speech {

    private String logID = "TTS";
    private TextToSpeech mTTS;
    float ttsPitch = 1.3f, ttsSpeed = 1.4f;
    static AudioManager mAudioManager = null;
    static AudioFocusRequest mFocusGain = null;
    Context context;

    void initiateTTS(Context context) {
        this.context = context;
        mTTS = null;
        mTTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.KOREA);
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        utils.logE(logID, "Language not supported");
                    }
                } else {
                    utils.logE(logID, "Initialization failed");
                }
            }
        });
        readyAudioTTS();
    }

    void setPitch(float p) {
        ttsPitch = p;
    }

    void setSpeed(float s) {
        ttsSpeed = s;
    }

    void speak(String text) {

        try {
            mAudioManager.requestAudioFocus(mFocusGain);
        } catch (Exception e) {
            utils.logE(logID, "requestAudioFocus");
        }

        ttsSpeak(text);
        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) { }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {
                mAudioManager.abandonAudioFocusRequest(mFocusGain);
            }

            @Override
            public void onError(String utteranceId) { }
        });
    }

    private void ttsSpeak(String text) {
        readyAudioTTS();
        Bundle params = new Bundle();
        params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f); // change the 0.5f to any value from 0f-1f (1f is default)
        try {
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, params, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        } catch (Exception e) {
            utils.logE(logID, "justSpeak:" + e.toString());
        }
    }

    void ttsStop() {
        mTTS.stop();
        mAudioManager.abandonAudioFocusRequest(mFocusGain);
        try {
            readyAudioTTS();
        } catch (Exception e) {
            utils.logE(logID, "ttsStop:" + e.toString());
        }
    }

    void readyAudioTTS() {

        if(mAudioManager == null) {
            try {
                mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                mFocusGain = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (ttsPitch == 0f) {
            ttsPitch = 1.2f;
            ttsSpeed = 1.4f;
        }
        if (mTTS == null) {
            utils.log(logID, "$$ mTTS NULL ");
            initiateTTS(mainContext);
        }
        mTTS.setPitch(ttsPitch);
        mTTS.setSpeechRate(ttsSpeed);
    }

}
