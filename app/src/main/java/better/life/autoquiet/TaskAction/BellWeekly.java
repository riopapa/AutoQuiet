package better.life.autoquiet.TaskAction;

import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.common.ReadyTTS.myTTS;
import static better.life.autoquiet.common.ReadyTTS.sounds;

import android.speech.tts.TextToSpeech;

import better.life.autoquiet.R;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.Sub.NotificationHelper;
import better.life.autoquiet.common.VibratePhone;
import better.life.autoquiet.common.IsSilent;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellWeekly {

    public void go(QuietTask qt) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (qt.vibrate)
                    new VibratePhone(mContext, 1);
                String say = qt.subject + " 를 확인";
                myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "now");
                NotificationHelper notificationHelper = new NotificationHelper(mContext);
                notificationHelper.sendNotification(R.drawable.bell_weekly,
                        qt.subject, "Weekly Check");
                new ScheduleNextTask(mContext, "event");
            }
        }, 1500);

    }

}
