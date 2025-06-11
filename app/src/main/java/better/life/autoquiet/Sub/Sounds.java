package better.life.autoquiet.Sub;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import better.life.autoquiet.R;
import better.life.autoquiet.Utility;

public class Sounds {

    private boolean ttsReady = false;
    public enum BEEP {BEEP, INFO, WEEK, BACK, START}
    final int [] beepRes = {R.raw.beep_beep, R.raw.wood_plank_flicks, R.raw.msg_inform, R.raw.back2normal,
            R.raw.manner_starting};
    // Corrected: Declare without immediate assignment, initialize in constructor
    final Uri [] dataSrc;
    private TextToSpeech mTTS;
    private AudioManager mAM;
    private AudioFocusRequest mFocusGain = null;
    private int rVol;
    private AudioAttributes beepAttr, ringAttr;
    private String blueDevice = "";
    private final Context context;

    private final List<Runnable> pendingSayTasks = new CopyOnWriteArrayList<>();
    private final Object ttsInitLock = new Object();
    final Utility utility;
    private static Sounds instance; // The single instance

    public static synchronized Sounds getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new Sounds(applicationContext.getApplicationContext()); // Use application context
        }
        return instance;
    }

    public Sounds(Context applicationContext) {
        this.context = applicationContext;
//
//        if (context == null) {
//            Log.e("Sounds", "Application context is null in constructor ////////////");
//            // Depending on your app's structure, you might throw an IllegalArgumentException here
//            // throw new IllegalArgumentException("Application context cannot be null for Sounds initialization.");
//            return; // Or handle this error gracefully
//        }

        // --- CORRECTED INITIALIZATION OF dataSrc HERE ---
        // Initialize dataSrc array inside the constructor after context is available
        dataSrc = new Uri[beepRes.length]; // Allocate the array
        for (int j = 0; j < beepRes.length; j++) {
            dataSrc[j] = Uri.parse("android.resource://" + context.getPackageName() + "/" + beepRes[j]);
        }
        // --------------------------------------------------

        ringAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        beepAttr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        utility = new Utility();
        initTextToSpeech();
    }

    // ... (rest of your Sounds class methods remain the same as the previous revision) ...

    private void initTextToSpeech() {
//        if (mTTS != null) {
//            mTTS.stop();
//            mTTS.shutdown();
//            mTTS = null;
//            ttsReady = false;
//        }

        mTTS = new TextToSpeech(context, status -> {
            synchronized (ttsInitLock) {
                if (status == TextToSpeech.SUCCESS) {
                    mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    mFocusGain = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                            .setAudioAttributes(ringAttr)
                            .build();

                    setupTTSLanguageAndListener();
                    ttsReady = true;

                    new Handler(Looper.getMainLooper()).post(() -> {
                        for (Runnable task : pendingSayTasks) {
                            task.run();
                        }
                        pendingSayTasks.clear();
                    });
                    utility.log("Sounds", "TTS initialized successfully and ready.");

                } else {
                    ttsReady = false;
                    utility.log("Sounds", "TTS initialization failed with status: " + status);
                }
            }
        });
    }

    private void setupTTSLanguageAndListener() {
        if (mTTS == null) {
            utility.log("Sounds", "mTTS is null during setupTTSLanguageAndListener. Cannot configure.");
            return;
        }

        int langResult = mTTS.setLanguage(Locale.KOREA);
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            utility.log("Sounds", "Korean language is not supported or missing TTS data.");
        }

        mTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                Log.d("Sounds", "TTS Utterance Started: " + utteranceId);
            }
            @Override
            public void onError(String utteranceId) {
                utility.log("Sounds", "TTS Utterance Error for ID: " + utteranceId);
                if (mAM != null && mFocusGain != null) {
                    mAM.abandonAudioFocusRequest(mFocusGain);
                }
            }
            @Override
            public void onDone(String utteranceId) {
                utility.log("Sounds", "TTS Utterance Done: " + utteranceId);
                if (mAM != null) {
                    if (blueDevice.isEmpty()) {
                        mAM.setStreamVolume(AudioManager.STREAM_RING, rVol, 0);
                    } else {
                        mAM.setStreamVolume(AudioManager.STREAM_ACCESSIBILITY, rVol, 0);
                    }
                    if (mFocusGain != null) {
                        mAM.abandonAudioFocusRequest(mFocusGain);
                    }
                }
            }
        });

        List<Voice> voices = getKoreanVoices(mTTS);
        boolean voiceSet = false;
        for (Voice voice : voices) {
            if (voice.getName().equals("ko-kr-x-kod-local")) {
                mTTS.setVoice(voice);
                voiceSet = true;
                Log.d("Sounds", "Set TTS voice to: " + voice.getName());
                break;
            }
        }
        if (!voiceSet) {
            utility.log("Sounds", "Specific Korean voice 'ko-kr-x-kod-local' not found. Using default.");
        }
    }

    private List<Voice> getKoreanVoices(TextToSpeech ttsInstance) {
        Set<Voice> allVoices = ttsInstance.getVoices();
        List<Voice> koreanVoices = new ArrayList<>();
        if (allVoices != null) {
            for (Voice voice : allVoices) {
                if (voice.getLocale().getLanguage().equals(Locale.KOREA.getLanguage())) {
                    koreanVoices.add(voice);
                }
            }
        }
        return koreanVoices;
    }

    public void setNormalMode() {
        if (mAM == null) {
            initAudioManager();
        }
        if (mAM != null) {
            mAM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }

    public void setSilentMode() {
        if (mAM == null) {
            initAudioManager();
        }
        if (mAM != null) {
            mAM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    public void setVibrateMode() {
        if (mAM == null) {
            initAudioManager();
        }
        if (mAM != null) {
            mAM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        }
    }

    private void initAudioManager() {
        if (context != null) {
            mAM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        } else {
            utility.log("Sounds Err", "Context is null, cannot initialize AudioManager.");
        }
    }

    public void beep(BEEP beep) {
        if (mAM == null) {
            initAudioManager();
            if (mAM == null) {
                utility.log("Sounds", "AudioManager not initialized, cannot play beep.");
                return;
            }
        }

        if (isPhoneQuiet()) {
            return;
        }

        final MediaPlayer beepMP = new MediaPlayer();
        beepMP.setAudioAttributes(beepAttr);
        getCurrVolumes();

        setVolumeTo(10);

        try {
            if (beep.ordinal() < dataSrc.length) {
                beepMP.setDataSource(context, dataSrc[beep.ordinal()]);
            } else {
                utility.log("sounds", "Invalid BEEP ordinal: " + beep.ordinal());
                beepMP.release();
                setVolumeTo(rVol);
                return;
            }
        } catch (Exception err) {
            new Utility().log("sounds","setDataSource "+beep+" Error: "+err);
            beepMP.release();
            setVolumeTo(rVol);
            return;
        }

        beepMP.setOnPreparedListener(MediaPlayer::start);
        beepMP.setOnErrorListener((mp, what, extra) -> {
            utility.log("sounds", "MediaPlayer Error: " + what + ", " + extra);
            mp.release();
            setVolumeTo(rVol);
            return true;
        });
        beepMP.setOnCompletionListener(mp -> {
            setVolumeTo(rVol);
            mp.release();
        });

        try {
            beepMP.prepareAsync();
        } catch (Exception err) {
            beepMP.release();
            new Utility().log("sounds","prepareAsync "+beep+" Error: "+err);
            setVolumeTo(rVol);
        }
    }

    public boolean isPhoneQuiet() {
        if (mAM == null) {
            initAudioManager();
            if (mAM == null) {
                utility.log("Sounds", "AudioManager still null, cannot check phone quiet mode.");
                return false;
            }
        }
        return (mAM.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAM.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }

    public void sayTask (String say) {
        if (isPhoneQuiet()) {
            phoneVibrate.go(2);
            return;
        }

        synchronized (ttsInitLock) {
            if (!ttsReady) {
                utility.log("sounds Err", "TTS not ready, queuing sayTask request: " + say);
                pendingSayTasks.add(() -> sayTask(say));
                return;
            }
        }

        if (mTTS == null) {
            utility.log("Sounds", "mTTS is null in sayTask despite ttsReady being true. Re-initializing TTS.");
            initTextToSpeech();
            pendingSayTasks.add(() -> sayTask(say));
            return;
        }

        if (context == null) {
            utility.log("Sounds", "Context is null in sayTask, cannot proceed with TTS.");
            return;
        }
        if (mAM == null) {
            initAudioManager();
            if (mAM == null) {
                utility.log("Sounds", "AudioManager not initialized in sayTask, cannot proceed with TTS.");
                return;
            }
        }

        blueDevice = BluetoothUtil.getConnectedTargetDeviceName(context);

        if (mFocusGain != null) {
            int result = mAM.requestAudioFocus(mFocusGain);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                utility.log("Sounds", "Audio focus request failed.");
            }
        } else {
            utility.log("Sounds", "AudioFocusRequest is null, skipping audio focus request.");
        }

        getCurrVolumes();
        if (blueDevice.isEmpty()) {
            mTTS.setAudioAttributes(ringAttr);
            setVolumeTo(11);
        } else{
            mTTS.setAudioAttributes(ringAttr);
            setVolumeTo(15);
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (mTTS != null) {
                int result = mTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "uniqueUtteranceId_" + System.currentTimeMillis());
                if (result == TextToSpeech.ERROR) {
                    utility.log("Sounds", "Error speaking: " + say + " (TTS status: " + mTTS.isSpeaking() + ")");
                } else {
                    utility.log("Sounds", "TTS speaking: " + say);
                }
            } else {
                utility.log("Sounds", "mTTS is null when attempting to speak: " + say);
            }
        }, 1000);
    }

    public void setVolumeTo(int volume) {
        if (mAM != null) {
            mAM.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
        } else {
            new Utility().log("Sounds", "AudioManager is null, cannot set volume.");
        }
    }

    private void getCurrVolumes() {
        if (mAM != null) {
            rVol = mAM.getStreamVolume(AudioManager.STREAM_RING);
        } else {
            utility.log("Sounds", "AudioManager is null, cannot get current volumes.");
        }
    }

    public void shutdown() {
        synchronized (ttsInitLock) {
            if (mTTS != null) {
                Log.i("Sounds", "Shutting down TTS engine.");
                mTTS.stop();
                mTTS.shutdown();
                mTTS = null;
            }
            if (mAM != null && mFocusGain != null) {
                mAM.abandonAudioFocusRequest(mFocusGain);
            }
            ttsReady = false;
            pendingSayTasks.clear();
        }
    }
}