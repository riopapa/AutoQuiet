package better.life.autoquiet;

import static better.life.autoquiet.activity.ActivityAddEdit.alarmIcons;
import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.common.ReadyTTS.myTTS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import better.life.autoquiet.Sub.AdjVolumes;
import better.life.autoquiet.common.ReadyTTS;
import better.life.autoquiet.TaskAction.TaskFinish;
import better.life.autoquiet.TaskAction.TaskOneTIme;
import better.life.autoquiet.TaskAction.TaskStart;
import better.life.autoquiet.Sub.VarsGetPut;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;

import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    QuietTask qt;
    int qtIdx;
    public static int several;
    String caseSFOW;
    Vars vars;
    final String TTSId = "tId";
    int icon;
    ReadyTTS readyTTS = null;
    public static AudioManager mAudioManager;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        Bundle args = intent.getBundleExtra("DATA");
        qt = (QuietTask) args.getSerializable("quietTask");
        quietTasks = new QuietTaskGetPut().get(context);
        caseSFOW = Objects.requireNonNull(intent.getExtras()).getString("case");

        several = Objects.requireNonNull(intent.getExtras()).getInt("several", -1);
        if (readyTTS == null)
            readyTTS = new ReadyTTS();
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

        switch (caseSFOW) {
            case "S":   // start
                new TaskStart().go(qt, several, qtIdx);
                break;
            case "F":   // finish
            case "W":   // work
                new TaskFinish().go(qt, several, qtIdx, caseSFOW);
                break;
            case "T":   // onetime
                Toast.makeText(mContext, "Quiet released", Toast.LENGTH_SHORT).show();
                new AdjVolumes(context, AdjVolumes.VOL.FORCE_ON);
                new ScheduleNextTask(mContext, "toss");
                break;
            case "O":   // onetime
                new TaskOneTIme().go(context, qt);
                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + caseSFOW);
        }
    }

}
