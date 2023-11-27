package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.ActivityAddEdit.BELL_EVENT;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_ONETIME;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_SEVERAL;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_VIBRATE;
import static com.riopapa.autoquiet.ActivityAddEdit.alarmIcons;
import static com.riopapa.autoquiet.ActivityMain.mContext;
import static com.riopapa.autoquiet.ActivityMain.mainRecycleAdapter;
import static com.riopapa.autoquiet.ActivityMain.quietTasks;
import static com.riopapa.autoquiet.ActivityMain.removeRecycler;
import static com.riopapa.autoquiet.ActivityMain.updateRecycler;
import static com.riopapa.autoquiet.Sub.ReadyTTS.myTTS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.riopapa.autoquiet.Sub.AlarmTime;
import com.riopapa.autoquiet.Sub.MannerMode;
import com.riopapa.autoquiet.Sub.NextTwoTasks;
import com.riopapa.autoquiet.Sub.ReadyTTS;
import com.riopapa.autoquiet.Sub.ShowNotification;
import com.riopapa.autoquiet.Sub.Sounds;
import com.riopapa.autoquiet.Sub.VarsGetPut;
import com.riopapa.autoquiet.Sub.VibratePhone;
import com.riopapa.autoquiet.models.QuietTask;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmReceiver extends BroadcastReceiver {

    QuietTask qt;
    int qtIdx;
    int several;
    String caseSFO;
    Vars vars;
    final int STOP_SPEAK = 1022;
    final String TOSS_BEEP = "삐이";
    final String TTSId = "tId";
    int icon;
    ReadyTTS readyTTS = null;
    Sounds sounds = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        // bundle contains saved scheduled quietTask info

        Bundle args = intent.getBundleExtra("DATA");
        qt = (QuietTask) args.getSerializable("quietTask");
        quietTasks = new QuietTaskGetPut().get(context);
        caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
        several = Objects.requireNonNull(intent.getExtras()).getInt("several", -1);
        if (readyTTS == null)
            readyTTS = new ReadyTTS();
        if (sounds == null)
            sounds = new Sounds();
        vars = new VarsGetPut().get(context);

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
//        if (vars.sharedManner) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    String say = "지금은 " + nowTimeToString(System.currentTimeMillis()) +
                                " 입니다. 무음 모드가 끝났습니다";
                    myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
                }
            }, 2000);
