package better.life.autoquiet;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;
import static better.life.autoquiet.activity.ActivityMain.sounds;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import better.life.autoquiet.TaskAction.TaskFinish;
import better.life.autoquiet.TaskAction.TaskOneTIme;
import better.life.autoquiet.TaskAction.TaskStart;
import better.life.autoquiet.common.ContextProvider;
import better.life.autoquiet.common.PhoneVibrate;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.NextTask;

public class AlarmReceiver extends BroadcastReceiver {

    NextTask nt;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (context == null) {
            Log.e("Context"," // is null on Receive() // "+this.getClass().getName()+ " onReceive");
        }
        ContextProvider.init(context);
        if (sounds == null) {
            sounds = new Sounds();
            phoneVibrate = new PhoneVibrate();
        }
        Bundle args = intent.getBundleExtra("DATA");
        nt = (NextTask) args.getSerializable("nextTask");

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
                new Utils().log("Alarm Receive","Case Error " + nt.SFO
                        + " " + nt.subject);
        }
    }
}
