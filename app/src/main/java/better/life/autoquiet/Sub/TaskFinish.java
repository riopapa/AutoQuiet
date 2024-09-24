package better.life.autoquiet.Sub;

import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;
import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.activity.ActivityMain.mainRecycleAdapter;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.Sub.ReadyTTS.myTTS;
import static better.life.autoquiet.Sub.ReadyTTS.sounds;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;

import better.life.autoquiet.NotificationService;
import better.life.autoquiet.R;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.Utils;
import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TaskFinish {

    int several, qtIdx;
    QuietTask qt;

    public void go(QuietTask qT, int severalTimes, int idx, String caseSFOW) {
        several = severalTimes;
        qtIdx = idx;
        this.qt = qT;

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
        String s = new AddSuffixStr().add(qt.subject) + mContext.getString(R.string.finishing_completed);
        myTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null, "finishN");
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
                    long now = System.currentTimeMillis();
                    String[] joins;
                    String nowTime = (qt.sayDate)? nowDateTimeToString(now) + " "
                                    + nowDateTimeToString(now): nowTimeToString(now);
                    String ending = mContext.getString(
                            (several == 1) ? R.string.finishing_completed :
                            (several > 1) ? R.string.finishing_started
                                    : R.string.finished);
                    joins = new String[]{nowTime,  new AddSuffixStr().add(qt.subject),
                            "", ending};
                    myTTS.speak(String.join(" , ", joins),
                            TextToSpeech.QUEUE_FLUSH, null, "finishS");

                    long nextTime = System.currentTimeMillis() + ((several == 1) ? 30 : 200) * 1000;
                    new AlarmTime().request(mContext, qt, nextTime, "F", --several);
                    SharedPreferences sharedPref = mContext.getSharedPreferences("saved", Context.MODE_PRIVATE);
                    sharedPref.edit().putInt("several", several).apply();
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
                    new ShowNotification().show(mContext, intent);

                } else {
                    if (qt.agenda)
                        quietTasks.remove(qtIdx);
                    new ScheduleNextTask(mContext, "say_FinDate");
                }

            }
        }, 1200);

    }

    public static String nowDateTimeToString(long time) {
        return new SimpleDateFormat(" MM 월 d 일 EEEE HH:mm ", Locale.getDefault()).format(time);
    }
    public static String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }

}
