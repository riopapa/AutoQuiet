package com.urrecliner.autoquiet.Sub;

import android.content.Context;

import com.urrecliner.autoquiet.QuietTaskGetPut;
import com.urrecliner.autoquiet.R;
import com.urrecliner.autoquiet.models.QuietTask;

import java.util.ArrayList;

public class ClearAllTasks {

    public ClearAllTasks(Context context) {

        boolean [] week;
        ArrayList<QuietTask> quietTasks = new ArrayList<>();
        week = new boolean[]{false, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(context.getString(R.string.Quiet_Once), 1,2,3,4,
                week, false, true, 0, 11, false));

        week = new boolean[]{true, true, true, true, true, true, false};
        quietTasks.add(new QuietTask(context.getString(R.string.WeekDay_Night), 22, 30, 6, 30, week, true, false, 11, 11, true));

        week = new boolean[]{false, false, false, false, false, false, true};
        quietTasks.add(new QuietTask(context.getString(R.string.WeekEnd_Night), 23, 30, 8, 10, week, true, false, 11, 11, true));

        week = new boolean[]{true, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(context.getString(R.string.Sunday_Church), 9, 20, 10, 45, week, true, true, 0, 11, false));

        new QuietTaskGetPut().put(quietTasks);
    }

}