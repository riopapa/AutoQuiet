package com.urrecliner.autoquiet.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.urrecliner.autoquiet.MainActivity;
import com.urrecliner.autoquiet.Vars;
import com.urrecliner.autoquiet.models.QuietTask;

import java.lang.reflect.Type;
import java.util.List;


public class VarsGetPut {

    public Vars get(Context context) {
        SharedPreferences sharedPref
            = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = sharedPref.getString("vars", "");
        Type type = new TypeToken<Vars>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void put(Vars vars) {
        SharedPreferences sharedPref
                = android.preference.PreferenceManager.getDefaultSharedPreferences(MainActivity.pContext);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(vars);
        sharedEditor.putString("vars", json);
        sharedEditor.apply();
    }
}