//        } else {
//            vibrate();
//        }
        setInactive(0);
        new ScheduleNextTask(context, "After oneTime");
    }

    // after speak make it disabled
    private void setInactive(int index) {
        qt.active = false;
        quietTasks.set(index, qt);
        new QuietTaskGetPut().put(quietTasks);
        Message msg = new Message();
        msg.obj = ""+index;
        updateRecycler.sendMessage(msg);
    }

    private void removeAgenda() {
        quietTasks.remove(qtIdx);
        if (mainRecycleAdapter != null) {
            Message msg = new Message();
            msg.obj = "" + qtIdx;
            removeRecycler.sendMessage(msg);
        }
    }

    void start_Task() {

        if (qt.alarmType < PHONE_VIBRATE)
            say_Started99();
        else {
            start_Normal();
        }

        Intent intent = new Intent(mContext, NotificationService.class);
        intent.putExtra("operation", STOP_SPEAK);
        new ShowNotification(mContext, intent);
    }

    private void say_Started99() {

        String subject = qt.subject;
        sounds.beep(mContext, (subject.contains("삐이")) ? Sounds.BEEP.TOSS:Sounds.BEEP.NOTY);

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
            else {
                String say = subject + " 를 확인 하시지요";
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            }
            new ScheduleNextTask(mContext, "ended");
            }
        }, 1500);

    }

    private void bellOneTime(String subject) {
        String say = subject + " 를 잊지 마세요";
        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
        setInactive(qtIdx);
    }

    private void bellEvent(String subject) {
        String say = subject + " 를 확인 하세요.";
        myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
    }

    private void bell_Several(String subject) {

        if (several > 0) {
            several--;
            int remain = secRemaining(System.currentTimeMillis()) - 3;
            Log.w("bell_Several "+several, "remain = "+remain);
            if (remain > 60) {
                several++;
//                new VibratePhone(mContext);
                remain = 20;
            } else if (isSilentNow()) {
                new VibratePhone(mContext);
            } else {
                String s = (qt.sayDate) ? nowDateToString(System.currentTimeMillis()) : "";
                if (subject.contains(TOSS_BEEP)) {
                    s += subject + ((remain > 0) ? (" 시작 " + remain + " 초 전 ") : " 진행 중 ");
                    s += (several == 0) ? " 이예요":"";
                } else
                    s += " " + subject + " 를 " + ((several== 0)? "꼬옥":"") + " 확인하세요, ";
                myTTS.speak(s, TextToSpeech.QUEUE_ADD, null, TTSId);
                remain = remain / 3;
            }

            long nextTime = System.currentTimeMillis() + remain * 1000L;
            new AlarmTime().request(mContext, qt, nextTime, "S", several);   // several 0 : no more
            NextTwoTasks nxtTsk = new NextTwoTasks(quietTasks);

            Intent uIntent = new Intent(mContext, NotificationService.class);
            uIntent.putExtra("beg", nowTimeToString(nextTime));
            uIntent.putExtra("end", "다시");
            uIntent.putExtra("stop_repeat", true);
            uIntent.putExtra("subject", qt.subject);
            uIntent.putExtra("icon", icon);
            uIntent.putExtra("iconNow", nxtTsk.icon);

            SharedPreferences sharedPref = mContext.getSharedPreferences("saved", Context.MODE_PRIVATE);
            uIntent.putExtra("begN", sharedPref.getString("begN", "없음"));
            uIntent.putExtra("endN", nxtTsk.beginOrEnd);
            uIntent.putExtra("subjectN", nxtTsk.subject);
            uIntent.putExtra("icon", nxtTsk.icon);
            new ShowNotification(mContext, uIntent);
            if (several == 0)
                setInactive(qtIdx);
        } else {
            String say = addPostPosition(subject) + "끝났습니다";
            myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            setInactive(qtIdx);
        }
    }
    private void start_Normal() {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = addPostPosition(qt.subject) + "시작 됩니다";
                Log.w("start_Normal", say);
                myTTS.speak(say, TextToSpeech.QUEUE_ADD, null, TTSId);
            }
        }, 2000);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            new MannerMode().turn2Quiet(mContext, qt.alarmType == PHONE_VIBRATE);
            new ScheduleNextTask(mContext, "Normal()");
            }
        }, 15000);
    }

    String addPostPosition(String s) {
        // 받침이 있으면 이, 없으면 가
        String lastNFKD = Normalizer.normalize(s.substring(s.length() - 1), Normalizer.Form.NFKD);
        return s + ((lastNFKD.length() == 2) ? "가 " : "이 ");
    }

    void finish_Task() {
        new MannerMode().turn2Normal(mContext);
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        if (!qt.sayDate)
            finish_Normal();
        else
            finish_Several();
    }

    private void finish_Normal() {
        sounds.beep(mContext, Sounds.BEEP.INFO);
        String s = addPostPosition(qt.subject) + "끝났습니다";
        myTTS.speak(s, TextToSpeech.QUEUE_ADD, null, TTSId);
        if (qt.agenda) { // delete if agenda based
            removeAgenda();
        } else if (qt.alarmType < PHONE_VIBRATE) {
            qt.active = false;
            quietTasks.set(qtIdx, qt);
            mainRecycleAdapter.notifyItemChanged(qtIdx);
        }
        new ScheduleNextTask(mContext, "say_FinishNormal()");
        new Utils(mContext).deleteOldLogFiles();
    }


    private void finish_Several() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (several > 0) {
                    String s= "";
                    long now = System.currentTimeMillis();
                    s +=  (qt.sayDate)? nowDateTimeToString(now) : nowTimeToString(now);
                    s += " " + addPostPosition(qt.subject) + ((several == 1) ? " 이제 끝났어요 " : " 끄으읏");
                    myTTS.speak(s, TextToSpeech.QUEUE_ADD, null, TTSId);

                    long nextTime = System.currentTimeMillis() + ((several == 1) ? 50 : 300) * 1000;
                    new AlarmTime().request(mContext, qt, nextTime, "F", --several);
                    SharedPreferences sharedPref = mContext.getSharedPreferences("saved", Context.MODE_PRIVATE);
                    String begN = sharedPref.getString("begN", nowTimeToString(nextTime));
                    String endN = sharedPref.getString("endN", "시작");
                    String subjectN = sharedPref.getString("subjectN", "Next Item");
                    int icon = sharedPref.getInt("icon", R.drawable.next_task);
                    int iconN = sharedPref.getInt("iconN", R.drawable.next_task);

                    Intent intent = new Intent(mContext, NotificationService.class);
                    intent.putExtra("beg", nowTimeToString(nextTime));
                    intent.putExtra("end", "반복" + several);
                    intent.putExtra("stop_repeat", true);
                    intent.putExtra("subject", qt.subject);
                    intent.putExtra("icon", icon);
                    intent.putExtra("iconNow", icon);
                    intent.putExtra("begN", begN);
                    intent.putExtra("endN", endN);
                    intent.putExtra("subjectN", subjectN);
                    intent.putExtra("iconN", iconN);
                    new ShowNotification(mContext, intent);

                } else {
                    if (qt.agenda)
                        removeAgenda();
                    new ScheduleNextTask(mContext, "say_FinDate");
                }

            }
        }, 1200);

    }


    String nowDateToString(long time) {
        String s =  new SimpleDateFormat(" MM 월 d 일 EEEE ", Locale.getDefault()).format(time);
        return s + s;
    }
    String nowDateTimeToString(long time) {
        String s =  new SimpleDateFormat(" MM 월 d 일 EEEE HH:mm ", Locale.getDefault()).format(time);
        return s + s;
    }
    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }
    int secRemaining(long time) {
        Calendar toDay = Calendar.getInstance();
        toDay.set(Calendar.HOUR_OF_DAY, qt.begHour);
        toDay.set(Calendar.MINUTE, qt.begMin);
        toDay.set(Calendar.SECOND, 0);
        return (int) ((toDay.getTimeInMillis() - time)/1000);
    }

    boolean isSilentNow() {
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT ||
                mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE);
    }
    Timer timer = new Timer();
    TimerTask timerTask = null;
    int count = 0;
    void waitLoop() {

        final long LOOP_INTERVAL = 20 * 60 * 1000;

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

}