package better.life.autoquiet;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_OFF;
import static better.life.autoquiet.activity.ActivityAddEdit.PHONE_VIBRATE;
import static better.life.autoquiet.activity.ActivityMain.quietTasks;

import better.life.autoquiet.Sub.ContextProvider;
import better.life.autoquiet.models.QuietTask;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public final class QuietTaskGetPut {
    public static void put() {
        Context context = ContextProvider.get();
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(quietTasks);
        prefsEditor.putString("silentInfo", json);
        prefsEditor.apply();
    }

    public static void get() {

        Context context = ContextProvider.get();
        ArrayList<QuietTask> list;
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("silentInfo", "");
        if (json.isEmpty()) {
            QuietTaskGetPut.init();
        } else {
            Type type = new TypeToken<List<QuietTask>>() {
            }.getType();
            list = gson.fromJson(json, type);
            quietTasks = list;
        }
    }

    public static QuietTask getDefault() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.MINUTE, 10);
        int hStart = calendar.get(Calendar.HOUR_OF_DAY);
        int mStart = calendar.get(Calendar.MINUTE);
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        int hFinish = calendar.get(Calendar.HOUR_OF_DAY);
        boolean [] week = new boolean[7];
        week[calendar.get(Calendar.DAY_OF_WEEK) - 1] = true;
        return new QuietTask("제목은 여기에", hStart, mStart, hFinish, mStart,
                week, true, PHONE_VIBRATE, false);
    }

    public static void init() {

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

        put();
    }

}