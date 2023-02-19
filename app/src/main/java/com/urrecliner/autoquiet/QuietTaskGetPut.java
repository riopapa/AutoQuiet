package com.urrecliner.autoquiet;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.urrecliner.autoquiet.models.QuietTask;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuietTaskGetPut {
    public void put(ArrayList<QuietTask> quietTasks, Context context, String info) {

        for (int i = 0; i < quietTasks.size(); i++) {
            QuietTask q = quietTasks.get(i);
            if (!q.agenda) {
                q.calId = 1000 + i;
                q.calStartDate = 10000 + i;
                quietTasks.set(i, q);
            }
        }
        quietTasks.sort(Comparator.comparingLong(arg0 -> arg0.calStartDate));
        SharedPreferences sharedPref = androidx.preference.
                PreferenceManager.getDefaultSharedPreferences(ActivityMain.pContext);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(quietTasks);
        prefsEditor.putString("silentInfo", json);
        prefsEditor.apply();
        ActivityMain.created = true;
    }

    ArrayList<QuietTask> get(Context context) {

        ArrayList<QuietTask> list;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPref.getString("silentInfo", "");
        if (json.isEmpty()) {
            list = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<QuietTask>>() {
            }.getType();
            list = gson.fromJson(json, type);
        }
        return list;
    }

}