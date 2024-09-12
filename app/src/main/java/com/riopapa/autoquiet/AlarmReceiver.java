package com.riopapa.autoquiet;

import static com.riopapa.autoquiet.activity.ActivityAddEdit.alarmIcons;
import static com.riopapa.autoquiet.activity.ActivityMain.mContext;
import static com.riopapa.autoquiet.activity.ActivityMain.quietTasks;
import static com.riopapa.autoquiet.Sub.ReadyTTS.myTTS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import com.riopapa.autoquiet.Sub.AdjVolumes;
import com.riopapa.autoquiet.Sub.MannerMode;
import com.riopapa.autoquiet.Sub.ReadyTTS;
import com.riopapa.autoquiet.Sub.TaskFinish;
import com.riopapa.autoquiet.Sub.TaskOneTIme;
import com.riopapa.autoquiet.Sub.TaskStart;
import com.riopapa.autoquiet.Sub.VarsGetPut;
import com.riopapa.autoquiet.models.QuietTask;
import com.riopapa.autoquiet.quiettask.QuietTaskGetPut;

import java.text.SimpleDateFormat;
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
    AudioManager mAudioManager;

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

        assert caseSFOW != null;

        switch (caseSFOW) {
            case "S":   // start
                new TaskStart().go(mAudioManager, qt, several, qtIdx);
                break;
            case "F":   // finish
            case "W":   // work
                new TaskFinish().go(qt, several, qtIdx, caseSFOW);
                break;
            case "T":   // onetime
                Toast.makeText(mContext, "Quiet released", Toast.LENGTH_SHORT).show();
                new AdjVolumes(context, AdjVolumes.VOL.COND_ON);
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
