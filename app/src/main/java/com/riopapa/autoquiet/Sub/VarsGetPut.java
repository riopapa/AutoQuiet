package com.riopapa.autoquiet.Sub;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.riopapa.autoquiet.ActivityMain;
import com.riopapa.autoquiet.Vars;

import java.lang.reflect.Type;


public class VarsGetPut {

    public Vars get(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("vars", "");
        Type type = new TypeToken<Vars>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public void put(Vars vars, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(vars);
        sharedEditor.putString("vars", json);
        sharedEditor.apply();
    }
}