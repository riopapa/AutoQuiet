package better.life.autoquiet.quiettask;

import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_OFF;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;

import android.content.Context;

import better.life.autoquiet.R;
import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.models.QuietTask;

import java.util.ArrayList;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

public class QuietTaskNew {

    public QuietTaskNew() {

        Context context = ContextProvider.get();
        boolean [] week;
        quietTasks = new ArrayList<>();
        week = new boolean[]{false, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(context.getString(R.string.Quiet_Once), 1,2,3,4,
                week, false, PHONE_VIBRATE, false));
        quietTasks.get(0).sortKey = 0;
        week = new boolean[]{true, true, true, true, true, true, false};
        quietTasks.add(new QuietTask(context.getString(R.string.WeekDay_Night), 22, 30, 6, 30, week, true, PHONE_VIBRATE, true));

        week = new boolean[]{false, false, false, false, false, false, true};
        quietTasks.add(new QuietTask(context.getString(R.string.WeekEnd_Night), 23, 30, 8, 10, week, true, PHONE_VIBRATE, true));

        week = new boolean[]{true, false, false, false, false, false, false};
        quietTasks.add(new QuietTask(context.getString(R.string.Sunday_Church), 9, 20, 10, 45, week, true, PHONE_OFF, false));

        new QuietTaskGetPut().save();
    }

}