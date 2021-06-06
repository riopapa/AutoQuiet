package com.urrecliner.autoquiet.utility;

import com.urrecliner.autoquiet.R;
import com.urrecliner.autoquiet.models.QuietTask;

import static com.urrecliner.autoquiet.Vars.mContext;
import static com.urrecliner.autoquiet.Vars.quietTasks;
import static com.urrecliner.autoquiet.Vars.utils;

public class ClearQuiteTasks {

    public ClearQuiteTasks() {
        boolean [] week;
        quietTasks.clear();
        week = new boolean[]{false, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(mContext.getString(R.string.Quiet_Once), 1,2,3,4,
                week, false, true, 0, 0));

        week = new boolean[]{false, true, true, true, true, true, false};
        quietTasks.add(new QuietTask(mContext.getString(R.string.WeekDay_Night), 22, 30, 6, 30, week, true, false, 1, 1));

        week = new boolean[]{true, false, false, false, false, false, true};
        quietTasks.add(new QuietTask(mContext.getString(R.string.WeekEnd_Night), 23, 30, 9, 30, week, true, false, 1, 1));

        week = new boolean[]{true, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(mContext.getString(R.string.Sunday_Church), 9, 30, 16, 30, week, true, true, 0, 0));

        utils.saveQuietTasksToShared();
    }

}
