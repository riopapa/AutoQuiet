package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.ActivityAddEdit.BELL_EVENT;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_ONETIME;
import static com.riopapa.autoquiet.ActivityAddEdit.BELL_SEVERAL;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_OFF;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_VIBRATE;
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
    public static ShowNotification showNotification;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;

        // bundle contains saved scheduled quietTask info

        Bundle args = intent.getBundleExtra("DATA");
        qt = (QuietTask) args.getSerializable("quietTask");
        quietTasks = new QuietTaskGetPut().get(context);
        caseSFO = Objects.requireNonNull(intent.getExtras()).getString("case");
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
        if (!caseSFO.equals("T")) {  // toss quiet a min
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
            case "T":   // onetime
                Toast.makeText(mContext, "Quiet released", Toast.LENGTH_SHORT).show();
                new AdjVolumes(context, AdjVolumes.VOL.COND_ON);
                new ScheduleNextTask(mContext, "toss");
                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFO);
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

//        Intent intent = new Intent(mContext, NotificationService.class);
//        intent.putExtra("operation", STOP_SPEAK);
//        showNotification.show(mContext, intent);
    }

    private void say_Started99() {

        String subject = qt.subject;

        if      (qt.alarmType == BELL_SEVERAL) {
            bell_Several(subject);
        } else if (qt.alarmType == BELL_EVENT)
            bellEvent(subject);
        else if (qt.alarmType == BELL_ONETIME)
            bellOneTime(subject);
        else {
            String say = subject + " 를 확인 하시지요";
            myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, TTSId);
        }
    }

    private void bellOneTime(String subject) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                String say = subject + " 를 알려 드립니다";
                myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, TTSId);
                setInactive(qtIdx);
                new ScheduleNextTask(mContext, "ended1");
            }
        }, 1500);
    }

    private void bellEvent(String subject) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                String say = subject + " 를 확인 하세요.";
                myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, TTSId);
                new ScheduleNextTask(mContext, "end 2");
            }
        }, 1500);

    }

    private void bell_Several(String subject) {
        int gapSec = secRemaining(System.currentTimeMillis());
        if (gapSec < 60 && gapSec > 5 && several > 0)
            sounds.beep(mContext, (subject.contains("삐이")) ? Sounds.BEEP.TOSS:Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                int afterSec = secRemaining(System.currentTimeMillis()) - 2;
                if (several > 0 && afterSec > 5) {
                    if (afterSec > 60) {
                        afterSec = 20;
                    } else if (isSilentNow()) {
                        new VibratePhone(mContext);
                        afterSec = afterSec / 2;
                    } else {
                        String s = (qt.sayDate) ? nowDateToString(System.currentTimeMillis()) : "";
                        if (subject.contains(TOSS_BEEP)) {
                            s += subject + afterSec + " 초 전 ";
                            s += (several == 0) ? " 이예요":"";
                        } else
                            s += " " + subject + " 를 " + ((several== 0)? "꼬옥":"") + " 확인하세요, ";
                        myTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null, TTSId);
                        if (afterSec < 20)
                            afterSec = 10;
                        else
                            afterSec = afterSec / 2;
                    }
                    if (afterSec > 5) {
                        long nextTime = System.currentTimeMillis() + afterSec * 1000L;
                        new AlarmTime().request(mContext, qt, nextTime, "S", several);   // several 0 : no more
                        NextTwoTasks nxtTsk = new NextTwoTasks(quietTasks);

                        Intent intent = new Intent(mContext, NotificationService.class);
                        intent.putExtra("beg", nowTimeToString(nextTime));
                        intent.putExtra("end", "다시");
                        intent.putExtra("stop_repeat", true);
                        intent.putExtra("subject", qt.subject);
                        intent.putExtra("icon", icon);
                        intent.putExtra("iconNow", nxtTsk.icon);

                        SharedPreferences sharedPref = mContext.getSharedPreferences("saved", Context.MODE_PRIVATE);
                        intent.putExtra("begN", sharedPref.getString("begN", "없음"));
                        intent.putExtra("endN", nxtTsk.beginOrEnd);
                        intent.putExtra("subjectN", nxtTsk.subject);
                        intent.putExtra("icon", nxtTsk.icon);
                        showNotification.show(mContext, intent);
                    } else {
                        setInactive(qtIdx);
                        new ScheduleNextTask(mContext, "end3");
                    }
                } else {
                    Log.w("a Schedule ","New task");
                    setInactive(qtIdx);
                    new ScheduleNextTask(mContext, "end F");
                }
            }
        }, 600);

    }
    private void start_Normal() {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String say = addPostPosition(qt.subject) + "시작 됩니다";
                Log.w("start_Normal", say);
                myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, TTSId);
            }
        }, 800);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            if (qt.alarmType == PHONE_OFF)
                new AdjVolumes(mContext, AdjVolumes.VOL.FORCE_OFF);   // force all off
            new MannerMode().turn2Quiet(mContext, qt.alarmType == PHONE_VIBRATE);
            new ScheduleNextTask(mContext, "Normal()");
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
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new AdjVolumes(mContext, AdjVolumes.VOL.FORCE_ON);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            if (!qt.sayDate)
                finish_Normal();
            else
                finish_Several();
            }
        }, 1000);
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
}
