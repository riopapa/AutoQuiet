package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.AlarmReceiver.sounds;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;
import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.activity.ActivityMain.mainRecycleAdapter;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import better.life.autoquiet.R;
import better.life.autoquiet.nexttasks.ScheduleNextTask;
import better.life.autoquiet.Sub.AddSuffixStr;
import better.life.autoquiet.Sub.AdjVolumes;
import better.life.autoquiet.Sub.MannerMode;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.Utils;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.models.QuietTask;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TaskFinish {

    NextTask nt;

    public void go(NextTask nt) {
        this.nt = nt;

        new MannerMode().turn2Normal(mContext);
        new AdjVolumes(mContext, AdjVolumes.VOL.FORCE_ON);
        if (!nt.sayDate)
            sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!nt.sayDate) {
//                    if (caseSFOW.equals("W"))
//                        finish_Work();
//                    else
                        finish_Normal();
                } else
                    finish_Several();
            }
        }, 800);
    }

    private void finish_Normal() {
        sounds.beep(mContext, Sounds.BEEP.INFO);
        String s = new AddSuffixStr().add(nt.subject) + mContext.getString(R.string.finishing_completed);
        sounds.sayTask(s);
        if (nt.alarmType < PHONE_VIBRATE) {
            QuietTask qt = quietTasks.get(nt.idx);
            qt.active = false;
            quietTasks.set(nt.idx, qt);
            mainRecycleAdapter.notifyItemChanged(nt.idx);
        }
        new ScheduleNextTask(mContext, "Fin Normal");
        new Utils(mContext).deleteOldLogFiles();
    }

    private void finish_Several() {
        new Timer().schedule(new TimerTask() {
            public void run() {
//                if (several > 0) {
//                    long now = System.currentTimeMillis();
//                    String[] joins;
//                    String nowTime = (nt.sayDate)? nowDateTimeToString(now) + " "
//                                    + nowDateTimeToString(now): nowTimeToString(now);
//                    String ending = mContext.getString(
//                            (several == 1) ? R.string.finishing_completed :
//                            (several > 1) ? R.string.finishing_started
//                                    : R.string.finished);
//                    joins = new String[]{nowTime,  new AddSuffixStr().add(nt.subject),
//                            "", ending};
//                    sounds.sayTask(String.join(" , ", joins));
//
//                    long nextTime = System.currentTimeMillis() + ((several == 1) ? 30 : 200) * 1000;
//                    new AlarmTime().request(mContext, nt, nextTime, "F", --several);
//                    SharedPreferences sharedPref = mContext.getSharedPreferences("saved", Context.MODE_PRIVATE);
//                    sharedPref.edit().putInt("several", several).apply();
//                    String begN = sharedPref.getString("begN", nowTimeToString(nextTime));
//                    String endN = sharedPref.getString("endN", "시작");
//                    String subjectN = sharedPref.getString("subjectN", "Next Item");
//                    int icon = sharedPref.getInt("icon", R.drawable.next_task);
//                    int iconN = sharedPref.getInt("iconN", R.drawable.next_task);
//
//                    Intent intent = new Intent(mContext, NotificationService.class);
//                    intent.putExtra("beg", nowTimeToString(nextTime));
//                    intent.putExtra("end", "반복" + several);
//                    intent.putExtra("stop_repeat", true);
//                    intent.putExtra("subject", nt.subject);
//                    intent.putExtra("icon", icon);
//                    intent.putExtra("iconNow", icon);
//                    intent.putExtra("begN", begN);
//                    intent.putExtra("endN", endN);
//                    intent.putExtra("subjectN", subjectN);
//                    intent.putExtra("iconN", iconN);
//                    new ShowNotification().show(mContext, intent);
//
//                } else {
//                    if (nt.agenda)
//                        quietTasks.remove(ntIdx);
//                    new ScheduleNextTask(mContext, "say_FinDate");
//                }

            }
        }, 1200);
    }

    public static String nowTimeDateToString(long time) {
        return new SimpleDateFormat(" HH:mm MM 월 d 일 ", Locale.getDefault()).format(time);
    }

    public static String nowDateTimeToString(long time) {
        return new SimpleDateFormat(" MM 월 d 일 EEEE HH:mm ", Locale.getDefault()).format(time);
    }
    public static String nowTimeToString(long time) {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdfTime.format(time);
    }

}
