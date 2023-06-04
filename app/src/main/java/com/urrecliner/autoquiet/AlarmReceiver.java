package com.urrecliner.autoquiet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.urrecliner.autoquiet.Sub.Alarm99Icon;
import com.urrecliner.autoquiet.Sub.SetAlarmTime;
import com.urrecliner.autoquiet.Sub.Sounds;
import com.urrecliner.autoquiet.models.QuietTask;
import com.urrecliner.autoquiet.Sub.MannerMode;
import com.urrecliner.autoquiet.Sub.VarsGetPut;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmReceiver extends BroadcastReceiver {

    TextToSpeech myTTS;
    ArrayList<QuietTask> quietTasks;
    QuietTask qt;
    int qtIdx;
    Context context;
    int loop;
    String caseSFO;
    Vars vars;
    final int STOP_SPEAK = 1022;
    int icon;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        Bundle args = intent.getBundleExtra("DATA");
        qt = (QuietTask) args.getSerializable("quietTask");
        quietTasks = new QuietTaskGetPut().get(context);
        caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        loop = Objects.requireNonNull(intent.getExtras()).getInt("loop", 0);
        icon = new Alarm99Icon().setId(qt.begLoop, qt.endLoop);

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

        assert caseSFO != null;
        boolean beep = vars.sharedManner && (qt.endLoop == 1);

        switch (caseSFO) {
            case "S":   // beg?
                say_Started();
                break;
            case "F":   // end
                new MannerMode().turn2Normal(beep, context);
                say_Finished();
                break;
            case "O":   // onetime
                new MannerMode().turn2Normal(beep, context);
                if (qt.endLoop > 1) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            String say = "지금은 " + nowTimeToString(System.currentTimeMillis()) +
                                        " 입니다. 무음 모드가 끝났습니다";
                            myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                        }
                    }, 3000);
                }
                qt.setActive(false);
                quietTasks.set(0, qt);
                new QuietTaskGetPut().put(quietTasks);
                new SetUpComingTask(context, quietTasks, "After oneTime");
            break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFO);
        }
        new VarsGetPut().put(vars, context);
    }

    void say_Started() {

        boolean end99 = qt.endHour == 99;
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (end99)
                    say_Started99();
                else {
                    say_StartedNormal();
                }
            }
        }, 1000);   // after beep

        Intent notification = new Intent(context, NotificationService.class);
        notification.putExtra("operation", STOP_SPEAK);
        context.startForegroundService(notification);
    }

    private void say_Started99() {
        String subject = qt.subject;
        icon = new Alarm99Icon().setId(qt.begLoop, qt.endLoop);
        if      (icon == R.drawable.bell_several) {
            bell_Several(subject);
            if (loop != 0)
                return;
        }
        else if (icon == R.drawable.bell_tomorrow)
            bellTomorrow(subject);
        else if (icon == R.drawable.bell_onetime)
            bellOneTime(subject);
        else if (icon == R.drawable.bell_once_gone)
            bellOnceThenGone(subject);
        else {
            new Sounds().beep(context, Sounds.BEEP.NOTY);
            String say = subject + " 를 확인 하시지요";
            myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
        }
        new SetUpComingTask(context, quietTasks, "ended");

    }

    private void bellOnceThenGone(String subject) {
        new Sounds().beep(context, Sounds.BEEP.NOTY);
        String say = subject + " 를 확인하면 조용해 집니다";
        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
        qt.active = false;
        quietTasks.set(qtIdx, qt);
        new QuietTaskGetPut().put(quietTasks);
    }

    private void bellOneTime(String subject) {
        new Sounds().beep(context, Sounds.BEEP.NOTY);
        String say = subject + " 를 잊지 마세요";
        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
    }

    private void bell_Several(String subject) {
        if (loop > 0) {
            loop--;
            String say;
            if (isQuiet()) {
                say = subject;
                loop = 0;
            } else {
                say = subject + " 를 확인하세요, " +
                        ((loop == 0) ? "마지막 안내입니다 " : "") + subject + " 를 확인하세요";
            }
            myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);

            long nextTime = System.currentTimeMillis() + 70 * 1000;
            new SetAlarmTime().request(context, qt, nextTime, "S", loop);   // loop 0 : no more
            Intent uIntent = new Intent(context, NotificationService.class);
            uIntent.putExtra("beg", nowTimeToString(nextTime));
            uIntent.putExtra("end", "다시");
            uIntent.putExtra("end99", true);
            uIntent.putExtra("subject", qt.subject);
            uIntent.putExtra("icon", icon);
            uIntent.putExtra("isUpdate", true);
            context.startForegroundService(uIntent);
        } else {
            String say = addPostPosition(subject) + "끝났습니다";
            myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            new QuietTaskGetPut().put(quietTasks);
        }
    }

    private void bellTomorrow(String subject) {
        new Sounds().beep(context, Sounds.BEEP.NOTY);
        String say = subject + " 를 내일도 확인하세요.";
        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        int wd = c.get(Calendar.DAY_OF_WEEK); // 1 for sunday
        if (wd > 6)
            wd = 0;
        for (int i = 0; i < 7; i++)
            qt.week[i] = false;
        qt.week[wd] = true;
        quietTasks.set(qtIdx, qt);
        new QuietTaskGetPut().put(quietTasks);
    }
    private void say_StartedNormal() {
        if (qt.begLoop > 1) {
            new Sounds().beep(context, Sounds.BEEP.NOTY);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    String subject = qt.subject;
                    String say = addPostPosition(subject) + "시작됩니다";
                    myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                }
            }, 3000);
        } else if (qt.begLoop == 1){
            new Sounds().beep(context, Sounds.BEEP.INFO);
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                boolean beep = vars.sharedManner && (qt.begLoop > 1);
                new MannerMode().turn2Quiet(context, beep, qt.vibrate);
            }
        }, 6000);
        new SetUpComingTask(context, quietTasks, "say_Started()");
    }

    String addPostPosition(String s) {
        // 받침이 있으면 이, 없으면 가
        String lastNFKD = Normalizer.normalize(s.substring(s.length() - 1), Normalizer.Form.NFKD);
        return s + ((lastNFKD.length() == 2) ? "가 " : "이 ");
    }

    void say_Finished() {
        new Timer().schedule(new TimerTask() {
            public void run () {
                if (qt.endLoop > 1) {
                    new Sounds().beep(context, Sounds.BEEP.NOTY);
                    String subject = qt.subject;
                    String d = (qt.sayDate) ? "지금은 " + nowDateToString(System.currentTimeMillis()) : "";
                    String t = nowTimeToString(System.currentTimeMillis());
                    String s = d + t +  " 입니다. ";
                    s += addPostPosition(subject) + "끝났습니다";
                    myTTS.speak(s, TextToSpeech.QUEUE_ADD, null, null);
                } else if (qt.endLoop == 1) {
                    new Sounds().beep(context, Sounds.BEEP.ALARM);
                }
                if (qt.agenda) { // delete if agenda based
                    for (int i = 0; i < quietTasks.size(); i++) {
                        if (quietTasks.get(i).calId == qt.calId) {
                            quietTasks.remove(i);
                            new QuietTaskGetPut().put(quietTasks);
                            break;
                        }
                    }
                }
                new SetUpComingTask(context, quietTasks, "say_Finished()");
                new Utils(context).deleteOldLogFiles();
            }
        }, 3000);
    }

    private void readyTTS() {

        myTTS = null;
        myTTS = new TextToSpeech(context, status -> {
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

        int result = myTTS.setLanguage(Locale.getDefault());
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(context, "Not supported Language", Toast.LENGTH_SHORT).show();
        } else {
            myTTS.setPitch(1.2f);
            myTTS.setSpeechRate(1.3f);
        }
    }

    String nowDateToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat(" MM 월 d 일 EEEE ", Locale.getDefault());
        final SimpleDateFormat sdfWeek = new SimpleDateFormat(" EEEE ", Locale.US);
        String s = sdfTime.format(time) + sdfWeek.format(time);
        return s + s;
    }
    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }

    boolean isQuiet() {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }

}