package better.life.autoquiet;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import better.life.autoquiet.TaskAction.TaskFinish;
import better.life.autoquiet.TaskAction.TaskOneTIme;
import better.life.autoquiet.TaskAction.TaskStart;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.Sub.PhoneVibrate;
import better.life.autoquiet.Sub.Sounds;
import better.life.autoquiet.models.NextTask;
import better.life.autoquiet.quiettask.QuietTaskGetPut;

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
            phoneVibrate = new PhoneVibrate();
        }
        Bundle args = intent.getBundleExtra("DATA");
        nt = (NextTask) args.getSerializable("nextTask");
        if (quietTasks == null)
            new QuietTaskGetPut().read();
        switch (nt.SFO) {
            case "S":   // start, from-to or 99 case
                new TaskStart().go(nt);
                break;
            case "F":   // finish
                new TaskFinish().go(nt);
                break;
            case "O":   // onetime
                new TaskOneTIme().go(nt);
                break;
            default:
                new Utility().log("Alarm Receive","Case Error " + nt.SFO
                        + " " + nt.subject);
        }
    }
}
