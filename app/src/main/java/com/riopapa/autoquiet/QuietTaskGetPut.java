package com.riopapa.autoquiet;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riopapa.autoquiet.Sub.ClearAllTasks;
import com.riopapa.autoquiet.models.QuietTask;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class QuietTaskGetPut {
    public void put(ArrayList<QuietTask> quietTasks) {
        SharedPreferences sharedPref = ActivityMain.mContext.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(quietTasks);
        prefsEditor.putString("silentInfo", json);
        prefsEditor.apply();
    }

    ArrayList<QuietTask> get(Context context) {

        ArrayList<QuietTask> list;
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("silentInfo", "");
        if (json.isEmpty()) {
            list = new ArrayList<>();
            new ClearAllTasks(context);
        } else {
            Type type = new TypeToken<List<QuietTask>>() {
            }.getType();
            list = gson.fromJson(json, type);
        }
        return list;
    }

}