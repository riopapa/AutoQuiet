package better.life.autoquiet.Sub;

import static better.life.autoquiet.activity.ActivityMain.vars;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import better.life.autoquiet.Vars;

public final class VarsGetPut {

    public static void get(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("vars", "");
        Type type = new TypeToken<Vars>() {
        }.getType();
        if (json.equals("null"))
            init();
        else
           vars = gson.fromJson(json, type);
    }

    public static void put(Vars vars, Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(vars);
        sharedEditor.putString("vars", json);
        sharedEditor.apply();
    }

    private static void init() {
        vars = new Vars();
        Context context = ContextProvider.get();
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        vars.sharedTimeBefore = sharedPref.getString("timeBefore", "");
        if (vars.sharedTimeBefore.isEmpty()) {
            sharedEditor.putBoolean("mannerBeep", true);
            sharedEditor.putString("timeInit","60");
            sharedEditor.putString("timeShort", "5");
            sharedEditor.putString("timeLong", "20");
            sharedEditor.putString("timeBefore", "2");
            sharedEditor.apply();
        }
        vars.sharedManner = sharedPref.getBoolean("mannerBeep", true);
        vars.sharedTimeInit = sharedPref.getString("timeInit", "60");
        vars.sharedTimeShort = sharedPref.getString("timeShort", "5");
        vars.sharedTimeLong = sharedPref.getString("timeLong", "20");
        vars.sharedTimeBefore = sharedPref.getString("timeBefore", "10");
        vars.sharedTimeAfter = sharedPref.getString("timeAfter", "-9");
    }
}