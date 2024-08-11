package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.ActivityAddEdit.BELL_WEEKLY;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_ONETIME;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_SEVERAL;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_OFF;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_VIBRATE;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_WORK;
import static com.riopapa.autoquiet.ActivityAddEdit.alarmIcons;
import static com.riopapa.autoquiet.ActivityMain.mContext;
import static com.riopapa.autoquiet.ActivityMain.mainRecycleAdapter;
import static com.riopapa.autoquiet.ActivityMain.quietTasks;
import static com.riopapa.autoquiet.Sub.ReadyTTS.myTTS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.riopapa.autoquiet.Sub.AlarmTime;
import com.riopapa.autoquiet.Sub.AdjVolumes;
import com.riopapa.autoquiet.Sub.BellOneTime;
import com.riopapa.autoquiet.Sub.BellSeveral;
import com.riopapa.autoquiet.Sub.BellWeekly;
import com.riopapa.autoquiet.Sub.MannerMode;
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
    String caseSFOW;
    Vars vars;
    final String TTSId = "tId";
    int icon;
    ReadyTTS readyTTS = null;
    Sounds sounds = null;
    public static ShowNotification showNotification;
    AudioManager mAudioManager;
    int mVol;
    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        // bundle contains saved scheduled quietTask info

        Bundle args = intent.getBundleExtra("DATA");
        qt = (QuietTask) args.getSerializable("quietTask");
        quietTasks = new QuietTaskGetPut().get(context);
        caseSFOW = Objects.requireNonNull(intent.getExtras()).getString("case");
        several = Objects.requireNonNull(intent.getExtras()).getInt("several", -1);
//        Log.w("on Receive", "case="+caseSFO+" several="+several+" qt="+qt.subject+" "+
//                qt.begHour+":"+qt.begMin);
        if (readyTTS == null)
            readyTTS = new ReadyTTS();
        if (sounds == null)
            sounds = new Sounds();
        if (showNotification == null)
            showNotification = new ShowNotification();
        vars = new VarsGetPut().get(context);
        if (!caseSFOW.equals("T")) {  // toss quiet a min
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
                String err = "quiet task index Error " + qt.subject;
                myTTS.speak(err, TextToSpeech.QUEUE_FLUSH, null, TTSId);
                Log.w("Quiet Idx Err", qt.subject);
            }

            icon = alarmIcons[qt.alarmType];
        }

        assert caseSFOW != null;

        switch (caseSFOW) {
            case "S":   // start
                start_Task();
                break;
            case "F":   // finish
            case "W":   // work
                finish_Task();
                break;
            case "T":   // onetime
                Toast.makeText(mContext, "Quiet released", Toast.LENGTH_SHORT).show();
                new AdjVolumes(context, AdjVolumes.VOL.COND_ON);
                new ScheduleNextTask(mContext, "toss");
                break;
            case "O":   // onetime
                only_OneTime(context);
                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFOW);
        }
    }

    private void only_OneTime(Context context) {
        new MannerMode().turn2Normal(context);
//        if (vars.sharedManner) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    String say = "지금은 " + nowTimeToString(System.currentTimeMillis()) +
                                " 입니다. 무음 모드가 끝났습니다";
                    myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, TTSId);
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
    }

    void start_Task() {

        if (qt.alarmType < PHONE_VIBRATE)
            say_Started99();
        else {
            start_Normal();
        }
    }

    private void say_Started99() {

        String subject = qt.subject;

        if      (qt.alarmType == BELL_SEVERAL) {
            new BellSeveral().go(mAudioManager, qt, several, qtIdx);

        } else if (qt.alarmType == BELL_WEEKLY)
            new BellWeekly().go(mAudioManager, qt);

        else if (qt.alarmType == BELL_ONETIME)
            new BellOneTime().go(mAudioManager, qt, qtIdx);

        else {
            String say = subject + "AlarmType 에러 확인 "+qt.alarmType;
            myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, TTSId);
            new ScheduleNextTask(mContext, "ended Err");
        }
    }

    private void start_Normal() {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = (qt.alarmType == PHONE_WORK) ? qt.subject : addPostPosition(qt.subject) + "시작 됩니다";
                myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, TTSId);
            }
        }, 800);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if ((qt.alarmType == PHONE_WORK))
                    new AdjVolumes(mContext, AdjVolumes.VOL.WORK);
                else {
                    new AdjVolumes(mContext, AdjVolumes.VOL.FORCE_OFF);
                    new MannerMode().turn2Quiet(mContext, qt.alarmType != PHONE_OFF);
                }
                new ScheduleNextTask(mContext, "Norm");
            }
        }, 5000);
    }

    String addPostPosition(String s) {
        // 받침이 있으면 이, 없으면 가
        String lastNFKD = Normalizer.normalize(s.substring(s.length() - 1), Normalizer.Form.NFKD);
        return s + ((lastNFKD.length() == 2) ? "가 " : "이 ");
    }

    void finish_Task() {
        new MannerMode().turn2Normal(mContext);
        new AdjVolumes(mContext, AdjVolumes.VOL.FORCE_ON);
        if (!qt.sayDate)
            sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            if (!qt.sayDate) {
                if (caseSFOW.equals("W"))
                    finish_Work();
                else
                    finish_Normal();
            } else
                finish_Several();
            }
        }, 500);
    }

    private void finish_Work() {
        sounds.beep(mContext, Sounds.BEEP.INFO);
        new ScheduleNextTask(mContext, "Fin Work");
    }

    private void finish_Normal() {
        sounds.beep(mContext, Sounds.BEEP.INFO);
        String s = addPostPosition(qt.subject) + "끝났습니다";
        myTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null, TTSId);
        if (qt.agenda) { // delete if agenda based
            quietTasks.remove(qtIdx);
        } else if (qt.alarmType < PHONE_VIBRATE) {
            qt.active = false;
            quietTasks.set(qtIdx, qt);
            mainRecycleAdapter.notifyItemChanged(qtIdx);
        }
        new ScheduleNextTask(mContext, "Fin Normal");
        new Utils(mContext).deleteOldLogFiles();
    }

    private void finish_Several() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (several > 0) {
                    String s= "";
                    long now = System.currentTimeMillis();
                    s +=  (qt.sayDate)? nowDateTimeToString(now) : nowTimeToString(now);
                    s += " " + addPostPosition(qt.subject) + ((several == 1) ? " 끝났어요 " : " 끄으읏");
                    myTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null, TTSId);

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
                    showNotification.show(mContext, intent);

                } else {
                    if (qt.agenda)
                        quietTasks.remove(qtIdx);
                    new ScheduleNextTask(mContext, "say_FinDate");
                }

            }
        }, 1200);

    }

    String nowDateTimeToString(long time) {
        String s =  new SimpleDateFormat(" MM 월 d 일 EEEE HH:mm ", Locale.getDefault()).format(time);
        return s + s;
    }
    String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }
}
