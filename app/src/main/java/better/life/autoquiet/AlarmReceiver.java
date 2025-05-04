package better.life.autoquiet;

import static better.life.autoquiet.activity.ActivityAddEdit.alarmIcons;
import static better.life.autoquiet.activity.ActivityMain.mContext;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import better.life.autoquiet.Sub.AdjVolumes;
import better.life.autoquiet.TaskAction.TaskFinish;
import better.life.autoquiet.TaskAction.TaskOneTIme;
import better.life.autoquiet.TaskAction.TaskStart;
import better.life.autoquiet.Sub.VarsGetPut;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.models.QuietTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;

import java.util.Objects;

public class AlarmReceiver extends BroadcastReceiver {

    NextTask nt;
    public static Sounds sounds = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;
        if (sounds == null)
            sounds = new Sounds(context);

        Bundle args = intent.getBundleExtra("DATA");
        nt = (NextTask) args.getSerializable("nextTask");

//        vars = new VarsGetPut().get(context);
//        if (!nt.SFO.equals("T")) {  // toss quiet a min
//            qtIdx = -1;
//            for (int i = 1; i < quietTasks.size(); i++) {
//                QuietTask qT1 = quietTasks.get(i);
//                if (qT1.begHour == qt.begHour && qT1.begMin == qt.begMin &&
//                        qT1.endHour == qt.endHour && qT1.endMin == qt.endMin) {
//                    qtIdx = i;
//                    break;
//                }
//            }
//            if (qtIdx == -1) {
//                String err = "quiet task index Error " + qt.subject;
//                sounds.sayTask(err);
//                Log.e("Quiet Idx Err", qt.subject);
//            }
//
//            icon = alarmIcons[qt.alarmType];
//        }

        switch (nt.SFO) {
            case "S":   // start
                new TaskStart().go(nt);
                break;
            case "F":   // finish
                new TaskFinish().go(nt);
                break;
//            case "T":   // onetime released
//                Toast.makeText(mContext, "Quiet released", Toast.LENGTH_SHORT).show();
//                new AdjVolumes(context, AdjVolumes.VOL.FORCE_ON);
//                new ScheduleNextTask(mContext, "toss");
//                break;
//            case "O":   // onetime
//                new TaskOneTIme().go(context, nt);
//                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + nt.SFO);
        }
    }

}
