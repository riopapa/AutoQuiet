package better.life.autoquiet;

import static better.life.autoquiet.activity.ActivityMain.phoneVibrate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import better.life.autoquiet.TaskAction.TaskFinish;
import better.life.autoquiet.TaskAction.TaskOneTIme;
import better.life.autoquiet.TaskAction.TaskStart;
import better.life.autoquiet.common.ContextProvider;
import better.life.autoquiet.common.PhoneVibrate;
import better.life.autoquiet.common.Sounds;
import better.life.autoquiet.models.NextTask;

public class AlarmReceiver extends BroadcastReceiver {

    NextTask nt;
    public static Sounds sounds = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        ContextProvider.init(context);
        if (sounds == null)
            sounds = new Sounds(context);
        if (phoneVibrate == null)
            phoneVibrate = new PhoneVibrate();

        Bundle args = intent.getBundleExtra("DATA");
        nt = (NextTask) args.getSerializable("nextTask");

        switch (nt.SFO) {
            case "S":   // start
                new TaskStart().go(nt);
                break;
            case "F":   // finish
                new TaskFinish().go(nt);
                break;
            case "O":   // onetime
                new TaskOneTIme().go(nt);
                break;
            default:
                new Utils(context).log("Alarm Receive","Case Error " + nt.SFO
                        + " " + nt.subject);
        }
    }
}
