package better.life.autoquiet.receiver;

import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import better.life.autoquiet.QuietTaskGetPut;
import better.life.autoquiet.TaskAction.TaskRun;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.Utils;
import better.life.autoquiet.models.NextTask;

public class AlarmReceiver extends BroadcastReceiver {

    NextTask nt;

    @Override
    public void onReceive(Context context, Intent intent) {

//        if (context == null) {
//            Log.e("Context"," // is null on Receive() // "+this.getClass().getName()+ " onReceive");
//        }
        ContextProvider.init(context);
        if (sounds == null) {
            sounds =  Sounds.getInstance(context.getApplicationContext());
        }
        Bundle args = intent.getBundleExtra("DATA");
        nt = (NextTask) args.getSerializable("nextTask");
        if (quietTasks == null)
            QuietTaskGetPut.get();
        switch (nt.SFO) {
            case "S":   // start, from-to or 99 case
                TaskRun.start(nt);
                break;
            case "F":   // finish
                TaskRun.finish(nt);
                break;
            case "O":   // onetime
                TaskRun.one(nt);
                break;
            default:
                new Utils().log("Alarm Receive","Case Error " + nt.SFO
                        + " " + nt.subject);
        }
    }
}
