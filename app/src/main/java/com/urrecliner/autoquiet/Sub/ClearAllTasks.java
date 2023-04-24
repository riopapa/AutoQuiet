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
                week, false, true, 0, 3, false));

        week = new boolean[]{false, true, true, true, true, true, false};
        quietTasks.add(new QuietTask(context.getString(R.string.WeekDay_Night), 22, 30, 6, 30, week, true, false, 1, 3, true));

        week = new boolean[]{true, false, false, false, false, false, true};
        quietTasks.add(new QuietTask(context.getString(R.string.WeekEnd_Night), 23, 30, 9, 30, week, true, false, 1, 3, true));

        week = new boolean[]{true, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(context.getString(R.string.Sunday_Church), 9, 30, 16, 30, week, true, true, 0, 3, false));

        new QuietTaskGetPut().put(quietTasks);
    }

}