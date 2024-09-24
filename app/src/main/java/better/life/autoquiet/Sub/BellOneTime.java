package better.life.autoquiet.Sub;

import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.Sub.ReadyTTS.myTTS;
import static better.life.autoquiet.Sub.ReadyTTS.sounds;

import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import better.life.autoquiet.R;
import better.life.autoquiet.quiettask.QuietTaskGetPut;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellOneTime {

    public void go(AudioManager mAudioManager, QuietTask qt, int qtIdx) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        if (new IsSilent().now(mAudioManager)) {
            new VibratePhone(mContext, (qt.vibrate)? 1:0);
        } else {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    String say = qt.subject + " 체크";
                    myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "one");
                    qt.active = false;
                    quietTasks.set(qtIdx, qt);
                    NotificationHelper notificationHelper = new NotificationHelper(mContext);
                    notificationHelper.sendNotification(R.drawable.bell_onetime,
                            qt.subject, "OneTime Check");
                    new QuietTaskGetPut().put(quietTasks);
                }
            }, 1500);
        }
        new ScheduleNextTask(mContext, "ended1");
    }

}
