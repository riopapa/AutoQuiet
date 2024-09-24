package better.life.autoquiet.Sub;

import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.Sub.ReadyTTS.myTTS;
import static better.life.autoquiet.Sub.ReadyTTS.sounds;

import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import better.life.autoquiet.R;
import better.life.autoquiet.ScheduleNextTask;
import better.life.autoquiet.models.QuietTask;

import java.util.Timer;
import java.util.TimerTask;

public class BellWeekly {

    public void go(AudioManager mAudioManager, QuietTask qt) {
        sounds.beep(mContext, Sounds.BEEP.NOTY);
        new Timer().schedule(new TimerTask() {
            public void run() {
                if (new IsSilent().now(mAudioManager)) {
                    new VibratePhone(mContext, (qt.vibrate)? 1:0);
                } else {
                    String say = qt.subject + " 를 확인";
                    myTTS.speak(say, TextToSpeech.QUEUE_FLUSH, null, "now");
                    NotificationHelper notificationHelper = new NotificationHelper(mContext);
                    notificationHelper.sendNotification(R.drawable.bell_weekly,
                            qt.subject, "Weekly Check");
                }
                new ScheduleNextTask(mContext, "event");
            }
        }, 1500);

    }

}
