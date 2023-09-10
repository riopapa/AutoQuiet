package com.riopapa.autoquiet.Sub;

import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_OFF;
import static com.riopapa.autoquiet.ActivityAddEdit.PHONE_VIBRATE;

import android.content.Context;

import com.riopapa.autoquiet.QuietTaskGetPut;
import com.riopapa.autoquiet.R;
import com.riopapa.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class ClearAllTasks {

    public ClearAllTasks(Context context) {

        boolean [] week;
        ArrayList<QuietTask> quietTasks = new ArrayList<>();
        week = new boolean[]{false, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(context.getString(R.string.Quiet_Once), 1,2,3,4,
                week, true, PHONE_VIBRATE, false));

        week = new boolean[]{true, true, true, true, true, true, false};
        quietTasks.add(new QuietTask(context.getString(R.string.WeekDay_Night), 22, 30, 6, 30, week, true, PHONE_VIBRATE, true));

        week = new boolean[]{false, false, false, false, false, false, true};
        quietTasks.add(new QuietTask(context.getString(R.string.WeekEnd_Night), 23, 30, 8, 10, week, true, PHONE_VIBRATE, true));

        week = new boolean[]{true, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(context.getString(R.string.Sunday_Church), 9, 20, 10, 45, week, true, PHONE_OFF, false));

        new QuietTaskGetPut().put(quietTasks);
    }

}