package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.ActivityAddEdit.BELL_EVENT;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_ONCE_GONE;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_ONETIME;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_SEVERAL;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_VIBRATE;
import static com.riopapa.autoquiet.ActivityAddEdit.alarmIcons;
import static com.riopapa.autoquiet.ActivityMain.mainRecycleAdapter;
import static com.riopapa.autoquiet.ActivityMain.removeHandler;
import static com.riopapa.autoquiet.ActivityMain.updateHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import com.riopapa.autoquiet.Sub.AlarmTime;
import com.riopapa.autoquiet.Sub.MannerMode;
import com.riopapa.autoquiet.Sub.Sounds;
import com.riopapa.autoquiet.Sub.VarsGetPut;
import com.riopapa.autoquiet.Sub.VibratePhone;
import com.riopapa.autoquiet.models.NextTwoTasks;
import com.riopapa.autoquiet.models.QuietTask;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmReceiver extends BroadcastReceiver {

    TextToSpeech myTTS;
    ArrayList<QuietTask> quietTasks;
    QuietTask qt;
    int qtIdx;
    Context rContext;
    int several;
    String caseSFO;
    Vars vars;
    final int STOP_SPEAK = 1022;
    int icon;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.rContext = context;

        Bundle args = intent.getBundleExtra("DATA");
        qt = (QuietTask) args.getSerializable("quietTask");
        quietTasks = new QuietTaskGetPut().get(context);
        caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        several = Objects.requireNonNull(intent.getExtras()).getInt("several", -1);

        vars = new VarsGetPut().get(context);
        readyTTS();
        qtIdx = -1;
        for (int i = 1; i < quietTasks.size(); i++) {
            QuietTask qT1 = quietTasks.get(i);
            if (qT1.begHour == qt.begHour && qT1.begMin == qt.begMin &&
                    qT1.endHour == qt.endHour && qT1.endMin == qt.endMin) {
                qtIdx = i;
                break;
            }
        }
        if (qtIdx == -1) {
            String err = "quiet task index Error "+ qt.subject;
            myTTS.speak(err, TextToSpeech.QUEUE_ADD, null, TTSId);
            Log.w("Quiet Idx Err", qt.subject);
        }
//        if (qt.alarmType == 0)
//            qt.alarmType = new AlarmType().getType(qt.endHour == 99, qt.vibrate, qt.begLoop, qt.endLoop);
        icon = alarmIcons[qt.alarmType];

        assert caseSFO != null;

        switch (caseSFO) {
            case "S":   // beg?
                start_Task();
                break;
            case "F":   // end
                finish_Task();
                break;
            case "O":   // onetime
                only_OneTime(context);
                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFO);
        }

        waitLoop(); // not to be killed
    }

    private void only_OneTime(Context context) {
        new MannerMode().turn2Normal(context);
        if (vars.sharedManner) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    String say = "지금은 " + nowTimeToString(System.currentTimeMillis()) +
                                " 입니다. 무음 모드가 끝났습니다";
                    myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                }
            }, 2000);
        } else {
            vibrate();
        }
        setInactive(0);
        new SetUpComingTask(context, quietTasks, "After oneTime");
    }

    private void setInactive(int index) {
        qt.active = false;
        quietTasks.set(index, qt);
        new QuietTaskGetPut().put(quietTasks);
        Message msg = new Message();
        msg.obj = ""+index;
        updateHandler.sendMessage(msg);
    }

    private void removeAgenda() {
        quietTasks.remove(qtIdx);
        if (mainRecycleAdapter == null)
            mainRecycleAdapter = new MainRecycleAdapter();
        Message msg = new Message();
        msg.obj = ""+qtIdx;
        removeHandler.sendMessage(msg);
    }

    void start_Task() {

        new Timer().schedule(new TimerTask() {
            public void run() {
                if (qt.alarmType < PHONE_VIBRATE)
                    say_Started99();
                else {
                    start_Normal();
                }
            }
        }, 1000);   // after beep

        Intent notification = new Intent(rContext, NotificationService.class);
        notification.putExtra("operation", STOP_SPEAK);
        rContext.startForegroundService(notification);
    }

    private void say_Started99() {

        String subject = qt.subject;
        new Sounds().beep(rContext, (subject.equals("삐이")) ? Sounds.BEEP.TOSS:Sounds.BEEP.NOTY);

        new Timer().schedule(new TimerTask() {
            public void run() {
            if      (qt.alarmType == BELL_SEVERAL) {
                bell_Several(subject);
                if (several != 0)
                    return;
            } else if (qt.alarmType == BELL_EVENT)
                bellEvent(subject);
            else if (qt.alarmType == BELL_ONETIME)
                bellOneTime(subject);
            else if (qt.alarmType == BELL_ONCE_GONE)
                bellOnceThenGone(subject);
            else {
                new Sounds().beep(rContext, Sounds.BEEP.NOTY);
                String say = subject + " 를 확인 하시지요";
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            }
            new SetUpComingTask(rContext, quietTasks, "ended");
            }
        }, 1500);

    }

    private void bellOnceThenGone(String subject) {
        String say = "잠시만요! " + subject + " 를 잊지 마세요! "+ subject +" 시간 입니다";
        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
        setInactive(qtIdx);
    }

    private void bellOneTime(String subject) {
        String say = subject + " 를 잊지 마세요";
        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
        setInactive(qtIdx);
    }

    private void bellEvent(String subject) {
        new Sounds().beep(rContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = subject + " 를 확인 하세요.";
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                setInactive(qtIdx);
            }
        }, 1500);
    }

    private void bell_Several(String subject) {
        if (several > 0) {
            several--;
            if (isSilentNow()) {
                new VibratePhone(rContext);
            } else {
                new Sounds().beep(rContext, (subject.equals("삐이")) ? Sounds.BEEP.TOSS:Sounds.BEEP.BBEEPP);
                String say = subject + " 를 확인하세요, " +
                        ((several == 0) ? "마지막 안내입니다 " : "") + subject + " 를 확인하세요";
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                if (several == 0)
                    setInactive(qtIdx);
            }
            NextTwoTasks n2 = new NextTwoTasks(quietTasks);

            long nextTime = System.currentTimeMillis() + ((several == 1) ? 20 : 90) * 1000;
            new AlarmTime().request(rContext, qt, nextTime, "S", several);   // several 0 : no more
            Intent uIntent = new Intent(rContext, NotificationService.class);

            uIntent.putExtra("beg", nowTimeToString(nextTime));
            uIntent.putExtra("end", "다시");
            uIntent.putExtra("stop_repeat", true);
            uIntent.putExtra("subject", qt.subject);
            uIntent.putExtra("icon", icon);
            uIntent.putExtra("iconNow", n2.icon);

            SharedPreferences sharedPref = rContext.getSharedPreferences("saved", Context.MODE_PRIVATE);
            uIntent.putExtra("begN", sharedPref.getString("begN", "없음"));
            uIntent.putExtra("endN", n2.soonOrUntil);
            uIntent.putExtra("subjectN", n2.subject);
            uIntent.putExtra("icon", n2.iconN);
            rContext.startForegroundService(uIntent);

        } else {
            String say = addPostPosition(subject) + "끝났습니다";
            myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            setInactive(qtIdx);
        }
    }
    private void start_Normal() {
        new Sounds().beep(rContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String subject = qt.subject;
                String say = addPostPosition(subject) + "시작 됩니다";
                Log.w("Task "+System.currentTimeMillis(), say);
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            }
        }, 1200);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            new MannerMode().turn2Quiet(rContext, qt.alarmType == PHONE_VIBRATE);
            Log.w("Task "+System.currentTimeMillis(), "SetUpComingTask");
            new SetUpComingTask(rContext, quietTasks, "say_Started()");
            }
        }, 20000);
    }

    String addPostPosition(String s) {
        // 받침이 있으면 이, 없으면 가
        String lastNFKD = Normalizer.normalize(s.substring(s.length() - 1), Normalizer.Form.NFKD);
        return s + ((lastNFKD.length() == 2) ? "가 " : "이 ");
    }

    void finish_Task() {
        new MannerMode().turn2Normal(rContext);
        if (!qt.sayDate)
            finish_Normal();
        else
            finish_Several();
    }

    private void finish_Several() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                if (several > 0) {
                    finish_Dated();
                    long nextTime = System.currentTimeMillis() + ((several == 1) ? 10 : 180) * 1000;
                    new AlarmTime().request(rContext, qt, nextTime, "F", --several);
                    SharedPreferences sharedPref = rContext.getSharedPreferences("saved", Context.MODE_PRIVATE);
                    String begN = sharedPref.getString("begN", nowTimeToString(nextTime));
                    String endN = sharedPref.getString("endN", "시작");
                    String subjectN = sharedPref.getString("subjectN", "Next Item");
                    int icon = sharedPref.getInt("icon", R.drawable.next_task);
                    int iconN = sharedPref.getInt("iconN", R.drawable.next_task);

                    Intent uIntent = new Intent(rContext, NotificationService.class);
                    uIntent.putExtra("beg", nowTimeToString(nextTime));
                    uIntent.putExtra("end", "반복" + several);
                    uIntent.putExtra("stop_repeat", true);
                    uIntent.putExtra("subject", qt.subject);
                    uIntent.putExtra("icon", icon);
                    uIntent.putExtra("iconNow", icon);
                    uIntent.putExtra("begN", begN);
                    uIntent.putExtra("endN", endN);
                    uIntent.putExtra("subjectN", subjectN);
                    uIntent.putExtra("iconN", iconN);
                    rContext.startForegroundService(uIntent);
                } else {
                    if (qt.agenda)
                        removeAgenda();
                    new SetUpComingTask(rContext, quietTasks, "say_FinDate");
                }
            }
        }, 3000);
    }

    private void finish_Normal() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                if (qt.alarmType < PHONE_VIBRATE) {
                    finish_Dated();
                } else if (qt.alarmType == PHONE_VIBRATE) {
                    new Sounds().beep(rContext, Sounds.BEEP.ALARM);
                }
                if (qt.agenda) { // delete if agenda based
                    removeAgenda();
                }
                if (qt.alarmType < PHONE_VIBRATE) {
                    qt.active = false;
                    quietTasks.set(qtIdx, qt);
                    mainRecycleAdapter.notifyItemChanged(qtIdx);
                }
                new SetUpComingTask(rContext, quietTasks, "say_FinishNormal()");
                new Utils(rContext).deleteOldLogFiles();
            }
        }, 3000);
    }


    private void finish_Dated() {
        new Sounds().beep(rContext, Sounds.BEEP.NOTY);
        String d = (qt.sayDate) ? "지금은 " + nowDateToString(System.currentTimeMillis()) : "";
        String t = nowTimeToString(System.currentTimeMillis());
        String s =  ((several == 1) ? "마지막 안내입니다 " : "") + d + t +  " 입니다. ";
        s += addPostPosition(qt.subject) + "끝났습니다";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, "0.1");
        myTTS.speak(s, TextToSpeech.QUEUE_ADD, params);
    }

    public void vibrate() {
        final long[] vibPattern = {0, 20, 200, 300, 300, 400, 400, 500};
        VibratorManager vibManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            vibManager = (VibratorManager) rContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        }
        Vibrator vibrator = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            vibrator = vibManager.getDefaultVibrator();
        }
        VibrationEffect vibEffect = VibrationEffect.createWaveform(vibPattern, -1);
        vibrator.vibrate(vibEffect);
    }
    private void readyTTS() {

        myTTS = null;
        myTTS = new TextToSpeech(rContext, status -> {
            if (status == TextToSpeech.SUCCESS) {
                initializeTTS();
            }
        });
    }

    String TTSId = "";

    private void initializeTTS() {

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

    String nowDateToString(long time) {
        String s =  new SimpleDateFormat(" MM 월 d 일 EEEE ", Locale.getDefault()).format(time);
        return s + s;
    }
    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }

    boolean isSilentNow() {
        AudioManager mAudioManager = (AudioManager) rContext.getSystemService(Context.AUDIO_SERVICE);
        return (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }
    Timer timer = new Timer();
    TimerTask timerTask = null;
    int count = 0;
    void waitLoop() {

        final long LOOP_INTERVAL = 25 * 60 * 1000;

        if (timerTask != null)
            timerTask.cancel();
        if (timer != null)
            timer.cancel();

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run () {
                count++;
            }
        };
        timer.schedule(timerTask, LOOP_INTERVAL, LOOP_INTERVAL);
    }

    private void setDisable () {

    }
}