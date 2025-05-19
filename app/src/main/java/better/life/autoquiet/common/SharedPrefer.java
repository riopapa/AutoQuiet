package better.life.autoquiet.common;

import android.content.Context;
import android.content.SharedPreferences;

import better.life.autoquiet.activity.ActivityMain;
import better.life.autoquiet.Vars;

public class SharedPrefer {

    public void get(Vars vars) {
        Context context = ContextProvider.get();
        SharedPreferences sharedPref = context.getSharedPreferences("saved", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPref.edit();
        vars.sharedTimeBefore = sharedPref.getString("timeBefore", "");
        if (vars.sharedTimeBefore.equals("")) {
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